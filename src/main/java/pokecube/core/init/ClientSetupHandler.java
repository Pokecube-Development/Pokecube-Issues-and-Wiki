package pokecube.core.init;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.signs.GenericSignBlockEntity;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.blocks.Healer;
import pokecube.core.client.gui.blocks.PC;
import pokecube.core.client.gui.blocks.TMs;
import pokecube.core.client.gui.blocks.Trade;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.client.render.RenderMoves;
import pokecube.core.client.render.mobs.GenericBoatRenderer;
import pokecube.core.client.render.mobs.RenderEgg;
import pokecube.core.client.render.mobs.RenderNPC;
import pokecube.core.client.render.mobs.RenderPokecube;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.database.Database;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.boats.GenericBoat.BoatType;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.ItemTM;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.MovesUtils;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.forge.ClientProxy;
import thut.lib.RegHelper;

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

    public static KeyMapping nextTarget;
    public static KeyMapping previousTarget;

    public static KeyMapping nextAlly;
    public static KeyMapping previousAlly;

    static
    {
        int unk = InputConstants.UNKNOWN.getValue();

        nextMob = new KeyMapping("key.pokemob.next", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_RIGHT,
                "key.categories.pokecube");
        previousMob = new KeyMapping("key.pokemob.prev", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_LEFT,
                "key.categories.pokecube");
        nextMove = new KeyMapping("key.pokemob.move.next", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_DOWN,
                "key.categories.pokecube");
        previousMove = new KeyMapping("key.pokemob.move.prev", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_UP, "key.categories.pokecube");
        mobBack = new KeyMapping("key.pokemob.recall", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_R,
                "key.categories.pokecube");
        mobAttack = new KeyMapping("key.pokemob.attack", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_G,
                "key.categories.pokecube");
        mobStance = new KeyMapping("key.pokemob.stance", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH, "key.categories.pokecube");
        mobMegavolve = new KeyMapping("key.pokemob.megaevolve", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_M, "key.categories.pokecube");
        noEvolve = new KeyMapping("key.pokemob.b", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_B,
                "key.categories.pokecube");

        mobMove1 = new KeyMapping("key.pokemob.move.1", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");
        mobMove2 = new KeyMapping("key.pokemob.move.2", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");
        mobMove3 = new KeyMapping("key.pokemob.move.3", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");
        mobMove4 = new KeyMapping("key.pokemob.move.4", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");

        mobUp = new KeyMapping("key.pokemob.up", KeyConflictContext.IN_GAME, Type.KEYSYM, GLFW.GLFW_KEY_SPACE,
                "key.categories.pokecube");
        mobDown = new KeyMapping("key.pokemob.down", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.pokecube");
        throttleUp = new KeyMapping("key.pokemob.speed.up", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET, "key.categories.pokecube");
        throttleDown = new KeyMapping("key.pokemob.speed.down", KeyConflictContext.IN_GAME, Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.pokecube");
        arrangeGui = new KeyMapping("key.pokemob.arrangegui", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");
        animateGui = new KeyMapping("key.pokemob.animategui", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");
        gzmove = new KeyMapping("key.pokemob.gzmove", KeyConflictContext.IN_GAME, Type.KEYSYM, unk, "key.categories.pokecube");

        nextTarget = new KeyMapping("key.pokemob.target.next", KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.categories.pokecube");
        previousTarget = new KeyMapping("key.pokemob.target.prev", KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.categories.pokecube");

        nextAlly = new KeyMapping("key.pokemob.ally.next", KeyConflictContext.IN_GAME, KeyModifier.ALT, Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT, "key.categories.pokecube");
        previousAlly = new KeyMapping("key.pokemob.ally.prev", KeyConflictContext.IN_GAME, KeyModifier.ALT, Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT, "key.categories.pokecube");
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        RenderPokemob.register();
    }

    @SubscribeEvent
    public static void registerGui(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll("pokecube_gui", GuiDisplayPokecubeInfo.instance());
    }

    private static void registerKey(KeyMapping key, RegisterKeyMappingsEvent event)
    {
        event.register(key);
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event)
    {
        PokecubeAPI.logDebug("Init Keybinds");
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

        registerKey(ClientSetupHandler.nextTarget, event);
        registerKey(ClientSetupHandler.previousTarget, event);
        registerKey(ClientSetupHandler.nextAlly, event);
        registerKey(ClientSetupHandler.previousAlly, event);

        ClientProxy.NBTEditKey = new KeyMapping("key.nbt.edit", InputConstants.UNKNOWN.getValue(),
                "key.categories.pokecube");
        event.register(ClientProxy.NBTEditKey);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Pokecube Client Setup");

        // Register event handlers
        EventsHandlerClient.register();

        // Forward this to PCEdit mod:
        NBTEdit.setupClient(event);

        // Register the gui side of the screens.
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Init Screen Factories");

        MenuScreens.register(MenuTypes.POKEMOB.get(), GuiPokemob::new);
        MenuScreens.register(MenuTypes.HEALER.get(), Healer<HealerContainer>::new);
        MenuScreens.register(MenuTypes.PC.get(), PC<PCContainer>::new);
        MenuScreens.register(MenuTypes.TRADE.get(), Trade<TradeContainer>::new);
        MenuScreens.register(MenuTypes.TMS.get(), TMs<TMContainer>::new);

        ClientSetupHandler.registerLayerDefinition(ForgeHooksClient::registerLayerDefinition);

        event.enqueueWork(() -> {
            BerriesWoodType.register();
        });
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
        event.registerEntityRenderer(EntityTypes.getBoat(), context -> new GenericBoatRenderer(context, false));
        event.registerEntityRenderer(EntityTypes.getChestBoat(), context -> new GenericBoatRenderer(context, true));

        if (GenericSignBlockEntity.SIGN_TYPE != null)
            event.registerBlockEntityRenderer(GenericSignBlockEntity.SIGN_TYPE.get(), SignRenderer::new);
    }

    public static void registerLayerDefinition(final BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer)
    {
        LayerDefinition noChest = BoatModel.createBodyModel();
        LayerDefinition withChest = BoatModel.createBodyModel();
        for (BoatType value : GenericBoat.getTypes())
        {
            String modid = RegHelper.getKey(value.item().get()).getNamespace();
            // This is layer without chest
            consumer.accept(GenericBoatRenderer.createBoatModelName(modid, value, false), () -> noChest);
            consumer.accept(GenericBoatRenderer.createBoatModelName(modid, value, true), () -> withChest);
        }
    }

    @SubscribeEvent
    public static void colourBlocks(final RegisterColorHandlersEvent.Block event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23).get();
        event.register((state, reader, pos, tintIndex) -> {
            return reader != null && pos != null ? BiomeColors.getAverageFoliageColor(reader, pos)
                    : FoliageColor.getDefaultColor();
        }, qualotLeaves);
    }

    @SubscribeEvent
    public static void colourItems(final RegisterColorHandlersEvent.Item event)
    {
        final Block qualotLeaves = BerryManager.berryLeaves.get(23).get();
        event.register((stack, tintIndex) -> {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, qualotLeaves);

        event.register((stack, tintIndex) -> {
            final PokeType type = PokeType.unknown;
            final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
            if (entry != null) return tintIndex == 0 ? entry.getType1().colour : entry.getType2().colour;
            return tintIndex == 0 ? type.colour : 0xFFFFFFFF;
        }, PokecubeItems.EGG.get());

        for (Item i : ItemMegawearable.INSTANCES)
        {
            event.register((stack, tintIndex) -> {
                if (!(stack.getItem() instanceof DyeableLeatherItem item)) return 0xFFFFFFFF;
                return tintIndex == 0 ? item.getColor(stack) : 0xFFFFFFFF;
            }, i);
        }

        event.register((stack, tintIndex) -> {
            if (!(stack.getItem() instanceof DyeableLeatherItem item)) return 0xFFFFFFFF;
            return tintIndex == 0 ? item.getColor(stack) : 0xFFFFFFFF;
        }, PokecubeItems.POKEWATCH.get());

        event.register((stack, tintIndex) -> {
            String moveName = ItemTM.getMoveFromStack(stack);
            if (moveName == null) return 0xFFFFFFFF;
            var move = MovesUtils.getMove(moveName);
            if (move != null) return move.getType(null).colour;
            return 0xFFFFFFFF;
        }, PokecubeItems.TM.get());
    }
}
