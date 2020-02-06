package thut.wearables.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
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
    float[]                             offsetArr = { 0, 0, 0 };

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
    public void render(final MatrixStack mat, final IRenderTypeBuffer buff, final int packedLightIn, final T wearer,
            final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks,
            final float netHeadYaw, final float headPitch)
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

        // TODO find out where this comes from?
        final int overlay = LivingRenderer.getPackedOverlay(wearer, 0);

        if (wearer instanceof AbstractClientPlayerEntity)
            thin = ((AbstractClientPlayerEntity) wearer).getSkinType().equals("slim");
        else if (theModel instanceof PlayerModel<?>) thin = ((PlayerModel<?>) theModel).smallArms;

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
                Leg.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case BACK:
                Body.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case EAR:
                Head.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case EYE:
                Head.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case FINGER:
                Arm.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case HAT:
                Head.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case NECK:
                Body.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case WAIST:
                Body.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            case WRIST:
                Arm.render(mat, buff, wearable, slot, index, wearer, stack, partialTicks, thin, packedLightIn, overlay,
                        theModel);
                break;
            default:
                break;

            }
        }
    }
}
