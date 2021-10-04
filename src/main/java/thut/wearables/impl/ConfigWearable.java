package thut.wearables.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
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

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        // TODO way to register renderers for config wearables

        // This is for items that should just be directly rendered
        if (stack.hasTag() && stack.getTag().contains("wslot"))
        {

            mat.pushPose();

            mat.mulPose(new Quaternion(0, 0, 180, true));

            if (stack.getTag().contains("winfo"))
            {
                final CompoundTag info = stack.getTag().getCompound("winfo");
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
                    mat.mulPose(new Quaternion(shift, 0, 0, true));
                }
                if (info.contains("roty"))
                {
                    final float shift = info.getFloat("roty");
                    mat.mulPose(new Quaternion(0, shift, 0, true));
                }
                if (info.contains("rotz"))
                {
                    final float shift = info.getFloat("rotz");
                    mat.mulPose(new Quaternion(0, 0, shift, true));
                }

            }

            mat.translate(-0.25f, 0, 0);
            Minecraft.getInstance().textureManager.bindForSetup(InventoryMenu.BLOCK_ATLAS);
            final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            final BakedModel ibakedmodel = itemRenderer.getModel(stack, wearer.getCommandSenderWorld(),
                    null);
            // TODO check lighting/etc in this call!
            itemRenderer.render(stack, net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.FIXED,
                    true, mat, buff, 0, 0,
                    ibakedmodel);
            mat.popPose();
        }

    }

}
