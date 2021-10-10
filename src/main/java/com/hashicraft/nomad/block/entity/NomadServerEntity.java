package com.hashicraft.nomad.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.github.hashicraft.stateful.blocks.StatefulBlockEntity;
import com.github.hashicraft.stateful.blocks.Syncable;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.NomadAlloc;
import com.hashicraft.nomad.block.NomadClient;
import com.hashicraft.nomad.block.NomadServer;
import com.hashicraft.nomad.block.NomadWires;
import com.hashicraft.nomad.state.AddServerData;
import com.hashicraft.nomad.state.Messages;
import com.hashicraft.nomad.state.NomadServerState.Server;
import com.hashicraft.nomad.util.AllocStatus;
import com.hashicraft.nomad.util.NodeStatus;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class NomadServerEntity extends StatefulBlockEntity {
  @Syncable
  public String address = "";

  @Syncable
  public Server server;

  public NomadServerEntity(BlockPos pos, BlockState state) {
    super(Nomad.NOMAD_SERVER_ENTITY, pos, state, null);
  }

  public NomadServerEntity(BlockPos pos, BlockState state, Block parent) {
    super(Nomad.NOMAD_SERVER_ENTITY, pos, state, parent);
  }

  public void setAddress(String address) {
    if (address.startsWith("http://") || address.startsWith("https://")) {
      this.address = address;
      this.markForUpdate();

      // fire an event to let the server know that a new server has been added
      AddServerData serverData = new AddServerData(this.pos, this.address);
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeByteArray(serverData.toBytes());

      ClientPlayNetworking.send(Messages.ADD_SERVER, buf);
    }
  }

  public String getAddress() {
    return this.address;
  }

  // Create a new client and set it's state depending on status.
  private static void placeClient(World world, BlockState serverState, BlockPos blockPos, NodeStatus status) {
    for (int i = 0; i < 255; i++) {
      BlockPos pos = blockPos.offset(Direction.UP, i + 1);
      world.removeBlock(pos, false);
      world.removeBlockEntity(pos);
    }

    Direction facing = serverState.get(NomadServer.FACING);
    BlockState clientState = Nomad.NOMAD_CLIENT.getDefaultState().with(NomadClient.FACING, facing)
        .with(NomadClient.STATUS, status);
    world.setBlockState(blockPos, clientState, Block.NOTIFY_ALL);
  }

  private static void placeAllocation(World world, BlockState serverState, BlockPos blockPos, int index,
      AllocationListStub alloc, AllocStatus status) {
    Direction facing = serverState.get(NomadServer.FACING);
    BlockState state = Nomad.NOMAD_ALLOC.getDefaultState().with(NomadAlloc.FACING, facing).with(NomadAlloc.STATUS,
        status);
    BlockPos allocPos = blockPos.offset(Direction.UP, index + 1);
    world.setBlockState(allocPos, state, Block.NOTIFY_ALL);

    NomadAllocEntity allocEntity = (NomadAllocEntity) world.getBlockEntity(allocPos);
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
    StatefulBlockEntity.tick(world, blockPos, state, entity);
    if (!world.isClient) {
      entity.update(state);
    }
  }

  private void update(BlockState state) {
    if (this.address != null) {
      Direction facing = state.get(Properties.HORIZONTAL_FACING);
      Direction offsetDirection = facing.rotateCounterclockwise(Axis.Y);

      if (server != null) {
        BlockPos serverPos = server.pos;
        ArrayList<NodeListStub> nodes = server.nodes;
        if (nodes == null)
          return;
        for (int i = 1; i <= nodes.size() * 2; i++) {
          BlockPos offsetPos = serverPos.offset(offsetDirection, i);
          if (world.isAir(offsetPos)) {
            placeWire(world, offsetPos);
          }

          if (i % 2 == 0) {
            int nodeIndex = (i / 2) - 1;
            NodeListStub node = nodes.get(nodeIndex);
            BlockPos nodePos = offsetPos.offset(facing.getOpposite());
            placeClient(world, state, nodePos, NodeStatus.asEnum(node.getStatus()));

            ArrayList<AllocationListStub> allocs = server.allocations.get(node.getId());
            int allocIndex = 0;
            for (AllocationListStub alloc : allocs) {
              placeAllocation(world, state, nodePos, allocIndex++, alloc, AllocStatus.asEnum(alloc.getClientStatus()));
            }
          }
        }

        world.setBlockState(serverPos, state, Block.NOTIFY_ALL);
      }
    }
  }

  private static void broadcast(World world, String message, boolean actionBar) {
    List<? extends PlayerEntity> players = world.getPlayers();
    for (PlayerEntity player : players) {
      player.sendMessage(new LiteralText(message), actionBar);
    }
  }
}
