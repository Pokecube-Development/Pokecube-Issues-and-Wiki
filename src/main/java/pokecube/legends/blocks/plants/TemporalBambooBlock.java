package pokecube.legends.blocks.plants;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.Reference;
import pokecube.legends.init.PlantsInit;

public class TemporalBambooBlock extends BambooBlock implements BonemealableBlock
{
   protected static final VoxelShape SMALL_SHAPE = Block.box(7, 0, 7, 9, 16, 9);
   protected static final VoxelShape LARGE_SHAPE = Block.box(6.5, 0, 6.5, 9.5, 16, 9.5);
   // Tags
   public static Tag.Named<Block> TEMPORAL_BAMBOO_PLANTABLE_ON = BlockTags.createOptional(new ResourceLocation(Reference.ID, "temporal_bamboo_plantable_on"));

   public TemporalBambooBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, Integer.valueOf(0)));
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
   {
      VoxelShape voxelshape = state.getValue(AGE) == 1 ? LARGE_SHAPE : SMALL_SHAPE;
      Vec3 vec3 = state.getOffset(block, pos);
      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
   {
      VoxelShape voxelshape = state.getValue(AGE) == 1 ? LARGE_SHAPE : SMALL_SHAPE;
      Vec3 vec3 = state.getOffset(block, pos);
      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   @Override
   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context)
   {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      if (!fluid.isEmpty())
      {
         return null;
      } else
      {
         BlockState state = context.getLevel().getBlockState(context.getClickedPos().below());
         if (state.is(TemporalBambooBlock.TEMPORAL_BAMBOO_PLANTABLE_ON))
         {
            if (state.is(PlantsInit.TEMPORAL_BAMBOO_SHOOT.get()))
            {
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(0));
            } else if (state.is(PlantsInit.TEMPORAL_BAMBOO.get()))
            {
               int i = state.getValue(AGE) > 0 ? 1 : 0;
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(i));
            } else
            {
               BlockState state1 = context.getLevel().getBlockState(context.getClickedPos().above());
               return state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) ? this.defaultBlockState().setValue(AGE, state1.getValue(AGE))
                   : PlantsInit.TEMPORAL_BAMBOO_SHOOT.get().defaultBlockState();
            }
         } else
         {
            return null;
         }
      }
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
   {
      return world.getBlockState(pos.below()).is(TemporalBambooBlock.TEMPORAL_BAMBOO_PLANTABLE_ON);
   }

   @Override
   public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
   {
      if (!state.canSurvive(world, pos))
      {
         world.getBlockTicks().scheduleTick(pos, this, 1);
      }

      if (direction == Direction.UP && state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) && state1.getValue(AGE) > state.getValue(AGE))
      {
         world.setBlock(pos, state.cycle(AGE), 2);
      }

      return super.updateShape(state, direction, state1, world, pos, pos1);
   }

   @Override
   public void growBamboo(BlockState state, Level world, BlockPos pos, Random random, int x)
   {
      BlockState state1 = world.getBlockState(pos.below());
      BlockPos pos1 = pos.below(2);
      BlockState state2 = world.getBlockState(pos1);
      BambooLeaves leaves = BambooLeaves.NONE;
      if (x >= 1)
      {
         if (state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) && state1.getValue(LEAVES) != BambooLeaves.NONE)
         {
            if (state1.is(PlantsInit.TEMPORAL_BAMBOO.get()) && state1.getValue(LEAVES) != BambooLeaves.NONE)
            {
               leaves = BambooLeaves.LARGE;
               if (state2.is(PlantsInit.TEMPORAL_BAMBOO.get()))
               {
                  world.setBlock(pos.below(), state1.setValue(LEAVES, BambooLeaves.SMALL), 3);
                  world.setBlock(pos1, state2.setValue(LEAVES, BambooLeaves.NONE), 3);
               }
            }
         } else
         {
            leaves = BambooLeaves.SMALL;
         }
      }

      int i = state.getValue(AGE) != 1 && !state2.is(PlantsInit.TEMPORAL_BAMBOO.get()) ? 0 : 1;
      int j = (x < 11 || !(random.nextFloat() < 0.25F)) && x != 15 ? 0 : 1;
      world.setBlock(pos.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(i)).setValue(LEAVES, leaves)
              .setValue(STAGE, Integer.valueOf(j)), 3);
   }

   @Override
   public int getHeightAboveUpToMax(BlockGetter block, BlockPos pos)
   {
      int i;
      for(i = 0; i < 16 && block.getBlockState(pos.above(i + 1)).is(PlantsInit.TEMPORAL_BAMBOO.get()); ++i)
      {
      }

      return i;
   }

   @Override
   public int getHeightBelowUpToMax(BlockGetter block, BlockPos pos)
   {
      int i;
      for(i = 0; i < 16 && block.getBlockState(pos.below(i + 1)).is(PlantsInit.TEMPORAL_BAMBOO.get()); ++i)
      {}
      return i;
   }
}
