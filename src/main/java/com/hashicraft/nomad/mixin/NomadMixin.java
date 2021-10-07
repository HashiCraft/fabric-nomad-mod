package com.hashicraft.nomad.mixin;

import com.hashicraft.nomad.Nomad;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

import com.google.gson.JsonElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class NomadMixin {
	// @Inject(method = "apply", at = @At("HEAD"))
	// 	public void interceptApply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
	// 		map.put(Nomad.NOMAD_SERVER_ID, Nomad.NOMAD_SERVER_RECIPE);
	// 	}
}
