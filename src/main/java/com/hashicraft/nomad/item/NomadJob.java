package com.hashicraft.nomad.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class NomadJob extends Item {
  public NomadJob(Settings settings) {
      super(settings);
  }

  // public Text getName() {
  //   return null;
  // }

  // public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
  //   return 1;
  // }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
      return TypedActionResult.success(playerEntity.getStackInHand(hand));
  }
}
