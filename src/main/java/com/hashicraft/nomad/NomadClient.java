package com.hashicraft.nomad;

import com.hashicraft.nomad.block.entity.NomadAllocEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class NomadClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(Nomad.NOMAD_ALLOC_ENTITY, NomadAllocEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_SERVER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_CLIENT, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_WIRES, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_ALLOC, RenderLayer.getTranslucent());
    }
}