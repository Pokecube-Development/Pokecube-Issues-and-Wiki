package thut.bling.client;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import thut.core.client.render.model.IModel;
import thut.wearables.EnumWearable;

public abstract class BlingRenderBase
{

    Map<EnumWearable, IModel>             defaultModels   = Maps.newHashMap();
    Map<EnumWearable, ResourceLocation[]> defaultTextures = Maps.newHashMap();

    protected IModel getModel(final EnumWearable slot, final ItemStack stack)
    {
        IModel imodel = Util.getCustomModel(slot, stack);
        if (imodel != null) return imodel;
        imodel = this.defaultModels.get(slot);
        return imodel;
    }

    protected ResourceLocation[] getTextures(final EnumWearable slot, final ItemStack stack)
    {
        ResourceLocation[] textures = Util.getCustomTextures(slot, stack);
        if (textures != null) return textures;
        textures = this.defaultTextures.get(slot);
        return textures;
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
        final ResourceLocation[] textures = this.getTextures(slot, stack);
        if (stack.hasTag() && stack.getTag().contains("gemTag") && !stack.getTag().contains("gem"))
        {
            final ItemStack gem = ItemStack.of(stack.getTag().getCompound("gemTag"));
            final ResourceLocation id = gem.getItem().getRegistryName();
            // TODO better way to do this.
            final String tex = id.getNamespace() + ":textures/item/" + id.getPath() + ".png";
            stack.getTag().putString("gem", tex);
        }
        // TODO remove hardcoded allowance for eyes to be textures instead of
        // models.
        if (model == null && slot != EnumWearable.EYE) return;
        switch (slot)
        {
        case ANKLE:
            Ankle.renderAnkle(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case BACK:
            Back.renderBack(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case EAR:
            Ear.renderEar(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case EYE:
            Eye.renderEye(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case FINGER:
            Finger.renderFinger(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case HAT:
            Hat.renderHat(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case NECK:
            Neck.renderNeck(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case WAIST:
            Waist.renderWaist(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        case WRIST:
            Wrist.renderWrist(mat, buff, wearer, stack, model, textures, brightness, overlay);
            break;
        default:
            break;
        }
    }
}
