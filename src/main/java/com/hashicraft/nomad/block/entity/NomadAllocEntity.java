package com.hashicraft.nomad.block.entity;

import java.util.HashMap;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicraft.nomad.Nomad;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NomadAllocEntity extends BlockEntity implements BlockEntityClientSerializable{
  private HashMap<String,String> details = new HashMap<String,String>();
  
  public NomadAllocEntity(BlockPos pos, BlockState state) {
		super(Nomad.NOMAD_ALLOC_ENTITY, pos, state);
	}

  public void setDetails(AllocationListStub alloc) {
    details.put("id", alloc.getId());
    details.put("name", alloc.getJobId());
    details.put("status", alloc.getClientStatus());

    this.markDirty();
    this.sync();
  }

  public HashMap<String,String> getDetails() {
    return this.details;
  }

  public static void tick(World world, BlockPos blockPos, BlockState state, NomadAllocEntity entity) {
  }

  @Override
  public void fromClientTag(NbtCompound tag) {
    if (tag.contains("alloc", 10)) {
      NbtCompound alloc = tag.getCompound("alloc");
      String id = alloc.getString("id");
      this.details.put("id", id);

      String name = alloc.getString("name");
      this.details.put("name", name);

      String status = alloc.getString("status");
      this.details.put("status", status);
    }
  }

  @Override
  public NbtCompound toClientTag(NbtCompound tag) {
    NbtCompound alloc = new NbtCompound();
    String id = this.details.get("id");
    alloc.putString("id", id);

    String name = this.details.get("name");
    alloc.putString("name", name);

    String status = this.details.get("status");
    alloc.putString("status", status);

    tag.put("alloc", alloc);

    return tag;
  }

}
