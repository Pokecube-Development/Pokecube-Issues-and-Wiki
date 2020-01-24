package thut.bling.client;

import net.minecraft.util.ResourceLocation;
import thut.bling.ThutBling;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;

public class BlingRender extends BlingRenderBase
{
    public static final BlingRender INSTANCE = new BlingRender();

    @Override
    protected void initModels()
    {
        if (!this.defaultModels.isEmpty()) return;
        for (final EnumWearable slot : EnumWearable.values())
        {
            IModel model = this.defaultModels.get(slot);
            ResourceLocation[] tex = this.defaultTextures.get(slot);
            if (model == null)
            {
                ModelHolder holder = null;
                if (slot == EnumWearable.WAIST || slot == EnumWearable.WRIST || slot == EnumWearable.ANKLE
                        || slot == EnumWearable.FINGER || slot == EnumWearable.EAR || slot == EnumWearable.NECK)
                {
                    tex = new ResourceLocation[2];
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"), tex[1],
                            null, "belt");
                }
                if (slot == EnumWearable.HAT)
                {
                    tex = new ResourceLocation[2];
                    tex[0] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat2.png");
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/hat.x3d"), tex[0], null,
                            "belt");
                }
                if (slot == EnumWearable.BACK)
                {
                    tex = new ResourceLocation[2];
                    tex[0] = new ResourceLocation(ThutBling.MODID, "textures/worn/bag1.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/bag2.png");
                    holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/bag.x3d"), tex[0], null,
                            "belt");
                }
                if (holder != null) model = ModelFactory.create(holder);
                if (model != null && tex != null)
                {
                    this.defaultModels.put(slot, model);
                    this.defaultTextures.put(slot, tex);
                }
            }
        }
    }
}
