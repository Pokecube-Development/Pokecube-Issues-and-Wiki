package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

public class TaintedRootsBlock extends RootsBlock
{
   public TaintedRootsBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
   {
      return state.is(BlockTags.NYLIUM) || state.is(Tags.Blocks.DIRT) || state.is(BlockTags.DIRT)
              || super.mayPlaceOn(state, block, pos);
   }
}