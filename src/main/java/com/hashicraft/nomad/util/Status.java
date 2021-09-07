package com.hashicraft.nomad.util;

import net.minecraft.util.StringIdentifiable;

public enum Status implements StringIdentifiable {
	READY("ready"),
	DOWN("down"),
	ERROR("error");

	private final String name;

	private Status(String name) {
		this.name = name;
	}

	public String asString() {
		return this.name;
	}
}
