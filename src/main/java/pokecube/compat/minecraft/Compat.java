package pokecube.compat.minecraft;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.Config;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.CapabilityInhabitable.HabitatProvider;
import pokecube.api.events.init.CompatEvent;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.bees.BeeTasks.BeeHabitat;
import pokecube.core.commands.Kill.KillCommandEvent;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.core.database.pokedex.PokemobsJson;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.eventhandlers.EventsHandler;
import thut.api.OwnableCaps;
import thut.api.item.ItemList;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.lib.RegHelper;

@Mod.EventBusSubscriber
public class Compat
{
    private static final PokedexEntry DERP;

    public static List<PokedexEntry> customEntries = Lists.newArrayList();

    private static final ResourceLocation NOTPOKEMOBS = new ResourceLocation(PokecubeCore.MODID, "never_pokemob");
    private static final ResourceLocation BEEHIVES = new ResourceLocation(PokecubeCore.MODID, "bee_hive_cap");

    static
    {
        pokecube.compat.Compat.BUS.register(Compat.class);
        DERP = new PokedexEntry(-1, "vanilla_mob");
        Compat.DERP.type1 = PokeType.unknown;
        Compat.DERP.type2 = PokeType.unknown;
        Compat.DERP.base = true;
        Compat.DERP.evs = new byte[6];
        Compat.DERP.stats = new int[6];
        Compat.DERP.height = 1;
        Compat.DERP.catchRate = 255;
        Compat.DERP.baseXP = 100;
        Compat.DERP.width = Compat.DERP.length = 0.41f;
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
        // Here will will register the vanilla mobs as a type of pokemob.
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, Compat::onEntityCaps);
        // Here will will register the vanilla bee hives as habitable
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, Compat::onTileEntityCaps);
        // Here we disable the pokecube kill command for vanilla mobs for #753
        PokecubeAPI.POKEMOB_BUS.addListener(Compat::onKillCommand);
        // Here will will register the handler for making the default datapack
        MinecraftForge.EVENT_BUS.addListener(Compat::onServerStarted);
    }

    private static void onServerStarted(final ServerStartedEvent event)
    {
        ServerLevel testLevel = event.getServer().getLevel(Level.OVERWORLD);
        PokemobsJson database = new PokemobsJson();
        ForgeRegistries.ENTITIES.forEach(t -> {
            Entity e = t.create(testLevel);
            if (e instanceof Mob && makePokemob.test(t))
            {
                @SuppressWarnings("unchecked")
                final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) t;
                final String name = RegHelper.getKey(mobType).toString().replace(":", "_");
                PokedexEntry newDerp = Database.getEntry(name);
                if (newDerp != null && !newDerp.stock)
                {
                    database.addEntry(new XMLPokedexEntry(newDerp));
                }
            }
        });
        if (!database.pokemon.isEmpty())
        {
            PokedexEntryLoader.writeCompoundDatabase(database);
        }
    }

    private static void onKillCommand(final KillCommandEvent event)
    {
        if (Compat.makePokemob.test(event.getEntity().getType())) event.setCanceled(true);
    }

    private static void onTileEntityCaps(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        // Only apply to BeehiveTileEntity
        // For now, we do an equality check, instead of instanceof check.
        // TODO replace with instanceof when resourcefull bees updates
        if (!(event.getObject().getClass() == BeehiveBlockEntity.class)) return;

        final BeeHabitat habitat = new BeeHabitat((BeehiveBlockEntity) event.getObject());
        final HabitatProvider provider = new HabitatProvider(event.getObject(), habitat);
        event.addCapability(Compat.BEEHIVES, provider);
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        // Only consider mobEntity, IPokemob requires that
        if (!(event.getObject() instanceof Mob mob)) return;
        // Do not apply this to trainers!
        if (Config.instance.shouldBeCustomTrainer(mob)) return;
        // This checks blacklists, configs, etc on the pokemob type
        if (!Compat.makePokemob.test(mob.getType())) return;
        // If someone already added it, lets skip
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
            final PokedexEntry entry = PokecubeCore.getEntryFor(mob.getType());
            if (entry == null) try
            {
                @SuppressWarnings("unchecked")
                final EntityType<? extends Mob> mobType = (EntityType<? extends Mob>) mob.getType();
                final String name = RegHelper.getKey(mobType).toString().replace(":", "_");
                PokedexEntry newDerp = Database.getEntry(name);
                if (newDerp == null)
                {
                    newDerp = new PokedexEntry(Compat.DERP.getPokedexNb(), name);
                    newDerp.setBaseForme(Compat.DERP);
                    Compat.DERP.copyToForm(newDerp);
                    newDerp.stock = false;
                }
                newDerp.setEntityType(mobType);
                PokecubeCore.typeMap.put(mobType, newDerp);
            }
            catch (final Exception e)
            {
                // Something went wrong, so log and exit early
                PokecubeAPI.LOGGER.warn("Error making pokedex entry for {}", RegHelper.getKey(mob.getType()));
                e.printStackTrace();
                return;
            }

            final VanillaPokemob pokemob = new VanillaPokemob(mob);
            final GeneticsProvider genes = new GeneticsProvider();
            final DataSync_Impl data = new DataSync_Impl();
            pokemob.setDataSync(data);
            pokemob.genes = genes.wrapped;
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(EventsHandler.POKEMOBCAP, pokemob);
            event.addCapability(EventsHandler.DATACAP, data);
            IGuardAICapability.addCapability(event);
            final ICapabilitySerializable<?> own = OwnableCaps.makeMobOwnable(mob, true);
            event.addCapability(OwnableCaps.LOCBASE, own);
        }
    }

}
