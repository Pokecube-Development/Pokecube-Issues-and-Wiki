package thut.wearables.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
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
    EnumWearable slot;

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
    public void renderWearable(final GuiGraphics graphics, final MultiBufferSource buff, final EnumWearable slot,
                               final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                               final int brightness, final int overlay)
    {
        // TODO way to register renderers for config wearables

        // This is for items that should just be directly rendered
        if (stack.hasTag() && stack.getTag().contains("wslot"))
        {

            graphics.pose().pushPose();

            // TODO: Fix this
            // graphics.pose().mulPose(new Quaternionf(0, 0, 180, true));

            if (stack.getTag().contains("winfo"))
            {
                final CompoundTag info = stack.getTag().getCompound("winfo");
                if (info.contains("scale"))
                {
                    final float scale = info.getFloat("scale");
                    graphics.pose().scale(scale, scale, scale);
                }
                if (info.contains("shiftx"))
                {
                    final float shift = info.getFloat("shiftx");
                    graphics.pose().translate(shift, 0, 0);
                }
                if (info.contains("shifty"))
                {
                    final float shift = info.getFloat("shifty");
                    graphics.pose().translate(0, shift, 0);
                }
                if (info.contains("shiftz"))
                {
                    final float shift = info.getFloat("shiftz");
                    graphics.pose().translate(0, 0, shift);
                }
//                TODO: Fix this
//                if (info.contains("rotx"))
//                {
//                    final float shift = info.getFloat("rotx");
//                    graphics.pose().mulPose(new Quaternionf(shift, 0, 0, true));
//                }
//                if (info.contains("roty"))
//                {
//                    final float shift = info.getFloat("roty");
//                    graphics.pose().mulPose(new Quaternionf(0, shift, 0, true));
//                }
//                if (info.contains("rotz"))
//                {
//                    final float shift = info.getFloat("rotz");
//                    graphics.pose().mulPose(new Quaternionf(0, 0, shift, true));
//                }

            }

            graphics.pose().translate(-0.25f, 0, 0);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

            final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            final BakedModel ibakedmodel = itemRenderer.getModel(stack, wearer.level(), null, 0);
            itemRenderer.render(stack, ItemDisplayContext.FIXED,
                    true, graphics.pose(), buff, 0, 0, ibakedmodel);
            graphics.pose().popPose();
        }

    }

}
