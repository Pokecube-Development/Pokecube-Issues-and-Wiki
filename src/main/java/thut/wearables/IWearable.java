package thut.wearables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IWearable
{
    default boolean canPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        return true;
    }

    default boolean canRemove(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
            final int subIndex)
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    /**
     * Does this wearable handle the render offsets by itself?
     *
     * @return
     */
    default boolean customOffsets()
    {
        return false;
    }

    default boolean dyeable(final ItemStack stack)
    {
        return false;
    }

    EnumWearable getSlot(ItemStack stack);

    @OnlyIn(Dist.CLIENT)
    /**
     * This is called after doing the main transforms needed to get the gl
     * calls to the correct spot.
     *
     * @param wearer
     *            - The entity wearing the stack
     * @param stack
     *            - The stack being worn
     */
    void renderWearable(EnumWearable slot, int subindex, LivingEntity wearer, ItemStack stack, float partialTicks);
}
