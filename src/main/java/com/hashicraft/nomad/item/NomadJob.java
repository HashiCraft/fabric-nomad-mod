package com.hashicraft.nomad.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class NomadJob extends Item {
  private String name;

  public NomadJob(Settings settings, String name) {
    super(settings);

    this.name = name;
  }

  @Override
  public String getTranslationKey() {
    return "item.nomad.nomad_job";
  }

  public String getFilename() {
    return this.name;
  }

  @Override
  public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
    tooltip.add(new LiteralText(this.name + ".hcl"));
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
    if(!world.isClient) {
      
    }
    return TypedActionResult.success(playerEntity.getStackInHand(hand));
  }
}
