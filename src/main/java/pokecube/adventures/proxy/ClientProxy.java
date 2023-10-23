package pokecube.adventures.proxy;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.PokecubeAdv;
import thut.api.ModelHolder;
import thut.bling.client.render.Back;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;

public class ClientProxy extends CommonProxy
{
    @OnlyIn(value = Dist.CLIENT)
    protected static class RenderWearable extends Wearable
    {
        IModel bag;

        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            if (bag == null)
                bag = ModelFactory.createScaled(new ModelHolder(new ResourceLocation(PokecubeAdv.MODID, "models/worn/bag")));
            Back.renderBack(mat, buff, wearer, stack, bag, brightness, overlay);
        }
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public Wearable getWearable()
    {
        return new RenderWearable();
    }
}
