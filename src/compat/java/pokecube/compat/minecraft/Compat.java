package pokecube.compat.minecraft;

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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import pokecube.adventures.Config;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.CapabilityInhabitable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.init.CompatEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.Kill.KillCommandEvent;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
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

@EventBusSubscriber
public class Compat
{
    private static final PokedexEntry DERP;

    public static List<PokedexEntry> customEntries = Lists.newArrayList();

    private static final ResourceLocation NOTPOKEMOBS = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "never_pokemob");
    private static final ResourceLocation BEEHIVES = ResourceLocation.fromNamespaceAndPath(PokecubeCore.MODID,
            "bee_hive_cap");

    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
        DERP = new PokedexEntry(-1, "vanilla_mob", false);
        Compat.DERP.type1 = PokeType.unknown;
        Compat.DERP.type2 = PokeType.unknown;
        Compat.DERP.base = true;
        Compat.DERP.evs = new byte[6];
        Compat.DERP.stats = new int[6];
        Compat.DERP.height = 1;
        Compat.DERP.sexeRatio = 128;
        Compat.DERP.catchRate = 255;
        Compat.DERP.baseXP = 100;
        Compat.DERP.width = Compat.DERP.length = 0.41f;
        Compat.DERP.mass = 10;
        Compat.DERP.stats[0] = 50;
        Compat.DERP.stats[1] = 50;
        Compat.DERP.stats[2] = 50;
        Compat.DERP.stats[3] = 50;
        Compat.DERP.stats[4] = 50;
        Compat.DERP.stats[5] = 50;
        Compat.DERP.addMoves(Lists.newArrayList(), Maps.newHashMap());
        Compat.DERP.addMove("skyattack");
        Compat.DERP.mobType = 1;
        Compat.DERP.evolutionMode = 2;
        Compat.DERP.stock = false;
    }

    private static final Set<PokedexEntry> generated = Sets.newHashSet();

    public static Predicate<EntityType<?>> makePokemob = e -> {
        // Already a pokemob.
        if (e instanceof PokemobType) return false;
        final boolean vanilla = RegHelper.getKey(e).getNamespace().equals("minecraft");
        if (!vanilla && !PokecubeCore.getConfig().non_vanilla_pokemobs) return false;
        if (vanilla && !PokecubeCore.getConfig().vanilla_pokemobs) return false;
        if (ItemList.is(Compat.NOTPOKEMOBS, e)) return false;
        return true;
    };

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        // Here we disable the pokecube kill command for vanilla mobs for #753
        PokecubeAPI.POKEMOB_BUS.addListener(Compat::onKillCommand);
        // Here will will register the handler for making the default datapack
        ThutCore.FORGE_BUS.addListener(Compat::onServerStarted);
        // And this handles applying the IPokemob to them.
        ThutCore.FORGE_BUS.addListener(Compat::LivingConstruct);

        CapabilityInhabitable.Register(ResourceLocation.parse("pokecube:vanilla_bees"), BeeTasks.BeeHabitat::new);
        Ownable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation KEY = ResourceLocation.parse("pokecube:ownable_blocks");

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
                if (!Compat.makePokemob.test(mob.getType())) return null;
                return new Ownable.Impl();
            }

            @Override
            public int getPriority()
            {
                return 1000;
            }
        });

        if (!PokecubeCore.getConfig().non_vanilla_pokemobs && !PokecubeCore.getConfig().vanilla_pokemobs) return;
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
                        newDerp = new PokedexEntry(Compat.DERP.getPokedexNb(), name, true);
                        newDerp.setBaseForme(Compat.DERP);
                        Compat.DERP.copyToForm(newDerp);
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
                    PokecubeAPI.LOGGER.warn("Error making pokedex entry for {}", RegHelper.getKey(mob.getType()));
                    e.printStackTrace();
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

    private static void LivingConstruct(final EntityEvent.EntityConstructing event)
    {
        if (!PokecubeCore.getConfig().non_vanilla_pokemobs && !PokecubeCore.getConfig().vanilla_pokemobs) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob instanceof EntityPokemob) return;
        // Only consider mobEntity, IPokemob requires that
        // Do not apply this to trainers!
        if (Config.instance.shouldBeCustomTrainer(mob)) return;
        // This checks blacklists, configs, etc on the pokemob type
        if (!Compat.makePokemob.test(mob.getType())) return;
        mob.getData(PokemobCaps.POKEMOB);
    }

    private static void onServerStarted(final ServerStartedEvent event)
    {
        if (!PokecubeCore.getConfig().non_vanilla_pokemobs && !PokecubeCore.getConfig().vanilla_pokemobs) return;
        ServerLevel testLevel = event.getServer().getLevel(Level.OVERWORLD);
        List<JsonPokedexEntry> entries = new ArrayList<>();
        BuiltInRegistries.ENTITY_TYPE.forEach(t -> {
            Entity e = t.create(testLevel);
            if (e instanceof Mob && makePokemob.test(t))
            {
                @SuppressWarnings("unchecked")
                final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) t;
                final String name = RegHelper.getKey(mobType).toString().replace(":", "_");
                PokedexEntry newDerp = Database.getEntry(name);
                if (newDerp != null && !newDerp.stock && generated.contains(newDerp))
                {
                    entries.add(JsonPokedexEntry.fromPokedexEntry(newDerp));
                }
            }
        });
        if (!entries.isEmpty())
        {
            File root = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks")
                    .resolve("__vanilla_template__").toFile();
            root.mkdirs();
            File data = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks")
                    .resolve("__vanilla_template__").resolve("data").resolve("my_addon").resolve("database")
                    .resolve("pokemobs").resolve("pokedex_entries").toFile();
            data.mkdirs();

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
                        e.printStackTrace();
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    private static void onKillCommand(final KillCommandEvent event)
    {
        if (Compat.makePokemob.test(event.getEntity().getType())) event.setCanceled(true);
    }
}
