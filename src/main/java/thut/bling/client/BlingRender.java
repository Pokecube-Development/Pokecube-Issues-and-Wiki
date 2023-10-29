package thut.bling.client;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import pokecube.compat.wearables.sided.Common;
import thut.api.ModelHolder;
import thut.bling.ThutBling;
import thut.bling.client.render.Util;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;

public class BlingRender extends BlingRenderBase
{
    public static final BlingRender INSTANCE = new BlingRender();

    public static Map<String, Common.WearablesRenderer> renderers = Maps.newHashMap();

    @Override
    protected void initModels()
    {
        boolean reload = Util.shouldReloadModel();
        if (reload)
        {
            Util.customModels.clear();
            Util.customTextures.clear();
        }
        if (!reload && !this.defaultModels.isEmpty()) return;
        if (!reload && !this.backpackModels.isEmpty()) return;

        for (final EnumWearable slot : EnumWearable.values())
        {
            IModel model = this.defaultModels.get(slot);
            if (model == null || reload)
            {
                ModelHolder holder = null;

                switch (slot)
                {
                case ANKLE:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/anklet"));
                    break;
                case BACK:
                    this.backpackModels.put(new ResourceLocation(ThutBling.MODID, "bling_bag"), ModelFactory
                            .createScaled(new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/bag"))));
                    this.backpackModels.put(new ResourceLocation(ThutBling.MODID, "bling_bag_ender_vanilla"),
                            ModelFactory.createScaled(
                                    new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/ender_bag"))));
                    this.backpackModels.put(new ResourceLocation(ThutBling.MODID, "bling_bag_ender_large"),
                            ModelFactory.createScaled(new ModelHolder(
                                    new ResourceLocation(ThutBling.MODID, "models/worn/ender_bag_large"))));
                    continue;
                case EAR:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/earring"));
                    break;
                case EYE:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/sun_glasses"));
                    break;
                case FINGER:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/ring"));
                    break;
                case HAT:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/hat"));
                    break;
                case NECK:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/necklace"));
                    break;
                case WAIST:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/belt"));
                    break;
                case WRIST:
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/bracelet"));
                    break;
                default:
                    break;
                }
                if (holder != null) model = ModelFactory.createScaled(holder);
                if (model != null)
                {
                    this.defaultModels.put(slot, model);
                }
            }
        }
    }
}
