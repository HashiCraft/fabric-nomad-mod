package com.hashicraft.nomad;

import com.hashicraft.events.NomadServerClicked;
import com.hashicraft.nomad.block.entity.NomadAllocEntityRenderer;
import com.hashicraft.nomad.gui.NomadServerGUI;
import com.hashicraft.nomad.gui.NomadServerScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
public class NomadClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(Nomad.NOMAD_ALLOC_ENTITY, NomadAllocEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_SERVER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_CLIENT, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_WIRES, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Nomad.NOMAD_ALLOC, RenderLayer.getTranslucent());

        NomadServerClicked.EVENT.register((block) -> {
            NomadServerGUI gui = new NomadServerGUI();
            NomadServerScreen screen = new NomadServerScreen(gui);
            MinecraftClient.getInstance().setScreen(screen);
      
            // set the default state
            gui.setup(block);
      
            return ActionResult.PASS;
          });
    }
}