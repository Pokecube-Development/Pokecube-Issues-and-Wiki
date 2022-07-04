package pokecube.core.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.zmoves.ZPower;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import pokecube.nbtedit.NBTEdit;
import pokecube.world.gen_old.WorldgenHandler;
import thut.api.terrain.TerrainSegment;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        // Initialize the capabilities.
        event.register(IGuardAICapability.class);
        event.register(IPokemob.class);
        event.register(IOngoingAffected.class);
        event.register(ZPower.class);
        event.register(IMegaCapability.class);
        event.register(IPokemobUseable.class);
        event.register(IInhabitable.class);
        event.register(IInhabitor.class);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        PokecubeCore.LOGGER.info("Hello from Common Proxy setup!");

        // Registers the event listeners.
        EventsHandler.register();

        // Register terrain effects
        TerrainSegment.terrainEffectClasses.add(PokemobTerrainEffects.class);

        // Registers the packets.
        PokecubePacketHandler.init();

        PokecubeTerrainChecker.init();

        // Forward this to PCEdit mod:
        NBTEdit.setup(event);

        event.enqueueWork(() -> {
            WorldgenHandler.setupAll();
        });
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        // Reload this here to initialze anything that needs to be done here.
        event.enqueueWork(() -> {
            PokecubeCore.getConfig().onUpdated();
            Database.onLoadComplete();
        });
    }

    @SubscribeEvent
    public static void onEntityAttributes(final EntityAttributeCreationEvent event)
    {
        // register a new mob here
        PokecubeCore.LOGGER.debug("Registering Pokecube Attributes");

        final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 10.0D);
        event.put(EntityPokecube.TYPE, attribs.build());
        event.put(EntityPokemobEgg.TYPE, attribs.build());
        event.put(NpcMob.TYPE, attribs.build());

        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry.dummy) continue;
            if (!entry.stock) continue;
            try
            {
                event.put(entry.getEntityType(), attribs.build());
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
