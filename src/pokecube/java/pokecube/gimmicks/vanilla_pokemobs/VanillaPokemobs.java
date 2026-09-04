package pokecube.gimmicks.vanilla_pokemobs;

import com.google.common.collect.Sets;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityEvent.EntityConstructing;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import pokecube.adventures.Config;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.Kill.KillCommandEvent;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.gimmicks.vanilla_pokemobs.network.PacketHandshake;
import thut.api.attachments.Ownable;
import thut.api.data.HolderProvider;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This gimmick allows making vanilla and otherwise not-pokemob mobs be pokemobs.
 * <br>
 * This provides an example of using the IPokemob system for arbitrary modded entity support.
 */
@Mod(value = PokecubeCore.MODID)
public class VanillaPokemobs
{
    private static final PokedexEntry DERP;

    static
    {
        DERP = new PokedexEntry(-1, "vanilla_mob", false);
        VanillaPokemobs.DERP.type1 = PokeType.unknown;
        VanillaPokemobs.DERP.type2 = PokeType.unknown;
        VanillaPokemobs.DERP.base = true;
        VanillaPokemobs.DERP.stock = false;
    }

    public static VanillaPokemobsConfig config = VanillaPokemobsConfig.loadConfig();
    
    private static final Set<PokedexEntry> generated = Sets.newHashSet();

    public static Predicate<EntityType<?>> makePokemob = e -> {
        // Already a pokemob.
        if (e instanceof PokemobType) return false;
        final boolean vanilla = RegHelper.getKey(e).getNamespace().equals("minecraft");
        if (!vanilla && !config.non_vanilla_pokemobs) return false;
        if (vanilla && !config.vanilla_pokemobs) return false;
        var s = RegHelper.getKey(e).toString();
        // This works before world load, as is just the strings
        if (config.not_pokemobs.contains(s)) return false;
        // Tags will only work after world load, so ones that need tags will still
        // get attributes added to them
        return config._tags_not_pokemob.stream().noneMatch(e::is);
    };

    @SuppressWarnings("unchecked")
    public static void onEntityAttributes(final EntityAttributeModificationEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Registering Pokecube Attributes to vanilla mobs");
        BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
            if (type.getBaseClass().isAssignableFrom(Mob.class) && makePokemob.test(type))
            {
                EntityType<? extends LivingEntity> etype = (EntityType<? extends LivingEntity>) type;
                var attribs = LivingEntity.createLivingAttributes();
                // Someone already added it for this mob
                if (attribs.hasAttribute(PokecubeAttributes.ATTACK)) return;
                if (!attribs.hasAttribute(Attributes.FOLLOW_RANGE)) event.add(etype, Attributes.FOLLOW_RANGE, 16);
                if (!attribs.hasAttribute(Attributes.FLYING_SPEED)) event.add(etype, Attributes.FLYING_SPEED, 0.6);
                for (var a : PokecubeAttributes.ATTRIBUTES) event.add(etype, a);
            }
        });
    }

    public static void onServerStarting(ServerStartingEvent event)
    {
        if (config._registered) return;
        config._registered = true;

        Ownable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation KEY = ResourceLocation.parse("pokecube:ownable_mobs");

            @Override
            protected ResourceLocation key()
            {
                return KEY;
            }

            /**
             * @param input the entity to apply to
             * @return null if not applicable, otherwise a new Ownable
             */
            @Override
            public Ownable.IOwnableSerializable apply(IAttachmentHolder input)
            {
                if (!(input instanceof Mob mob)) return null;
                // Only consider mobEntity, IPokemob requires that
                // Do not apply this to trainers!
                if (Config.instance.shouldBeCustomTrainer(mob)) return null;
                // This checks blacklists, configs, etc on the pokemob type
                if (!VanillaPokemobs.makePokemob.test(mob.getType())) return null;
                return new Ownable.ImplE(mob);
            }

            /**
             * This is set to 1000 so that it applies later in the queue than other sources.
             */
            @Override
            public int getPriority()
            {
                return 1000;
            }
        });
        // Register default pokemobs
        ResourceLocation KEY = ResourceLocation.parse("pokecube:custom_pokemob");
        PokemobCaps._REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            protected ResourceLocation key()
            {
                return KEY;
            }

            /**
             * @param input the entity to apply to
             * @return null if not applicable, otherwise a new VanillaPokemob
             */
            @Override
            public IPokemob apply(IAttachmentHolder input)
            {
                if (!(input instanceof Mob mob)) return null;

                final PokedexEntry entry = PokecubeCore.getEntryFor(mob.getType());
                if (entry == null) try
                {
                    @SuppressWarnings("unchecked")
                    final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
                    final String name = RegHelper.getKey(mobType).toString().replace(":", "_");
                    PokedexEntry newDerp = Database.getEntry(name);
                    if (newDerp == null)
                    {
                        newDerp = new PokedexEntry(VanillaPokemobs.DERP.getPokedexNb(), name, true);
                        newDerp.setBaseForme(VanillaPokemobs.DERP);
                        VanillaPokemobs.DERP.copyToForm(newDerp);
                        newDerp.stock = false;
                        newDerp._root_json.size.width = mob.getBbWidth();
                        newDerp._root_json.size.length = mob.getBbWidth();
                        newDerp._root_json.size.height = mob.getBbHeight();
                        generated.add(newDerp);
                    }
                    newDerp.setEntityType(mobType);
                    PokecubeCore.typeMap.put(mobType, newDerp);
                }
                catch (final Exception e)
                {
                    // Something went wrong, so log and exit early
                    PokecubeAPI.LOGGER.warn("Error making pokedex entry for {}", RegHelper.getKey(mob.getType()), e);
                    return null;
                }

                return new VanillaPokemob(mob);
            }

            /**
             * This is set to 1000 so that it applies later in the queue than other sources.
             */
            @Override
            public int getPriority()
            {
                return 1000;
            }
        });
    }

    private static void onMobConstructingEvent(final EntityConstructing event)
    {
        if (duringCheck) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        // Return here incase some other addon has already added it in
        if (mob.hasData(PokemobCaps.POKEMOB)) return;
        // Don't try to make pokemobs vanilla pokemobs
        if (mob instanceof EntityPokemob) return;
        // Only consider mobEntity, IPokemob requires that
        // Do not apply this to trainers!
        if (Config.instance.shouldBeCustomTrainer(mob)) return;
        // This checks blacklists, configs, etc on the pokemob type
        if (!VanillaPokemobs.makePokemob.test(mob.getType())) return;

        // Add our attachements, the getData call initialises them.
        mob.getData(Ownable.TYPE);
        mob.getData(PokemobCaps.POKEMOB);
    }

    private static boolean duringCheck = true;
    private static void onServerStarted(final ServerStartedEvent event)
    {
        ServerLevel testLevel = event.getServer().getLevel(Level.OVERWORLD);
        List<JsonPokedexEntry> entries = new ArrayList<>();
        duringCheck = true;
        BuiltInRegistries.ENTITY_TYPE.forEach(t -> {
            Entity e;
            try
            {
                e = t.create(testLevel);
            }
            catch (Exception ex)
            {
                return;
            }
            if (e instanceof Mob && makePokemob.test(t))
            {
                @SuppressWarnings("unchecked")
                final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) t;
                final String name = RegHelper.getKey(mobType).toString().replace(":", "_");
                PokedexEntry newDerp = Database.getEntry(name);
                if (newDerp == null)
                {
                    newDerp = new PokedexEntry(DERP.getPokedexNb(), name, true);
                    DERP.copyToForm(newDerp);
                    newDerp.stock = false;
                }
                if (!newDerp.stock && generated.add(newDerp))
                {
                    var entry = JsonPokedexEntry.fromPokedexEntry(newDerp);
                    entry.size.height = e.getBbHeight();
                    entry.size.width = e.getBbWidth();
                    entry.size.length = e.getBbWidth();
                    entries.add(entry);
                }
            }
        });
        duringCheck = false;
        if (!entries.isEmpty())
        {
            File root = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks")
                    .resolve("__vanilla_template__").toFile();
            if(root.mkdirs()) PokecubeAPI.logInfo("Made datapack template root");
            File data = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks")
                    .resolve("__vanilla_template__").resolve("data").resolve("my_addon").resolve("database")
                    .resolve("pokemobs").resolve("pokedex_entries").toFile();
            if(data.mkdirs()) PokecubeAPI.logInfo("Made datapack template entries directory");

            String metacontents = "{\r\n" + "  \"pack\": {\r\n" + "    \"pack_format\": 8,\r\n".replace("8",
                    "" + SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA))
                    + "    \"description\": \"Sample Adding Mobs for Pokecube \\n (MC 1.16.4+)\"\r\n" + "  }\r\n" + "}";
            File mcmeta = new File(root, "pack.mcmeta");

            try
            {
                FileOutputStream writer = new FileOutputStream(mcmeta);
                writer.write(metacontents.getBytes());
                writer.close();
                entries.forEach(entry -> {
                    File pokemobs = new File(data, entry.name + ".json");
                    String json = JsonUtil.gson.toJson(entry);
                    try
                    {
                        var writer2 = new FileOutputStream(pokemobs);
                        writer2.write(json.getBytes());
                        writer2.close();
                    }
                    catch (Exception e)
                    {
                        PokecubeAPI.LOGGER.error(e);
                    }
                });
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error(e);
            }

        }
    }

    private static void onKillCommand(final KillCommandEvent event)
    {
        if (VanillaPokemobs.makePokemob.test(event.getEntity().getType())) event.setCanceled(true);
    }

    private static void onTagsReloaded(TagsUpdatedEvent event)
    {
        if (!event.shouldUpdateStaticData()) return;
        Set<String> entries = new HashSet<>(config.not_pokemobs);
        Set<String> tags = new HashSet<>(config.not_pokemobs);
        entries.removeIf(s->s.startsWith("#"));
        tags.removeIf(s->!s.startsWith("#"));
        var reg = event.getRegistryAccess().registry(Registries.ENTITY_TYPE).get();
        for (var key : config._tags_not_pokemob)
        {
            var things = reg.asLookup().get(key);
            if (things.isEmpty()) continue;
            var tagged = things.get();
            for (var type : tagged.stream().toList())
            {
                entries.add(RegHelper.getKey(type.value()).toString());
            }
        }
        config.not_pokemobs.clear();
        config.not_pokemobs.addAll(tags);
        config.not_pokemobs.addAll(entries);
        VanillaPokemobsConfig.saveConfig(config);
    }

    public static void onFMLCommonSetup(FMLCommonSetupEvent ignored)
    {
        PokecubeCore.packets.registerToClientMessage(PacketHandshake.class);
    }

    public static void onHandshake(OnDatapackSyncEvent event)
    {
        if (event.getPlayer() != null) PacketHandshake.sendPacket(event.getPlayer());
    }

    public VanillaPokemobs(IEventBus bus)
    {
        // we register the packet and handshake regardless
        bus.addListener(VanillaPokemobs::onFMLCommonSetup);
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onHandshake);

        // We have loaded the config when class was loaded, let now register if we are able to.
        if (!config.non_vanilla_pokemobs && !config.vanilla_pokemobs) return;
        // Register our events
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onServerStarting);
        ThutCore.FORGE_BUS.addListener(EventPriority.LOW, VanillaPokemobs::onMobConstructingEvent);
        // Here we disable the pokecube kill command for vanilla mobs for #753
        PokecubeAPI.POKEMOB_BUS.addListener(VanillaPokemobs::onKillCommand);
        // Here will will register the handler for making the default datapack
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onServerStarted);
        // And this handles applying the IPokemob to them.
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onMobConstructingEvent);
        // Add listener for when tags reload to update the config appropriately.
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onTagsReloaded);

        bus.addListener(VanillaPokemobs::onEntityAttributes);
    }
}
