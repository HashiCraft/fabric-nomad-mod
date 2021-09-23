package com.hashicraft.nomad.block;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadServerEntity;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;
import com.hashicraft.nomad.gui.NomadServerGUI;
import com.hashicraft.nomad.gui.NomadServerScreen;
import com.hashicraft.nomad.item.NomadJob;
import com.hashicraft.nomad.util.NodeStatus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NomadServer extends BlockWithEntity {
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<NodeStatus> STATUS = EnumProperty.of("status", NodeStatus.class);
  public static final IntProperty NODES = IntProperty.of("nodes", 0, 999);

  public NomadServer(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(STATUS, NodeStatus.DOWN).with(NODES, 0));
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    Item item = player.getMainHandStack().getItem();
    NomadServerEntity server = (NomadServerEntity)world.getBlockEntity(pos);

    if (!world.isClient) {
      if (item instanceof NomadJob) {
        NomadJob job = (NomadJob)item;
        server.registerJob(job.getFilename());
      }
    }
    if (world.isClient) {
      if (item instanceof AirBlockItem) {
        MinecraftClient.getInstance().setScreen(new NomadServerScreen(new NomadServerGUI(server)));
      }
    }

    return ActionResult.SUCCESS;
  }

  @Override
  public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    super.onBreak(world, pos, state, player);
    Direction facing = state.get(FACING);
    Direction offsetDirection = facing.rotateYCounterclockwise();

    while(true) {
      pos = pos.offset(offsetDirection);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if(!(blockEntity instanceof NomadWiresEntity)) {
        return;
      }

      world.removeBlock(pos, false);

      BlockPos clientPos = pos.offset(offsetDirection.rotateYCounterclockwise());
      world.removeBlock(clientPos, false);

      for (int i = 0; i < 255; i++) {
        BlockPos allocPos = clientPos.offset(Direction.UP, i + 1);
        world.removeBlock(allocPos, false);
      }
    }
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new NomadServerEntity(pos, state);
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
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
    return world.isClient ? null : checkType(type, Nomad.NOMAD_SERVER_ENTITY, NomadServerEntity::tick);
  }
}