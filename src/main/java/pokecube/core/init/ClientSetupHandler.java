package pokecube.core.init;

import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.lwjgl.glfw.GLFW;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.blocks.Healer;
import pokecube.core.client.gui.blocks.PC;
import pokecube.core.client.gui.blocks.TMs;
import pokecube.core.client.gui.blocks.Trade;
import pokecube.core.client.gui.pokemob.*;
import pokecube.core.client.render.RenderMoves;
import pokecube.core.client.render.mobs.RenderEgg;
import pokecube.core.client.render.mobs.RenderNPC;
import pokecube.core.client.render.mobs.RenderPokecube;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.ShoulderLayer.IShoulderHolder;
import pokecube.core.client.render.mobs.ShoulderLayer.ShoulderHolder;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.nbtedit.NBTEdit;
import thut.core.client.gui.ConfigGui;
import thut.core.client.render.animation.CapabilityAnimation;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    public static KeyBinding nextMob;
    public static KeyBinding nextMove;
    public static KeyBinding previousMob;
    public static KeyBinding previousMove;
    public static KeyBinding mobBack;
    public static KeyBinding mobAttack;
    public static KeyBinding mobStance;
    public static KeyBinding mobMegavolve;
    public static KeyBinding noEvolve;
    public static KeyBinding mobMove1;
    public static KeyBinding mobMove2;
    public static KeyBinding mobMove3;
    public static KeyBinding mobMove4;
    public static KeyBinding gzmove;
    public static KeyBinding mobUp;
    public static KeyBinding mobDown;
    public static KeyBinding throttleUp;
    public static KeyBinding throttleDown;
    public static KeyBinding arrangeGui;
    public static KeyBinding animateGui;

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        RenderPokemob.register();
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        PokecubeCore.LOGGER.debug("Pokecube Client Setup");

        // Register the pokemob class for animations.
        CapabilityAnimation.registerAnimateClass(GenericPokemob.class);

        // Register event handlers
        EventsHandlerClient.register();

        // Register keybinds
        PokecubeCore.LOGGER.debug("Init Keybinds");
        ClientRegistry.registerKeyBinding(ClientSetupHandler.nextMob = new KeyBinding("key.pokemob.next",
                GLFW.GLFW_KEY_RIGHT, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.previousMob = new KeyBinding("key.pokemob.prev",
                GLFW.GLFW_KEY_LEFT, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.nextMove = new KeyBinding("key.pokemob.move.next",
                GLFW.GLFW_KEY_DOWN, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.previousMove = new KeyBinding("key.pokemob.move.prev",
                GLFW.GLFW_KEY_UP, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobBack = new KeyBinding("key.pokemob.recall",
                GLFW.GLFW_KEY_R, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobAttack = new KeyBinding("key.pokemob.attack",
                GLFW.GLFW_KEY_G, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobStance = new KeyBinding("key.pokemob.stance",
                GLFW.GLFW_KEY_BACKSLASH, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMegavolve = new KeyBinding("key.pokemob.megaevolve",
                GLFW.GLFW_KEY_M, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.noEvolve = new KeyBinding("key.pokemob.b", GLFW.GLFW_KEY_B,
                "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove1 = new KeyBinding("key.pokemob.move.1",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove2 = new KeyBinding("key.pokemob.move.2",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove3 = new KeyBinding("key.pokemob.move.3",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove4 = new KeyBinding("key.pokemob.move.4",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobUp = new KeyBinding("key.pokemob.up",
                GLFW.GLFW_KEY_SPACE, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobDown = new KeyBinding("key.pokemob.down",
                GLFW.GLFW_KEY_LEFT_CONTROL, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.throttleUp = new KeyBinding("key.pokemob.speed.up",
                GLFW.GLFW_KEY_LEFT_BRACKET, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.throttleDown = new KeyBinding("key.pokemob.speed.down",
                GLFW.GLFW_KEY_RIGHT_BRACKET, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.arrangeGui = new KeyBinding("key.pokemob.arrangegui",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.animateGui = new KeyBinding("key.pokemob.animategui",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.gzmove = new KeyBinding("key.pokemob.gzmove",
                InputMappings.UNKNOWN.getValue(), "Pokecube"));

        // Forward this to PCEdit mod:
        NBTEdit.setupClient(event);

        // Register the gui side of the screens.
        PokecubeCore.LOGGER.debug("Init Screen Factories");

        final ScreenManager.IScreenFactory<ContainerPokemob, GuiPokemobBase> factory = (c, i, t) ->
        {
            switch (c.mode)
            {
            case PacketPokemobGui.AI:
                return new GuiPokemobAI(c, i);
            case PacketPokemobGui.STORAGE:
                return new GuiPokemobStorage(c, i);
            case PacketPokemobGui.ROUTES:
                return new GuiPokemobRoutes(c, i);
            }
            return new GuiPokemob(c, i);
        };

        ScreenManager.register(ContainerPokemob.TYPE, factory);
        ScreenManager.register(HealerContainer.TYPE, Healer<HealerContainer>::new);
        ScreenManager.register(PCContainer.TYPE, PC<PCContainer>::new);
        ScreenManager.register(TradeContainer.TYPE, Trade<TradeContainer>::new);
        ScreenManager.register(TMContainer.TYPE, TMs<TMContainer>::new);

        // Register mob rendering
        PokecubeCore.LOGGER.debug("Init Mob Renderers");

        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (!e.stock) continue;
            final EntityType<? extends MobEntity> t = e.getEntityType();
            RenderingRegistry.registerEntityRenderingHandler(t, (manager) -> new RenderPokemob(e, manager));
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityPokecube.TYPE, RenderPokecube::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMoveUse.TYPE, RenderMoves::new);
        RenderingRegistry.registerEntityRenderingHandler(NpcMob.TYPE, RenderNPC::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPokemobEgg.TYPE, RenderEgg::new);

        // Register shouldercap
        CapabilityManager.INSTANCE.register(IShoulderHolder.class, IShoulderHolder.STORAGE, ShoulderHolder::new);

        // Register the render layers
        for (final Block crop : BerryManager.berryCrops.values())
            RenderTypeLookup.setRenderLayer(crop, RenderType.cutoutMipped());
        for (final Block fruit : BerryManager.berryFruits.values())
            RenderTypeLookup.setRenderLayer(fruit, RenderType.cutoutMipped());
        for (final Block leaf : ItemGenerator.leaves.values())
            RenderTypeLookup.setRenderLayer(leaf, RenderType.cutoutMipped());
        for (final Block trapdoor : ItemGenerator.trapdoors.values())
            RenderTypeLookup.setRenderLayer(trapdoor, RenderType.cutoutMipped());
        for (final Block door : ItemGenerator.doors.values())
            RenderTypeLookup.setRenderLayer(door, RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(PokecubeItems.NESTBLOCK.get(), RenderType.getCutoutMipped());

        // Register config gui
        ModList.get().getModContainerById(PokecubeCore.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeCore.getConfig(),
                        parent)));

    }
}
