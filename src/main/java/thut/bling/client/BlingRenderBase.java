package thut.bling.client;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

    public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
            final ItemStack stack, final float partialTicks)
    {
        this.initModels();
        final IModel model = this.getModel(slot, stack);
        final ResourceLocation[] textures = this.getTextures(slot, stack);
        // TODO remove hardcoded allowance for eyes to be textures instead of
        // models.
        if (model == null && slot != EnumWearable.EYE) return;
        final int brightness = wearer.getBrightnessForRender();
        switch (slot)
        {
        case ANKLE:
            Ankle.renderAnkle(wearer, stack, model, textures, brightness);
            break;
        case BACK:
            Back.renderBack(wearer, stack, model, textures, brightness);
            break;
        case EAR:
            Ear.renderEar(wearer, stack, model, textures, brightness);
            break;
        case EYE:
            Eye.renderEye(wearer, stack, model, textures, brightness);
            break;
        case FINGER:
            Finger.renderFinger(wearer, stack, model, textures, brightness);
            break;
        case HAT:
            Hat.renderHat(wearer, stack, model, textures, brightness);
            break;
        case NECK:
            Neck.renderNeck(wearer, stack, model, textures, brightness);
            break;
        case WAIST:
            Waist.renderWaist(wearer, stack, model, textures, brightness);
            break;
        case WRIST:
            Wrist.renderWrist(wearer, stack, model, textures, brightness);
            break;
        default:
            break;
        }
    }
}
