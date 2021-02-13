package pokecube.compat.minecraft;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.Config;
import pokecube.adventures.events.CompatEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.bees.BeeTasks.BeeHabitat;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable.HabitatProvider;
import pokecube.core.utils.PokeType;
import thut.api.OwnableCaps;
import thut.api.item.ItemList;
import thut.core.common.world.mobs.data.DataSync_Impl;

@Mod.EventBusSubscriber
public class Compat
{
    private static final PokedexEntry DERP;

    public static Map<EntityType<?>, PokedexEntry> customEntries = Maps.newHashMap();

    private static final ResourceLocation NOTPOKEMOBS = new ResourceLocation(PokecubeCore.MODID, "never_pokemob");
    private static final ResourceLocation BEEHIVES    = new ResourceLocation(PokecubeCore.MODID, "bee_hive_cap");

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

    public static Predicate<EntityType<?>> makePokemob = e ->
    {
        // Already a pokemob.
        if (e instanceof PokemobType) return false;
        final boolean vanilla = e.getRegistryName().getNamespace().equals("minecraft");
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
        MinecraftForge.EVENT_BUS.addGenericListener(TileEntity.class, Compat::onTileEntityCaps);
    }

    private static void onTileEntityCaps(final AttachCapabilitiesEvent<TileEntity> event)
    {
        // Only apply to BeehiveTileEntity
        // For now, we do an equality check, instead of instanceof check.
        // TODO replace with instanceof when resourcefull bees updates
        if (!(event.getObject().getClass() == BeehiveTileEntity.class)) return;

        final BeeHabitat habitat = new BeeHabitat((BeehiveTileEntity) event.getObject());
        final HabitatProvider provider = new HabitatProvider(event.getObject(), habitat);
        event.addCapability(Compat.BEEHIVES, provider);
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        // Only consider mobEntity, IPokemob requires that
        if (!(event.getObject() instanceof MobEntity)) return;
        // Do not apply this to trainers!
        if (Config.instance.shouldBeCustomTrainer((LivingEntity) event.getObject())) return;
        // This checks blacklists, configs, etc on the pokemob type
        if (!Compat.makePokemob.test(event.getObject().getType())) return;
        // If someone already added it, lets skip
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
            final PokedexEntry entry = PokecubeCore.getEntryFor(event.getObject().getType());
            if (entry == null) try
            {
                @SuppressWarnings("unchecked")
                final EntityType<? extends MobEntity> mobType = (EntityType<? extends MobEntity>) event.getObject()
                        .getType();
                final String name = mobType.toString();
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
                PokecubeCore.LOGGER.warn("Error making pokedex entry for {}", event.getObject().getType()
                        .getRegistryName());
                e.printStackTrace();
                return;
            }

            final VanillaPokemob pokemob = new VanillaPokemob((MobEntity) event.getObject());
            final GeneticsProvider genes = new GeneticsProvider();
            final DataSync_Impl data = new DataSync_Impl();
            pokemob.setDataSync(data);
            pokemob.genes = genes.wrapped;
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(EventsHandler.POKEMOBCAP, pokemob);
            event.addCapability(EventsHandler.DATACAP, data);
            IGuardAICapability.addCapability(event);
            final ICapabilitySerializable<?> own = OwnableCaps.makeMobOwnable(event.getObject(), true);
            event.addCapability(OwnableCaps.LOCBASE, own);
        }
    }

}
