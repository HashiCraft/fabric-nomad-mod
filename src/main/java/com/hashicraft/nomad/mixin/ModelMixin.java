package com.hashicraft.nomad.mixin;

import java.util.Random;

import com.hashicraft.nomad.Nomad;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ModelLoader.class)
public class ModelMixin {
  private Random random = new Random();

  @Inject(method = "loadModelFromJson", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceManager;getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;"), cancellable = true)
  public void loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
    if (!"nomad".equals(id.getNamespace())) return;
    if(!id.getPath().startsWith("item/nomad_job_")) return;

    int color = random.nextInt(5);

    String modelJson = Nomad.createItemModelJson(color);
    JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson);
    model.id = id.toString();
    cir.setReturnValue(model);
  }
}