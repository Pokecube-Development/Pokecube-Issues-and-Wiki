package pokecube.gimmicks.vanilla_pokemobs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
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
import thut.api.attachments.Ownable;
import thut.api.data.HolderProvider;
import thut.api.item.ItemList;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class VanillaPokemobs
{
    private static final PokedexEntry DERP;
    private static final ResourceLocation NOTPOKEMOBS = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "never_pokemob");

    static
    {
        DERP = new PokedexEntry(-1, "vanilla_mob", false);
        VanillaPokemobs.DERP.type1 = PokeType.unknown;
        VanillaPokemobs.DERP.type2 = PokeType.unknown;
        VanillaPokemobs.DERP.base = true;
        VanillaPokemobs.DERP.addMoves(Lists.newArrayList(), Maps.newHashMap());
        VanillaPokemobs.DERP.addMove("skyattack");
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
        return !ItemList.is(VanillaPokemobs.NOTPOKEMOBS, e);
    };

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onEntityAttributes(final EntityAttributeModificationEvent event)
    {
        if (!config.non_vanilla_pokemobs && !config.vanilla_pokemobs) return;
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

    @SubscribeEvent
    public static void register(ServerStartingEvent event)
    {
        if (config._registered) return;
        config._registered = true;

        // Here we disable the pokecube kill command for vanilla mobs for #753
        PokecubeAPI.POKEMOB_BUS.addListener(VanillaPokemobs::onKillCommand);
        // Here will will register the handler for making the default datapack
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onServerStarted);
        // And this handles applying the IPokemob to them.
        ThutCore.FORGE_BUS.addListener(VanillaPokemobs::onMobConstructingEvent);

        Ownable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation KEY = ResourceLocation.parse("pokecube:ownable_mobs");

            @Override
            protected ResourceLocation key()
            {
                return KEY;
            }

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

            @Override
            public int getPriority()
            {
                return 1000;
            }
        });
        if (!config.non_vanilla_pokemobs && !config.vanilla_pokemobs) return;
        // Register default pokemobs
        ResourceLocation KEY = ResourceLocation.parse("pokecube:custom_pokemob");
        PokemobCaps._REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            protected ResourceLocation key()
            {
                return KEY;
            }

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
                        newDerp.width = mob.getBbWidth();
                        newDerp.height = mob.getBbHeight();
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

            @Override
            public int getPriority()
            {
                return 1000;
            }
        });
    }

    @SubscribeEvent
    private static void onMobConstructingEvent(final EntityConstructing event)
    {
        if (!config.non_vanilla_pokemobs && !config.vanilla_pokemobs || duringCheck) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
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
    @SubscribeEvent
    private static void onServerStarted(final ServerStartedEvent event)
    {
        if (!config.non_vanilla_pokemobs && !config.vanilla_pokemobs) return;
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
                    entries.add(JsonPokedexEntry.fromPokedexEntry(newDerp));
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

    public VanillaPokemobs(IEventBus ignored)
    {
    }
}
