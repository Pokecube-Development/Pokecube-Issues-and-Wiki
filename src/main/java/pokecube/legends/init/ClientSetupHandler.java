package pokecube.legends.init;

import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.Reference;
import pokecube.core.client.particle.FallingLeafParticle;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;
import pokecube.legends.client.render.model.LegendsModelLayers;
import pokecube.legends.client.render.model.armor.ImprisonmentArmorModel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Stream.of(FluidInit.DISTORTIC_WATER, FluidInit.DISTORTIC_WATER_FLOWING).map(RegistryObject::get)
                    .forEach(fluid -> ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent()));
            // Shields
            ItemInit.addItemModelProperties();
        });
    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event) {
        // Renderer for blocks
        event.registerBlockEntityRenderer(BlockInit.RAID_SPAWN_ENTITY.get(), Raid::new);
        event.registerBlockEntityRenderer(TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireRenderer::new);

        // Register entity renderer for the wormhole
        event.registerEntityRenderer(EntityInit.WORMHOLE.get(), Wormhole::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ParticleInit.GOLD_STAR.get(), SuspendedTownParticle.HappyVillagerProvider::new);
        event.registerSpriteSet(ParticleInit.ERROR.get(), SuspendedTownParticle.HappyVillagerProvider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_FIRE_FLAME.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SMOKE.get(), SmokeParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SOUL.get(), SoulParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.INFECTED_SPARK.get(), LavaParticle.Provider::new);
        event.registerSpriteSet(ParticleInit.MUSHROOM.get(), SuspendedTownParticle.Provider::new);

        event.registerSpriteSet(ParticleInit.AGED_LEAF.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z, spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_RED.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z, spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_PINK.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z, spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_PASTEL_PINK.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z, spriteSet));
        event.registerSpriteSet(ParticleInit.MIRAGE_LEAF.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z, spriteSet));
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(LegendsModelLayers.IMPRISONMENT_ARMOR_INNER, ImprisonmentArmorModel::createBodyLayer);
    }
}
