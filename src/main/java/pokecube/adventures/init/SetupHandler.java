package pokecube.adventures.init;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.Tasks;
import pokecube.adventures.blocks.BlockEventHandler;
import pokecube.adventures.capabilities.CapabilityHasTrades;
import pokecube.adventures.capabilities.player.PlayerPokemobs;
import pokecube.adventures.events.TrainerEventHandler;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.adventures.items.Linker;
import pokecube.adventures.items.bag.BagItem;
import pokecube.adventures.network.PacketAFA;
import pokecube.adventures.network.PacketBag;
import pokecube.adventures.network.PacketCommander;
import pokecube.adventures.network.PacketTrainer;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.adventures.utils.InventoryHandler;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.init.CompatEvent;
import pokecube.compat.Compat;
import thut.api.OwnableCaps;
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeAdv.MODID)
public class SetupHandler
{
    public static void registerListeners()
    {
        ThutCore.FORGE_BUS.addGenericListener(Entity.class, EventPriority.LOWEST,
                TrainerEventHandler::onAttachMobCaps);

        // These two interact ones handle right click custom effects on npcs
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onEntityInteract);
        // This one handles npcs being invulnerable to pokemobs, as well as some
        // damage target allocation
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

        // One phase of initializing trainers.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onJoinWorld);
        // Does similar to onJoinWorld, but can take a different SpawnReason
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onNpcSpawn);
        // ticks the IHasPokemobs, and also ensures that the mob goes back to
        // idle mode if it was in battle, and battle is over.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onNpcTick);
        // This initializes the mob's brain for use.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onBrainInit);
        // Loads the trades for the trainers.
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGH, TrainerEventHandler::onPostServerStart);
        // Manages npcs joining battles, such as preventing always friendly ones
        // from doing so.
        ThutCore.FORGE_BUS.addListener(TrainerEventHandler::onBattleJoin);
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

        ThutCore.FORGE_BUS.register(TrainerSpawnHandler.class);
        ThutCore.FORGE_BUS.register(BagItem.class);
        ThutCore.FORGE_BUS.register(Linker.class);
        ThutCore.FORGE_BUS.register(EnergyHandler.class);
        ThutCore.FORGE_BUS.register(InventoryHandler.class);
        ThutCore.FORGE_BUS.register(BlockEventHandler.class);
        ThutCore.FORGE_BUS.register(TrainerTracker.class);
        ThutCore.FORGE_BUS.register(CapabilityHasTrades.class);
        ThutCore.FORGE_BUS.register(PlayerPokemobs.class);
    }

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        TrainerCaps.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {

        // Register packets
        PokecubeAdv.packets.registerMessage(PacketBag.class, PacketBag::new);
        PokecubeAdv.packets.registerMessage(PacketTrainer.class, PacketTrainer::new);
        PokecubeAdv.packets.registerMessage(PacketCommander.class, PacketCommander::new);
        PokecubeAdv.packets.registerMessage(PacketAFA.class, PacketAFA::new);

        event.enqueueWork(() -> {
            OwnableCaps.TILES.add(PokecubeAdv.AFA_TYPE.get());
            OwnableCaps.TILES.add(PokecubeAdv.WARP_PAD_TYPE.get());
            OwnableCaps.TILES.add(PokecubeAdv.STATUE_TYPE.get());
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
