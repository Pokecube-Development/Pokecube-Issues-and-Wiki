package thut.wearables.client.render;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.client.render.slots.Arm;
import thut.wearables.client.render.slots.Body;
import thut.wearables.client.render.slots.Head;
import thut.wearables.client.render.slots.Leg;
import thut.wearables.inventory.PlayerWearables;

public class WearablesRenderer<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M>
{
    float[] offsetArr = { 0, 0, 0 };

    private final IEntityRenderer<?, ?> livingEntityRenderer;

    public WearablesRenderer(final IEntityRenderer<T, M> livingEntityRendererIn)
    {
        super(livingEntityRendererIn);
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    private IWearable getWearable(final ItemStack stack)
    {
        if (stack.getItem() instanceof IWearable) return (IWearable) stack.getItem();
        return stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
    }

    @Override
    public void render(final LivingEntity wearer, final float f, final float f1, final float partialTicks,
            final float f3, final float f4, final float f5, final float scale)
    {
        // No Render invisible.
        if (wearer.getActivePotionEffect(Effects.INVISIBILITY) != null) return;
        // Only applies to bipeds, anyone else needs to write their own render
        // layer.
        if (!(this.livingEntityRenderer.getEntityModel() instanceof BipedModel<?>)) return;
        final BipedModel<?> theModel = (BipedModel<?>) this.livingEntityRenderer.getEntityModel();

        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;

        boolean thin = false;

        if (wearer instanceof AbstractClientPlayerEntity) thin = ((AbstractClientPlayerEntity) wearer).getSkinType()
                .equals("slim");
        else
        {
            final ModelBox box = theModel.bipedLeftArm.cubeList.get(0);
            thin = box.posX2 - box.posX1 != box.posZ2 - box.posZ1;
        }

        GlStateManager.pushMatrix();
        for (int i = 0; i < worn.getSlots(); i++)
        {
            final EnumWearable slot = EnumWearable.getWearable(i);
            final int index = EnumWearable.getSubIndex(i);
            final ItemStack stack = worn.getWearable(slot, index);
            if (stack.isEmpty()) continue;
            final IWearable wearable = this.getWearable(stack);
            if (wearable == null) continue;
            switch (slot)
            {
            case ANKLE:
                Leg.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case BACK:
                Body.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case EAR:
                Head.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case EYE:
                Head.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case FINGER:
                Arm.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case HAT:
                Head.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case NECK:
                Body.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case WAIST:
                Body.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            case WRIST:
                Arm.render(wearable, slot, index, wearer, stack, partialTicks, thin, scale, theModel);
                break;
            default:
                break;

            }
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
