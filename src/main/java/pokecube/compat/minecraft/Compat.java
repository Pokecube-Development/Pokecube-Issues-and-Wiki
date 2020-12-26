package pokecube.compat.minecraft;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.events.CompatEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.PokeType;
import thut.api.OwnableCaps;
import thut.core.common.world.mobs.data.DataSync_Impl;

@Mod.EventBusSubscriber
public class Compat
{
    private static final PokedexEntry DERP;

    public static Map<EntityType<?>, PokedexEntry> customEntries = Maps.newHashMap();

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
        Compat.DERP.width = Compat.DERP.length = 0.41f;
        Compat.DERP.stats[0] = 50;
        Compat.DERP.stats[1] = 50;
        Compat.DERP.stats[2] = 50;
        Compat.DERP.stats[3] = 50;
        Compat.DERP.stats[4] = 50;
        Compat.DERP.stats[5] = 50;
        Compat.DERP.addMoves(Lists.newArrayList(), Maps.newHashMap());
        Compat.DERP.addMove("skyattack");
        Compat.DERP.mobType = 15;
        Compat.DERP.stock = false;
    }

    public static Predicate<EntityType<?>> isMob = e ->
    {
        if (e instanceof PokemobType) return false;
        Entity mob = null;
        try
        {
            mob = e.create(FakeWorld.INSTANCE);
        }
        catch (final Exception e1)
        {
            PokecubeCore.LOGGER.warn("EntityType {} errors when trying to create from FakeWorld!", e
                    .getTranslationKey());
            e1.printStackTrace();
            return false;
        }
        if (mob instanceof INPC) return false;
        return mob instanceof MobEntity;
    };

    public static Predicate<EntityType<?>> makePokemob = e ->
    {
        // Already a pokemob.
        if (e instanceof PokemobType) return false;
        final boolean vanilla = e.getRegistryName().getNamespace().equals("minecraft");
        if (!vanilla && !PokecubeCore.getConfig().non_vanilla_pokemobs) return false;
        if (vanilla && !PokecubeCore.getConfig().vanilla_pokemobs) return false;
        return Compat.isMob.test(e);
    };

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        // Here will will register the vanilla mobs as a type of pokemob.
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, Compat::onEntityCaps);
        for (final EntityType<?> type : ForgeRegistries.ENTITIES.getValues())
            if (Compat.isMob.test(type)) try
            {
                final String name = type.getRegistryName().toString();
                PokedexEntry newDerp = Database.getEntry(name);
                if (newDerp == null)
                {
                    newDerp = new PokedexEntry(Compat.DERP.getPokedexNb(), name);
                    newDerp.setBaseForme(Compat.DERP);
                    Compat.DERP.copyToForm(newDerp);
                    newDerp.stock = false;
                }
                @SuppressWarnings("unchecked")
                final EntityType<? extends MobEntity> mobType = (EntityType<? extends MobEntity>) type;
                newDerp.setEntityType(mobType);
                PokecubeCore.typeMap.put(mobType, newDerp);
            }
            catch (final Exception e)
            {
                // Wasn't a mob entity, so lets skip.
            }
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject().getEntityWorld() instanceof FakeWorld) return;
        if (!Compat.makePokemob.test(event.getObject().getType())) return;
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
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
