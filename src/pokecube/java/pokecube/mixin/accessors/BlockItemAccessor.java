package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockItem.class)
public abstract class BlockItemAccessor extends Item
{
    public BlockItemAccessor(Properties properties)
    {
        super(properties);
    }

    @Shadow
    public abstract BlockState getPlacementState(BlockPlaceContext context);
}
