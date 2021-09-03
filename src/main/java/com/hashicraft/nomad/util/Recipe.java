package com.hashicraft.nomad.util;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

public class Recipe {
  private JsonObject json;
  
  public Recipe(ArrayList<Ingredient> ingredients, ArrayList<String> pattern, Identifier output) {
    JsonObject recipe = new JsonObject();
    recipe.addProperty("type", "minecraft:crafting_shaped");

    JsonArray jsonArray = new JsonArray();
    jsonArray.add(pattern.get(0));
    jsonArray.add(pattern.get(1));
    jsonArray.add(pattern.get(2));
    recipe.add("pattern", jsonArray);

    JsonObject individualKey;
    JsonObject keyList = new JsonObject();
    for (Ingredient i : ingredients) {
      individualKey = new JsonObject();
      individualKey.addProperty(i.type, i.item.toString());
      keyList.add(i.id, individualKey);
    }
    recipe.add("key", keyList);

    JsonObject result = new JsonObject();
    result.addProperty("item", output.toString());
    result.addProperty("count", 1);
    recipe.add("result", result);

    this.json = recipe;
  }

  public JsonObject JSON() {
    return this.json;
  }
}