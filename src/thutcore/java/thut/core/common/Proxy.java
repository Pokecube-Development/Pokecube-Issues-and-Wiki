package thut.core.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.RegistryEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public interface Proxy
{
    default MinecraftServer getServer()
    {
        return ServerLifecycleHooks.getCurrentServer();
    }

    default boolean isClientSide()
    {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    default boolean isServerSide()
    {
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    default RegistryAccess getRegistries()
    {
        try
        {
            var server = this.getServer();
            if (server == null)
            {
                //@formatter:off These are copied from NeoForgeRegistriesSetup
                Set<Registry<?>> VANILLA_SYNC_REGISTRIES = Set.of(
                        BuiltInRegistries.SOUND_EVENT, // Required for SoundEvent packets
                        BuiltInRegistries.MOB_EFFECT, // Required for MobEffect packets
                        BuiltInRegistries.BLOCK, // Required for chunk BlockState paletted containers syncing
                        BuiltInRegistries.ENTITY_TYPE, // Required for Entity spawn packets
                        BuiltInRegistries.ITEM, // Required for Item/ItemStack packets
                        BuiltInRegistries.FLUID, // Required for Fluid/FluidStack packets
                        BuiltInRegistries.PARTICLE_TYPE, // Required for ParticleType packets
                        BuiltInRegistries.BLOCK_ENTITY_TYPE, // Required for BlockEntity packets
                        BuiltInRegistries.MENU, // Required for ClientboundOpenScreenPacket
                        BuiltInRegistries.COMMAND_ARGUMENT_TYPE, // Required for ClientboundCommandsPacket
                        BuiltInRegistries.STAT_TYPE, // Required for ClientboundAwardStatsPacket
                        BuiltInRegistries.VILLAGER_TYPE, // Required for EntityDataSerializers
                        BuiltInRegistries.VILLAGER_PROFESSION, // Required for EntityDataSerializers
                        BuiltInRegistries.CAT_VARIANT, // Required for EntityDataSerializers
                        BuiltInRegistries.FROG_VARIANT, // Required for EntityDataSerializers
                        BuiltInRegistries.DATA_COMPONENT_TYPE, // Required for itemstack sync
                        BuiltInRegistries.RECIPE_SERIALIZER, // Required for Recipe sync
                        BuiltInRegistries.ATTRIBUTE, // Required for ClientboundUpdateAttributesPacket

                        // Required due to appearing in usages of ByteBufCodecs#registry
                        BuiltInRegistries.POTION, // PotionContents#STREAM_CODEC
                        BuiltInRegistries.NUMBER_FORMAT_TYPE, // NumberFormatTypes#STREAM_CODEC
                        BuiltInRegistries.CUSTOM_STAT, // StatType creates a registry StreamCodec using the provided stat registry
                        BuiltInRegistries.POSITION_SOURCE_TYPE, // PositionSource#STREAM_CODEC
                        BuiltInRegistries.ARMOR_MATERIAL, // TrimMaterial#DIRECT_STREAM_CODEC
                        BuiltInRegistries.MAP_DECORATION_TYPE // MapDecorationType#STREAM_CODEC
                );
                //@formatter:on
                Map<ResourceKey<? extends Registry<?>>, Registry<?>> regs = new HashMap<>();
                List<RegistryEntry<?>> REGEs = new ArrayList<>();
                VANILLA_SYNC_REGISTRIES.forEach(r -> {
                    REGEs.add(new RegistryEntry(r.key(), r));
                    regs.put(r.key(), r);
                });

                return new RegistryAccess()
                {
                    @Override
                    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> registryKey)
                    {
                        Registry<E> reg = (Registry<E>) regs.get(registryKey);
                        return Optional.of(reg);
                    }

                    @Override
                    public Stream<RegistryEntry<?>> registries()
                    {
                        return REGEs.stream();
                    }
                };
            }
            return this.getServer().registryAccess();
        }
        catch (final Exception e)
        {
            // During pre-loading or similar, so exit.
            return null;
        }
    }

    default void loaded(final FMLLoadCompleteEvent event)
    {}

    default void setup(final FMLCommonSetupEvent event)
    {

    }

    default void setupClient(final FMLClientSetupEvent event)
    {}
}
