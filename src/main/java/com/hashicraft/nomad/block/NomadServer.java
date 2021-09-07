package com.hashicraft.nomad.block;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadServerEntity;
import com.hashicraft.nomad.item.NomadJob;
import com.hashicraft.nomad.util.Status;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;

public class NomadServer extends BlockWithEntity {
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<Status> STATUS = EnumProperty.of("status", Status.class);
  public static final IntProperty NODES = IntProperty.of("nodes", 0, 999);

  public NomadServer(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(STATUS, Status.DOWN).with(NODES, 0));
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
    if (!world.isClient) {
      Item item = player.getMainHandStack().getItem();
      if (item instanceof NomadJob) {
        NomadJob job = (NomadJob)item;
        
        NomadServerEntity server = (NomadServerEntity)world.getBlockEntity(pos);
        server.registerJob(job.getFilename());
      }
    }

    return ActionResult.SUCCESS;
  }

  // public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    // Direction facing = state.get(Properties.HORIZONTAL_FACING);
    // Direction offsetDirection = facing.rotateCounterclockwise(Axis.Y);

    // int nodes = state.get(NODES);
    // for (int i = 0; i < nodes * 2; i++) {
    //   BlockPos offsetPos = pos.offset(offsetDirection, i);
    //   world.breakBlock(offsetPos, false);

    //   BlockPos nodePos = offsetPos.offset(facing.getOpposite());
    //   world.breakBlock(nodePos, false);
    // }
  // }

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