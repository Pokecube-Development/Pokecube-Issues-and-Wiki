package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.blocks.FlowerBase;

public class AzureColeusBlock extends FlowerBase
{
   public AzureColeusBlock(final MobEffect effects, int seconds, final BlockBehaviour.Properties properties)
   {
      super(effects, seconds, properties);
   }

   public boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
   {
      return state.is(BlockTags.SAND) || state.is(Blocks.RED_SAND) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.DIRT);
   }
}
