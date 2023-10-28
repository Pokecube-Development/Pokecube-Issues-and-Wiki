package thut.bling.client;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.api.AnimatedCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.bling.client.render.Ankle;
import thut.bling.client.render.Back;
import thut.bling.client.render.Ear;
import thut.bling.client.render.Eye;
import thut.bling.client.render.Finger;
import thut.bling.client.render.Hat;
import thut.bling.client.render.Neck;
import thut.bling.client.render.Util;
import thut.bling.client.render.Waist;
import thut.bling.client.render.Wrist;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.model.IModel;
import thut.lib.RegHelper;
import thut.wearables.EnumWearable;

public abstract class BlingRenderBase
{

    Map<EnumWearable, IModel> defaultModels = Maps.newHashMap();
    Map<ResourceLocation, IModel> backpackModels = Maps.newHashMap();

    protected IModel getModel(final EnumWearable slot, final ItemStack stack)
    {
        IModel imodel = Util.getCustomModel(stack);
        if (imodel != null) return imodel;

        if (slot != EnumWearable.BACK) imodel = this.defaultModels.get(slot);
        else imodel = this.backpackModels.get(RegHelper.getKey(stack));

        return imodel;
    }

    /**
     * This should setup the models if they are not setup, it will be called at
     * the begining of the render, so should do nothing if there is no setup to
     * do.
     */
    protected abstract void initModels();

    public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
            final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
            final int brightness, final int overlay)
    {
        this.initModels();
        final IModel model = this.getModel(slot, stack);
        if (stack.hasTag() && stack.getTag().contains("gemTag") && !stack.getTag().contains("gem"))
        {
            final ItemStack gem = ItemStack.of(stack.getTag().getCompound("gemTag"));
            final ResourceLocation id = RegHelper.getKey(gem);
            // TODO better way to do this.
            final String tex = id.getNamespace() + ":textures/item/" + id.getPath() + ".png";
            stack.getTag().putString("gem", tex);
        }
        if (model == null) return;
        final IAnimationHolder holder = AnimationHelper.getHolder(wearer);
        holder.setContext(AnimatedCaps.getAnimated(wearer));
        model.setAnimationHolder(holder);
        switch (slot)
        {
        case ANKLE:
            Ankle.renderAnkle(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case BACK:
            Back.renderBack(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case EAR:
            Ear.renderEar(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case EYE:
            Eye.renderEye(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case FINGER:
            Finger.renderFinger(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case HAT:
            Hat.renderHat(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case NECK:
            Neck.renderNeck(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case WAIST:
            Waist.renderWaist(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        case WRIST:
            Wrist.renderWrist(mat, buff, wearer, stack, model, brightness, overlay);
            break;
        default:
            break;
        }
    }
}
