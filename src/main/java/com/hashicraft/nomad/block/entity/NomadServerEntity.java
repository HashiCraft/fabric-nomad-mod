package com.hashicraft.nomad.block.entity;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadJson;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.NomadAlloc;
import com.hashicraft.nomad.block.NomadClient;
import com.hashicraft.nomad.block.NomadServer;
import com.hashicraft.nomad.block.NomadWires;
import com.hashicraft.nomad.util.AllocStatus;
import com.hashicraft.nomad.util.NodeStatus;

import org.apache.commons.io.FileUtils;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class NomadServerEntity extends BlockEntity implements BlockEntityClientSerializable {
  private static int tickCounter = 0;
  static NomadApiClient API;

  private static ArrayList<NodeListStub> nodes = new ArrayList<NodeListStub>();
  private static HashMap<String, ArrayList<AllocationListStub>> allocations = new HashMap<String, ArrayList<AllocationListStub>>();

  public NomadServerEntity(BlockPos pos, BlockState state) {
    super(Nomad.NOMAD_SERVER_ENTITY, pos, state);
    // wait for the server to be healthy...
    // get the number of nodes...
    // clear an area the size of the nodes + 1
  }

  public void setAddress(String address) {
    if (address.startsWith("http://") || address.startsWith("https://")) {
      NomadApiConfiguration config = new NomadApiConfiguration.Builder().setAddress(address).build();
      API = new NomadApiClient(config);
    }
    this.markDirty();
  }

  public String getAddress() {
    String address = "";
    if (API != null) {
      address = API.getConfig().getAddress().getSchemeName() + "://" + API.getConfig().getAddress().getHostName() + ":"
          + API.getConfig().getAddress().getPort();
    }
    return address;
  }

  public void toggleNodeDrain(int index) {
    NodeListStub node = nodes.get(index);
    
    String data;
    String endpoint;
    if (node.getSchedulingEligibility().equalsIgnoreCase("ineligible")) {
      endpoint = this.getAddress() + "/v1/node/" + node.getId() + "/eligibility";
      data = """
      {
        \"Eligibility\": \"eligible\"
      }
      """;
    } else {
      endpoint = this.getAddress() + "/v1/node/" + node.getId() + "/drain";
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

  // Create a new client and set it's state depending on status.
  private static void placeClient(World world, BlockState serverState, BlockPos blockPos, NodeStatus status) {
    for (int i = 0; i < 255; i++) {
      BlockPos pos = blockPos.offset(Direction.UP, i + 1);
      world.removeBlock(pos, false);
    }
    
    Direction facing = serverState.get(NomadServer.FACING);
    BlockState clientState = Nomad.NOMAD_CLIENT.getDefaultState().with(NomadClient.FACING, facing).with(NomadClient.STATUS, status);
    world.setBlockState(blockPos, clientState, Block.NOTIFY_ALL);
  }

  private static void placeAllocation(World world, BlockState serverState, BlockPos blockPos, int index, AllocationListStub alloc, AllocStatus status) {
    Direction facing = serverState.get(NomadServer.FACING);
    BlockState state = Nomad.NOMAD_ALLOC.getDefaultState().with(NomadAlloc.FACING, facing).with(NomadAlloc.STATUS, status);
    BlockPos allocPos = blockPos.offset(Direction.UP, index + 1);
    world.setBlockState(allocPos, state, Block.NOTIFY_ALL);

    NomadAllocEntity allocEntity = (NomadAllocEntity)world.getBlockEntity(allocPos);
    allocEntity.setDetails(alloc);
  }

  // Create a new wire and set it's state depending on surrounding blocks.
  private static void placeWire(World world, BlockPos blockPos) {
    BlockState state = Nomad.NOMAD_WIRES.getDefaultState();

    BlockPos northPos = blockPos.offset(Direction.NORTH);
    Block northBlock = world.getBlockState(northPos).getBlock();
    if (northBlock != null
        && (northBlock == Nomad.NOMAD_SERVER || northBlock == Nomad.NOMAD_CLIENT || northBlock == Nomad.NOMAD_WIRES)) {
      state = state.with(NomadWires.NORTH_CONNECTED, true);
    } else {
      state = state.with(NomadWires.NORTH_CONNECTED, false);
    }
 
    BlockPos eastPos = blockPos.offset(Direction.EAST);
    Block eastBlock = world.getBlockState(eastPos).getBlock();
    if (eastBlock != null
        && (eastBlock == Nomad.NOMAD_SERVER || eastBlock == Nomad.NOMAD_CLIENT || eastBlock == Nomad.NOMAD_WIRES)) {
      state = state.with(NomadWires.EAST_CONNECTED, true);
    } else {
      state = state.with(NomadWires.EAST_CONNECTED, false);
    }

    BlockPos southPos = blockPos.offset(Direction.SOUTH);

    Block southBlock = world.getBlockState(southPos).getBlock();
    if (southBlock != null
        && (southBlock == Nomad.NOMAD_SERVER || southBlock == Nomad.NOMAD_CLIENT || southBlock == Nomad.NOMAD_WIRES)) {
      state = state.with(NomadWires.SOUTH_CONNECTED, true);
    } else {
      state = state.with(NomadWires.SOUTH_CONNECTED, false);
    }

    BlockPos westPos = blockPos.offset(Direction.WEST);
    Block westBlock = world.getBlockState(westPos).getBlock();
    if (westBlock != null
        && (westBlock == Nomad.NOMAD_SERVER || westBlock == Nomad.NOMAD_CLIENT || westBlock == Nomad.NOMAD_WIRES)) {
      state = state.with(NomadWires.WEST_CONNECTED, true);
    } else {
      state = state.with(NomadWires.WEST_CONNECTED, false);
    }

    world.setBlockState(blockPos, state, Block.NOTIFY_ALL);
  }

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadServerEntity entity) {
    if (tickCounter % 20 == 0) {
      if (!world.isClient) {
        try {
          if (API != null) {
            // Clear the state.
            nodes.clear();
            allocations.clear();

            // Get a list of Nomad nodes.
            ServerQueryResponse<List<NodeListStub>> nodesResponse = API.getNodesApi().list();
            for (NodeListStub node : nodesResponse.getValue()) {
              nodes.add(node);
              allocations.put(node.getId(), new ArrayList<AllocationListStub>());
            }

            // Get a list of Nomad allocations.
            ServerQueryResponse<List<AllocationListStub>> allocationsResponse = API.getAllocationsApi().list();
            for (AllocationListStub allocation : allocationsResponse.getValue()) {
              allocations.get(allocation.getNodeId()).add(allocation);
            }
          }

          // Figure out our own orientation.
          Direction facing = state.get(Properties.HORIZONTAL_FACING);
          Direction offsetDirection = facing.rotateCounterclockwise(Axis.Y);

          // Start drawing the nodes and wires.
          for (int i = 1; i <= nodes.size() * 2; i++) {
            // Offset the position by 1.
            BlockPos offsetPos = blockPos.offset(offsetDirection, i);

            // Place a wire for every step.
            if (world.isAir(offsetPos)) {
              placeWire(world, offsetPos);
            }

            // Place a client node with 1 space between.
            if (i % 2 == 0) {
              int nodeIndex = (i / 2) - 1;
              NodeListStub node = nodes.get(nodeIndex);

              BlockPos nodePos = offsetPos.offset(facing.getOpposite());
              placeClient(world, state, nodePos, NodeStatus.asEnum(node.getStatus()));

              ArrayList<AllocationListStub> allocs = allocations.get(node.getId());
              for (int j = 0; j < allocs.size(); j++) {
                AllocationListStub alloc = allocs.get(j);
                placeAllocation(world, state, nodePos, j, alloc, AllocStatus.asEnum(alloc.getClientStatus()));
              }
            }
          }
          
          world.setBlockState(blockPos, state, Block.NOTIFY_ALL);

        } catch (Exception e) {
          world.setBlockState(blockPos, state.with(NomadServer.STATUS, NodeStatus.DOWN));
          broadcast(world, "[ERROR] Can't reach the server: " + e.getMessage(), false);
        }
      }
    }
    tickCounter++;
  }

  private static void broadcast(World world, String message, boolean actionBar) {
    List<? extends PlayerEntity> players = world.getPlayers();
    for (PlayerEntity player : players) {
      player.sendMessage(new LiteralText(message), actionBar);
    }
  }

  public void registerJob(String filename) {
    try {
      File file = Nomad.NOMAD_JOB_FILES.get(filename);
      String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      Job job = NomadJson.readJobSpec(contents);
      EvaluationResponse response = API.getJobsApi().register(job);
      broadcast(world, "[INFO] Registered job: " + response.getValue(), true);
    } catch (Exception e) {
      broadcast(world, "[ERROR] Could not register job: " + e.getMessage(), true);
    }
  }

  @Override
  public void readNbt(NbtCompound tag) {
    super.readNbt(tag);
    if (tag.contains("server", 10)) {
      NbtCompound server = tag.getCompound("server");
      String address = server.getString("address");
      this.setAddress(address);
    }
  }

  @Override
  public void fromClientTag(NbtCompound tag) {
    if (tag.contains("server", 10)) {
      NbtCompound server = tag.getCompound("server");
      String address = server.getString("address");
      this.setAddress(address);
    }
  }

  @Override
  public NbtCompound writeNbt(NbtCompound tag) {
    super.writeNbt(tag);

    NbtCompound server = new NbtCompound();
    String address = this.getAddress();
    server.putString("address", address);
    tag.put("server", server);

    return tag;
  }

  @Override
  public NbtCompound toClientTag(NbtCompound tag) {
    NbtCompound server = new NbtCompound();
    String address = this.getAddress();
    server.putString("address", address);
    tag.put("server", server);

    return tag;
  }
}
