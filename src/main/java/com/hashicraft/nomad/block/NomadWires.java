package com.hashicraft.nomad.block;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;

public class NomadWires extends BlockWithEntity {
  private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 0.1D, 16.0D);

  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

  public static final BooleanProperty NORTH_CONNECTED = BooleanProperty.of("north_connected");
  public static final BooleanProperty EAST_CONNECTED = BooleanProperty.of("east_connected");
  public static final BooleanProperty SOUTH_CONNECTED = BooleanProperty.of("south_connected");
  public static final BooleanProperty WEST_CONNECTED = BooleanProperty.of("west_connected");

  public NomadWires(Settings settings) {
    super(settings);
    setDefaultState(
      this.stateManager.getDefaultState()
      .with(FACING, Direction.NORTH)
      .with(NORTH_CONNECTED, false)
      .with(EAST_CONNECTED, false)
      .with(SOUTH_CONNECTED, false)
      .with(WEST_CONNECTED, false)
    );
  }

  public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
    return RAYCAST_SHAPE;
  }

  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return RAYCAST_SHAPE;
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new NomadWiresEntity(pos, state);
  }

  @Override
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
    builder.add(NORTH_CONNECTED);
    builder.add(EAST_CONNECTED);
    builder.add(SOUTH_CONNECTED);
    builder.add(WEST_CONNECTED);
	}

  @Override
  public void onPlaced(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack itemStack) {
    
  }

  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
    return world.isClient ? null : checkType(type, Nomad.NOMAD_WIRES_ENTITY, NomadWiresEntity::tick);
  }
}