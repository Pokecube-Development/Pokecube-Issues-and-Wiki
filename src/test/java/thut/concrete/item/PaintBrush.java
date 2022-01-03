package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;

public class PaintBrush extends Item
{
    private final DyeColor colour;

    public PaintBrush(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IDyedBlock b)
        {
            if (colour != b.getColour())
            {
                BlockState painted = IFlowingBlock.copyValidTo(state, b.getFor(colour).defaultBlockState());
                level.setBlock(pos, painted, 3);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useOn(context);
    }

}
