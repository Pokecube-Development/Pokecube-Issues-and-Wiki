package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;
import thut.concrete.block.ConcreteBlock;
import thut.concrete.block.WetConcreteBlock;

public class ConcreteBucket extends Item implements DispensibleContainerItem
{

    public ConcreteBucket(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = new BlockPos(context.getClickLocation());
        BlockHitResult result = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), false);
        if (emptyContents(context.getPlayer(), level, pos, result))
            return InteractionResult.sidedSuccess(level.isClientSide);
        return super.useOn(context);
    }

    @Override
    public boolean emptyContents(Player player, Level level, BlockPos pos, BlockHitResult hit)
    {
        BlockState blockstate = level.getBlockState(pos);

        BlockState concrete = Concrete.WET_BLOCK.get().defaultBlockState();
        if (concrete.getBlock() instanceof IFlowingBlock b)
        {
            boolean alreadyConcrete = blockstate.getBlock() instanceof ConcreteBlock
                    || blockstate.getBlock() instanceof WetConcreteBlock;
            if (b.canReplace(blockstate) || alreadyConcrete)
            {
                if (alreadyConcrete)
                {
                    int amt = b.getAmount(blockstate);
                    BlockState b2 = level.getBlockState(pos.above());
                    if (b.canReplace(b2))
                    {
                        Block wet_layer = Concrete.WET_LAYER.get();
                        BlockState remaining = b.setAmount(wet_layer.defaultBlockState(), amt);
                        remaining = remaining.setValue(IFlowingBlock.FALLING, true);
                        level.setBlock(pos.above(), remaining, 3);
                        level.setBlock(pos, concrete, 3);
                        return true;
                    }
                }
                else
                {
                    level.setBlock(pos, concrete, 3);
                    return true;
                }
            }
        }
        return false;
    }
}
