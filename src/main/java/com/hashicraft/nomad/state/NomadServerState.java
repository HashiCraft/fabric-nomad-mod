package com.hashicraft.nomad.state;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.hashicraft.stateful.blocks.EntityStateData;
import com.github.hashicraft.stateful.blocks.StatefulBlockEntity;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.NomadJson;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadServerEntity;

import org.apache.commons.io.FileUtils;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class NomadServerState {
  public static boolean registered = false;

  public class Server {
    public String address = "";
    public BlockPos pos = null;
    public ArrayList<NodeListStub> nodes = new ArrayList<NodeListStub>();
    public Hashtable<String, ArrayList<AllocationListStub>> allocations = new Hashtable<String, ArrayList<AllocationListStub>>();

    public Server(BlockPos pos, String address) {
      this.pos = pos;
      this.address = address;
    }
  }

  static NomadServerState INSTANCE = new NomadServerState();
  private Object mutex = new Object();

  private Hashtable<BlockPos, Server> servers = new Hashtable<BlockPos, Server>();

  public static NomadServerState getInstance() {
    return INSTANCE;
  }

  // register for events sent by the client
  public static void RegisterStateUpdates(MinecraftServer mcserver) {
    getInstance().start(mcserver);

    // register the event handler so the client can add nomad servers to monitor
    ServerPlayNetworking.registerGlobalReceiver(Messages.ADD_SERVER, (MinecraftServer server, ServerPlayerEntity player,
        ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {

      AddServerData serverData = AddServerData.fromBytes(buf.readByteArray());
      server.execute(() -> {
        getInstance().addServer(serverData.getBlockPos(), serverData.server, server.getOverworld());
      });
    });

    // register an event for removing an entity
    ServerPlayNetworking.registerGlobalReceiver(Messages.REMOVE_SERVER,
        (MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf,
            PacketSender responseSender) -> {

          AddServerData serverData = AddServerData.fromBytes(buf.readByteArray());
          server.execute(() -> {
            getInstance().removeServer(serverData.getBlockPos());
          });
        });

    // register an event for draining a node
    ServerPlayNetworking.registerGlobalReceiver(Messages.NODE_DRAIN, (MinecraftServer server, ServerPlayerEntity player,
        ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) -> {

      AddServerData serverData = AddServerData.fromBytes(buf.readByteArray());
      server.execute(() -> {
        getInstance().toggleNodeDrain(serverData.getBlockPos(), serverData.index);
      });
    });

    ServerPlayNetworking.registerGlobalReceiver(Messages.REGISTER_JOB,
        (MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf,
            PacketSender responseSender) -> {

          AddServerData serverData = AddServerData.fromBytes(buf.readByteArray());
          server.execute(() -> {
            getInstance().registerJob(serverData.getBlockPos(), serverData.filename);
          });
        });
  }

  private void start(MinecraftServer server) {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleWithFixedDelay(() -> {
      if (servers.size() > 0) {
        for (Server s : servers.values()) {
          server.execute(() -> {
            update(s.pos, server.getOverworld());
          });
        }
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  public void addServer(BlockPos pos, String address, ServerWorld world) {
    System.out.println("Add server " + pos.toString() + " address " + address);
    servers.put(pos, new Server(pos, address));
    update(pos, world);
  }

  public void removeServer(BlockPos pos) {
    System.out.println("Remove server " + pos.toString());
    servers.remove(pos);
  }

  private Server getServer(BlockPos pos) {
    return servers.get(pos);
  }

  private String registerJob(BlockPos pos, String filename) {
    System.out.println("Register job " + pos.toString() + " filename " + filename);
    try {
      File file = Nomad.NOMAD_JOB_FILES.get(filename);
      String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      Job job = NomadJson.readJobSpec(contents);

      Server server = this.servers.get(pos);
      NomadApiClient client = new NomadApiClient(server.address);
      EvaluationResponse response = client.getJobsApi().register(job);
      client.close();
      return response.getValue();
    } catch (Exception e) {
      return null;
    }
  }

  public void toggleNodeDrain(BlockPos pos, int index) {
    System.out.println("Draining Node " + pos.toString() + " index " + index);

    Server server = this.servers.get(pos);
    NodeListStub node = server.nodes.get(index);

    String data;
    String endpoint;

    if (node.getSchedulingEligibility().equalsIgnoreCase("ineligible")) {
      endpoint = server.address + "/v1/node/" + node.getId() + "/eligibility";
      data = """
          {
            \"Eligibility\": \"eligible\"
          }
          """;
    } else {
      endpoint = server.address + "/v1/node/" + node.getId() + "/drain";
      data = """
          {
            \"DrainSpec\": {
              \"Deadline\": 3600000000000,
              \"IgnoreSystemJobs\": true
            }
          }
          """;
    }

    try {
      URL url = new URL(endpoint);
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.setDoOutput(true);
      httpCon.setRequestMethod("PUT");
      httpCon.setRequestProperty("Content-Type", "application/json");

      OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
      out.write(data);
      out.close();
      httpCon.getInputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void update(BlockPos pos, ServerWorld world) {
    // query nomad and update the data
    Server server = this.servers.get(pos);
    server.nodes.clear();
    server.allocations.clear();

    NomadApiClient client = new NomadApiClient(server.address);

    try {
      ServerQueryResponse<List<NodeListStub>> nodesResponse = client.getNodesApi().list();
      for (NodeListStub node : nodesResponse.getValue()) {
        server.nodes.add(node);
        server.allocations.put(node.getId(), new ArrayList<AllocationListStub>());
      }

      ServerQueryResponse<List<AllocationListStub>> allocationsResponse = client.getAllocationsApi().list();
      for (AllocationListStub allocation : allocationsResponse.getValue()) {
        server.allocations.get(allocation.getNodeId()).add(allocation);
      }

      client.close();
    } catch (NomadException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // update the entity
    NomadServerEntity entity = (NomadServerEntity) world.getBlockEntity(pos);
    entity.server = server;

    // sync the server state with the client nodes
    // this should probably be a function in the StatefulBlockEntity
    entity.setPropertiesToState();
    EntityStateData state = entity.serverState;
    entity.serverStateUpdated(state);
  }
}