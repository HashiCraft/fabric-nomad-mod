package com.hashicraft.nomad.block.entity;

import java.util.Hashtable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class NomadAllocEntityRenderer<T extends NomadAllocEntity> implements BlockEntityRenderer<T> {
  
  public NomadAllocEntityRenderer(BlockEntityRendererFactory.Context ctx) {
  }

  @Override
  public void render(NomadAllocEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
    Direction direction = blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING);

    float xTranslate = 0.0F;
    float yTranslate = 0.7F;
    float zTranslate = 0.0F;
    float scale = 0.01F;

    Quaternion yRotation = Vec3f.POSITIVE_Y.getDegreesQuaternion(0.0F);

    switch (direction) {
      case UP:
        break;
      case DOWN:
        break;
      case NORTH:
        xTranslate = 0.5F;
        zTranslate = -0.05F;
        break;
      case SOUTH:
        xTranslate = 0.5F;
        zTranslate = 1.05F;
        yRotation = Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F);
        break;
      case EAST:
        xTranslate = 1.05F;
        zTranslate = 0.5F;
        yRotation = Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0F);
        break;
        case WEST:
        xTranslate = -0.05F;
        zTranslate = 0.5F;
        yRotation = Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0F);
        
        break;
    }

    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    Text id = new LiteralText(blockEntity.getID().split("-")[0]);
    matrices.push();
    matrices.translate(xTranslate, yTranslate, zTranslate);
    matrices.scale(-scale, -scale, scale);
    matrices.multiply(yRotation);

    float idWidth = (float)(-textRenderer.getWidth((StringVisitable)id) / 2);
    textRenderer.drawWithShadow(matrices, id, idWidth, 0F, 0xFFFFFFFF);
    matrices.pop();


    Text name = new LiteralText(blockEntity.getName());
    matrices.push();
    matrices.translate(xTranslate, yTranslate - 0.15F, zTranslate);
    matrices.scale(-scale, -scale, scale);
    matrices.multiply(yRotation);

    float nameWidth = (float)(-textRenderer.getWidth((StringVisitable)name) / 2);
    textRenderer.drawWithShadow(matrices, name, nameWidth, 0F, 0xFFFFFFFF);
    matrices.pop();

    Text status = new LiteralText(blockEntity.getStatus());
    matrices.push();
    matrices.translate(xTranslate, yTranslate - 0.30F, zTranslate);
    matrices.scale(-scale, -scale, scale);
    matrices.multiply(yRotation);

    float statusWidth = (float)(-textRenderer.getWidth((StringVisitable)status) / 2);
    textRenderer.drawWithShadow(matrices, status, statusWidth, 0F, 0xFFFFFFFF);
    matrices.pop();
  }
}