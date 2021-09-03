package com.hashicraft.nomad.block;

import java.util.Random;

import com.hashicraft.nomad.Nomad;
import com.hashicraft.nomad.block.entity.NomadClientEntity;

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
import net.minecraft.state.property.Properties;

public class NomadClient extends BlockWithEntity {
  private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

  public NomadClient(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
  }

  public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
    return RAYCAST_SHAPE;
  }

  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return RAYCAST_SHAPE;
  }

  public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    for(int i = 0; i < 4; ++i) {
        double x = (double)pos.getX();
        double y = (double)pos.getY();
        double z = (double)pos.getZ();
        double vx = ((double)random.nextFloat() - 0.5D) * 0.5D;
        double vy = ((double)random.nextFloat() - 0.5D) * 0.5D;
        double vz = ((double)random.nextFloat() - 0.5D) * 0.5D;
        x = x + 0.5D;
        y = y + 0.5D;
        z = z + 0.5D;

        world.addParticle(ParticleTypes.PORTAL, x, y, z, vx, vy, vz);
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