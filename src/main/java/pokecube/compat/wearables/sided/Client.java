package pokecube.compat.wearables.sided;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.compat.wearables.sided.Common.WearablesRenderer;
import pokecube.core.PokecubeCore;
import pokecube.core.impl.PokecubeMod;
import thut.bling.client.render.Finger;
import thut.bling.client.render.Hat;
import thut.bling.client.render.Waist;
import thut.bling.client.render.Wrist;
import thut.core.client.render.model.parts.Material;
import thut.wearables.EnumWearable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class Client
{
    static
    {
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOW, Client::initWearables);
    }

    public static Map<String, WearablesRenderer> renderers = Maps.newHashMap();

    @OnlyIn(Dist.CLIENT)
    public static Predicate<Material> IS_KEYSTONE = m -> (m.name.contains("keystone")
            || m.tex != null && m.tex.getPath().contains("keystone")
            || m.tex != null && m.tex.getPath().contains("_overlay"));
    @OnlyIn(Dist.CLIENT)
    public static Predicate<Material> IS_OVERLAY = m -> (m.name.contains("_overlay")
            || m.tex != null && m.tex.getPath().contains("_overlay"));

    public static void initWearables(final RegisterMiscItems event)
    {
        Client.renderers.put("pokewatch", new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/pokewatch"))
        {
            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                    final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.WRIST) return;
                super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                Wrist.renderWrist(mat, buff, wearer, stack, this.model, brightness, overlay, IS_OVERLAY);
            }
        });
        Client.renderers.put("ring", new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_ring"))
        {
            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.FINGER) return;
                super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                Finger.renderFinger(mat, buff, wearer, stack, this.model, brightness, overlay, IS_KEYSTONE);
            }
        });
        Client.renderers.put("belt", new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_belt"))
        {
            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.WAIST) return;
                super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                Waist.renderWaist(mat, buff, wearer, stack, this.model, brightness, overlay, IS_KEYSTONE);
            }
        });
        Client.renderers.put("hat", new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_hat"))
        {
            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final PoseStack mat, final MultiBufferSource buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.HAT) return;
                super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                Hat.renderHat(mat, buff, wearer, stack, this.model, brightness, overlay, IS_OVERLAY);
            }
        });
    }
}
