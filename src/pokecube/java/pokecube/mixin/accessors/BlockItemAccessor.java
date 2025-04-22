package pokecube.mixin.accessors;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pokecube.core.utils.mixin.IBlockItem;

@Mixin(BlockItem.class)
public abstract class BlockItemAccessor extends Item implements IBlockItem
{
    public BlockItemAccessor(Properties properties)
    {
        super(properties);
    }

    @Shadow
    protected abstract BlockState getPlacementState(BlockPlaceContext context);

    @Override
    public BlockState getPlacement(BlockPlaceContext context)
    {
        return getPlacementState(context);
    }
}
