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

public class ConcreteDispenseBehaviour implements DispenseItemBehavior
{

    public static final DispenseItemBehavior INSTANCE = new ConcreteDispenseBehaviour();

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
            int amt = 0;
            if (b.canReplace(state) && (amt = b.getAmount(state)) != 16 && amt >= 0)
            {
                level.setBlock(pos, concrete, 3);
                ItemStack newStack = stack;
                if (stack.getItem() == Concrete.BUCKET.get()) newStack = new ItemStack(Items.BUCKET);
                else stack.split(1);
                return newStack;
            }
        }
        return stack;
    }

}
