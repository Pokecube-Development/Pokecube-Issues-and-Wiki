package pokecube.core.handlers.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.ai.tasks.ants.AntTasks.AntInhabitor;
import pokecube.core.ai.tasks.bees.BeeTasks.BeeInhabitor;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.commands.CommandManager;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.events.CustomInteractEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityAffected.DefaultAffected;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable.SaveableHabitatProvider;
import pokecube.core.interfaces.capabilities.CapabilityInhabitor.InhabitorProvider;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.capabilities.TextureableCaps;
import pokecube.core.interfaces.capabilities.TextureableCaps.NPCCap;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.inventory.pc.PCWrapper;
import pokecube.core.inventory.tms.TMInventory;
import pokecube.core.inventory.trade.TradeInventory;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.megastuff.WearablesCompat;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.world.gen.jigsaw.CustomJigsawPiece;
import pokecube.nbtedit.NBTEdit;
import thut.api.Tracker;
import thut.api.entity.CopyCaps;
import thut.api.entity.ShearableCaps;
import thut.api.inventory.InvHelper.ItemCap;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EventsHandler
{
    public static class ChooseFirst
    {
        final Player player;

        final long start;

        public ChooseFirst(final Player player)
        {
            this.player = player;
            this.start = player.getLevel().getGameTime();
            if (!SpawnHandler.canSpawnInWorld(player.getLevel(), false)) return;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(final TickEvent.PlayerTickEvent event)
        {
            if (event.player.getLevel().getGameTime() - this.start < 20) return;
            if (event.player == this.player)
            {
                PacketChoose packet;
                packet = new PacketChoose(PacketChoose.OPENGUI);
                final boolean hasStarter = PokecubeSerializer.getInstance().hasStarter(this.player);
                if (hasStarter)
                {
                    packet.data.putBoolean("C", false);
                    packet.data.putBoolean("H", hasStarter);
                }
                else
                {
                    final boolean special = false;
                    final boolean pick = false;
                    packet = PacketChoose.createOpenPacket(special, pick, Database.getStarters());
                }
                PokecubeCore.packets.sendTo(packet, (ServerPlayer) event.player);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static boolean RUNNING = false;

    private static class WorldTickScheduler implements IWorldTickListener
    {
        static WorldTickScheduler INSTANCE = new WorldTickScheduler();

        @Override
        public void onTickEnd(final ServerLevel world)
        {
            if (!EventsHandler.RUNNING) return;
            final ResourceKey<Level> dim = world.dimension();
            final List<IRunnable> tasks = EventsHandler.scheduledTasks.getOrDefault(dim, Collections.emptyList());
            if (!world.getServer().isSameThread()) throw new IllegalStateException("World ticking off thread!");

            final Set<IRunnable> done = Sets.newHashSet();
            final List<IRunnable> toRun = Lists.newArrayList();

            synchronized (tasks)
            {
                toRun.addAll(tasks);
            }

            final long start = System.currentTimeMillis();
            for (final IRunnable run : toRun)
            {
                if (run.run(world)) done.add(run);
                final long dt = System.currentTimeMillis() - start;
                if (dt > 5) break;
            }

            synchronized (tasks)
            {
                tasks.removeAll(done);
            }
            // Call spawner tick at end of world tick.
            if (!Database.spawnables.isEmpty()) PokecubeCore.spawner.tick(world);
        }
    }

    public static final ResourceLocation POKEMOBCAP = new ResourceLocation(PokecubeMod.ID, "pokemob");
    public static final ResourceLocation AFFECTEDCAP = new ResourceLocation(PokecubeMod.ID, "affected");
    public static final ResourceLocation DATACAP = new ResourceLocation(PokecubeMod.ID, "data");
    public static final ResourceLocation BEECAP = new ResourceLocation(PokecubeMod.ID, "bee");
    public static final ResourceLocation ANTCAP = new ResourceLocation(PokecubeMod.ID, "ant");
    public static final ResourceLocation TEXTURECAP = new ResourceLocation(PokecubeMod.ID, "textured");
    public static final ResourceLocation INVENTORYCAP = new ResourceLocation(PokecubeMod.ID, "tile_inventory");

    static double max = 0;

    /**
     * This returns true if the given entity is not a vanilla entity, or is not
     * a mob-like entity, it returns false for modded mobs, as well as players,
     * armour stands, boats, etc
     */
    private static Predicate<Entity> NOTVANILLAANIMALORMOB;

    /**
     * This returns true if the given entity is ia "vanilla" monster, but not a
     * boss
     */
    public static Predicate<Entity> MONSTERMATCHER;

    /**
     * This returns true if the given entity is ia "vanilla" animal
     */
    public static Predicate<Entity> ANIMALMATCHER;

    static
    {
        // This deals with making sure it is actually a mob, as well as not an
        // npc, or a pokemob
        EventsHandler.NOTVANILLAANIMALORMOB = e -> {
            boolean canSpawn = false;
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
            // This includes players, armour stands, effects, etc
            final boolean noSpawnBlock = !(e instanceof Mob);
            // We don't want to block something if we have made it a pokemob
            final boolean isPokemob = pokemob != null;
            // Simple check for vanillaness, via the entity type registry name
            boolean isVanilla = e.getType().getRegistryName().getNamespace().equals("minecraft");

            // Now also check that the world is also a vanilla world, or one
            // specifically allowed to have spawns revoked.
            final String worldRegName = e.level.dimension().location().toString();
            isVanilla = isVanilla && (worldRegName.startsWith("minecraft:")
                    || PokecubeCore.getConfig().deactivateWhitelist.contains(worldRegName));

            // Lets not block villagers/merchants/pillagers
            final boolean isNpc = e instanceof Npc || e instanceof Merchant || e instanceof WitherBoss;
            // Lets also not block the ender dragon/parts
            final boolean isDragon = e instanceof EnderDragon || e instanceof EnderDragonPart;
            canSpawn = noSpawnBlock || isDragon || isNpc || isPokemob || !isVanilla;
            return !canSpawn;
        };

        // IMob -> monster
        EventsHandler.MONSTERMATCHER = e -> (e instanceof Enemy);
        // Not IMob -> animal
        EventsHandler.ANIMALMATCHER = e -> !(e instanceof Enemy);

        EventsHandler.ANIMALMATCHER = EventsHandler.NOTVANILLAANIMALORMOB.and(EventsHandler.ANIMALMATCHER);
        EventsHandler.MONSTERMATCHER = EventsHandler.NOTVANILLAANIMALORMOB.and(EventsHandler.MONSTERMATCHER);
    }

    private static Map<ResourceKey<Level>, List<IRunnable>> scheduledTasks = Maps.newConcurrentMap();

    public static void Schedule(final Level world, final IRunnable task)
    {
        EventsHandler.Schedule(world, task, true);
    }

    public static void Schedule(final Level world, final IRunnable task, final boolean immedateIfPossible)
    {
        if (!(world instanceof ServerLevel)) return;
        final ServerLevel swrld = (ServerLevel) world;

        // If we are tickingEntities, do not do this, as it can cause
        // concurrent modification exceptions.
        if (immedateIfPossible && !swrld.isHandlingTick() && swrld.getServer().isSameThread())
        {
            // This will either run it now, or run it on main thread soon
            swrld.getServer().execute(() -> task.run(swrld));
            return;
        }
        final ResourceKey<Level> dim = world.dimension();
        final List<IRunnable> tasks = EventsHandler.scheduledTasks.getOrDefault(dim, Lists.newArrayList());
        synchronized (tasks)
        {
            tasks.add(task);
        }
        EventsHandler.scheduledTasks.put(dim, tasks);
    }

    /**
     * If this is false, then the effects occur on every valid tick. Only set
     * this false if you have something else to manage the TerrainEffectEvent
     * and the OngoingTickEvent!
     */
    public static boolean COOLDOWN_BASED = true;

    static int count = 0;

    static int countAbove = 0;
    static double mean = 0;
    static long starttime = 0;
    static boolean notified = false;

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double candyChance = 4.5;

    public static double juiceChance = 3.5;

    public static void register()
    {
        // Allows mining properly while riding dive pokemobs.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onBreakSpeedCheck);

        // This adds: AffectCapability, PokemobCapability + supporting (data,
        // genetics, textures, shearable) as well as NPCMob capabilities for
        // textures.
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EventsHandler::onEntityCaps);
        // This one does the mega wearable caps for worn items
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, EventsHandler::onItemCaps);
        // This does inventory capabilities for:
        // TM machine, Trading Maching and PCs
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, EventsHandler::onTileCaps);
        // This is being used as an earlier "world load" like event, for
        // re-setting the pokecube serializer for the overworld.
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, EventsHandler::onWorldCaps);

        // This handles preventing blacklisted mobs from joining a world, for
        // the disable<thing> configs. It also adds the creepers avoid psychic
        // types AI, and does some cleanup for shoulder mobs.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onMobJoinWorld);
        // This handles one part of preventing natural spawns for the
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onCheckSpawnCheck);

        // Here we handle bed healing if enabled in configs
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerWakeUp);
        // This ticks ongoing effects (like burn, poison, etc)
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onLivingUpdate);
        // This synchronizes stats and data for the player, and sends the
        // GuiOnLogin if enabled and required.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerLogin);
        // This one handles not sending "hidden" pokecubes to the player, for
        // loot-pokecubes which the player is not allowed to pick up yet.
        // This also handles syncing player data over to other players, for
        // stats information in pokewatch.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onStartTracking);

        // Does some debug output in pokecube tags if enabled.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onServerStarting);
        // Cleans up some things for when server next starts.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onServerStopped);
        // Initialises or reloads some datapack dependent values in Database
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onResourcesReloaded);
        // This does similar to the above, but on dedicated servers only.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onTagsUpdated);
        // Registers our commands.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onCommandRegister);

        // This deals with running the tasks scheduled via
        MinecraftForge.EVENT_BUS.addListener(WorldTickManager::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(WorldTickManager::onWorldLoad);
        MinecraftForge.EVENT_BUS.addListener(WorldTickManager::onWorldUnload);

        // This attempts to recall the mobs following the player when they
        // change dimension.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onChangeDimension);
        // This handles preventing players from being kicked for flying, if they
        // are riding a pokemob that can fly.
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerTick);
        // This saves the pokecube Serializer
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onWorldSave);

        // These 4 are for handling interaction events, etc
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEntityInteract);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEntityInteractSpecific);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onItemRightClick);
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onEmptyRightClick);

        // now let our other handlers register their stuff

        MoveEventsHandler.register();
        PCEventsHandler.register();
        PokemobEventsHandler.register();
        SpawnEventsHandler.register();
        StatsHandler.register();
        MoveQueuer.register();

        WorldTickManager.registerStaticData(() -> WorldTickScheduler.INSTANCE, p -> true);

        // Here we register the onWorldLoad for pokemob tracker, this handles
        // initializing the tracked pokemob maps, etc.
        MinecraftForge.EVENT_BUS.addListener(PokemobTracker::onWorldLoad);

    }

    private static void onEntityInteract(final PlayerInteractEvent.EntityInteract evt)
    {
        if (evt instanceof CustomInteractEvent) return;
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final ServerPlayer player = (ServerPlayer) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(evt.getPlayer(), evt.getHand(), evt.getTarget());
            MinecraftForge.EVENT_BUS.post(event);
            if (event.getResult() == Result.ALLOW)
            {
                player.getPersistentData().putLong("__poke_int_c_", Tracker.instance().getTick());
                player.getPersistentData().putLong(ID, Tracker.instance().getTick());
            }
        }
    }

    private static void onEntityInteractSpecific(final PlayerInteractEvent.EntityInteractSpecific evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final ServerPlayer player = (ServerPlayer) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(evt.getPlayer(), evt.getHand(), evt.getTarget());
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled())
            {
                player.getPersistentData().putLong("__poke_int_c_", Tracker.instance().getTick());
                player.getPersistentData().putLong(ID, Tracker.instance().getTick());
            }
        }
    }

    private static void onItemRightClick(final PlayerInteractEvent.RightClickItem evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final ServerPlayer player = (ServerPlayer) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }

        boolean isSpawnPresetDebug = evt.getItemStack().getDisplayName().getString().contains("spawn_preset_debug");

        if (isSpawnPresetDebug)
        {
            Vector3 v = Vector3.getNewVector().set(player);
            Level level = player.level;
            SpawnCheck check = new SpawnCheck(v, level);

            List<String> valid = Lists.newArrayList();

            for (Entry<String, SpawnRule> entry : SpawnBiomeMatcher.PRESETS.entrySet())
            {
                SpawnBiomeMatcher m = new SpawnBiomeMatcher(entry.getValue().copy());
                if (m.matches(check)) valid.add(entry.getKey());
            }
            if (!valid.isEmpty())
            {
                player.sendMessage(new TextComponent("Spawn Presets valid for here:"), player.getUUID());
                for (String s : valid) player.sendMessage(new TextComponent(s), player.getUUID());
            }
            else player.sendMessage(new TextComponent("No matching presets for this location"), player.getUUID());
        }
    }

    private static void onEmptyRightClick(final PlayerInteractEvent.RightClickEmpty evt)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final ServerPlayer player = (ServerPlayer) evt.getPlayer();
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
    }

    private static void onBreakSpeedCheck(final PlayerEvent.BreakSpeed evt)
    {
        final Entity ridden = evt.getEntity().getVehicle();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(ridden);
        if (pokemob != null)
        {
            boolean aqua = evt.getEntity().isInWater();
            if (aqua) aqua = !EnchantmentHelper.hasAquaAffinity((LivingEntity) evt.getEntity());
            if (aqua) evt.setNewSpeed(evt.getOriginalSpeed() / 0.04f);
            else evt.setNewSpeed(evt.getOriginalSpeed() / 0.2f);
        }
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity)) return;
        if (event.getObject() instanceof LivingEntity
                && !event.getCapabilities().containsKey(EventsHandler.AFFECTEDCAP))
        {
            final DefaultAffected affected = new DefaultAffected((LivingEntity) event.getObject());
            event.addCapability(EventsHandler.AFFECTEDCAP, affected);
        }
        if (event.getObject() instanceof EntityPokemob
                && !event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
            final EntityPokemob mob = (EntityPokemob) event.getObject();
            final DefaultPokemob pokemob = new DefaultPokemob(mob);
            final GeneticsProvider genes = new GeneticsProvider();
            final DataSync_Impl data = new DataSync_Impl();
            final TextureableCaps.PokemobCap tex = new TextureableCaps.PokemobCap(mob);
            pokemob.setDataSync(data);
            pokemob.genes = genes.wrapped;
            event.addCapability(GeneticsManager.POKECUBEGENETICS, genes);
            event.addCapability(EventsHandler.POKEMOBCAP, pokemob);
            event.addCapability(EventsHandler.DATACAP, data);
            event.addCapability(EventsHandler.TEXTURECAP, tex);
            event.addCapability(ShearableCaps.LOC, new ShearableCaps.Wrapper(pokemob));
            event.addCapability(CopyCaps.LOC, (ICapabilityProvider) pokemob.getCopy());
            IGuardAICapability.addCapability(event);

            // If it is a bee, we will add this to it.
            if (EntityTypeTags.BEEHIVE_INHABITORS.contains(mob.getType()))
                event.addCapability(EventsHandler.BEECAP, new InhabitorProvider(new BeeInhabitor(mob)));
            if (ItemList.is(IMoveConstants.ANTS, mob))
                event.addCapability(EventsHandler.ANTCAP, new InhabitorProvider(new AntInhabitor(mob)));
        }

        if (event.getObject() instanceof NpcMob)
        {
            final NpcMob prof = (NpcMob) event.getObject();
            event.addCapability(EventsHandler.TEXTURECAP, new NPCCap<>(prof, e -> e.getTex(), e -> !e.isMale()));
            IGuardAICapability.addCapability(event);
        }
    }

    private static void onItemCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemEffects.registerCapabilities(event);
        GeneticsManager.registerCapabilities(event);
        WearablesCompat.registerCapabilities(event);
        if (!MegaCapability.isStoneOrWearable(event.getObject())) return;
        final ResourceLocation key = new ResourceLocation("pokecube:megawearable");
        if (event.getCapabilities().containsKey(key)) return;
        event.addCapability(key, new MegaCapability(event.getObject()));
    }

    private static void onTileCaps(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        final ResourceLocation key = EventsHandler.INVENTORYCAP;
        if (event.getCapabilities().containsKey(key)) return;
        if (event.getObject() instanceof TMTile) event.addCapability(key, new TMInventory((TMTile) event.getObject()));
        if (event.getObject() instanceof TraderTile)
            event.addCapability(key, new TradeInventory((TraderTile) event.getObject()));
        if (event.getObject() instanceof PCTile) event.addCapability(key, new PCWrapper((PCTile) event.getObject()));
        if (event.getObject() instanceof NestTile)
        {
            final ResourceLocation nestCap = new ResourceLocation("pokecube:nest");
            event.addCapability(key, new ItemCap(54, 64));
            event.addCapability(nestCap, new SaveableHabitatProvider(event.getObject()));
        }
    }

    private static void onWorldCaps(final AttachCapabilitiesEvent<Level> event)
    {
        if (event.getObject() instanceof ServerLevel && Level.OVERWORLD.equals(event.getObject().dimension()))
            PokecubeSerializer.newInstance((ServerLevel) event.getObject());
    }

    private static void onMobJoinWorld(final EntityJoinWorldEvent evt)
    {
        if (PokecubeCore.getConfig().disableVanillaMonsters && EventsHandler.MONSTERMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().discard();
            evt.setCanceled(true);
            return;
        }
        if (PokecubeCore.getConfig().disableVanillaAnimals && EventsHandler.ANIMALMATCHER.test(evt.getEntity()))
        {
            evt.getEntity().discard();
            evt.setCanceled(true);
            return;
        }
        // Forge workaround for this not being called server side!
        if (!evt.getEntity().isAddedToWorld()) evt.getEntity().onAddedToWorld();

        if (evt.getEntity() instanceof IPokemob && evt.getEntity().getPersistentData().getBoolean("onShoulder"))
        {
            ((IPokemob) evt.getEntity()).setLogicState(LogicStates.SITTING, false);
            evt.getEntity().getPersistentData().remove("onShoulder");
        }
        if (evt.getEntity() instanceof Creeper)
        {
            final Creeper creeper = (Creeper) evt.getEntity();
            final AvoidEntityGoal<?> avoidAI = new AvoidEntityGoal<>(creeper, EntityPokemob.class, 6.0F, 1.0D, 1.2D,
                    e -> CapabilityPokemob.getPokemobFor(e).isType(PokeType.getType("psychic")));
            creeper.goalSelector.addGoal(3, avoidAI);
        }
    }

    private static void onCheckSpawnCheck(final LivingSpawnEvent.CheckSpawn event)
    {
        // Only deny them from these reasons.
        if (!(event.getSpawnReason() == MobSpawnType.NATURAL || event.getSpawnReason() == MobSpawnType.CHUNK_GENERATION
                || event.getSpawnReason() == MobSpawnType.STRUCTURE))
            return;

        if (EventsHandler.MONSTERMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateMonsters)
            event.setResult(Result.DENY);
        if (EventsHandler.ANIMALMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateAnimals)
            event.setResult(Result.DENY);
    }

    private static void onPlayerWakeUp(final PlayerWakeUpEvent evt)
    {
        if (!PokecubeCore.getConfig().bedsHeal) return;
        for (int i = 0; i < evt.getPlayer().getInventory().getContainerSize(); i++)
        {
            final ItemStack stack = evt.getPlayer().getInventory().getItem(i);
            if (PokecubeManager.isFilled(stack)) PokecubeManager.heal(stack, evt.getPlayer().getLevel());
        }
    }

    private static void onLivingUpdate(final LivingUpdateEvent evt)
    {
        final IPokemob poke = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (poke != null) poke.onTick();

        if (evt.getEntity().getLevel().isClientSide || !evt.getEntity().isAlive()) return;
        final int tick = Math.max(PokecubeCore.getConfig().attackCooldown, 1);
        // Handle ongoing effects for this mob.
        if (evt.getEntity().tickCount % tick == 0 || !EventsHandler.COOLDOWN_BASED)
        {
            final IOngoingAffected affected = CapabilityAffected.getAffected(evt.getEntity());
            if (affected != null) affected.tick();
        }
    }

    private static void onPlayerLogin(final PlayerLoggedInEvent evt)
    {
        final Player player = evt.getPlayer();
        if (!player.isEffectiveAi()) return;
        EventsHandler.sendInitInfo((ServerPlayer) player);
    }

    private static void onStartTracking(final StartTracking event)
    {
        // Check if the pokecube is loot, and is not collectable by the player,
        // if this is the case, it should be set invisible.
        if (event.getTarget() instanceof EntityPokecube && event.getEntity() instanceof ServerPlayer)
        {
            final EntityPokecube pokecube = (EntityPokecube) event.getTarget();
            if (pokecube.isLoot && pokecube.cannotCollect(event.getEntity()))
                PacketPokecube.sendMessage((Player) event.getEntity(), pokecube.getId(),
                        Tracker.instance().getTick() + pokecube.resetTime);
        }
        if (event.getTarget() instanceof ServerPlayer && event.getEntity() instanceof ServerPlayer)
        {
            final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData((Player) event.getTarget());
            final PlayerData data = manager.getData("pokecube-stats");
            PacketDataSync.syncData(data, event.getTarget().getUUID(), (ServerPlayer) event.getEntity(), false);
        }
    }

    private static void onServerStarting(final ServerStartingEvent event)
    {
        PokecubeCore.LOGGER.info("Server Starting");
        PokecubeItems.init(event.getServer());
        EventsHandler.RUNNING = true;
    }

    private static void onServerStopped(final ServerStoppedEvent event)
    {
        // Reset this.
        PokecubeSerializer.clearInstance();
        EventsHandler.RUNNING = false;
        CustomJigsawPiece.sent_events.clear();
        EventsHandler.scheduledTasks.clear();
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        PokecubeCore.LOGGER.info("Registering Commands");
        CommandConfigs.register(PokecubeCore.getConfig(), event.getDispatcher(), "pokesettings");
        CommandManager.register(event.getDispatcher());
        NBTEdit.registerCommands(event);
    }

    private static void onChangeDimension(final EntityTravelToDimensionEvent evt)
    {
        final Entity entity = evt.getEntity();
        final Level tworld = entity.getLevel();
        if (tworld.isClientSide || !(tworld instanceof ServerLevel)) return;
        // Recall the pokemobs if the player changes dimension.
        final ServerLevel world = (ServerLevel) tworld;
        final ResourceKey<Level> newDim = evt.getDimension();
        if (newDim == world.dimension() || entity.getPersistentData().contains("thutcore:dimtp")) return;
        final List<Entity> pokemobs = new ArrayList<>(
                world.getEntities(EntityTypeTest.forClass(Entity.class), e -> EventsHandler.validFollowing(entity, e)));
        PCEventsHandler.recallAll(pokemobs, false);
    }

    private static void onPlayerTick(final PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) event.player;
            final IPokemob ridden = CapabilityPokemob.getPokemobFor(player.getVehicle());
            if (ridden != null && (ridden.floats() || ridden.flys()))
            {
                player.connection.aboveGroundTickCount = 0;
                player.connection.aboveGroundVehicleTickCount = 0;
            }
        }
    }

    private static void onWorldSave(final WorldEvent.Save evt)
    {
        if (evt.getWorld().isClientSide()) return;
        if (!(evt.getWorld() instanceof ServerLevel)) return;
        // Save the pokecube data whenever the overworld saves.
        if (((Level) evt.getWorld()).dimension().equals(Level.OVERWORLD))
        {
            final long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            final double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }

    private static void onTagsUpdated(final TagsUpdatedEvent event)
    {
        // Database.onResourcesReloaded();
    }

    private static void onResourcesReloaded(final AddReloadListenerEvent event)
    {
        event.addListener(Database.ReloadListener.INSTANCE);
    }

    public static void sendInitInfo(final ServerPlayer player)
    {
        PacketDataSync.syncData(player, "pokecube-data");
        PacketDataSync.syncData(player, "pokecube-stats");
        PacketPokedex.sendLoginPacket(player);
        if (PokecubeCore.getConfig().guiOnLogin) new ChooseFirst(player);
        else if (!PokecubeSerializer.getInstance().hasStarter(player) && PokecubeCore.getConfig().msgAboutProfessor)
            player.sendMessage(new TranslatableComponent("pokecube.login.find_prof_or_config"), Util.NIL_UUID);
    }

    public static void recallAllPokemobs(final LivingEntity user)
    {
        if (!user.isEffectiveAi()) return;
        final List<Entity> pokemobs = PokemobTracker.getMobs(user,
                e -> EventsHandler.validRecall(user, e, null, false));
        PCEventsHandler.recallAll(pokemobs, true);
    }

    public static void recallAllPokemobsExcluding(final ServerPlayer player, final IPokemob excluded,
            final boolean includeStaying)
    {
        final List<Entity> pokemobs = PokemobTracker.getMobs(player,
                e -> EventsHandler.validRecall(player, e, excluded, includeStaying));
        PCEventsHandler.recallAll(pokemobs, true);
    }

    /**
     * Checks if Entity is owned by owner, it checks if it is a pokemob, or a
     * filled pokecube. If it is a pokemob, it also confirms that it is not set
     * to stay.
     *
     * @param owner
     * @param toRecall
     * @return
     */
    public static boolean validFollowing(final Entity owner, final Entity toRecall)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToWorld()) return false;
        final IPokemob mob = CapabilityPokemob.getPokemobFor(toRecall);
        if (mob == null)
        {
            if (toRecall instanceof EntityPokecube)
            {
                final EntityPokecube cube = (EntityPokecube) toRecall;
                if (!cube.getItem().isEmpty())
                {
                    final String name = PokecubeManager.getOwner(cube.getItem());
                    if (name != null && name.equals(owner.getStringUUID())) return true;
                }
            }
            return false;
        }
        if (!mob.getGeneralState(GeneralStates.TAMED)) return false;
        if (mob.getGeneralState(GeneralStates.STAYING)) return false;
        return mob.getOwner() == owner;
    }

    public static boolean validRecall(final LivingEntity player, final Entity toRecall, final IPokemob excluded,
            final boolean includeStay)
    {
        return EventsHandler.validRecall(player, toRecall, excluded, true, includeStay);
    }

    public static boolean validRecall(final LivingEntity player, final Entity toRecall, final IPokemob excluded,
            final boolean includeCubes, final boolean includeStay)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToWorld()) return false;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(toRecall);
        if (pokemob != null)
        {
            if (pokemob != excluded && pokemob.getOwner() == player
                    && (includeStay || !pokemob.getGeneralState(GeneralStates.STAYING)))
                return true;
        }
        else if (toRecall instanceof EntityPokecube)
        {
            final EntityPokecube mob = (EntityPokecube) toRecall;
            if (!mob.getItem().isEmpty())
            {
                final String name = PokecubeManager.getOwner(mob.getItem());
                if (name != null && name.equals(player.getStringUUID())) return true;
            }
        }
        return false;
    }
}
