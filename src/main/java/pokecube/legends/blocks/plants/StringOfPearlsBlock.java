package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IForgeShearable;

public class StringOfPearlsBlock extends VineBlock implements BonemealableBlock, IForgeShearable
{
   public static final float CHANCE_OF_FLOWERS_ON_GROWTH = 0.11F;
   public static final BooleanProperty FLOWERS = BooleanProperty.create("flowers");

   public StringOfPearlsBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false))
         .setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false))
         .setValue(FLOWERS, Boolean.valueOf(false)));
   }

   public int getBlocksToGrowWhenBonemealed(Random random)
   {
      return 1;
   }

   public boolean canGrowInto(BlockState state)
   {
      return state.isAir();
   }

   @Override
   public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
   {
      return !state.getValue(FLOWERS);
   }

   @Override
   public boolean isBonemealSuccess(Level world, Random random, BlockPos pos, BlockState state)
   {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
      world.setBlock(pos, state.setValue(FLOWERS, Boolean.valueOf(true)), 2);
   }

   public boolean canSupportAtFace(BlockGetter block, BlockPos pos, Direction direction)
   {
      if (direction == Direction.DOWN)
      {
         return false;
      } else
      {
         BlockPos pos1 = pos.relative(direction);
         if (isAcceptableNeighbour(block, pos1, direction))
         {
            return true;
         } else if (direction.getAxis() == Direction.Axis.Y)
         {
            return false;
         } else
         {
            BooleanProperty b = PROPERTY_BY_DIRECTION.get(direction);
            BlockState state = block.getBlockState(pos.above());
            return state.is(this) && state.getValue(b);
         }
      }
   }

   @Override
   public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
   {
      if (world.random.nextInt(4) == 0 && world.isAreaLoaded(pos, 4))
      {
         Direction direction = Direction.getRandom(random);
         BlockPos pos1 = pos.above();
         if (direction.getAxis().isHorizontal() && !state.getValue(getPropertyForFace(direction)))
         {
            if (this.canSpread(world, pos))
            {
               BlockPos pos2 = pos.relative(direction);
               BlockState state1 = world.getBlockState(pos2);
               if (state1.isAir())
               {
                  Direction direction3 = direction.getClockWise();
                  Direction direction4 = direction.getCounterClockWise();
                  boolean flag = state.getValue(getPropertyForFace(direction3));
                  boolean flag1 = state.getValue(getPropertyForFace(direction4));
                  BlockPos pos3 = pos2.relative(direction3);
                  BlockPos pos4 = pos2.relative(direction4);
                  if (flag && isAcceptableNeighbour(world, pos3, direction3))
                  {
                     world.setBlock(pos2, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true))
                             .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                  } else if (flag1 && isAcceptableNeighbour(world, pos4, direction4))
                  {
                     world.setBlock(pos2, this.defaultBlockState().setValue(getPropertyForFace(direction4), Boolean.valueOf(true))
                             .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                  } else
                  {
                     Direction direction1 = direction.getOpposite();
                     if (flag && world.isEmptyBlock(pos3) && isAcceptableNeighbour(world, pos.relative(direction3), direction1))
                     {
                        world.setBlock(pos3, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true))
                                .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                     } else if (flag1 && world.isEmptyBlock(pos4) && isAcceptableNeighbour(world, pos.relative(direction4), direction1))
                     {
                        world.setBlock(pos4, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true))
                                .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                     } else if ((double)random.nextFloat() < 0.05D && isAcceptableNeighbour(world, pos2.above(), Direction.UP))
                     {
                        world.setBlock(pos2, this.defaultBlockState().setValue(UP, Boolean.valueOf(true))
                                .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                     }
                  }
               } else if (isAcceptableNeighbour(world, pos2, direction))
               {
                  world.setBlock(pos, state.setValue(getPropertyForFace(direction), Boolean.valueOf(true))
                          .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
               }

            }
         } else {
            if (direction == Direction.UP && pos.getY() < world.getMaxBuildHeight() - 1)
            {
               if (this.canSupportAtFace(world, pos, direction))
               {
                  world.setBlock(pos, state.setValue(UP, Boolean.valueOf(true))
                          .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < 0.11F)), 2);
                  return;
               }

               if (world.isEmptyBlock(pos1))
               {
                  if (!this.canSpread(world, pos))
                  {
                     return;
                  }

                  BlockState state2 = state;

                  for(Direction direction2 : Direction.Plane.HORIZONTAL)
                  {
                     if (random.nextBoolean() || !isAcceptableNeighbour(world, pos1.relative(direction2), direction2))
                     {
                        state2 = state2.setValue(getPropertyForFace(direction2), Boolean.valueOf(false));
                     }
                  }

                  if (this.hasHorizontalConnection(state2))
                  {
                     world.setBlock(pos1, state2, 2);
                  }
                  return;
               }
            }

            if (pos.getY() > world.getMinBuildHeight())
            {
               BlockPos pos5 = pos.below();
               BlockState state3 = world.getBlockState(pos5);
               if (state3.isAir() || state3.is(this)) {
                  BlockState state4 = state3.isAir() ? this.defaultBlockState() : state3;
                  BlockState state5 = this.copyRandomFaces(state, state4, random);
                  if (state4 != state5 && this.hasHorizontalConnection(state5))
                  {
                     world.setBlock(pos5, state5, 2);
                  }
               }
            }

         }
      }
   }

   public BlockState copyRandomFaces(BlockState state, BlockState state1, Random random)
   {
      for(Direction direction : Direction.Plane.HORIZONTAL)
      {
         if (random.nextBoolean())
         {
            BooleanProperty b = getPropertyForFace(direction);
            if (state.getValue(b))
            {
               state1 = state1.setValue(b, Boolean.valueOf(true))
                       .setValue(FLOWERS, Boolean.valueOf(random.nextFloat() < CHANCE_OF_FLOWERS_ON_GROWTH));
            }
         }
      }

      return state1;
   }

   public boolean hasHorizontalConnection(BlockState state)
   {
      return state.getValue(NORTH) || state.getValue(EAST) || state.getValue(SOUTH) || state.getValue(WEST);
   }

   public boolean canSpread(BlockGetter block, BlockPos pos)
   {
      int i = 4;
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 4, pos.getY() + 1, pos.getZ() + 4);
      int j = 5;

      for(BlockPos pos1 : iterable)
      {
         if (block.getBlockState(pos1).is(this))
         {
            --j;
            if (j <= 0)
            {
               return false;
            }
         }
      }
      return true;
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHit)
   {
      if (state.getValue(FLOWERS))
      {
         Block.popResource(world, pos, new ItemStack(Items.PINK_DYE, 1));
         float f = Mth.randomBetween(world.random, 0.8F, 1.2F);
         world.playSound((Player)null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
         world.setBlock(pos, state.setValue(FLOWERS, Boolean.valueOf(false)), 2);
         return InteractionResult.sidedSuccess(world.isClientSide);
      } else
      {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
   {
      builder.add(UP, NORTH, EAST, SOUTH, WEST, FLOWERS);
   }
}
