package pokecube.core.init;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
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
import pokecube.core.database.Database;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoatRenderer;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pokemob.PokemobContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.forge.ClientProxy;

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

    static
    {
        int unk = InputConstants.UNKNOWN.getValue();

        nextMob = new KeyMapping("key.pokemob.next", GLFW.GLFW_KEY_RIGHT, "Pokecube");
        previousMob = new KeyMapping("key.pokemob.prev", GLFW.GLFW_KEY_LEFT, "Pokecube");
        nextMove = new KeyMapping("key.pokemob.move.next", GLFW.GLFW_KEY_DOWN, "Pokecube");
        previousMove = new KeyMapping("key.pokemob.move.prev", GLFW.GLFW_KEY_UP, "Pokecube");
        mobBack = new KeyMapping("key.pokemob.recall", GLFW.GLFW_KEY_R, "Pokecube");
        mobAttack = new KeyMapping("key.pokemob.attack", GLFW.GLFW_KEY_G, "Pokecube");
        mobStance = new KeyMapping("key.pokemob.stance", GLFW.GLFW_KEY_BACKSLASH, "Pokecube");
        mobMegavolve = new KeyMapping("key.pokemob.megaevolve", GLFW.GLFW_KEY_M, "Pokecube");
        noEvolve = new KeyMapping("key.pokemob.b", GLFW.GLFW_KEY_B, "Pokecube");

        mobMove1 = new KeyMapping("key.pokemob.move.1", unk, "Pokecube");
        mobMove2 = new KeyMapping("key.pokemob.move.2", unk, "Pokecube");
        mobMove3 = new KeyMapping("key.pokemob.move.3", unk, "Pokecube");
        mobMove4 = new KeyMapping("key.pokemob.move.4", unk, "Pokecube");

        mobUp = new KeyMapping("key.pokemob.up", GLFW.GLFW_KEY_SPACE, "Pokecube");
        mobDown = new KeyMapping("key.pokemob.down", GLFW.GLFW_KEY_LEFT_CONTROL, "Pokecube");
        throttleUp = new KeyMapping("key.pokemob.speed.up", GLFW.GLFW_KEY_LEFT_BRACKET, "Pokecube");
        throttleDown = new KeyMapping("key.pokemob.speed.down", GLFW.GLFW_KEY_RIGHT_BRACKET, "Pokecube");
        arrangeGui = new KeyMapping("key.pokemob.arrangegui", unk, "Pokecube");
        animateGui = new KeyMapping("key.pokemob.animategui", unk, "Pokecube");
        gzmove = new KeyMapping("key.pokemob.gzmove", unk, "Pokecube");
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        RenderPokemob.register();
    }

    private static void registerKey(KeyMapping key, Object event)
    {
        ClientRegistry.registerKeyBinding(key);
    }

    public static void registerKeybinds(Object event)
    {
        PokecubeAPI.LOGGER.debug("Init Keybinds");
        registerKey(ClientSetupHandler.nextMob, event);
        registerKey(ClientSetupHandler.previousMob, event);
        registerKey(ClientSetupHandler.nextMove, event);
        registerKey(ClientSetupHandler.previousMove, event);
        registerKey(ClientSetupHandler.mobBack, event);
        registerKey(ClientSetupHandler.mobAttack, event);
        registerKey(ClientSetupHandler.mobStance, event);
        registerKey(ClientSetupHandler.mobMegavolve, event);
        registerKey(ClientSetupHandler.noEvolve, event);
        registerKey(ClientSetupHandler.mobMove1, event);
        registerKey(ClientSetupHandler.mobMove2, event);
        registerKey(ClientSetupHandler.mobMove3, event);
        registerKey(ClientSetupHandler.mobMove4, event);
        registerKey(ClientSetupHandler.mobUp, event);
        registerKey(ClientSetupHandler.mobDown, event);
        registerKey(ClientSetupHandler.throttleUp, event);
        registerKey(ClientSetupHandler.throttleDown, event);
        registerKey(ClientSetupHandler.arrangeGui, event);
        registerKey(ClientSetupHandler.animateGui, event);
        registerKey(ClientSetupHandler.gzmove, event);

        ClientProxy.NBTEditKey = new KeyMapping("NBTEdit Shortcut", InputConstants.UNKNOWN.getValue(),
                "key.categories.misc");
        ClientRegistry.registerKeyBinding(ClientProxy.NBTEditKey);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        PokecubeAPI.LOGGER.debug("Pokecube Client Setup");

        // Register event handlers
        EventsHandlerClient.register();

        // Register keybinds
        registerKeybinds(event);

        // Forward this to PCEdit mod:
        NBTEdit.setupClient(event);

        // Register the gui side of the screens.
        PokecubeAPI.LOGGER.debug("Init Screen Factories");

        final MenuScreens.ScreenConstructor<PokemobContainer, GuiPokemobBase> factory = (c, i, t) -> {
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

        MenuScreens.register(MenuTypes.POKEMOB.get(), factory);
        MenuScreens.register(MenuTypes.HEALER.get(), Healer<HealerContainer>::new);
        MenuScreens.register(MenuTypes.PC.get(), PC<PCContainer>::new);
        MenuScreens.register(MenuTypes.TRADE.get(), Trade<TradeContainer>::new);
        MenuScreens.register(MenuTypes.TMS.get(), TMs<TMContainer>::new);

        // Register mob rendering
        PokecubeAPI.LOGGER.debug("Init Mob Renderers");

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
        ItemBlockRenderTypes.setRenderLayer(PokecubeItems.NEST.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(PokecubeItems.DYNAMAX.get(), RenderType.cutoutMipped());

        ClientSetupHandler.registerLayerDefinition(ForgeHooksClient::registerLayerDefinition);

        event.enqueueWork(() ->
        {
            BerriesWoodType.register();
        });

        // FIXME Register config gui
    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event)
    {
        for (final PokedexEntry e : Database.getSortedFormes())
        {
            if (!e.stock) continue;
            final EntityType<? extends Mob> t = e.getEntityType();
            event.registerEntityRenderer(t, (manager) -> new RenderPokemob(e, manager));
        }
        event.registerEntityRenderer(EntityTypes.getPokecube(), RenderPokecube::new);
        event.registerEntityRenderer(EntityTypes.getMove(), RenderMoves::new);
        event.registerEntityRenderer(EntityTypes.getNpc(), RenderNPC::new);
        event.registerEntityRenderer(EntityTypes.getEgg(), RenderEgg::new);
        event.registerEntityRenderer(EntityTypes.getBoat(), GenericBoatRenderer::new);

        event.registerBlockEntityRenderer(PokecubeItems.SIGN_TYPE.get(), SignRenderer::new);
    }

    public static void registerLayerDefinition(final BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer)
    {
        for (GenericBoat.Type value : GenericBoat.Type.values())
        {
            consumer.accept(GenericBoatRenderer.createBoatModelName(value), BoatModel::createBodyModel);
        }
    }

    @SubscribeEvent
    public static void colourBlocks(final ColorHandlerEvent.Block event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23);
        event.getBlockColors().register((state, reader, pos, tintIndex) -> {
            return reader != null && pos != null ? BiomeColors.getAverageFoliageColor(reader, pos)
                    : FoliageColor.getDefaultColor();
        }, qualotLeaves);
    }

    @SubscribeEvent
    public static void colourItems(final ColorHandlerEvent.Item event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23);
        event.getItemColors().register((stack, tintIndex) -> {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, qualotLeaves);

        event.getItemColors().register((stack, tintIndex) -> {
            final PokeType type = PokeType.unknown;
            final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
            if (entry != null) return tintIndex == 0 ? entry.getType1().colour : entry.getType2().colour;
            return tintIndex == 0 ? type.colour : 0xFFFFFFFF;
        }, PokecubeItems.EGG.get());

        for (Item i : ItemMegawearable.INSTANCES)
        {
            event.getItemColors().register((stack, tintIndex) -> {
                if (!(stack.getItem() instanceof DyeableLeatherItem item)) return 0xFFFFFFFF;
                return tintIndex == 0 ? item.getColor(stack) : 0xFFFFFFFF;
            }, i);
        }
    }

    @SubscribeEvent
    public static void textureStitch(final TextureStitchEvent.Pre event)
    {
        if (!event.getAtlas().location().toString().equals("minecraft:textures/atlas/blocks.png")) return;
        PokecubeAPI.LOGGER.debug("Registering Pokecube Slot Textures");
        event.addSprite(new ResourceLocation(PokecubeCore.MODID, "gui/slot_cube"));
        event.addSprite(new ResourceLocation(PokecubeCore.MODID, "gui/slot_tm"));
    }
}
