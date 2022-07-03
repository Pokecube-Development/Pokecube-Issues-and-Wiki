package pokecube.adventures.proxy;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.adventures.PokecubeAdv;
import thut.bling.client.render.Back;
import thut.core.client.render.model.IModel;
import thut.core.client.render.x3d.X3dModel;
import thut.wearables.EnumWearable;

@OnlyIn(value = Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID, value = Dist.CLIENT)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistryEvent event)
        {
            PokecubeAdv.proxy = new ClientProxy();
        }
    }

    protected static class RenderWearable extends Wearable
    { // One model for each layer.
        IModel bag;

        // One Texture for each layer.
        private final ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_1.png");
        private final ResourceLocation BAG_2 = new ResourceLocation(PokecubeAdv.MODID, "textures/worn/bag_2.png");

        private final ResourceLocation[] BAG_TEXS = { this.BAG_1, this.BAG_2 };

        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            if (this.bag == null) this.bag = new X3dModel(new ResourceLocation(PokecubeAdv.MODID,
                    "models/worn/bag.x3d"));
            Back.renderBack(mat, buff, wearer, stack, this.bag, this.BAG_TEXS, brightness, overlay);
        }
    }

    @Override
    public Wearable getWearable()
    {
        return new RenderWearable();
    }
}
