package com.hashicraft.nomad.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.math.BlockPos;

public class AddServerData implements java.io.Serializable {
  public int x;
  public int y;
  public int z;
  public String server;
  public int index;
  public String filename;

  public AddServerData() {
  }

  public AddServerData(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public AddServerData(int x, int y, int z, String server) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.server = server;
  }

  public AddServerData(BlockPos pos, String server) {
    this.setBlockPos(pos);
    this.server = server;
  }

  public AddServerData(BlockPos pos) {
    this.setBlockPos(pos);
  }

  public void setBlockPos(BlockPos pos) {
    this.x = pos.getX();
    this.y = pos.getY();
    this.z = pos.getZ();
  }

  public BlockPos getBlockPos() {
    return new BlockPos(this.x, this.y, this.z);
  }

  public byte[] toBytes() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String json = gson.toJson(this);
    return json.getBytes();
  }

  public static AddServerData fromBytes(byte[] data) {
    try {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(AddServerData.class, new AddServerDataCreator());

      String json = new String(data);

      Gson gson = builder.create();
      AddServerData state = gson.fromJson(json, AddServerData.class);

      return state;
    } catch (JsonSyntaxException is) {
      is.printStackTrace();
      String json = new String(data);
      System.out.println("Unable to create AddServerData from JSON:" + json);
      return null;
    }
  }
}