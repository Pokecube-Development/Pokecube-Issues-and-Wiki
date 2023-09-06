package pokecube.legends.init;

import java.util.stream.Stream;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            Stream.of(FluidInit.DISTORTIC_WATER, FluidInit.DISTORTIC_WATER_FLOWING).map(RegistryObject::get)
                    .forEach(fluid -> ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent()));
            // Shields
            ItemInit.addItemModelProperties();
            LegendsWoodType.register();
        });
    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event)
    {
        // Renderer for blocks
        event.registerBlockEntityRenderer(BlockInit.RAID_SPAWN_ENTITY.get(), Raid::new);
        event.registerBlockEntityRenderer(TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireRenderer::new);

        // Register entity renderer for the wormhole
        event.registerEntityRenderer(EntityInit.WORMHOLE.get(), Wormhole::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ParticleInit.INFECTED_FIRE_FLAME.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SMOKE.get(), SmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SOUL.get(), SoulParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SPARK.get(), LavaParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.MUSHROOM.get(), SuspendedTownParticle.Provider::new);
    }
}
