package com.hashicraft.nomad.block.entity;

import com.github.hashicraft.stateful.blocks.StatefulBlockEntity;
import com.github.hashicraft.stateful.blocks.Syncable;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicraft.nomad.Nomad;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NomadAllocEntity extends StatefulBlockEntity {
  @Syncable
  private String id = "";

  @Syncable
  private String name = "";

  @Syncable
  private String status = "";
  
  public NomadAllocEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_ALLOC_ENTITY, pos, state, null);
	}

  public NomadAllocEntity(BlockPos pos, BlockState state, Block parent) {
		super(Nomad.NOMAD_ALLOC_ENTITY, pos, state, parent);
	}

  public void setDetails(AllocationListStub alloc) {
    this.id = alloc.getId();
    this.name = alloc.getJobId();
    this.status = alloc.getClientStatus();
    this.markForUpdate();
  }

  public String getID() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getStatus() {
    return this.status;
  }

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadAllocEntity entity) {
  }
}
