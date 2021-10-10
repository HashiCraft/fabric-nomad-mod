package com.hashicraft.nomad.state;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

public class AddServerDataCreator implements InstanceCreator<AddServerData> {
  @Override
  public AddServerData createInstance(Type type) {
    return new AddServerData();
  }
}
