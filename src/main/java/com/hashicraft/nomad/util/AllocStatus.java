package com.hashicraft.nomad.util;

import net.minecraft.util.StringIdentifiable;

public enum AllocStatus implements StringIdentifiable {
	PENDING("pending"),
	RUNNING("running"),
	COMPLETE("complete"),
	FAILED("failed"),
	LOST("lost");

	private final String name;

	private AllocStatus(String name) {
		this.name = name;
	}

	public String asString() {
		return this.name;
	}

	public static AllocStatus asEnum(String name) {
		return valueOf(name.toUpperCase());
	}
}
