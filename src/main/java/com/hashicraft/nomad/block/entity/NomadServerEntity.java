package com.hashicraft.nomad.block.entity;

import com.hashicraft.nomad.Nomad;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NomadServerEntity extends BlockEntity {
  public NomadServerEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_SERVER_ENTITY, pos, state);
	}

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadServerEntity entity) {
  }

}
