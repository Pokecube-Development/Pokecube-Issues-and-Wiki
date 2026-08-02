package pokecube.adventures.init;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.Tasks;
import pokecube.adventures.capabilities.CapabilityHasTrades;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.adventures.network.PacketAFA;
import pokecube.adventures.network.PacketBag;
import pokecube.adventures.network.PacketCommander;
import pokecube.adventures.network.PacketTrainer;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.init.CompatEvent;
import pokecube.compat.Compat;
import thut.api.attachments.Ownable;
import thut.core.common.ThutCore;

@EventBusSubscriber(modid = PokecubeAdv.MODID)
public class SetupHandler
{
    public static void registerListeners()
    {
        // These two interact ones handle right click custom effects on npcs
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::processInteract);
        // This one handles npcs being invulnerable to pokemobs, as well as some
        // damage target allocation
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onEntityInvulnerabilityCheckEvent);
        // This one handles starting battles when hurt
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onLivingHurt);
        // Increases reputation for nearby NPCs if the player defeats wild
        // pokemobs
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onLivingDeath);
        // Prevents npcs flagged as not mating from mating
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onNpcBreedCheck);
        // Hotkey to open belt inventory
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onWearableUse);
        // Hotkey to open belt inventory
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::dropBelt);

        // Does similar to onJoinWorld, but can take a different SpawnReason
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onNpcSpawn);
        // ticks the IHasPokemobs, and also ensures that the mob goes back to
        // idle mode if it was in battle, and battle is over.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onNpcTick);
        // This initializes the mob's brain for use.
        ThutCore.FORGE_BUS.addListener(EventPriority.LOW, TrainerEventHandler::onBrainInit);
        // Loads the trades for the trainers.
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGH, TrainerEventHandler::onPostServerStart);
        // Manages npcs joining battles, such as preventing always friendly ones
        // from doing so.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onBattleJoin);
        // Remove combat target if we have been removed from a battle
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, TrainerEventHandler::onBattleExit);
        // Remove combat target if target is npc, and npcs are not allowed to be agressed
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onAgroTest);
        // Prevent trainer's pokemobs going to the PC
        PokecubeAPI.POKEMOB_BUS.addListener(TrainerEventHandler::onSentToPC);
        // Prevents normal processing for recalling pokemobs, this re-adds it to
        // the trainer's inventory.
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.NORMAL, false, TrainerEventHandler::onRecalledPokemob);
        // Ensures the trainer is linked to its pokemob when it is sent out.
        PokecubeAPI.POKEMOB_BUS.addListener(TrainerEventHandler::onPostSendOut);
        // Used to make un-battleable trainers invisible if configured to do so.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onWatchTrainer);
        // Prevent capturing trainers in snag cubes
        PokecubeAPI.POKEMOB_BUS.addListener(TrainerEventHandler::captureAttempt);
        
        // Init the mobs
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGH, TrainerEventHandler::entityLivingConstruct);

        ThutCore.FORGE_BUS.register(TrainerSpawnHandler.class);
        ThutCore.FORGE_BUS.register(EnergyHandler.class);
        ThutCore.FORGE_BUS.register(TrainerTracker.class);
        ThutCore.FORGE_BUS.register(CapabilityHasTrades.class);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {

        // Register packets
        PokecubeAdv.packets.registerBiDirectionalMessage(PacketBag.class);
        PokecubeAdv.packets.registerBiDirectionalMessage(PacketTrainer.class);
        PokecubeAdv.packets.registerBiDirectionalMessage(PacketCommander.class);
        PokecubeAdv.packets.registerToServerMessage(PacketAFA.class);

        event.enqueueWork(() -> {
            Ownable.TILES.add(PokecubeAdv.AFA_TYPE.get());
            Ownable.TILES.add(PokecubeAdv.WARP_PAD_TYPE.get());
            Ownable.TILES.add(PokecubeAdv.STATUE_TYPE.get());
        });

        PacketTrainer.register();
        Tasks.init();
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        Compat.BUS.post(new CompatEvent());
        PokecubeAdv.config.loaded = true;
        // Reload this here to initialze anything that needs to be done here.
        PokecubeAdv.config.onUpdated();
    }
}
