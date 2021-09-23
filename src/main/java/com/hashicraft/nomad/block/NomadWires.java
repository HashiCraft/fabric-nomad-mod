package com.hashicraft.nomad.block;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

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

  public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
    if (
      neighborState.getBlock() == Nomad.NOMAD_SERVER ||
      neighborState.getBlock() == Nomad.NOMAD_CLIENT ||
      neighborState.getBlock() == Nomad.NOMAD_WIRES
    ) {
      switch (direction) {
        case NORTH:
          state = state.with(NomadWires.NORTH_CONNECTED, true);
          break;
        case EAST:
          state = state.with(NomadWires.EAST_CONNECTED, true);
          break;
        case SOUTH:
          state = state.with(NomadWires.SOUTH_CONNECTED, true);
          break;
        case WEST:
          state = state.with(NomadWires.WEST_CONNECTED, true);
          break;
        case UP:
          break;
        case DOWN:
          break;
        default:
          break;
      }
    } else {
      switch (direction) {
        case NORTH:
          state = state.with(NomadWires.NORTH_CONNECTED, false);
          break;
        case EAST:
          state = state.with(NomadWires.EAST_CONNECTED, false);
          break;
        case SOUTH:
          state = state.with(NomadWires.SOUTH_CONNECTED, false);
          break;
        case WEST:
          state = state.with(NomadWires.WEST_CONNECTED, false);
          break;
        case UP:
          break;
        case DOWN:
          break;
        default:
          break;
      }
    }

    return state;
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
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
  }
}