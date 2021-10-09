package com.hashicraft.events;

import com.hashicraft.nomad.block.entity.NomadServerEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface NomadServerClicked {
  Event<NomadServerClicked> EVENT = EventFactory.createArrayBacked(NomadServerClicked.class,
      (listeners) -> (block) -> {
        for (NomadServerClicked listener : listeners) {
          ActionResult result = listener.interact(block);

          if (result != ActionResult.PASS) {
            return result;
          }
        }

        return ActionResult.PASS;
      });

  ActionResult interact(NomadServerEntity block);
}