package pokecube.core.init;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.blocks.Healer;
import pokecube.core.client.gui.blocks.PC;
import pokecube.core.client.gui.blocks.TMs;
import pokecube.core.client.gui.blocks.Trade;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.client.gui.pokemob.GuiPokemobAI;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.client.gui.pokemob.GuiPokemobRoutes;
import pokecube.core.client.gui.pokemob.GuiPokemobStorage;
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
    public static KeyMapping nextMob;
    public static KeyMapping nextMove;
    public static KeyMapping previousMob;
    public static KeyMapping previousMove;
    public static KeyMapping mobBack;
    public static KeyMapping mobAttack;
    public static KeyMapping mobStance;
    public static KeyMapping mobMegavolve;
    public static KeyMapping noEvolve;
    public static KeyMapping mobMove1;
    public static KeyMapping mobMove2;
    public static KeyMapping mobMove3;
    public static KeyMapping mobMove4;
    public static KeyMapping gzmove;
    public static KeyMapping mobUp;
    public static KeyMapping mobDown;
    public static KeyMapping throttleUp;
    public static KeyMapping throttleDown;
    public static KeyMapping arrangeGui;
    public static KeyMapping animateGui;

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
        ClientRegistry.registerKeyBinding(ClientSetupHandler.nextMob = new KeyMapping("key.pokemob.next",
                GLFW.GLFW_KEY_RIGHT, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.previousMob = new KeyMapping("key.pokemob.prev",
                GLFW.GLFW_KEY_LEFT, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.nextMove = new KeyMapping("key.pokemob.move.next",
                GLFW.GLFW_KEY_DOWN, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.previousMove = new KeyMapping("key.pokemob.move.prev",
                GLFW.GLFW_KEY_UP, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobBack = new KeyMapping("key.pokemob.recall",
                GLFW.GLFW_KEY_R, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobAttack = new KeyMapping("key.pokemob.attack",
                GLFW.GLFW_KEY_G, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobStance = new KeyMapping("key.pokemob.stance",
                GLFW.GLFW_KEY_BACKSLASH, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMegavolve = new KeyMapping("key.pokemob.megaevolve",
                GLFW.GLFW_KEY_M, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.noEvolve = new KeyMapping("key.pokemob.b", GLFW.GLFW_KEY_B,
                "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove1 = new KeyMapping("key.pokemob.move.1",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove2 = new KeyMapping("key.pokemob.move.2",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove3 = new KeyMapping("key.pokemob.move.3",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobMove4 = new KeyMapping("key.pokemob.move.4",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobUp = new KeyMapping("key.pokemob.up",
                GLFW.GLFW_KEY_SPACE, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.mobDown = new KeyMapping("key.pokemob.down",
                GLFW.GLFW_KEY_LEFT_CONTROL, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.throttleUp = new KeyMapping("key.pokemob.speed.up",
                GLFW.GLFW_KEY_LEFT_BRACKET, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientSetupHandler.throttleDown = new KeyMapping("key.pokemob.speed.down",
                GLFW.GLFW_KEY_RIGHT_BRACKET, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.arrangeGui = new KeyMapping("key.pokemob.arrangegui",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.animateGui = new KeyMapping("key.pokemob.animategui",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientSetupHandler.gzmove = new KeyMapping("key.pokemob.gzmove",
                InputConstants.UNKNOWN.getValue(), "Pokecube"));

        // Forward this to PCEdit mod:
        NBTEdit.setupClient(event);

        // Register the gui side of the screens.
        PokecubeCore.LOGGER.debug("Init Screen Factories");

        final MenuScreens.ScreenConstructor<ContainerPokemob, GuiPokemobBase> factory = (c, i, t) ->
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

        MenuScreens.register(ContainerPokemob.TYPE, factory);
        MenuScreens.register(HealerContainer.TYPE, Healer<HealerContainer>::new);
        MenuScreens.register(PCContainer.TYPE, PC<PCContainer>::new);
        MenuScreens.register(TradeContainer.TYPE, Trade<TradeContainer>::new);
        MenuScreens.register(TMContainer.TYPE, TMs<TMContainer>::new);

        // Register mob rendering
        PokecubeCore.LOGGER.debug("Init Mob Renderers");

        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (!e.stock) continue;
            final EntityType<? extends Mob> t = e.getEntityType();
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
            ItemBlockRenderTypes.setRenderLayer(crop, RenderType.cutoutMipped());
        for (final Block fruit : BerryManager.berryFruits.values())
            ItemBlockRenderTypes.setRenderLayer(fruit, RenderType.cutoutMipped());
        for (final Block leaf : ItemGenerator.leaves.values())
            ItemBlockRenderTypes.setRenderLayer(leaf, RenderType.cutoutMipped());
        for (final Block trapdoor : ItemGenerator.trapdoors.values())
            ItemBlockRenderTypes.setRenderLayer(trapdoor, RenderType.cutoutMipped());
        for (final Block door : ItemGenerator.doors.values())
            ItemBlockRenderTypes.setRenderLayer(door, RenderType.cutoutMipped());
        for (final Block door : BerryManager.pottedBerries.values())
            ItemBlockRenderTypes.setRenderLayer(door, RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(PokecubeItems.NESTBLOCK.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(PokecubeItems.DYNABLOCK.get(), RenderType.cutoutMipped());

        // Register config gui
        ModList.get().getModContainerById(PokecubeCore.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeCore.getConfig(),
                        parent)));

    }
}
