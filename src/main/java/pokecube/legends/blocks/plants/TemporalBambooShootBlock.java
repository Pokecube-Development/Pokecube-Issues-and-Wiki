package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

public class TemporalBambooShootBlock extends BambooSaplingBlock implements BonemealableBlock
{
   // Tags
   public static Tag.Named<Block> TEMPORAL_BAMBOO_PLANTABLE_ON = BlockTags.createOptional(new ResourceLocation(Reference.ID, "temporal_bamboo_plantable_on"));

   public TemporalBambooShootBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
   {
      return (world.getBlockState(pos.below()).is(TemporalBambooShootBlock.TEMPORAL_BAMBOO_PLANTABLE_ON) &&
              !world.getBlockState(pos.below()).is(Blocks.BAMBOO_SAPLING));
   }

   @Override
   public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
   {
      if (!state.canSurvive(world, pos))
      {
         return Blocks.AIR.defaultBlockState();
      } else
      {
         if (direction == Direction.UP && state1.is(PlantsInit.TEMPORAL_BAMBOO.get()))
         {
            world.setBlock(pos, PlantsInit.TEMPORAL_BAMBOO.get().defaultBlockState(), 2);
         }
         return super.updateShape(state, direction, state1, world, pos, pos1);
      }
   }

   @Override
   public void growBamboo(Level world, BlockPos pos)
   {
      world.setBlock(pos.above(), PlantsInit.TEMPORAL_BAMBOO.get().defaultBlockState().setValue(BambooBlock.LEAVES, BambooLeaves.SMALL), 3);
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
      return new ItemStack(ItemInit.TEMPORAL_BAMBOO.get());
   }
}
