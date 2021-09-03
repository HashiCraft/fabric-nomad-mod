package com.hashicraft.nomad.block.entity;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.NomadWires;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NomadWiresEntity extends BlockEntity {
  public NomadWiresEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_WIRES_ENTITY, pos, state);
	}

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadWiresEntity entity) {
    BlockPos northPos = blockPos.offset(Direction.NORTH);
    BlockEntity northBlock = world.getBlockEntity(northPos);
    if (northBlock != null && (
      northBlock.getType() == Nomad.NOMAD_SERVER_ENTITY || 
      northBlock.getType() == Nomad.NOMAD_CLIENT_ENTITY ||
      northBlock.getType() == Nomad.NOMAD_WIRES_ENTITY
    )) {
      state = state.with(NomadWires.NORTH_CONNECTED, true);
    } else {
      state = state.with(NomadWires.NORTH_CONNECTED, false);
    }

    BlockPos eastPos = blockPos.offset(Direction.EAST);
    BlockEntity eastBlock = world.getBlockEntity(eastPos);
    if (eastBlock != null && (
      eastBlock.getType() == Nomad.NOMAD_SERVER_ENTITY || 
      eastBlock.getType() == Nomad.NOMAD_CLIENT_ENTITY ||
      eastBlock.getType() == Nomad.NOMAD_WIRES_ENTITY
    )) {
      state = state.with(NomadWires.EAST_CONNECTED, true);
    } else {
      state = state.with(NomadWires.EAST_CONNECTED, false);
    }

    BlockPos southPos = blockPos.offset(Direction.SOUTH);
    BlockEntity southBlock = world.getBlockEntity(southPos);
    if (southBlock != null && (
      southBlock.getType() == Nomad.NOMAD_SERVER_ENTITY || 
      southBlock.getType() == Nomad.NOMAD_CLIENT_ENTITY ||
      southBlock.getType() == Nomad.NOMAD_WIRES_ENTITY
    )) {
      state = state.with(NomadWires.SOUTH_CONNECTED, true);
    } else {
      state = state.with(NomadWires.SOUTH_CONNECTED, false);
    }

    BlockPos westPos = blockPos.offset(Direction.WEST);
    BlockEntity westBlock = world.getBlockEntity(westPos);
    if (westBlock != null && (
      westBlock.getType() == Nomad.NOMAD_SERVER_ENTITY || 
      westBlock.getType() == Nomad.NOMAD_CLIENT_ENTITY ||
      westBlock.getType() == Nomad.NOMAD_WIRES_ENTITY
    )) {
      state = state.with(NomadWires.WEST_CONNECTED, true);
    } else {
      state = state.with(NomadWires.WEST_CONNECTED, false);
    }

    // boolean hasClientBlock = false;
    // boolean hasWireBlock = false;
    // BlockPos wirePos = blockPos.offset(wireBlockDirection);
    // BlockEntity wireBlock = world.getBlockEntity(wirePos);
    // if (wireBlock != null && wireBlock.getType() == Nomad.NOMAD_WIRES_ENTITY) {
    //   hasWireBlock = true;
    // }

    // BlockPos clientPos = blockPos.offset(clientBlockDirection);
    // BlockEntity clientBlock = world.getBlockEntity(clientPos);
    // if (clientBlock != null && clientBlock.getType() == Nomad.NOMAD_CLIENT_ENTITY) {
    //   hasClientBlock = true;
    // }

    // int wireState = 0;
    // if(hasWireBlock && hasClientBlock) {
    //   wireState = 1;
    // } else if (!hasWireBlock && hasClientBlock) {
    //   wireState = 2;
    // }

    // state = state.with(NomadWires.CONNECTED, wireState);
    world.setBlockState(blockPos, state, Block.NOTIFY_ALL);
  }
}