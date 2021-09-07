package com.hashicraft.nomad;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class NomadClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_SERVER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_CLIENT, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_WIRES, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_ALLOC, RenderLayer.getTranslucent());
    }
}