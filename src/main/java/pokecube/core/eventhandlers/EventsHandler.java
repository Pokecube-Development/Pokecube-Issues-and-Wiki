package pokecube.core.eventhandlers;

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

import net.minecraft.core.BlockPos;
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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.CapabilityAffected;
import pokecube.api.entity.CapabilityAffected.DefaultAffected;
import pokecube.api.entity.CapabilityInhabitable.SaveableHabitatProvider;
import pokecube.api.entity.CapabilityInhabitor.InhabitorProvider;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.CustomInteractEvent;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.compat.wearables.sided.Common;
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
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager.GeneticsProvider;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.impl.capabilities.TextureableCaps;
import pokecube.core.impl.capabilities.TextureableCaps.NPCCap;
import pokecube.core.inventory.pc.PCWrapper;
import pokecube.core.inventory.tms.TMInventory;
import pokecube.core.inventory.trade.TradeInventory;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPokecube;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokemobTracker;
import pokecube.nbtedit.NBTEdit;
import pokecube.world.gen.structures.pool_elements.ExpandedJigsawPiece;
import thut.api.Tracker;
import thut.api.entity.CopyCaps;
import thut.api.entity.ShearableCaps;
import thut.api.inventory.InvHelper.ItemCap;
import thut.api.item.ItemList;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.lib.RegHelper;
import thut.lib.TComponent;

public class EventsHandler
{
    public static class ChooseFirst
    {
        final Player player;

        final long start;

        public ChooseFirst(final Player player)
        {
            this.player = player;
            this.start = player.level().getGameTime();
            if (!SpawnHandler.canSpawnInWorld(player.level(), false)) return;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(final TickEvent.PlayerTickEvent event)
        {
            if (event.player.level.getGameTime() - this.start < 20) return;
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
            final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
            // This includes players, armour stands, effects, etc
            final boolean noSpawnBlock = !(e instanceof Mob);
            // We don't want to block something if we have made it a pokemob
            final boolean isPokemob = pokemob != null;
            // Simple check for vanillaness, via the entity type registry name
            boolean isVanilla = RegHelper.getKey(e).getNamespace().equals("minecraft");

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
        if (!(world instanceof ServerLevel level)) return;

        // If we are tickingEntities, do not do this, as it can cause
        // concurrent modification exceptions.
        if (immedateIfPossible && !level.isHandlingTick() && level.getServer().isSameThread())
        {
            // This will either run it now, or run it on main thread soon
            level.getServer().execute(() -> task.run(level));
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

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double candyChance = 4.5;

    public static double juiceChance = 3.5;

    public static void register()
    {
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
        // This handles one part of preventing natural spawns for the mobs
        // disabled via configs
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
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(player, evt.getHand(), evt.getTarget());
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
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
        if (!evt.isCanceled())
        {
            final CustomInteractEvent event = new CustomInteractEvent(player, evt.getHand(), evt.getTarget());
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
        if (!(evt.getEntity() instanceof ServerPlayer player)
                || !(evt.getEntity().level() instanceof ServerLevel level))
            return;
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }

        // These two are fine for production environments, as players can use
        // them to try to figure out what might be spawning where they currently
        // are.
        boolean isSpawnPresetDebug = evt.getItemStack().getDisplayName().getString().contains("spawn_preset_debug");
        boolean isEvoLocDebug = evt.getItemStack().getDisplayName().getString().contains("evolution_location_debug");
        boolean isSubbiomeDebug = evt.getItemStack().getDisplayName().getString().contains("subbiome_debug");
        boolean isStructureDebug = evt.getItemStack().getDisplayName().getString().contains("structure_debug");

        Vector3 v = new Vector3().set(player);
        if (isSpawnPresetDebug)
        {
            SpawnCheck check = new SpawnCheck(v, level, evt.getPos(), evt.getLevel().getBlockState(evt.getPos()));
            List<String> valid = Lists.newArrayList();

            for (Entry<String, SpawnRule> entry : SpawnBiomeMatcher.PRESETS.entrySet())
            {
                SpawnBiomeMatcher m = SpawnBiomeMatcher.get(entry.getValue());
                m.reset();
                if (m.matches(check)) valid.add(entry.getKey());
            }
            if (!valid.isEmpty())
            {
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("Spawn Presets valid for here:"));
                for (String s : valid) thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(s));
            }
            else thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.literal("No matching presets for this location"));
        }
        if (isEvoLocDebug)
        {
            for (var entry : Database.getSortedFormes())
            {
                for (var d : entry.evolutions)
                {
                    if (d.matcher != null)
                    {
                        d.matcher.reset();
                        d.matcher.parse();
                        if (!d.matcher._valid)
                        {
                            PokecubeAPI.LOGGER
                                    .error("Invalid! " + entry + " " + d.evolution + " " + d.matcher.spawnRule);
                        }
                    }
                }
            }
        }
        if (isSubbiomeDebug)
        {
            TerrainSegment seg = TerrainManager.getInstance().getTerrainForEntity(player);
            BiomeType type = seg.getBiome(v);
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal("SubBiome Type: " + type.name));
        }
        if (isStructureDebug) 
        {

            final Set<INamedStructure> set = StructureManager.getFor(level.dimension(), v.getPos(), true);
            System.out.println(set);
        }
    }

    private static void onEmptyRightClick(final PlayerInteractEvent.RightClickEmpty evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;
        final String ID = "__poke_interact__";
        final long time = player.getPersistentData().getLong(ID);
        if (time == Tracker.instance().getTick())
        {
            if (player.getPersistentData().getLong("__poke_int_c_") == time) evt.setCanceled(true);
            return;
        }
    }

    private static void onEntityCaps(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity living)) return;
        if (!event.getCapabilities().containsKey(EventsHandler.AFFECTEDCAP))
        {
            final DefaultAffected affected = new DefaultAffected((LivingEntity) event.getObject());
            event.addCapability(EventsHandler.AFFECTEDCAP, affected);
        }
        if (event.getObject() instanceof EntityPokemob mob
                && !event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP))
        {
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
            if (mob.getType().is(EntityTypeTags.BEEHIVE_INHABITORS))
                event.addCapability(EventsHandler.BEECAP, new InhabitorProvider(new BeeInhabitor(mob)));
            if (ItemList.is(IMoveConstants.ANTS, mob))
                event.addCapability(EventsHandler.ANTCAP, new InhabitorProvider(new AntInhabitor(mob)));
        }

        if (event.getObject() instanceof NpcMob npc)
        {
            event.addCapability(EventsHandler.TEXTURECAP, new NPCCap<>(npc, e -> e.getTex(), e -> !e.isMale()));
            IGuardAICapability.addCapability(event);
        }
    }

    private static void onItemCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        UsableItemEffects.registerCapabilities(event);
        GeneticsManager.registerCapabilities(event);
        Common.registerCapabilities(event);
        if (!MegaCapability.isStoneOrWearable(event.getObject())) return;
        final ResourceLocation key = new ResourceLocation("pokecube:megawearable");
        if (event.getCapabilities().containsKey(key)) return;
        event.addCapability(key, new MegaCapability(event.getObject()));
    }

    private static void onTileCaps(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        final ResourceLocation key = EventsHandler.INVENTORYCAP;
        if (event.getCapabilities().containsKey(key)) return;
        if (event.getObject() instanceof TMTile te) event.addCapability(key, new TMInventory(te));
        if (event.getObject() instanceof TraderTile te) event.addCapability(key, new TradeInventory(te));
        if (event.getObject() instanceof PCTile te) event.addCapability(key, new PCWrapper(te));
        if (event.getObject() instanceof NestTile te)
        {
            final ResourceLocation nestCap = new ResourceLocation("pokecube:nest");
            event.addCapability(key, new ItemCap(54, 64));
            event.addCapability(nestCap, new SaveableHabitatProvider(te));
        }
    }

    private static void onWorldCaps(final AttachCapabilitiesEvent<Level> event)
    {
        if (event.getObject() instanceof ServerLevel level && Level.OVERWORLD.equals(level.dimension()))
            PokecubeSerializer.newInstance(level);
    }

    private static void onMobJoinWorld(final EntityJoinLevelEvent evt)
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

        if (evt.getEntity() instanceof IPokemob pokemob && evt.getEntity().getPersistentData().getBoolean("onShoulder"))
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            evt.getEntity().getPersistentData().remove("onShoulder");
        }
        if (evt.getEntity() instanceof Creeper creeper)
        {
            final AvoidEntityGoal<?> avoidAI = new AvoidEntityGoal<>(creeper, EntityPokemob.class, 6.0F, 1.0D, 1.2D,
                    e -> PokemobCaps.getPokemobFor(e).isType(PokeType.getType("psychic")));
            creeper.goalSelector.addGoal(3, avoidAI);
        }
    }

    private static void onCheckSpawnCheck(final MobSpawnEvent.PositionCheck event)
    {
        // Only deny them from these reasons.
        if (!(event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION
                || event.getSpawnType() == MobSpawnType.STRUCTURE))
            return;

        if (EventsHandler.MONSTERMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateMonsters)
            event.setResult(Result.DENY);
        if (EventsHandler.ANIMALMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateAnimals)
            event.setResult(Result.DENY);
    }

    private static void onPlayerWakeUp(final PlayerWakeUpEvent evt)
    {
        if (!PokecubeCore.getConfig().bedsHeal || !(evt.getEntity() instanceof ServerPlayer player)) return;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
        {
            final ItemStack stack = player.getInventory().getItem(i);
            if (PokecubeManager.isFilled(stack)) PokecubeManager.heal(stack, player.level());
        }
    }

    private static void onLivingUpdate(final LivingTickEvent evt)
    {
        final IPokemob poke = PokemobCaps.getPokemobFor(evt.getEntity());
        if (poke != null)
        {
            if (PokecubeCore.getConfig().pokemobsAreAllFrozen)
            {
                evt.setCanceled(true);
                return;
            }
            poke.onTick();
        }

        if (evt.getEntity().level().isClientSide || !evt.getEntity().isAlive()) return;
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
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;
        EventsHandler.sendInitInfo(player);

        if (PokecubeCore.getConfig().spawnInBuilding)
        {
            boolean ready = PokecubeSerializer.getInstance().hasPlacedSpawn();
            ready = ready && !PokecubePlayerDataHandler.getCustomDataTag(player).contains("_spawned_");
            if (ready)
            {
                final ServerLevel world = player.getServer().getLevel(Level.OVERWORLD);
                BlockPos worldSpawn = world.getSharedSpawnPos();
                player.teleportTo(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
                PokecubePlayerDataHandler.getCustomDataTag(player).putBoolean("_spawned_", true);
                PokecubePlayerDataHandler.saveCustomData(player);
            }
        }
    }

    private static void onStartTracking(final StartTracking event)
    {
        // Check if the pokecube is loot, and is not collectable by the player,
        // if this is the case, it should be set invisible.
        if (event.getTarget() instanceof EntityPokecube pokecube && event.getEntity() instanceof ServerPlayer player)
        {
            if (pokecube.isLoot && pokecube.cannotCollect(event.getEntity()))
                PacketPokecube.sendMessage(player, pokecube.getId(), Tracker.instance().getTick() + pokecube.resetTime);
        }
        if (event.getTarget() instanceof ServerPlayer player1 && event.getEntity() instanceof ServerPlayer player2)
        {
            final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player1);
            final PlayerData data = manager.getData("pokecube-stats");
            PacketDataSync.syncData(data, player1.getUUID(), player2, false);
        }
    }

    private static void onServerStarting(final ServerStartingEvent event)
    {
        PokecubeAPI.logInfo("Server Starting");
        PokecubeItems.init(event.getServer());
        EventsHandler.RUNNING = true;

    }

    private static void onServerStopped(final ServerStoppedEvent event)
    {
        // Reset this.
        PokecubeSerializer.clearInstance();
        EventsHandler.RUNNING = false;
        ExpandedJigsawPiece.sent_events.clear();
        EventsHandler.scheduledTasks.clear();
    }

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        PokecubeAPI.logInfo("Registering Commands");
        CommandConfigs.register(PokecubeCore.getConfig(), event.getDispatcher(), "pokesettings");
        CommandManager.register(event.getDispatcher());
        NBTEdit.registerCommands(event);
    }

    private static void onChangeDimension(final EntityTravelToDimensionEvent evt)
    {
        final Entity entity = evt.getEntity();
        final Level tworld = entity.level();
        if (tworld.isClientSide || !(tworld instanceof ServerLevel world)) return;
        // Recall the pokemobs if the player changes dimension.
        final ResourceKey<Level> newDim = evt.getDimension();
        if (newDim == world.dimension() || entity.getPersistentData().contains("thutcore:dimtp")) return;
        final List<Entity> pokemobs = new ArrayList<>(world.getEntities(EntityTypeTest.forClass(Entity.class),
                e -> EventsHandler.shouldRecallOnChangeDimension(entity, e)));
        PCEventsHandler.recallAll(pokemobs, false);
    }

    private static void onPlayerTick(final PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayer player)
        {
            final IPokemob ridden = PokemobCaps.getPokemobFor(player.getVehicle());
            if (ridden != null && (ridden.floats() || ridden.flys()))
            {
                player.connection.aboveGroundTickCount = 0;
                player.connection.aboveGroundVehicleTickCount = 0;
            }
        }
    }

    private static void onWorldSave(final LevelEvent.Save evt)
    {
        if (evt.getLevel().isClientSide()) return;
        if (!(evt.getLevel() instanceof ServerLevel level)) return;
        // Save the pokecube data whenever the overworld saves.
        if (level.dimension().equals(Level.OVERWORLD))
        {
            final long time = System.nanoTime();
            PokecubeSerializer.getInstance().save();
            final double dt = (System.nanoTime() - time) / 1000000d;
            if (dt > 20) System.err.println("Took " + dt + "ms to save pokecube data");
        }
    }

    private static void onTagsUpdated(final TagsUpdatedEvent event)
    {
        // Final setup of tag required things
        for (final PokedexEntry entry : Database.getSortedFormes()) entry.postTagsReloaded();
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
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.login.find_prof_or_config"));
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
    public static boolean shouldRecallOnChangeDimension(final Entity owner, final Entity toRecall)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToWorld()) return false;
        final IPokemob mob = PokemobCaps.getPokemobFor(toRecall);
        if (mob == null)
        {
            if (toRecall instanceof EntityPokecube cube && !cube.getItem().isEmpty())
            {
                final String name = PokecubeManager.getOwner(cube.getItem());
                if (name != null && name.equals(owner.getStringUUID())) return true;
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
        final IPokemob pokemob = PokemobCaps.getPokemobFor(toRecall);
        if (pokemob != null)
        {
            if (pokemob != excluded && pokemob.getOwner() == player
                    && (includeStay || !pokemob.getGeneralState(GeneralStates.STAYING)))
                return true;
        }
        else if (toRecall instanceof EntityPokecube mob && !mob.getItem().isEmpty())
        {
            final String name = PokecubeManager.getOwner(mob.getItem());
            if (name != null && name.equals(player.getStringUUID())) return true;
        }
        return false;
    }
}
