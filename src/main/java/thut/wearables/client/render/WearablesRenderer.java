package thut.wearables.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
import thut.wearables.client.render.slots.Arm;
import thut.wearables.client.render.slots.Body;
import thut.wearables.client.render.slots.Head;
import thut.wearables.client.render.slots.Leg;
import thut.wearables.inventory.PlayerWearables;

public class WearablesRenderer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
    float[]                             offsetArr = { 0, 0, 0 };

    private final RenderLayerParent<?, ?> livingEntityRenderer;

    public WearablesRenderer(final RenderLayerParent<T, M> livingEntityRendererIn)
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
    public void render(final PoseStack mat, final MultiBufferSource buff, final int packedLightIn, final T wearer,
            final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks,
            final float netHeadYaw, final float headPitch)
    {
        // No Render invisible.
        if (wearer.getEffect(MobEffects.INVISIBILITY) != null) return;
        // Only applies to bipeds, anyone else needs to write their own render
        // layer.
        if (!(this.livingEntityRenderer.getModel() instanceof HumanoidModel<?>)) return;
        final HumanoidModel<?> theModel = (HumanoidModel<?>) this.livingEntityRenderer.getModel();

        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;
        boolean thin = false;

        final int overlay = LivingEntityRenderer.getOverlayCoords(wearer, 0);

        if (wearer instanceof AbstractClientPlayer)
            thin = ((AbstractClientPlayer) wearer).getModelName().equals("slim");
        else if (theModel instanceof PlayerModel<?>) thin = ((PlayerModel<?>) theModel).slim;

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
