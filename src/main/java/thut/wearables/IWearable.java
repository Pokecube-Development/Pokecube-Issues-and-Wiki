package thut.wearables;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
    /** Does this wearable handle the render offsets by itself?
     *
     * @return */
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
    /** This is called after doing the main transforms needed to get the gl
     * calls to the correct spot.
     *
     * @param wearer
     *            - The entity wearing the stack
     * @param stack
     *            - The stack being worn */
    public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks, int brightness,
            int overlay);
}
