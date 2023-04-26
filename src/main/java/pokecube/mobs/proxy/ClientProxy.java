package pokecube.mobs.proxy;

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
import pokecube.compat.wearables.sided.Client;
import pokecube.compat.wearables.sided.Common.WearablesRenderer;
import pokecube.core.impl.PokecubeMod;
import pokecube.mobs.PokecubeMobs;
import thut.bling.client.render.Ankle;
import thut.bling.client.render.Eye;
import thut.bling.client.render.Finger;
import thut.bling.client.render.Hat;
import thut.bling.client.render.Neck;
import thut.wearables.EnumWearable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeMobs.MODID, value = Dist.CLIENT)
public class ClientProxy
{
    static
    {
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, ClientProxy::initWearables);
    }

    public static void initWearables(final RegisterMiscItems event)
    {
        // Tiara like worn by Lisia, but rotated to be centered on head instead
        // of at angle.
        Client.renderers.put("tiara",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_tiara"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.HAT) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Hat.renderHat(mat, buff, wearer, stack, this.model, brightness, overlay, Client.IS_KEYSTONE);
                    }
                });

        // Mega Anklet like one worn by Zinnia
        Client.renderers.put("ankletzinnia",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_anklet_zinnia"))
                {
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.ANKLE) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Ankle.renderAnkle(mat, buff, wearer, stack, model, brightness, overlay, Client.IS_KEYSTONE);
                    }
                });
        // a Pendant
        Client.renderers.put("pendant",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_pendant"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.NECK) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Neck.renderNeck(mat, buff, wearer, stack, model, brightness, overlay, Client.IS_KEYSTONE);

                    }
                });
        // Earrings
        Client.renderers.put("earring",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_earring"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.EAR) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Finger.renderFinger(mat, buff, wearer, stack, model, brightness, overlay, Client.IS_KEYSTONE);
                    }
                });
        // Glasses
        Client.renderers.put("glasses",
                new WearablesRenderer(new ResourceLocation(PokecubeMod.ID, "models/worn/mega_glasses"))
                {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void renderWearable(final PoseStack mat, final MultiBufferSource buff,
                            final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                            final float partialTicks, final int brightness, final int overlay)
                    {
                        if (slot != EnumWearable.EYE) return;
                        super.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
                        Eye.renderEye(mat, buff, wearer, stack, model, brightness, overlay, Client.IS_KEYSTONE);
                    }
                });
    }
}
