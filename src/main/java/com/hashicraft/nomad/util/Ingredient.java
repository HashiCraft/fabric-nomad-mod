package com.hashicraft.nomad.util;

import net.minecraft.util.Identifier;

public class Ingredient {
  public String id;
  public Identifier item;
  public String type;

  public Ingredient(String id, Identifier item, String type) {
    this.id = id;
    this.item = item;
    this.type = type;
  }
}