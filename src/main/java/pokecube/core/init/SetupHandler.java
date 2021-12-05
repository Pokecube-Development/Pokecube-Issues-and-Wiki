package pokecube.core.init;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.zmoves.ZPower;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import pokecube.nbtedit.NBTEdit;
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

        event.enqueueWork(() ->
        {
            WorldgenHandler.setupAll();
        });
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        // Reload this here to initialze anything that needs to be done here.
        event.enqueueWork(() ->
        {
            PokecubeCore.getConfig().onUpdated();
            Database.onLoadComplete();
        });
    }

}
