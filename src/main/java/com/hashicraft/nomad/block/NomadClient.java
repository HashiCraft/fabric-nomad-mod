package com.hashicraft.nomad.block;

import java.util.Random;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadClientEntity;
import com.hashicraft.nomad.util.Status;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;



public class NomadClient extends BlockWithEntity {
  private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<Status> STATUS = EnumProperty.of("status", Status.class);

  public NomadClient(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(STATUS, Status.DOWN));
  }

  public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
    return RAYCAST_SHAPE;
  }

  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return RAYCAST_SHAPE;
  }

  public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    if (state.get(STATUS) == Status.DOWN) {
      for(int i = 0; i < 4; ++i) {
        double x = (double)pos.getX() + 0.2D + (double)random.nextInt(6)/10;
        double y = (double)pos.getY() + 0.6D;
        double z = (double)pos.getZ() + 0.2D + (double)random.nextInt(6)/10;
        double vx = 0;
        double vy = ((double)random.nextFloat()) * 0.1D;
        double vz = 0;
        world.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
      }
    } else {
      for(int i = 0; i < 2; ++i) {
        double x = (double)pos.getX() + 0.2D + (double)random.nextInt(6)/10;
        double y = (double)pos.getY() + 0.2D;
        double z = (double)pos.getZ() + 0.2D + (double)random.nextInt(6)/10;
        double vx = 0;
        double vy = ((double)random.nextFloat()) * 1.1D;
        double vz = 0;
        world.addParticle(ParticleTypes.ENCHANT, x, y, z, vx, vy, vz);
      }
    }
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new NomadClientEntity(pos, state);
  }

  @Override
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
    builder.add(STATUS);
	}

  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
    return world.isClient ? null : checkType(type, Nomad.NOMAD_CLIENT_ENTITY, NomadClientEntity::tick);
  }
}