package thut.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thut.api.attachments.LocationTracker;
import thut.api.attachments.Ownable;
import thut.api.entity.event.BreakTestEvent;
import thut.api.world.WorldTickManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ThutAPI
{
    public static final String MODID = "thutcore";
    public static final Logger LOGGER = LogManager.getLogger(ThutAPI.MODID);
    private static final Pattern ALLOWED = Pattern.compile("([^a-z0-9 /_-])");
    private static final Map<String, String> trimmed = new Object2ObjectOpenHashMap<String, String>();

    public static synchronized String trim(final String name)
    {
        if (name == null) return null;
        return trimmed.computeIfAbsent(name, ThutAPI::_trim);
    }

    private static String _trim(String name)
    {
        String trim = name;
        // ROOT locale to prevent issues with turkish letters.
        trim = trim.toLowerCase(Locale.ROOT).trim();
        // Replace all not-resourcelocation chars
        trim = ALLOWED.matcher(trim).replaceAll("");
        // Replace these too.
        trim = trim.replaceAll(" ", "_");
        return trim;
    }

    private static boolean alreadySetup = false;
    private static boolean alreadyworldticks = false;
    private static boolean alreadylocationTrack = false;
    private static boolean alreadyownables = false;
    private static boolean alreadyexplosions = false;

    public static void initAPI(boolean worldticks, boolean locationTrack, boolean ownables, boolean explosions)
    {
        if(!alreadySetup)
        {
            Tracker.init();
            alreadySetup = true;
        }
        if (worldticks && !alreadyworldticks)
        {
            alreadyworldticks = true;
            NeoForge.EVENT_BUS.register(WorldTickManager.class);
        }
        if (locationTrack && !alreadylocationTrack)
        {
            alreadylocationTrack = true;
            NeoForge.EVENT_BUS.register(LocationTracker.class);
        }
        if (ownables && !alreadyownables)
        {
            alreadyownables = true;
            NeoForge.EVENT_BUS.register(Ownable.class);
        }
        if (explosions && !alreadyexplosions)
        {
            alreadyexplosions = true;
            BreakTestEvent.init();
        }
    }

    public static Random newRandom()
    {
        return new Random();
    }

    public static boolean isClientSide()
    {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    public static boolean isServerSide()
    {
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Supplier<RegistryAccess> REGISTRY_SUPPLY = ()->{
        try
        {
            var server = ServerLifecycleHooks.getCurrentServer();
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
                List<RegistryAccess.RegistryEntry<?>> REGEs = new ArrayList<>();
                VANILLA_SYNC_REGISTRIES.forEach(r -> {
                    REGEs.add(new RegistryAccess.RegistryEntry(r.key(), r));
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
            return server.registryAccess();
        }
        catch (final Exception e)
        {
            // During pre-loading or similar, so exit.
            return null;
        }
    };

    public static RegistryAccess getRegistries()
    {
        return REGISTRY_SUPPLY.get();
    }
}
