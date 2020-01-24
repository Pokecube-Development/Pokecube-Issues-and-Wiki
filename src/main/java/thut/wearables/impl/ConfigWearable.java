package thut.wearables.impl;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class ConfigWearable implements IActiveWearable, ICapabilityProvider
{
    private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);
    EnumWearable                                slot;

    public ConfigWearable(final EnumWearable slot)
    {
        this.slot = slot;
    }

    @Override
    public boolean dyeable(final ItemStack stack)
    {
        // TODO see if this should be appled here.
        return false;
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return ThutWearables.WEARABLE_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public EnumWearable getSlot(final ItemStack stack)
    {
        if (this.slot == null && stack.hasTag() && stack.getTag().contains("wslot")) this.slot = EnumWearable.valueOf(
                stack.getTag().getString("wslot"));
        return this.slot;
    }

    @Override
    public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
            final ItemStack stack, final float partialTicks)
    {
        // TODO way to register renderers for config wearables

        // This is for items that should just be directly rendered
        if (stack.hasTag() && stack.getTag().contains("wslot"))
        {

            GlStateManager.pushMatrix();

            GlStateManager.rotatef(180, 0, 0, 1);

            if (stack.getTag().contains("winfo"))
            {
                final CompoundNBT info = stack.getTag().getCompound("winfo");
                if (info.contains("scale"))
                {
                    final float scale = info.getFloat("scale");
                    GlStateManager.scalef(scale, scale, scale);
                }
                if (info.contains("shiftx"))
                {
                    final float shift = info.getFloat("shiftx");
                    GlStateManager.translatef(shift, 0, 0);
                }
                if (info.contains("shifty"))
                {
                    final float shift = info.getFloat("shifty");
                    GlStateManager.translatef(0, shift, 0);
                }
                if (info.contains("shiftz"))
                {
                    final float shift = info.getFloat("shiftz");
                    GlStateManager.translatef(0, 0, shift);
                }
                if (info.contains("rotx"))
                {
                    final float shift = info.getFloat("rotx");
                    GlStateManager.rotatef(shift, 1, 0, 0);
                }
                if (info.contains("roty"))
                {
                    final float shift = info.getFloat("roty");
                    GlStateManager.rotatef(shift, 0, 1, 0);
                }
                if (info.contains("rotz"))
                {
                    final float shift = info.getFloat("rotz");
                    GlStateManager.rotatef(shift, 0, 0, 1);
                }

            }

            GlStateManager.translatef(-0.25f, 0, 0);
            Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            final IBakedModel model = Minecraft.getInstance().getItemRenderer().getModelWithOverrides(stack);
            Minecraft.getInstance().getItemRenderer().renderItem(stack, model);
            GlStateManager.popMatrix();
        }

    }

}
