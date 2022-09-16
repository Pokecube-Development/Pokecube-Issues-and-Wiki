package pokecube.legends.init;

import java.util.function.Predicate;

import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.Reference;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    static final Predicate<Material> notSolid = m -> m == Material.ICE || m == Material.ICE_SOLID
            || m == Material.HEAVY_METAL || m == Material.LEAVES || m == Material.REPLACEABLE_PLANT;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            // Shields
            ItemInit.addItemModelProperties();
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
        event.register(ParticleInit.INFECTED_FIRE_FLAME.get(), FlameParticle.Provider::new);
        event.register(ParticleInit.INFECTED_SMOKE.get(), SmokeParticle.Provider::new);
        event.register(ParticleInit.INFECTED_SOUL.get(), SoulParticle.Provider::new);
        event.register(ParticleInit.INFECTED_SPARK.get(), LavaParticle.Provider::new);
        event.register(ParticleInit.MUSHROOM.get(), SuspendedTownParticle.Provider::new);
    }
}
