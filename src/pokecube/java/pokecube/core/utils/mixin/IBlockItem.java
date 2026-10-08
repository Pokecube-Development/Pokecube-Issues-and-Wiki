package pokecube.core.utils.mixin;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockItem
{
    default BlockState getPlacement(BlockPlaceContext context) {return null;}
}
