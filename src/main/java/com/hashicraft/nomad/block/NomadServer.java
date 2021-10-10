package com.hashicraft.nomad.block;

import java.util.List;

import com.github.hashicraft.stateful.blocks.StatefulBlock;
import com.hashicraft.events.NomadServerClicked;
import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadServerEntity;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;
import com.hashicraft.nomad.item.NomadJob;
import com.hashicraft.nomad.state.AddServerData;
import com.hashicraft.nomad.state.Messages;
import com.hashicraft.nomad.util.NodeStatus;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NomadServer extends StatefulBlock {
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<NodeStatus> STATUS = EnumProperty.of("status", NodeStatus.class);
  public static final IntProperty NODES = IntProperty.of("nodes", 0, 999);

  public NomadServer(Settings settings) {
    super(settings);
    setDefaultState(
        this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(STATUS, NodeStatus.DOWN).with(NODES, 0));
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
      BlockHitResult hit) {
    Item item = player.getMainHandStack().getItem();
    NomadServerEntity server = (NomadServerEntity) world.getBlockEntity(pos);

    if (!world.isClient) {
      if (item instanceof NomadJob) {
        NomadJob job = (NomadJob) item;
        // fire an event to let the server know a job has been registered
        AddServerData serverData = new AddServerData(pos);
        serverData.filename = job.getFilename();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByteArray(serverData.toBytes());

        ClientPlayNetworking.send(Messages.NODE_DRAIN, buf);
      }
    }
    if (world.isClient) {
      if (item instanceof AirBlockItem) {
        NomadServerClicked.EVENT.invoker().interact(server);
      }
    }

    return ActionResult.SUCCESS;
  }

  private void broadcast(World world, String message, boolean actionBar) {
    List<? extends PlayerEntity> players = world.getPlayers();
    for (PlayerEntity player : players) {
      player.sendMessage(new LiteralText(message), actionBar);
    }
  }

  @Override
  public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    super.onBreak(world, pos, state, player);
    // fire an event to let the server know that a new server has been added
    AddServerData serverData = new AddServerData(pos, "");
    PacketByteBuf buf = PacketByteBufs.create();
    buf.writeByteArray(serverData.toBytes());

    ClientPlayNetworking.send(Messages.REMOVE_SERVER, buf);

    Direction facing = state.get(FACING);
    Direction offsetDirection = facing.rotateYCounterclockwise();

    while (true) {
      pos = pos.offset(offsetDirection);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (!(blockEntity instanceof NomadWiresEntity)) {
        return;
      }

      world.removeBlock(pos, false);
      world.removeBlockEntity(pos);

      BlockPos clientPos = pos.offset(offsetDirection.rotateYCounterclockwise());
      world.removeBlock(clientPos, false);
      world.removeBlockEntity(clientPos);

      for (int i = 0; i < 255; i++) {
        BlockPos allocPos = clientPos.offset(Direction.UP, i + 1);
        world.removeBlock(allocPos, false);
        world.removeBlockEntity(allocPos);
      }
    }
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new NomadServerEntity(pos, state, this);
  }

  @Override
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
    builder.add(STATUS);
    builder.add(NODES);
  }

  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
      BlockEntityType<T> type) {
    return checkType(type, Nomad.NOMAD_SERVER_ENTITY, NomadServerEntity::tick);
  }
}