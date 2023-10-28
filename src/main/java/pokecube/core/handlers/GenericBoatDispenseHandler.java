package pokecube.core.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatType;

public class GenericBoatDispenseHandler extends DefaultDispenseItemBehavior
{
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final BoatType type;

    public GenericBoatDispenseHandler(BoatType type)
    {
        this.type = type;
    }

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack)
    {
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        Level level = source.getLevel();
        double d0 = source.x() + direction.getStepX() * 1.125F;
        double d1 = source.y() + direction.getStepY() * 1.125F;
        double d2 = source.z() + direction.getStepZ() * 1.125F;
        BlockPos blockpos = source.getPos().relative(direction);
        double d3;
        if (level.getFluidState(blockpos).is(FluidTags.WATER))
        {
            d3 = 1.0D;
        } else
        {
            if (!level.getBlockState(blockpos).isAir() || !level.getFluidState(blockpos.below()).is(FluidTags.WATER))
            {
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }

            d3 = 0.0D;
        }

        GenericBoat boat = new GenericBoat(level, d0, d1 + d3, d2);
        boat.setType(this.type);
        boat.setYRot(direction.toYRot());
        level.addFreshEntity(boat);
        stack.shrink(1);
        return stack;
    }
}
