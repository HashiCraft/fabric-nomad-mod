package com.hashicraft.nomad.state;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.NomadJson;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import com.hashicraft.nomad.Nomad;

import org.apache.commons.io.FileUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class NomadServerState {
  public class Server {
    public String address = "";
    public BlockPos pos = null;
    public ArrayList<NodeListStub> nodes = new ArrayList<NodeListStub>();
    public HashMap<String, ArrayList<AllocationListStub>> allocations = new HashMap<String, ArrayList<AllocationListStub>>();
    
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

  public void start() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleWithFixedDelay(() -> {
      if (servers.size() > 0) {
        for (Server server : servers.values()) {
          update(server.pos);
        }
      }
    }, 0, 5, TimeUnit.SECONDS);
  }

  public void addServer(BlockPos pos, String address) {
    servers.put(pos, new Server(pos, address));
    update(pos);
  }

  public void removeServer(BlockPos pos) {
    servers.remove(pos);
  }

  public Server getServer(BlockPos pos) {
    return servers.get(pos);
  }

  public String registerJob(BlockPos pos, String filename) {
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

  public void update(BlockPos pos) {
    // query nomad and update the data
    synchronized (mutex) {
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
    }
  }
}

