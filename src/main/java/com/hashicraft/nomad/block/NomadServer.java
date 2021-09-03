package com.hashicraft.nomad.block;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadServerEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;

public class NomadServer extends BlockWithEntity {
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

  public NomadServer(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
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