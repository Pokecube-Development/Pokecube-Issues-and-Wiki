package pokecube.compat.minecraft;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import pokecube.core.database.PokedexEntry;
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

    @SubscribeEvent
    public static void register(final CompatEvent event)
    {
        // Here will will register the vanilla mobs as a type of pokemob.
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, Compat::onEntityCaps);
        for (final EntityType<?> type : ForgeRegistries.ENTITIES.getValues())
        {
            final boolean vanilla = type.getRegistryName().getNamespace().equals("minecraft");
            if (type == EntityType.VILLAGER) continue;
            if (vanilla) try
            {
                final PokedexEntry newDerp = new PokedexEntry(Compat.DERP.getPokedexNb(), "vanilla_mob_" + type
                        .getRegistryName().getPath());
                newDerp.setBaseForme(Compat.DERP);
                Compat.DERP.copyToForm(newDerp);
                newDerp.stock = false;
                @SuppressWarnings("unchecked")
                final EntityType<? extends MobEntity> mobType = (EntityType<? extends MobEntity>) type;
                PokecubeCore.typeMap.put(newDerp, mobType);
            }
            catch (final Exception e)
            {
                // Wasn't a mob entity, so lets skip.
            }
        }
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!PokecubeCore.getConfig().vanilla_pokemobs) return;
        if (!(event.getObject() instanceof MobEntity)) return;
        final EntityType<?> type = event.getObject().getType();
        if (!type.getRegistryName().getNamespace().equals("minecraft")) return;
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
