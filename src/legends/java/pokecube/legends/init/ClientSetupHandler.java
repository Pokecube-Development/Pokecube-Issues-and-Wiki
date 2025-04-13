package pokecube.legends.init;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import pokecube.core.client.particle.FallingLeafParticle;
import pokecube.legends.Reference;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;
import pokecube.legends.client.render.model.LegendsModelLayers;
import pokecube.legends.client.render.model.armor.ImprisonmentArmorModel;
import pokecube.legends.fluids.DistorticWaterType;
import pokecube.legends.fluids.MoltenMeteoriteType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            Stream.of(FluidInit.DISTORTIC_WATER, FluidInit.DISTORTIC_WATER_FLOWING).map(Supplier::get)
                    .forEach(fluid -> ItemBlockRenderTypes.setRenderLayer(fluid, RenderType.translucent()));
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
    public static void registerExtensions(RegisterClientExtensionsEvent event)
    {
        event.registerFluidType(new IClientFluidTypeExtensions()
        {
            @Override
            public ResourceLocation getStillTexture()
            {
                return DistorticWaterType.DISTORTIC_WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return DistorticWaterType.DISTORTIC_WATER_FLOWING;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture()
            {
                return DistorticWaterType.DISTORTIC_WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc)
            {
                return DistorticWaterType.UNDERWATER_LOCATION;
            }

            @Override
            public int getTintColor()
            {
                return 0x99d7efcc;
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                    int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor)
            {
                int color = this.getTintColor();
                return new Vector3f((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F);
            }
        }, FluidInit.DISTORTIC_WATER_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions()
                                {
                                    @Override
                                    public ResourceLocation getStillTexture()
                                    {
                                        return MoltenMeteoriteType.MOLTEN_METEORITE_STILL;
                                    }

                                    @Override
                                    public ResourceLocation getFlowingTexture()
                                    {
                                        return MoltenMeteoriteType.MOLTEN_METEORITE_FLOWING;
                                    }

                                    @Nullable
                                    @Override
                                    public ResourceLocation getOverlayTexture()
                                    {
                                        return MoltenMeteoriteType.MOLTEN_METEORITE_OVERLAY;
                                    }
                                }, FluidInit.MOLTEN_METEORITE_TYPE.get());

        event.registerItem(new IClientItemExtensions()
        {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> defaultModel)
            {
                EntityModelSet models = Minecraft.getInstance().getEntityModels();

                HumanoidModel<? extends LivingEntity> armorModel = new HumanoidModel<>(
                        new ModelPart(Collections.emptyList(), Map.of("head", new ImprisonmentArmorModel<>(
                                        models.bakeLayer(LegendsModelLayers.IMPRISONMENT_ARMOR_INNER)).head, "hat",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg",
                                new ModelPart(Collections.emptyList(), Collections.emptyMap()))));

                armorModel.crouching = entity.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = entity.isBaby();

                return armorModel;
            }
        }, ItemInit.IMPRISIONMENT_HELMET.get());
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
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z,
                        spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_RED.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z,
                        spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_PINK.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z,
                        spriteSet));
        event.registerSpriteSet(ParticleInit.DYNA_LEAF_PASTEL_PINK.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z,
                        spriteSet));
        event.registerSpriteSet(ParticleInit.MIRAGE_LEAF.get(),
                (spriteSet) -> (particleType, world, x, y, z, j, k, l) -> new FallingLeafParticle(world, x, y, z,
                        spriteSet));
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(LegendsModelLayers.IMPRISONMENT_ARMOR_INNER,
                ImprisonmentArmorModel::createBodyLayer);
    }
}
