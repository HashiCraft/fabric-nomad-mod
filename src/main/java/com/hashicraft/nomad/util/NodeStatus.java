package com.hashicraft.nomad.util;

import net.minecraft.util.StringIdentifiable;

public enum NodeStatus implements StringIdentifiable {
	READY("ready"),
	DOWN("down"),
	ERROR("error");

	private final String name;

	private NodeStatus(String name) {
		this.name = name;
	}

	public String asString() {
		return this.name;
	}

	public static NodeStatus asEnum(String name) {
		return valueOf(name.toUpperCase());
	}
}
