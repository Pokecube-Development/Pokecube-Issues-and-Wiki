package pokecube.core.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatType;
import pokecube.core.entity.boats.GenericChestBoat;

public class GenericBoatDispenseHandler extends DefaultDispenseItemBehavior
{
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final BoatType type;
    private final boolean isChestBoat;

    public GenericBoatDispenseHandler(BoatType type, boolean hasChest)
    {
        this.type = type;
        this.isChestBoat = hasChest;
    }

    @Override
    public ItemStack execute(BlockSource source, ItemStack item)
    {
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Level level = source.level();
        var mid = source.center();
        double d0 = mid.x() + direction.getStepX() * 1.125F;
        double d1 = mid.y() + direction.getStepY() * 1.125F;
        double d2 = mid.z() + direction.getStepZ() * 1.125F;
        BlockPos blockpos = source.pos().relative(direction);
        GenericBoat boat = this.isChestBoat ? new GenericChestBoat(level, d0, d1, d2)
                : new GenericBoat(level, d0, d1, d2);
        boat.setType(this.type);
        boat.setYRot(direction.toYRot());
        double d3;
        if (boat.canBoatInFluid(level.getFluidState(blockpos)))
        {
            d3 = 1.0D;
        }
        else
        {
            if (!level.getBlockState(blockpos).isAir() || !boat.canBoatInFluid(level.getFluidState(blockpos.below())))
            {
                return this.defaultDispenseItemBehavior.dispense(source, item);
            }

            d3 = 0.0D;
        }
        boat.setPos(d0, d1 + d3, d2);
        level.addFreshEntity(boat);
        item.shrink(1);
        return item;
    }

    protected void playSound(BlockSource source)
    {
        source.level().levelEvent(1000, source.pos(), 0);
    }
}
