package pokecube.core.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
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
        Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
        Level level = source.getLevel();
        double d0 = source.x() + (double) ((float) direction.getStepX() * 1.125F);
        double d1 = source.y() + (double) ((float) direction.getStepY() * 1.125F);
        double d2 = source.z() + (double) ((float) direction.getStepZ() * 1.125F);
        BlockPos blockpos = source.getPos().relative(direction);
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
        source.getLevel().levelEvent(1000, source.getPos(), 0);
    }
}
