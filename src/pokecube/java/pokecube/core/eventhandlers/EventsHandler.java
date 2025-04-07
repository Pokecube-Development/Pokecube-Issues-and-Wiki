package pokecube.core.eventhandlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityEvent.EntityConstructing;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.FaintEvent;
import pokecube.api.moves.Battle;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.IRunnable;
import pokecube.core.commands.CommandManager;
import pokecube.core.database.Database;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.impl.capabilities.TextureableCaps;
import pokecube.core.impl.capabilities.TextureableCaps.NPCCap;
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
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.attachments.Ownable;
import thut.api.attachments.Shearable;
import thut.api.attachments.Shearable.IShearableSerializable;
import thut.api.data.HolderProvider;
import thut.api.entity.IMobTexturable;
import thut.api.entity.event.LevelEntityEvent;
import thut.api.item.ItemList;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.ThutCore;
import thut.core.common.commands.CommandConfigs;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;
import thut.lib.RegHelper;
import thut.lib.TComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

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
            ThutCore.FORGE_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(final PlayerTickEvent.Pre event)
        {
            var player = event.getEntity();
            if (player.level.getGameTime() - this.start < 20) return;
            if (player == this.player)
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
                PokecubeCore.packets.sendTo(packet, (ServerPlayer) player);
                ThutCore.FORGE_BUS.unregister(this);
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

    public static final ResourceLocation NOGENESTAG = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID,
            "no_genetics");

    static double max = 0;

    /**
     * This returns true if the given entity is not a vanilla entity, or is not a mob-like entity, it returns false for
     * modded mobs, as well as players, armour stands, boats, etc
     */
    private static Predicate<Entity> NOTVANILLAANIMALORMOB;

    /**
     * This returns true if the given entity is ia "vanilla" monster, but not a boss
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
            boolean canSpawn = noSpawnBlock || isDragon || isNpc || isPokemob || !isVanilla;
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
     * If this is false, then the effects occur on every valid tick. Only set this false if you have something else to
     * manage the TerrainEffectEvent and the OngoingTickEvent!
     */
    public static boolean COOLDOWN_BASED = true;

    // 4 = 1 per 10mins, 2 = 1 per 10s, 5 = 1 per 48 hours
    public static double candyChance = 4.5;

    public static double juiceChance = 3.5;

    public static void register()
    {
        // This handles preventing blacklisted mobs from joining a world, for
        // the disable<thing> configs. It also adds the creepers avoid psychic
        // types AI, and does some cleanup for shoulder mobs.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onMobJoinWorld);
        // This handles one part of preventing natural spawns for the mobs
        // disabled via configs
        ThutCore.FORGE_BUS.addListener(EventsHandler::onCheckSpawnCheck);

        // Here we handle bed healing if enabled in configs
        ThutCore.FORGE_BUS.addListener(EventsHandler::onPlayerWakeUp);
        // This ticks ongoing effects (like burn, poison, etc)
        ThutCore.FORGE_BUS.addListener(EventsHandler::onLivingUpdate);
        // This synchronizes stats and data for the player, and sends the
        // GuiOnLogin if enabled and required.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onPlayerLogin);
        // This one handles not sending "hidden" pokecubes to the player, for
        // loot-pokecubes which the player is not allowed to pick up yet.
        // This also handles syncing player data over to other players, for
        // stats information in pokewatch.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onStartTracking);

        // Does some debug output in pokecube tags if enabled.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onServerStarting);
        // Cleans up some things for when server next starts.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onServerStopped);
        // Initialises or reloads some datapack dependent values in Database
        ThutCore.FORGE_BUS.addListener(EventsHandler::onResourcesReloaded);
        // This does similar to the above, but on dedicated servers only.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onTagsUpdated);
        // Registers our commands.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onCommandRegister);

        ThutCore.FORGE_BUS.addListener(EventsHandler::onMobSize);
        ThutCore.FORGE_BUS.addListener(EventsHandler::entityConstruct);

        // This attempts to recall the mobs following the player when they
        // change dimension.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onChangeDimension);
        // This handles preventing players from being kicked for flying, if they
        // are riding a pokemob that can fly.
        ThutCore.FORGE_BUS.addListener(EventsHandler::onPlayerTick);
        // This saves the pokecube Serializer
        ThutCore.FORGE_BUS.addListener(EventsHandler::onWorldSave);
        // This sets up the pokecube Serializer
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGHEST, EventsHandler::onServerAboutStart);

        // These are for handling interaction events, etc
        ThutCore.FORGE_BUS.addListener(EventsHandler::onItemRightClick);
        ThutCore.FORGE_BUS.addListener(EventsHandler::onVanillaEntityEvent);

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
        ThutCore.FORGE_BUS.addListener(PokemobTracker::onWorldLoad);

        ResourceLocation TEXTURECAPP = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "textured_pokemob");
        ResourceLocation TEXTURECAPN = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "textured_npc");
        IMobTexturable.Defaults._REGISTRY.register(new HolderProvider.Provider<IMobTexturable>()
        {

            @Override
            public IMobTexturable apply(IAttachmentHolder t)
            {
                if (t instanceof EntityPokemob mob) return new TextureableCaps.PokemobCap(mob);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return TEXTURECAPP;
            }

            @Override
            public int getPriority()
            {
                return 10;
            }
        });
        IMobTexturable.Defaults._REGISTRY.register(new HolderProvider.Provider<IMobTexturable>()
        {

            @Override
            public IMobTexturable apply(IAttachmentHolder t)
            {
                if (t instanceof NpcMob npc) return new NPCCap<>(npc, NpcMob::getTex, e -> !e.isMale());
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return TEXTURECAPN;
            }

            @Override
            public int getPriority()
            {
                return 90;
            }
        });
        ResourceLocation SHEARABLE = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "pokemob");
        Shearable._REGISTRY.register(new HolderProvider.Provider<Shearable.IShearableSerializable>()
        {

            @Override
            public IShearableSerializable apply(IAttachmentHolder t)
            {
                if (t instanceof EntityPokemob mob) return new Shearable.WrapperImpl(mob.pokemobCap);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return SHEARABLE;
            }
        });
    }

    private static void onServerAboutStart(ServerAboutToStartEvent event)
    {
        PokecubeSerializer.newInstance();
    }

    private static void onItemRightClick(final PlayerInteractEvent.RightClickItem evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player) || !(evt.getEntity()
                .level() instanceof ServerLevel level)) return;

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
            SpawnCheck check = new SpawnCheck(v, level);
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

    private static void entityConstruct(EntityConstructing event)
    {
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        // Calling get here sets it up.
        if (!ItemList.is(NOGENESTAG, living)) ThutCaps.getGenetics(living);

        if (living instanceof EntityPokemob mob)
        {
            // Init data, genes, copy
            var data = ThutCaps.getDataSync(living);
            var genes = ThutCaps.getGenetics(living);
            var copy = ThutCaps.getCopyMob(living);
            var ownable = ThutCaps.getOwnable(living);

            // Ensure this is setup.
            if (ownable == null)
            {
                living.getData(Ownable.TYPE.get());
            }

            // Texture
            living.getData(IMobTexturable.Defaults.TYPE);
            var pokemob = PokemobCaps.getPokemobFor(living);
            if (pokemob == null)
            {
                pokemob = new DefaultPokemob(mob);
                mob.setData(PokemobCaps.POKEMOB, pokemob);
            }
            if (pokemob instanceof DefaultPokemob def)
            {
                def.setDataSync(data);
                def.setDataSync(data);
                def.setGenes(genes);
                def.setCopy(copy);
                genes.addChangeListener(def);
            }
        }
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
        if (!evt.getEntity().isAddedToLevel()) evt.getEntity().onAddedToLevel();

        if (evt.getEntity() instanceof IPokemob pokemob && evt.getEntity().getPersistentData()
                .getBoolean(TagNames.ON_SHOULDER))
        {
            pokemob.setLogicState(LogicStates.SITTING, false);
            evt.getEntity().getPersistentData().remove(TagNames.ON_SHOULDER);
        }
        if (evt.getEntity() instanceof Creeper creeper)
        {
            final AvoidEntityGoal<?> avoidAI = new AvoidEntityGoal<>(creeper, EntityPokemob.class, 6.0F, 1.0D, 1.2D,
                    e -> PokemobCaps.getPokemobFor(e).isType(PokeType.getType("psychic")));
            creeper.goalSelector.addGoal(3, avoidAI);
        }
    }

    public static void playPoofParticles(LivingEntity living)
    {
        for (int k = 0; k < 20; ++k)
        {
            final double d2 = living.getRandom().nextGaussian() * 0.02D;
            final double d0 = living.getRandom().nextGaussian() * 0.02D;
            final double d1 = living.getRandom().nextGaussian() * 0.02D;
            living.level.addParticle(ParticleTypes.POOF,
                    living.getX() + living.getRandom().nextFloat() * living.getBbWidth() * 2.0F - living.getBbWidth(),
                    living.getY() + living.getRandom().nextFloat() * living.getBbHeight(),
                    living.getZ() + living.getRandom().nextFloat() * living.getBbWidth() * 2.0F - living.getBbWidth(),
                    d2, d0, d1);
        }
    }

    public static void preTickLivingDeath(LivingEntity living)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        if (pokemob == null) return;
        long time = Tracker.instance().getTick();
        if (pokemob.getDeathTime() <= 0) pokemob.setDeathTime(time);

        int deadTimer = PokecubeCore.getConfig().deadDespawnTimer;
        int reviveTimer = PokecubeCore.getConfig().deadReviveTimer;
        int deathTime = (int) (time - pokemob.getDeathTime());

        final boolean isTamed = pokemob.getOwnerId() != null;
        boolean fullHeal = !isTamed;

        boolean poofDisabled = false;
        boolean noPoof =
                living.getPersistentData().getBoolean(TagNames.NOPOOF) || (poofDisabled = !pokemob.isRoutineEnabled(
                        AIRoutine.POOFS));
        boolean forcePoof = living.getPersistentData().getBoolean("pokecube:force_poof");
        if (noPoof)
        {
            fullHeal = true;
            if (poofDisabled) reviveTimer = PokecubeCore.getConfig().noPoofReviveTimer;
        }
        else if (isTamed && !forcePoof)
        {
            final FaintEvent event = new FaintEvent(pokemob);
            PokecubeAPI.POKEMOB_BUS.post(event);
            TriState res = event.getResult();
            boolean despawn = isTamed
                    ? PokecubeCore.getConfig().tameDeadDespawn
                    : PokecubeCore.getConfig().wildDeadDespawn;
            despawn = res == TriState.DEFAULT ? despawn : res == TriState.TRUE;
            if (despawn)
            {
                pokemob.onRecall(true);
                living.getPersistentData().putBoolean("pokecube:force_poof", true);
            }
        }
        if (deathTime >= deadTimer && forcePoof)
        {
            living.deathTime = 19;
            if (living.level.isClientSide())
            {
                living.remove(Entity.RemovalReason.KILLED);
                playPoofParticles(living);
            }
            return;
        }
        else if (deathTime < deadTimer || noPoof)
        {
            // Pause the death timer to prevent vanilla poofing
            if (living.deathTime >= 19) living.deathTime = -2;
            if (!noPoof && deathTime >= deadTimer)
            {
                final FaintEvent event = new FaintEvent(pokemob);
                PokecubeAPI.POKEMOB_BUS.post(event);
                TriState res = event.getResult();
                boolean despawn = isTamed
                        ? PokecubeCore.getConfig().tameDeadDespawn
                        : PokecubeCore.getConfig().wildDeadDespawn;
                despawn = res == TriState.DEFAULT ? despawn : res == TriState.TRUE;
                if (despawn) pokemob.onRecall(true);
            }
        }
        // Otherwise set death timer back to 19 to enable vanilla poofing
        else if (living.deathTime >= 19) living.deathTime = 19;

        if (deathTime >= reviveTimer && reviveTimer > 0)
        {
            if (forcePoof)
            {
                living.deathTime = 19;
                return;
            }
            if (living.getPersistentData().contains("pokecube:raid_boss"))
            {
                pokemob.onRecall(true);
                Battle battle = Battle.getBattle(living);
                if (battle != null) battle.removeFromBattle(living);
                if (living.level.isClientSide()) playPoofParticles(living);
            }
            else
            {
                pokemob.revive(fullHeal);
                // If we revive naturally, we remove this tag, it only applies
                // for
                // forced revivals
                living.getPersistentData().remove(TagNames.REVIVED);
            }
        }
    }

    public static void postTickLivingDeath(LivingEntity living)
    {
        // Between pre and post there is a deathTime++, so it we set to -2
        // before, it should be -1 now, so set timer to 20.
        if (living.deathTime == -1)
        {
            living.deathTime = 20;
        }
    }

    private static void onVanillaEntityEvent(final LevelEntityEvent event)
    {
        if (event.getKey() == 60 && !event.getEntity().level.isClientSide())
        {
            IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
            if (pokemob == null || pokemob.getOwnerId() == null) return;
            pokemob.onRecall(true);
        }
    }

    private static void onCheckSpawnCheck(final PositionCheck event)
    {
        // Only deny them from these reasons.
        if (!(event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION
                || event.getSpawnType() == MobSpawnType.STRUCTURE)) return;

        if (EventsHandler.MONSTERMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateMonsters)
            event.setResult(PositionCheck.Result.FAIL);
        if (EventsHandler.ANIMALMATCHER.test(event.getEntity()) && PokecubeCore.getConfig().deactivateAnimals)
            event.setResult(PositionCheck.Result.FAIL);
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

    private static void onLivingUpdate(final EntityTickEvent.Pre evt)
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
        final IOngoingAffected affected = PokemobCaps.getAffected(evt.getEntity());
        if (affected != null) affected.tick();
        if (evt.getEntity().tickCount % tick == 0 || !EventsHandler.COOLDOWN_BASED)
        {
            affected.tickDamage();
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

    private static void onMobSize(EntityEvent.Size event)
    {
        // Attributes can be null when this is called in the initial set for the
        // constructor of the Entity itself.
        if (event.getEntity() instanceof LivingEntity living)
        {
            float s = (float) SharedAttributes.getScale(living);
            event.setNewSize(event.getNewSize().withEyeHeight(event.getNewSize().eyeHeight() * s));
        }
    }

    private static void onPlayerTick(final PlayerTickEvent.Pre event)
    {
        var p = event.getEntity();
        if (p instanceof ServerPlayer player)
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
     * Checks if Entity is owned by owner, it checks if it is a pokemob, or a filled pokecube. If it is a pokemob, it
     * also confirms that it is not set to stay.
     *
     * @param owner
     * @param toRecall
     * @return
     */
    public static boolean shouldRecallOnChangeDimension(final Entity owner, final Entity toRecall)
    {
        if (!toRecall.isAlive()) return false;
        if (!toRecall.isAddedToLevel()) return false;
        final IPokemob mob = PokemobCaps.getPokemobFor(toRecall);
        if (mob == null)
        {
            if (toRecall instanceof EntityPokecube cube && !cube.getItem().isEmpty())
            {
                final String name = PokecubeManager.getOwner(cube.getItem(), toRecall.level());
                return name != null && name.equals(owner.getStringUUID());
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
        if (!toRecall.isAddedToLevel()) return false;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(toRecall);
        if (pokemob != null)
        {
            return pokemob != excluded && pokemob.getOwner() == player && (includeStay || !pokemob.getGeneralState(
                    GeneralStates.STAYING));
        }
        else if (toRecall instanceof EntityPokecube mob && !mob.getItem().isEmpty())
        {
            final String name = PokecubeManager.getOwner(mob.getItem(), toRecall.level());
            return name != null && name.equals(player.getStringUUID());
        }
        return false;
    }
}
