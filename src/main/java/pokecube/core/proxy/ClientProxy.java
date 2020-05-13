package pokecube.core.proxy;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.MoveSound;
import pokecube.core.client.PokecenterSound;
import pokecube.core.client.gui.GuiInfoMessages;
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
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.client.render.mobs.RenderNPC;
import pokecube.core.client.render.mobs.RenderPokecube;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.ShoulderLayer.IShoulderHolder;
import pokecube.core.client.render.mobs.ShoulderLayer.ShoulderHolder;
import pokecube.core.client.render.util.URLSkinTexture;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.PokeType;
import pokecube.nbtedit.NBTEdit;
import thut.api.maths.Vector3;
import thut.core.client.gui.ConfigGui;

public class ClientProxy extends CommonProxy
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

    private static Map<String, ResourceLocation> players  = Maps.newHashMap();
    private static Map<String, ResourceLocation> urlSkins = Maps.newHashMap();

    @SubscribeEvent
    public void colourBlocks(final ColorHandlerEvent.Block event)
    {
        final Block[] leaves = ItemGenerator.leaves.values().toArray(new Block[0]);
        event.getBlockColors().register((state, reader, pos, tintIndex) ->
        {
            return reader != null && pos != null ? BiomeColors.func_228361_b_(reader, pos) : FoliageColors.getDefault();
        }, leaves);
    }

    @SubscribeEvent
    public void colourItems(final ColorHandlerEvent.Item event)
    {
        final Block[] leaves = ItemGenerator.leaves.values().toArray(new Block[0]);
        event.getItemColors().register((stack, tintIndex) ->
        {
            final BlockState blockstate = ((BlockItem) stack.getItem()).getBlock().getDefaultState();
            return event.getBlockColors().getColor(blockstate, null, null, tintIndex);
        }, leaves);

        event.getItemColors().register((stack, tintIndex) ->
        {
            final PokeType type = PokeType.unknown;
            final PokedexEntry entry = ItemPokemobEgg.getEntry(stack);
            if (entry != null) return tintIndex == 0 ? entry.getType1().colour : entry.getType2().colour;
            return tintIndex == 0 ? type.colour : 0xFFFFFFFF;
        }, PokecubeItems.EGG);

    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public ServerWorld getServerWorld()
    {
        try
        {
            return super.getServerWorld();
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    @Override
    public World getWorld()
    {
        return Minecraft.getInstance().world;
    }

    @Override
    public ResourceLocation getPlayerSkin(final String name)
    {
        if (ClientProxy.players.containsKey(name)) return ClientProxy.players.get(name);
        final Minecraft minecraft = Minecraft.getInstance();
        GameProfile profile = new GameProfile((UUID) null, name);
        profile = SkullTileEntity.updateGameProfile(profile);
        final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager()
                .loadSkinFromCache(profile);
        ResourceLocation resourcelocation;
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) resourcelocation = minecraft.getSkinManager().loadSkin(
                map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        else
        {
            final UUID uuid = PlayerEntity.getUUID(profile);
            resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        ClientProxy.players.put(name, resourcelocation);
        return resourcelocation;
    }

    @Override
    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        if (ClientProxy.urlSkins.containsKey(urlSkin)) return ClientProxy.urlSkins.get(urlSkin);
        try
        {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(urlSkin.getBytes("UTF-8"));
            final StringBuilder sb = new StringBuilder(2 * hash.length);
            for (final byte b : hash)
                sb.append(String.format("%02x", b & 0xff));
            final ResourceLocation resourcelocation = new ResourceLocation("skins/" + sb.toString());
            final TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            final Texture object = new URLSkinTexture(null, urlSkin, DefaultPlayerSkin.getDefaultSkinLegacy());
            texturemanager.loadTexture(resourcelocation, object);
            ClientProxy.urlSkins.put(urlSkin, resourcelocation);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return ClientProxy.urlSkins.get(urlSkin);
    }

    @Override
    public void loaded(final FMLLoadCompleteEvent event)
    {
        super.loaded(event);
        RenderPokemob.register();
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        PokecubeCore.LOGGER.debug("Pokecube Client Setup");

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(EventsHandlerClient.class);
        MinecraftForge.EVENT_BUS.register(GuiInfoMessages.class);
        MinecraftForge.EVENT_BUS.register(RenderMobOverlays.class);

        // Register keybinds
        PokecubeCore.LOGGER.debug("Init Keybinds");
        ClientRegistry.registerKeyBinding(ClientProxy.nextMob = new KeyBinding("key.pokemob.next", GLFW.GLFW_KEY_RIGHT,
                "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.previousMob = new KeyBinding("key.pokemob.prev",
                GLFW.GLFW_KEY_LEFT, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.nextMove = new KeyBinding("key.pokemob.move.next",
                GLFW.GLFW_KEY_DOWN, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.previousMove = new KeyBinding("key.pokemob.move.prev",
                GLFW.GLFW_KEY_UP, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.mobBack = new KeyBinding("key.pokemob.recall", GLFW.GLFW_KEY_R,
                "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobAttack = new KeyBinding("key.pokemob.attack", GLFW.GLFW_KEY_G,
                "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobStance = new KeyBinding("key.pokemob.stance",
                GLFW.GLFW_KEY_BACKSLASH, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.mobMegavolve = new KeyBinding("key.pokemob.megaevolve",
                GLFW.GLFW_KEY_M, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.noEvolve = new KeyBinding("key.pokemob.b", GLFW.GLFW_KEY_B,
                "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.mobMove1 = new KeyBinding("key.pokemob.move.1",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobMove2 = new KeyBinding("key.pokemob.move.2",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobMove3 = new KeyBinding("key.pokemob.move.3",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobMove4 = new KeyBinding("key.pokemob.move.4",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.mobUp = new KeyBinding("key.pokemob.up", GLFW.GLFW_KEY_SPACE,
                "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.mobDown = new KeyBinding("key.pokemob.down",
                GLFW.GLFW_KEY_LEFT_CONTROL, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.throttleUp = new KeyBinding("key.pokemob.speed.up",
                GLFW.GLFW_KEY_LEFT_BRACKET, "Pokecube"));
        ClientRegistry.registerKeyBinding(ClientProxy.throttleDown = new KeyBinding("key.pokemob.speed.down",
                GLFW.GLFW_KEY_RIGHT_BRACKET, "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.arrangeGui = new KeyBinding("key.pokemob.arrangegui",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.animateGui = new KeyBinding("key.pokemob.animategui",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));

        ClientRegistry.registerKeyBinding(ClientProxy.gzmove = new KeyBinding("key.pokemob.gzmove",
                InputMappings.INPUT_INVALID.getKeyCode(), "Pokecube"));

        // Forward this to PCEdit mod:
        NBTEdit.setupClient(event);

        // Register to model loading registry
        // TODO see if this was needed
        // OBJLoader.INSTANCE.addDomain(PokecubeCore.MODID);

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

        ScreenManager.registerFactory(ContainerPokemob.TYPE, factory);
        ScreenManager.registerFactory(HealerContainer.TYPE, Healer<HealerContainer>::new);
        ScreenManager.registerFactory(PCContainer.TYPE, PC<PCContainer>::new);
        ScreenManager.registerFactory(TradeContainer.TYPE, Trade<TradeContainer>::new);
        ScreenManager.registerFactory(TMContainer.TYPE, TMs<TMContainer>::new);

        // Register mob rendering
        PokecubeCore.LOGGER.debug("Init Mob Renderers");

        for (final PokedexEntry e : Database.getSortedFormes())
        {
            final EntityType<ShoulderRidingEntity> t = PokecubeCore.typeMap.get(e);
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

        // Register config gui
        ModList.get().getModContainerById(PokecubeCore.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeCore.getConfig(),
                        parent)));

    }

    private final Map<BlockPos, PokecenterSound> pokecenter_sounds = Maps.newHashMap();
    private final Map<SoundEvent, Integer>       move_sounds       = Maps.newHashMap();
    private final Map<SoundEvent, Vector3>       move_positions    = Maps.newHashMap();

    @SubscribeEvent
    public void worldTick(final ClientTickEvent event)
    {
        final Set<SoundEvent> stale = Sets.newHashSet();
        for (final Map.Entry<SoundEvent, Integer> entry : this.move_sounds.entrySet())
        {
            final Integer tick = entry.getValue() - 1;
            if (tick < 0) stale.add(entry.getKey());
            entry.setValue(tick);
        }
        final PlayerEntity player = Minecraft.getInstance().player;
        final Vector3 pos2 = Vector3.getNewVector().set(player);
        for (final SoundEvent e : stale)
        {
            final Vector3 pos = this.move_positions.remove(e);
            this.move_sounds.remove(e);
            final float volume = MoveSound.getVolume(pos, pos2);
            if (volume > 0) Minecraft.getInstance().getSoundHandler().play(new MoveSound(e, pos));
        }
    }

    @Override
    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        this.move_positions.clear();
        this.move_sounds.clear();
        this.pokecenter_sounds.clear();
    }

    @Override
    public void moveSound(final Vector3 pos, final SoundEvent event)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        final Vector3 pos1 = Vector3.getNewVector().set(player);
        final double dist = pos1.distanceTo(pos);
        // Implement a speed of sound delay to this.
        final int delay = (int) (dist * 20.0 / 340.0);
        this.move_sounds.put(event, delay);
        this.move_positions.put(event, pos.copy());
    }

    @Override
    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {
        if (play && !this.pokecenter_sounds.containsKey(tileIn.getPos()))
        {
            final PokecenterSound sound = new PokecenterSound(tileIn);
            Minecraft.getInstance().getSoundHandler().play(sound);
            this.pokecenter_sounds.put(tileIn.getPos(), sound);
        }
        else if (!play && this.pokecenter_sounds.containsKey(tileIn.getPos()))
        {
            final PokecenterSound sound = this.pokecenter_sounds.remove(tileIn.getPos());
            sound.stopped = true;
            Minecraft.getInstance().getSoundHandler().stop(sound);
        }

    }
}
