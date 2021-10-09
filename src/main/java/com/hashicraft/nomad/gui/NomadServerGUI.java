package com.hashicraft.nomad.gui;

import com.hashicraft.nomad.block.entity.NomadServerEntity;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class NomadServerGUI extends LightweightGuiDescription {
  WGridPanel root = new WGridPanel();

  private NomadServerEntity server;

  public NomadServerGUI() {
    setRootPanel(root);
    root.setSize(240, 50);
    root.setInsets(Insets.ROOT_PANEL);
  }

  public void setup(NomadServerEntity entity) {
    this.server = entity;

    WLabel label = new WLabel(new LiteralText("Nomad Server"));
    root.add(label, 0, 0, 3, 1);

    WTextField address;
    address = new WTextField(new LiteralText("http://127.0.0.1:4646"));
    root.add(address, 0, 1, 10, 1);
    address.setMaxLength(255);
    address.setText(server.getAddress());

    WButton button = new WButton(new LiteralText("Save"));
    button.setOnClick(() -> {
      server.setAddress(address.getText());
      MinecraftClient.getInstance().player.closeScreen();
      MinecraftClient.getInstance().setScreen((Screen)null);
    });
    root.add(button, 11, 1, 3, 1);

    root.validate(this);
  }
}