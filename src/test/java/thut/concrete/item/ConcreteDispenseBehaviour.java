package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;
import thut.concrete.block.ConcreteBlock;
import thut.concrete.block.WetConcreteBlock;

public class ConcreteDispenseBehaviour implements DispenseItemBehavior
{

    @Override
    public ItemStack dispense(BlockSource source, ItemStack stack)
    {
        ServerLevel level = source.getLevel();
        BlockPos pos = source.getPos();
        BlockState state = source.getBlockState();

        if (state.hasProperty(BlockStateProperties.FACING))
        {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            pos = pos.relative(dir);
            state = level.getBlockState(pos);
        }

        BlockState concrete = Concrete.WET_BLOCK.get().defaultBlockState();
        if (concrete.getBlock() instanceof IFlowingBlock b)
        {
            if (b.canReplace(state) || state.getBlock() instanceof ConcreteBlock
                    || state.getBlock() instanceof WetConcreteBlock)
            {
                level.setBlock(pos, concrete, 3);
                ItemStack newStack = new ItemStack(Items.BUCKET);
                return newStack;
            }
        }
        if (stack.isEmpty()) stack = new ItemStack(Items.BUCKET);

        return stack;
    }

}
