package com.hashicraft.nomad.block;

import java.util.Random;

import com.hashicraft.nomad.block.entity.NomadServerEntity;
import com.hashicraft.nomad.block.entity.NomadWiresEntity;
import com.hashicraft.nomad.state.AddServerData;
import com.hashicraft.nomad.state.Messages;
import com.hashicraft.nomad.util.NodeStatus;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class NomadClient extends Block {
  private static final VoxelShape RAYCAST_SHAPE = createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<NodeStatus> STATUS = EnumProperty.of("status", NodeStatus.class);

  public NomadClient(Settings settings) {
    super(settings);
    setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(STATUS, NodeStatus.DOWN));
  }

  private BlockPos findServer(World world, BlockPos pos, Direction direction) {
    // Get the block in front of the server
    BlockPos nextPos = pos.offset(direction);
    BlockEntity startEntity = world.getBlockEntity(nextPos);

    if (startEntity instanceof NomadWiresEntity) {
      // Rotate clockwise to find the block to the right.
      Direction offsetDirection = direction.rotateClockwise(Axis.Y);

      while (true) {
        // Get the next block and see if it is a wire or a server.
        nextPos = nextPos.offset(offsetDirection);
        BlockEntity nextEntity = world.getBlockEntity(nextPos);
        if (nextEntity instanceof NomadWiresEntity) {
          // If it is a wire, continue.
          continue;
        } else if (nextEntity instanceof NomadServerEntity) {
          // If it is a server, we have found our target.
          return nextPos;
        } else {
          // If it is anything else, something went wrong.
          return null;
        }
      }
    }
    return null;
  }

  @Override
  public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
      BlockHitResult hit) {
    if (!world.isClient) {
      Direction facing = state.get(Properties.HORIZONTAL_FACING);
      BlockPos serverPos = findServer(world, pos, facing);
      if (serverPos != null) {
        Axis serverDirection = facing.rotateClockwise(Axis.Y).getAxis();
        int distance = 0;
        switch (serverDirection) {
          case X:
            distance = pos.getX() - serverPos.getX();
            break;
          case Z:
            distance = pos.getZ() - serverPos.getZ();
            break;
          default:
            break;
        }

        int index = (Math.abs(distance) / 2) - 1;

        if (player.getMainHandStack().isOf(Items.BUCKET)) {
          // fire an event to let the server know a node has been drained
          AddServerData serverData = new AddServerData(serverPos);
          serverData.index = index;
          PacketByteBuf buf = PacketByteBufs.create();
          buf.writeByteArray(serverData.toBytes());

          ClientPlayNetworking.send(Messages.NODE_DRAIN, buf);
        }
      }
    }

    return ActionResult.SUCCESS;
  }

  public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
    return RAYCAST_SHAPE;
  }

  public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    return RAYCAST_SHAPE;
  }

  public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    if (state.get(STATUS) == NodeStatus.DOWN) {
      for (int i = 0; i < 4; ++i) {
        double x = (double) pos.getX() + 0.2D + (double) random.nextInt(6) / 10;
        double y = (double) pos.getY() + 0.6D;
        double z = (double) pos.getZ() + 0.2D + (double) random.nextInt(6) / 10;
        double vx = 0;
        double vy = ((double) random.nextFloat()) * 0.1D;
        double vz = 0;
        world.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
      }

      for (int i = 0; i < 2; ++i) {
        double x = (double) pos.getX() + 0.2D + (double) random.nextInt(6) / 10;
        double y = (double) pos.getY() + 0.6D;
        double z = (double) pos.getZ() + 0.2D + (double) random.nextInt(6) / 10;
        double vx = 0;
        double vy = ((double) random.nextFloat()) * 0.01D;
        double vz = 0;
        world.addParticle(ParticleTypes.FLAME, x, y, z, vx, vy, vz);
      }
    } else {
      for (int i = 0; i < 10; ++i) {
        double x = (double) pos.getX() + 0.2D + (double) random.nextInt(6) / 10;
        double y = (double) pos.getY() + 0.2D;
        double z = (double) pos.getZ() + 0.2D + (double) random.nextInt(6) / 10;
        double vx = 0;
        double vy = ((double) random.nextFloat()) * 1.1D;
        double vz = 0;
        world.addParticle(ParticleTypes.ENCHANT, x, y, z, vx, vy, vz);
      }
    }
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
}