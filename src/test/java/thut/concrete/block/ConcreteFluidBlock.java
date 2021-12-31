package thut.concrete.block;

import java.util.function.Supplier;

import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

public class ConcreteFluidBlock extends LiquidBlock implements BucketPickup
{

    public ConcreteFluidBlock(Properties p_54695_, Supplier<? extends FlowingFluid> p_54694_)
    {
        super(p_54694_, p_54695_);
    }

    
}
