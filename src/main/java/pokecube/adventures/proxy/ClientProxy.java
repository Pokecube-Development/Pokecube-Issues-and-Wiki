package pokecube.adventures.proxy;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import pokecube.adventures.PokecubeAdv;
import thut.bling.client.render.Util;
import thut.core.client.render.json.JsonModel;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
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
    { // We render layers based on material!
        IModel bag;

        // This is just a dummy texture for getting a fake initial renderer
        private final ResourceLocation BAG_1 = new ResourceLocation(PokecubeAdv.MODID, "textures/hologram.png");

        @Override
        public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            if (this.bag == null)
                this.bag = new JsonModel(new ResourceLocation(PokecubeAdv.MODID, "models/worn/bag.json"));

            if (!(bag instanceof IModelCustom renderable)) return;
            if (!bag.isLoaded() || !bag.isValid()) return;

            float s;
            mat.pushPose();
            s = 1.0f;
            mat.scale(s, -s, -s);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));
            mat.translate(-0.5, -.6, -0.16);

            DyeColor ret;
            Color colour;
            ret = DyeColor.RED;
            if (stack.hasTag() && stack.getTag().contains("dyeColour"))
            {
                final int damage = stack.getTag().getInt("dyeColour");
                ret = DyeColor.byId(damage);
            }
            colour = new Color(ret.getTextColor() + 0xFF000000);
            for (final IExtendedModelPart part1 : bag.getParts().values())
            {
                // Overlay texture is the fixed one, the rest can be recoloured.
                if (!part1.getMaterials().get(0).name.contains("overlay"))
                    part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
                else part1.setRGBABrO(255, 255, 255, 255, brightness, overlay);
            }
            final VertexConsumer buf1 = Util.makeBuilder(buff, BAG_1);
            renderable.renderAll(mat, buf1);
            mat.popPose();
        }
    }

    @Override
    public Wearable getWearable()
    {
        return new RenderWearable();
    }
}
