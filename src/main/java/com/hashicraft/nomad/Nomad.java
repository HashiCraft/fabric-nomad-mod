package com.hashicraft.nomad;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.hashicraft.nomad.block.NomadServer;
import com.hashicraft.nomad.block.NomadWires;
import com.hashicraft.nomad.block.entity.NomadServerEntity;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;
import com.hashicraft.nomad.item.NomadJob;
import com.hashicraft.nomad.block.NomadClient;
import com.hashicraft.nomad.block.entity.NomadClientEntity;
import com.hashicraft.nomad.util.Recipe;
import com.hashicraft.nomad.util.Ingredient;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Nomad implements ModInitializer {
	public static final String MODID = "nomad";
	public static final String MOD_NAME = "Nomad";

	public static final Identifier NOMAD_JOB_ID = identifier("nomad_job");
	public static JsonObject NOMAD_JOB_RECIPE;
	public static Item NOMAD_JOB;

	public static final Identifier NOMAD_SERVER_ID = identifier("nomad_server");
	public static JsonObject NOMAD_SERVER_RECIPE;
	public static final NomadServer NOMAD_SERVER = new NomadServer(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
	public static BlockEntityType<NomadServerEntity> NOMAD_SERVER_ENTITY;

	public static final Identifier NOMAD_CLIENT_ID = identifier("nomad_client");
	public static JsonObject NOMAD_CLIENT_RECIPE;
	public static final NomadClient NOMAD_CLIENT = new NomadClient(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
	public static BlockEntityType<NomadClientEntity> NOMAD_CLIENT_ENTITY;

	public static final Identifier NOMAD_WIRES_ID = identifier("nomad_wires");
	public static JsonObject NOMAD_WIRES_RECIPE;
	public static final NomadWires NOMAD_WIRES = new NomadWires(FabricBlockSettings.of(Material.GLASS).hardness(4.0f).nonOpaque());
	public static BlockEntityType<NomadWiresEntity> NOMAD_WIRES_ENTITY;

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(identifier("general"), () -> new ItemStack(NOMAD_SERVER));

	@Override
	public void onInitialize() {
		NOMAD_JOB = Registry.register(Registry.ITEM, NOMAD_JOB_ID, new NomadJob(new Item.Settings().group(ITEM_GROUP)));

		Registry.register(Registry.BLOCK, NOMAD_SERVER_ID, NOMAD_SERVER);
		Registry.register(Registry.ITEM, NOMAD_SERVER_ID, new BlockItem(NOMAD_SERVER, new Item.Settings().group(ITEM_GROUP)));
		NOMAD_SERVER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, NOMAD_SERVER_ID, FabricBlockEntityTypeBuilder.create(NomadServerEntity::new, NOMAD_SERVER).build(null));
		NOMAD_SERVER_RECIPE = new Recipe(
			Lists.newArrayList(
				new Ingredient("G", new Identifier("glass"), "item"),
				new Ingredient("A", new Identifier("respawn_anchor"), "item"),
				new Ingredient("C", new Identifier("dirt"), "item"),
				new Ingredient("E", new Identifier("ender_chest"), "item"),
				new Ingredient("I", new Identifier("iron_ingot"), "item")
			),
			Lists.newArrayList(
				"GAG",
				"ICI",
				"IEI"
			), 
			NOMAD_SERVER_ID
		).JSON();

		Registry.register(Registry.BLOCK, NOMAD_CLIENT_ID, NOMAD_CLIENT);
		Registry.register(Registry.ITEM, NOMAD_CLIENT_ID, new BlockItem(NOMAD_CLIENT, new Item.Settings().group(ITEM_GROUP)));
		NOMAD_CLIENT_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, NOMAD_CLIENT_ID, FabricBlockEntityTypeBuilder.create(NomadClientEntity::new, NOMAD_CLIENT).build(null));

		Registry.register(Registry.BLOCK, NOMAD_WIRES_ID, NOMAD_WIRES);
		Registry.register(Registry.ITEM, NOMAD_WIRES_ID, new BlockItem(NOMAD_WIRES, new Item.Settings().group(ITEM_GROUP)));
		NOMAD_WIRES_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, NOMAD_WIRES_ID, FabricBlockEntityTypeBuilder.create(NomadWiresEntity::new, NOMAD_WIRES).build(null));
	}


	public static Identifier identifier(String path) {
		return new Identifier(MODID, path);
	}
}