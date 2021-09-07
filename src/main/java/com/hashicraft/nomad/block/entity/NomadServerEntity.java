package com.hashicraft.nomad.block.entity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.hashicorp.nomad.apimodel.AgentHealthResponse;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.NomadJson;
import com.hashicorp.nomad.javasdk.NomadResponse;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.NomadClient;
import com.hashicraft.nomad.block.NomadServer;
import com.hashicraft.nomad.util.Status;

import org.apache.commons.io.FileUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class NomadServerEntity extends BlockEntity {
  private static int tickCounter = 0;
  private static NomadApiClient API;

  public NomadServerEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_SERVER_ENTITY, pos, state);

    NomadApiConfiguration config = new NomadApiConfiguration.Builder().setAddress("http://127.0.0.1:32079").build();
		API = new NomadApiClient(config);

    // wait for the server to be healthy...
    // get the number of nodes...
    // clear an area the size of the nodes + 1
	}

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadServerEntity entity) {
    if (tickCounter % 10 == 0) {
      if(!world.isClient) {
        try {
          NomadResponse<AgentHealthResponse> healthResponse = API.getAgentApi().health();
          AgentHealthResponse health = healthResponse.getValue();
          if (!health.getServer().getOk()) {
            world.setBlockState(blockPos, state.with(NomadServer.STATUS, Status.DOWN), Block.NOTIFY_ALL);
          } else {
            world.setBlockState(blockPos, state.with(NomadServer.STATUS, Status.READY), Block.NOTIFY_ALL);
          }

          Direction facing = state.get(Properties.HORIZONTAL_FACING);
          Direction offsetDirection = facing.rotateCounterclockwise(Axis.Y);

          ServerQueryResponse<List<NodeListStub>> nodesResponse =  API.getNodesApi().list();
          List<NodeListStub> nodes = nodesResponse.getValue();
          for (int i = 1; i <= nodes.size() * 2; i++) {
            // offset the position by 1.
            BlockPos offsetPos = blockPos.offset(offsetDirection, i);

            // place a wire for every step.
            if (world.isAir(offsetPos)){
              world.setBlockState(offsetPos, Nomad.NOMAD_WIRES.getDefaultState(), Block.NOTIFY_ALL);
            }

            // place a client node with 1 space between.
            if (i % 2 == 0) {
              int nodeIndex = (i/2) - 1;
              BlockPos nodePos = offsetPos.offset(facing.getOpposite());
              boolean ready = nodes.get(nodeIndex).getStatus().equalsIgnoreCase("ready");

              // if the client node doesn't exist yet create it
              if (world.isAir(nodePos)){
                world.setBlockState(nodePos, Nomad.NOMAD_CLIENT.getDefaultState().with(NomadClient.STATUS, Status.READY), Block.NOTIFY_ALL);
              } else {
                if(!ready) {
                  world.setBlockState(nodePos, Nomad.NOMAD_CLIENT.getDefaultState().with(NomadClient.STATUS, Status.DOWN), Block.NOTIFY_ALL);

                  // if a node is down, the server should be unhappy too
                  world.setBlockState(blockPos, state.with(NomadClient.STATUS, Status.ERROR));
                } else {
                  world.setBlockState(nodePos, Nomad.NOMAD_CLIENT.getDefaultState().with(NomadClient.STATUS, Status.READY), Block.NOTIFY_ALL);
                }
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          world.setBlockState(blockPos, state.with(NomadServer.STATUS, Status.DOWN));
          world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos);
        }
      }
    }
    tickCounter++;
  }

  public void registerJob(String filename) {
    try {
      File file = Nomad.NOMAD_JOB_FILES.get(filename);
      String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
      Job job = NomadJson.readJobSpec(contents);
      EvaluationResponse response = API.getJobsApi().register(job);
      System.out.println(response.getValue());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
