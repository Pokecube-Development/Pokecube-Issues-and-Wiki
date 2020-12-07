package pokecube.adventures.proxy;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import thut.bling.client.render.Back;
import thut.core.client.render.x3d.X3dModel;
import thut.wearables.EnumWearable;

public class ClientProxy extends CommonProxy
{
    protected static class RenderWearable extends Wearable
    { // One model for each layer.
        X3dModel bag;

        // One Texture for each layer.
        private final ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_1.png");
        private final ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_2.png");

        private final ResourceLocation[] BAG_TEXS = { this.BAG_1, this.BAG_2 };

        @Override
        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            if (this.bag == null) this.bag = new X3dModel(new ResourceLocation(PokecubeAdv.MODID,
                    "models/worn/bag.x3d"));
            Back.renderBack(mat, buff, wearer, stack, this.bag, this.BAG_TEXS, brightness, overlay);
        }
    }

    private static Map<TypeTrainer, ResourceLocation> males   = Maps.newHashMap();
    private static Map<TypeTrainer, ResourceLocation> females = Maps.newHashMap();

    @Override
    public ResourceLocation getTrainerSkin(final LivingEntity mob, final TypeTrainer type, final byte gender)
    {
        ResourceLocation texture = null;
        final boolean male = gender == 1;
        if (male) texture = ClientProxy.males.get(type);
        else texture = ClientProxy.females.get(type);
        if (texture == null)
        {
            texture = type.getTexture(mob);
            if (male) ClientProxy.males.put(type, texture);
            else ClientProxy.females.put(type, texture);
        }
        return texture;
    }

    @Override
    public Wearable getWearable()
    {
        return new RenderWearable();
    }
}
