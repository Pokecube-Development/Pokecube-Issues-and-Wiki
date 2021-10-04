package thut.api.entity;

import net.minecraft.world.item.ItemStack;

public interface IShearable
{
    boolean isSheared();

    default void shear()
    {

    }

    default void shear(final ItemStack shears)
    {
        this.shear();
    }
}
