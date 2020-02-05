package thut.wearables.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        if (this.slot == null && stack.hasTag() && stack.getTag().contains("wslot"))
            this.slot = EnumWearable.valueOf(stack.getTag().getString("wslot"));
        return this.slot;
    }

    @SuppressWarnings("deprecation")
    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        // TODO way to register renderers for config wearables

        // This is for items that should just be directly rendered
        if (stack.hasTag() && stack.getTag().contains("wslot"))
        {

            mat.push();

            mat.rotate(new Quaternion(0, 0, 180, true));

            if (stack.getTag().contains("winfo"))
            {
                final CompoundNBT info = stack.getTag().getCompound("winfo");
                if (info.contains("scale"))
                {
                    final float scale = info.getFloat("scale");
                    mat.scale(scale, scale, scale);
                }
                if (info.contains("shiftx"))
                {
                    final float shift = info.getFloat("shiftx");
                    mat.translate(shift, 0, 0);
                }
                if (info.contains("shifty"))
                {
                    final float shift = info.getFloat("shifty");
                    mat.translate(0, shift, 0);
                }
                if (info.contains("shiftz"))
                {
                    final float shift = info.getFloat("shiftz");
                    mat.translate(0, 0, shift);
                }
                if (info.contains("rotx"))
                {
                    final float shift = info.getFloat("rotx");
                    mat.rotate(new Quaternion(shift, 0, 0, true));
                }
                if (info.contains("roty"))
                {
                    final float shift = info.getFloat("roty");
                    mat.rotate(new Quaternion(0, shift, 0, true));
                }
                if (info.contains("rotz"))
                {
                    final float shift = info.getFloat("rotz");
                    mat.rotate(new Quaternion(0, 0, shift, true));
                }

            }

            mat.translate(-0.25f, 0, 0);
            Minecraft.getInstance().textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
            final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            final IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, wearer.getEntityWorld(),
                    null);
            // TODO check lighting/etc in this call!
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, mat, buff, 0, 0,
                    ibakedmodel);
            mat.pop();
        }

    }

}
