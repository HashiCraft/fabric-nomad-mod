package com.hashicraft.nomad.block.entity;

import com.hashicraft.nomad.Nomad;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NomadAllocEntity extends BlockEntity {
  public NomadAllocEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_ALLOC_ENTITY, pos, state);
	}

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadAllocEntity entity) {
  }

}
