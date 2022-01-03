package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.block.flowing.IFlowingBlock;

public class Smoother extends Item
{

    public Smoother(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof IFlowingBlock b && !b.flows() && !b.isFullBlock())
        {
            int r = 1;
            int amt = b.getAmount(state);
            if (context.getPlayer().isShiftKeyDown()) r = 0;
            int n = 0;
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++)
            {
                pos = context.getClickedPos().offset(x, 0, z);
                state = level.getBlockState(pos);
                if (state.getBlock() instanceof IFlowingBlock b2 && !b2.flows() && !b2.isFullBlock())
                {
                    int amt2 = b2.getAmount(state);
                    if (amt2 >= amt)
                    {
                        int diff = amt2 - (amt - 1);
                        level.setBlock(pos, b2.setAmount(state, amt - 1), 3);
                        BlockState removed = b2.setAmount(state, diff);
                        
                    }
                    n++;
                }
            }
            if (n > 0) return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }
}
