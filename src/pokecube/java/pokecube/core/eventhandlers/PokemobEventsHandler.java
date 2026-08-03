package pokecube.core.eventhandlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StopTracking;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.IInhabitor;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.Nature;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.api.events.pokemobs.InteractEvent;
import pokecube.api.events.pokemobs.ai.BrainInitEvent;
import pokecube.api.events.pokemobs.combat.KillEvent;
import pokecube.api.items.IPokemobUseable;
import pokecube.api.moves.Battle;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.tasks.combat.management.FindTargetsTask;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.init.Config;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.moves.damage.sources.PokecubeDamageSources;
import pokecube.core.moves.damage.sources.PokemobDamageSource;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketSyncNewMoves;
import pokecube.core.utils.AITools;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokemobTracker;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Ownable;
import thut.api.entity.ai.RootTask;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.event.CopyUpdateEvent;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.item.ItemList;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.api.world.WorldTickManager;
import thut.api.world.WorldTickManager.DelayedTask;
import thut.core.common.ThutCore;
import thut.core.common.network.SyncAttachments;
import thut.lib.RegHelper;
import thut.lib.TComponent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PokemobEventsHandler
{
    public static class EvoTicker implements Runnable
    {
        final LivingEntity thisEntity;
        final LivingEntity evolution;
        final Level world;
        boolean done = false;

        public EvoTicker(final LivingEntity thisEntity, final LivingEntity evolution)
        {
            this.thisEntity = thisEntity;
            this.evolution = evolution;
            this.world = thisEntity.level();
        }

        public void init()
        {
            DelayedTask run = new DelayedTask(0, this);
            WorldTickManager.scheduleTask(this.world.dimension(), run);
        }

        @Override
        public void run()
        {
            if (this.done) return;
            this.done = true;
            final ServerLevel world = (ServerLevel) this.thisEntity.level();
            final IPokemob oldPokemob = PokemobCaps.getPokemobFor(this.thisEntity);

            if (this.thisEntity != this.evolution)
            {
                // Remount riders on the new mob.
                final List<Entity> riders = this.thisEntity.getPassengers();
                for (final Entity e : riders) e.stopRiding();
                for (final Entity e : riders) e.startRiding(this.evolution, true);

                // remove the IPokemob, then kill it.
                if (oldPokemob != null) thisEntity.removeData(PokemobCaps.POKEMOB);
                // Remove old mob
                this.thisEntity.discard();
                // Add new mob
                if (!this.evolution.isAlive()) this.evolution.revive();
                this.evolution.getPersistentData().remove(TagNames.REMOVED);

                this.evolution.setUUID(this.thisEntity.getUUID());
                this.evolution.level().addFreshEntity(this.evolution);

                this.evolution.refreshDimensions();
                final AABB oldBox = this.thisEntity.getBoundingBox();
                final AABB newBox = this.evolution.getBoundingBox();

                // Take the larger of the boxes, collide off that.
                final AABB biggerBox = oldBox.minmax(newBox);

                final List<VoxelShape> hits = Lists.newArrayList();
                // Find all voxel shapes in the area
                BlockPos.betweenClosedStream(biggerBox).forEach(pos -> {
                    final BlockState state = world.getBlockState(pos);
                    final VoxelShape shape = state.getCollisionShape(world, pos);
                    if (!shape.isEmpty()) hits.add(shape.move(pos.getX(), pos.getY(), pos.getZ()));
                });

                // If there were any voxel shapes, then check if we need to
                // collidedw
                if (!hits.isEmpty())
                {
                    VoxelShape total = Shapes.empty();
                    // Merge the found shapes into a single one
                    for (final VoxelShape s : hits) total = Shapes.joinUnoptimized(total, s, BooleanOp.OR);
                    final List<AABB> aabbs = Lists.newArrayList();
                    // Convert to colliding AABBs
                    BlockEntityUpdater.fill(aabbs, biggerBox, total);
                    // Push off the AABBS if needed
                    final boolean col = BlockEntityUpdater.applyEntityCollision(this.evolution, biggerBox, aabbs,
                            Vec3.ZERO);

                    // This gives us an indication if if we did actually
                    // collide, if this occured, then we need to do some extra
                    // processing to make sure that we fit properly
                    if (col)
                    {
                        Vector3 v = new Vector3().set(this.evolution);
                        v = SendOutManager.getFreeSpot(this.evolution, world, v, false);
                        this.evolution.refreshDimensions();
                        if (v != null) v.moveEntity(this.evolution);
                    }
                }
            }
            SyncAttachments.syncChange(this.evolution, SyncAttachments.SYNCED);
        }

        public static void scheduleEvolve(final LivingEntity thisEntity, final LivingEntity evolution,
                final boolean immediate)
        {
            if (!(thisEntity.level instanceof ServerLevel)) return;
            final EvoTicker ticker = new EvoTicker(thisEntity, evolution);
            if (!immediate) ticker.init();
            else ticker.run();
        }
    }

    /** Simlar to EvoTicker, but for more general form changing. */
    public static class MegaEvoTicker implements Runnable
    {
        public static void scheduleChange(int delay, PokedexEntry mega, IPokemob evolver, Component message,
                Runnable pre, Runnable post)
        {
            final Entity mob = evolver.getEntity();
            if (!(mob.level instanceof ServerLevel level)) return;

            DelayedTask preRun = new DelayedTask(0, pre);
            long tick = Tracker.instance().getTick() + delay;
            DelayedTask run = new DelayedTask(tick, new MegaEvoTicker(mega, evolver, message));
            DelayedTask postRun = new DelayedTask(tick + 1, post);

            WorldTickManager.scheduleTask(level.dimension(), preRun);
            WorldTickManager.scheduleTask(level.dimension(), run);
            WorldTickManager.scheduleTask(level.dimension(), postRun);
        }

        public static void scheduleEvolve(PokedexEntry mega, final IPokemob evolver, Component message)
        {
            scheduleEvolve(PokecubeCore.getConfig().evolutionTicks, mega, evolver, message);
        }

        public static void scheduleEvolve(int delay, PokedexEntry mega, final IPokemob evolver, Component message)
        {
            scheduleChange(delay, mega, evolver, message, () -> {
                // Flag as evolving
                evolver.setGeneralState(GeneralStates.EVOLVING, true);
                evolver.setGeneralState(GeneralStates.EXITINGCUBE, false);
                evolver.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                evolver.setEvolutionStack(PokecubeItems.getStack(ICanEvolve.EVERSTONE));
                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Pre(evolver));
            }, () -> {
                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(evolver));
                evolver.setGeneralState(GeneralStates.EVOLVING, false);
                evolver.setEvolutionStack(ItemStack.EMPTY);
            });
        }

        public static void scheduleRevert(PokedexEntry mega, final IPokemob evolver, Component message)
        {
            scheduleRevert(PokecubeCore.getConfig().evolutionTicks, mega, evolver, message);
        }

        public static void scheduleRevert(int delay, PokedexEntry mega, final IPokemob evolver, Component message)
        {
            scheduleChange(delay, mega, evolver, message, () -> {
                // Flag as evolving
                evolver.setGeneralState(GeneralStates.EVOLVING, true);
                evolver.setGeneralState(GeneralStates.EXITINGCUBE, false);
                evolver.setEvolutionTicks(PokecubeCore.getConfig().evolutionTicks + 50);
                evolver.setEvolutionStack(PokecubeItems.getStack(ICanEvolve.EVERSTONE));
                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Revert(evolver, false));
            }, () -> {
                PokecubeAPI.POKEMOB_BUS.post(new ChangeForm.Post(evolver));
                evolver.setGeneralState(GeneralStates.EVOLVING, false);
                evolver.setEvolutionStack(ItemStack.EMPTY);
            });
        }

        private final Entity mob;
        private final IPokemob pokemob;
        private final PokedexEntry mega;
        private final Component message;

        private MegaEvoTicker(final PokedexEntry mega, final IPokemob evolver, final Component message)
        {
            this.mob = evolver.getEntity();
            this.message = message;
            this.mega = mega;
            this.pokemob = evolver;
        }

        @Override
        public void run()
        {
            if (!this.mob.isAddedToLevel() || !this.mob.isAlive()) return;

            if (this.pokemob.getPokedexEntry().isMega() && this.pokemob.getOwner() instanceof ServerPlayer player)
                Triggers.MEGAEVOLVEPOKEMOB.get().trigger(player, this.pokemob);
            final int evoTicks = this.pokemob.getEvolutionTicks();
            final float hp = this.pokemob.getHealth();
            this.pokemob.changeForm(this.mega, true, false);
            this.pokemob.setHealth(hp);
            /*
             * Flag the new mob as evolving to continue the animation effects.
             */
            this.pokemob.setGeneralState(GeneralStates.EVOLVING, true);
            this.pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);

            this.pokemob.setEvolutionTicks(evoTicks);
            this.pokemob.getEntity().getPersistentData().remove(TagNames.REMOVED);
            if (this.message != null) this.pokemob.displayMessageToOwner(this.message);
        }
    }

    private static final Map<DyeColor, TagKey<Item>> DYETAGS = Maps.newHashMap();

    public static void register()
    {
        // This handles exp yield from lucky eggs and exp_shares.
        PokecubeAPI.POKEMOB_BUS.addListener(PokemobEventsHandler::onKillEvent);

        // Highest to prevent other things from trying to do things with our
        // drops if we cancel them, and to allow us to add things properly to
        // the drops. This adds the inventory items to the drops list for wild
        // pokemobs, and prevents drops for pokemobs which have been revived or
        // are tame
        ThutCore.FORGE_BUS.addListener(EventPriority.HIGHEST, PokemobEventsHandler::onLivingDrops);
        // This is done twice as some events only send one rather than the other
        // from client side!
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::processInteract);
        // This handles pokemob damage stuff. It deals with: cancelling damage
        // on invalid targets, adjusting damage amount by the scaling configs
        // and preventing player suffocating while riding a pokemob into a
        // cieling.
        // Used to reset the "NOITEMUSE" flag, which controls using healing
        // items, the capture delay, etc.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onLivingHurt);
        // check if the entity shouldn't take damage
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::checkLivingInvulnerable);

        // This ensures that the damage sources apply for the correct entity,
        // this part is for support for mods like customnpcs
        // It also handles exp gain for the pokemobs when they kill something.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onLivingDeath);
        // This deals with pokemob initialization, it initializes the AI, and
        // also does some checks for things like evolution, etc
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onJoinWorld);
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, false, PokemobEventsHandler::onJoinWorldLast);
        // This synchronizes genetics over to the clients when they start
        // tracking the mob locally.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onStartTracking);
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onStopTracking);
        // This syncs rotation of the ridden pokemob with the rider.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onWorldTick);
        // Monitors sim speed and reduces idle tick rate if lagging too much
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onServerTick);
        // This pauses the pokemobs if too close to the edge of the loaded area,
        // preventing them from chunkloading during their AI. It also then
        // ensures their UUID is correct after evolution, and then ticks the
        // "logic" section of their AI.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onMobTick);
        // Similar as the above, except only for "logic" on the copied state
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onCopyTick);
        // Called by MixinMobEntity before the first brain tick, to ensure the
        // brain has AI setup, etc.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onBrainInit);

        // This checks if we are an inhabitor of a nest, and we just left it. if
        // this is the case, then some extra processing is done related to
        // finishing tasks, etc upon leaving the nest.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onMobAddedToWorld);

        // Checks to see if we are diving mob+dive, or flyingmob+fly, and if so,
        // we speed back up breaking.
        ThutCore.FORGE_BUS.addListener(PokemobEventsHandler::onBreakSpeed);

        // If noone has modified result of a capture event pre, we deny it if
        // the mob is not alive.
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOWEST, false, PokemobEventsHandler::onCapturePre);
    }

    public static Set<ResourceKey<Level>> BEE_RELEASE_TICK = Sets.newConcurrentHashSet();

    /**
     * Here we will check if it was a bee, added from a bee-hive, and if so, we will increment the honey level as
     * needed.
     */
    private static void onMobAddedToWorld(final EntityJoinLevelEvent event)
    {
        // We only consider MobEntities
        if (!(event.getEntity() instanceof Mob mob)) return;

        IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob != null)
        {
            // Initialise these when added to world.
            pokemob.getMoveStats().reset();
            PokecubeAttributes.resetToEntry(pokemob);
        }

        if (mob.level().isClientSide()) return;

        // We only want to run this from execution thread.
        if (!mob.getServer().isSameThread() || !(mob.level instanceof ServerLevel world)) return;

        final IInhabitor inhabitor = CapHolders.getInhabitor(mob);
        // Not a valid inhabitor of things, so return.
        if (inhabitor == null) return;

        // This gets set by the mixin in pokecube.mixin.entity.BeeHiveFix
        if (BEE_RELEASE_TICK.contains(world.dimension()))
        {
            // It is called for each bee added, so remove it now.
            PokemobEventsHandler.BEE_RELEASE_TICK.remove(world.dimension());
            // Vanilla breaks things here, by deleting the memory tag in the
            // brain,
            // we need that, so restore it.
            if (mob.getPersistentData().contains("__bee_fix__"))
            {
                CompoundTag tag = mob.getPersistentData().getCompound("__bee_fix__");
                mob.getPersistentData().remove("__bee_fix__");
                CompoundTag old = mob.saveWithoutId(new CompoundTag());
                for (String s : tag.getAllKeys())
                {
                    old.put(s, tag.get(s));
                }
                mob.load(old);
                // Some cases we end up with this occuring, so let's deal with
                // it
                Entity oldEntity = world.getEntity(mob.getUUID());
                if (oldEntity != null) oldEntity.remove(RemovalReason.DISCARDED);
            }
        }
        // No Home spot, so definitely not leaving home
        if (inhabitor.getHome() == null) return;

        final GlobalPos pos = inhabitor.getHome();
        // not same dimension, not a bee leaving hive
        if (pos.dimension() != world.dimension()) return;
        // This will indicate if the tile did actually cause the spawn.
        // not loaded, definitely not a bee leaving hive
        if (!world.isPositionEntityTicking(pos.pos())) return;
        final BlockEntity tile = world.getBlockEntity(pos.pos());
        // No tile entity here? also not a bee leaving hive!
        if (tile == null) return;
        final IInhabitable habitat = CapHolders.getInhabitable(tile);
        // Not a habitat, so not going to be a bee leaving a hive
        if (habitat == null) return;

        // from here down, schedule for end of tick, incase things happen
        // related to block placement, etc
        habitat.onExitHabitat(mob);
        inhabitor.onExitHabitat();
    }

    private static void onLivingDrops(final LivingDropsEvent event)
    {
        // Once it has been revived, we don't drop anything anymore
        if (event.getEntity().getPersistentData().getBoolean(TagNames.REVIVED))
        {
            event.setCanceled(true);
            return;
        }

        // Handles the mobs dropping their inventory.
        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            if (pokemob.getOwnerId() != null)
            {
                event.setCanceled(true);
                return;
            }

            final Collection<ItemEntity> bak = Lists.newArrayList();
            event.getEntity().captureDrops(Lists.newArrayList());
            if (!pokemob.getGeneralState(GeneralStates.TAMED))
                for (int i = 0; i < pokemob.getInventory().getContainerSize(); i++)
                {
                    final ItemStack stack = pokemob.getInventory().getItem(i);
                    if (!stack.isEmpty())
                    {
                        final ItemEntity drop = event.getEntity().spawnAtLocation(stack.copy(), 0.0f);
                        if (drop != null) bak.add(drop);
                    }
                    pokemob.getInventory().setItem(i, ItemStack.EMPTY);
                }
            else event.getDrops().clear();
            if (!bak.isEmpty()) event.getDrops().addAll(bak);
        }
    }

    /**
     * This provides our default handling to prevent capturing dead pokemobs.
     */
    private static void onCapturePre(CaptureEvent.Pre event)
    {
        if (event.getResult() != TriState.FALSE) return;
        if (!event.mob.isAlive()) event.setResult(TriState.FALSE);
    }

    private static void checkLivingInvulnerable(EntityInvulnerabilityCheckEvent evt)
    {
        if (evt.getSource().getDirectEntity() == evt.getEntity()) return;
        if (evt.getSource().getEntity() == evt.getEntity()) return;
        if (evt.getSource().getDirectEntity() != null && evt.getSource().getDirectEntity()
                .isPassengerOfSameVehicle(evt.getEntity()))
        {
            evt.setInvulnerable(true);
        }

        // Only process these server side
        if (!(evt.getEntity().level() instanceof ServerLevel)) return;
        /*
         * No harming invalid targets, only apply this to pokemob related damage
         * sources
         */
        if (evt.getSource().is(PokecubeDamageSources.POKEMOB_DAMAGE) && !AITools.validCombatTargets.test(
                evt.getEntity()))
        {
            evt.setInvulnerable(true);
        }
    }

    private static void onLivingHurt(final LivingIncomingDamageEvent evt)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(evt.getSource().getDirectEntity());
        if (pokemob != null) pokemob.setCombatState(CombatStates.NOITEMUSE, false);

        pokemob = PokemobCaps.getPokemobFor(evt.getEntity());
        // check if configs say this damage can't happen
        if (pokemob != null)
        {
            if (!AITools.validToHitPokemob.test(evt.getSource()))
            {
                evt.setCanceled(true);
                return;
            }
            // Apply scaling from config for this
            if (evt.getSource().getEntity() instanceof Player)
                evt.setAmount((float) (evt.getAmount() * PokecubeCore.getConfig().playerToPokemobDamageScale));
        }
        // Some special handling for in wall stuff
        if (evt.getSource() == evt.getEntity().damageSources().inWall())
        {
            Mob toPush = pokemob != null ? pokemob.getEntity() : null;

            // Check if a player riding something, if so, reduce the damage, but
            // still make it happen to notify the player they need to leave the
            // wall.
            pokemob = PokemobCaps.getPokemobFor(evt.getEntity().getVehicle());
            final boolean playerRiding = evt.getEntity() instanceof Player && pokemob != null;
            if (playerRiding) toPush = pokemob.getEntity();

            if (toPush != null) evt.setAmount(0.1f);
        }
    }

    /**
     * Here we apply the exp bonus from exp share and lucky eggs
     */
    private static void onKillEvent(final KillEvent evt)
    {
        IPokemob killer = evt.killer;
        IPokemob killed = evt.killed;
        LivingEntity killedMob = evt.killedEntity;
        // Handles extra EXP gain from lucky egg and exp share.
        if (killer != null && evt.giveExp && killedMob.level() instanceof ServerLevel level)
        {
            int exp = Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor, killedMob, killed,
                    level, killer.getEntity());
            final LivingEntity owner = killer.getOwner();
            final ItemStack stack = killer.getHeldItem();
            if (ItemList.is(ResourceLocation.fromNamespaceAndPath("pokecube", "luckyegg"), stack))
            {
                killer.setExp(killer.getExp() + exp, true);
            }
            if (owner != null)
            {
                final List<Entity> pokemobs = PCEventsHandler.getOutMobs(owner, false);
                pokemobs.removeIf(e -> !e.isAlive());
                for (final Entity mob : pokemobs)
                {
                    final IPokemob poke = PokemobCaps.getPokemobFor(mob);
                    if (poke != null && poke.getEntity().getHealth() > 0 && ItemList.is(
                            ResourceLocation.fromNamespaceAndPath("pokecube", "exp_share"), poke.getHeldItem())
                            && !poke.getLogicState(LogicStates.SITTING))
                    {
                        poke.setExp(poke.getExp() + exp, true);
                    }
                }
            }
        }
    }

    private static void onLivingDeath(final LivingDeathEvent evt)
    {
        LivingEntity living = evt.getEntity();

        // If the thing that died was a pokemob, ensure no boss bar left
        final IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        if (pokemob != null && pokemob.getBossInfo() != null)
        {
            pokemob.getBossInfo().removeAllPlayers();
            pokemob.getBossInfo().setVisible(false);
        }

        final DamageSource damageSource = evt.getSource();
        // Handle transferring the kill info over, This is in place for mod
        // support.
        if (damageSource instanceof PokemobDamageSource && living.level() instanceof ServerLevel level)
            damageSource.getDirectEntity().killedEntity(level, living);

        // Handle exp gain for the mob.
        final IPokemob attacker = PokemobCaps.getPokemobFor(damageSource.getDirectEntity());
        if (attacker != null && damageSource.getDirectEntity() instanceof Mob mob)
            PokemobEventsHandler.handleExp(mob, attacker, living);
    }

    private static void onJoinWorldLast(final EntityJoinLevelEvent event)
    {
    }

    private static void onJoinWorld(final EntityJoinLevelEvent event)
    {
        final Entity mob = event.getEntity();
        final Level world = mob.level;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return;
        final IPokemob modified = pokemob.onAddedInit();
        if (modified.getEntity() != mob)
        {
            pokemob.markRemoved();
            final Mob newMob = modified.getEntity();
            if (world instanceof ServerLevel && !newMob.isAddedToLevel())
            {
                world.addFreshEntity(newMob);
                event.setCanceled(true);
                return;
            }
        }
        // This init stage involves block checks, etc, so do that here
        pokemob.postInitAI();
        // Ensure it is tracked
        PokemobTracker.addPokemob(pokemob);
        // then cache it if player's
        if (pokemob.isPlayerOwned() && pokemob.getOwnerId() != null) PlayerPokemobCache.UpdateCache(pokemob);
    }

    /**
     * This applies the pokemob AI to the entity, it is done via an event here so we can apply this to mobs added by
     * other things, such as vanilla.
     */
    private static void onBrainInit(final BrainInitEvent event)
    {
        final Entity mob = event.getEntity();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.preInitAI();
    }

    private static void onStartTracking(final StartTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // If the player is the owner, we sync over the mob's new moves
        if (player == pokemob.getOwner()) PacketSyncNewMoves.sendUpdatePacket(pokemob);

        // If the mob has a boss bar, add the player to track from that as well
        if (pokemob.getBossInfo() != null) pokemob.getBossInfo().addPlayer(player);
    }

    private static void onStopTracking(final StopTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (pokemob.getBossInfo() != null) pokemob.getBossInfo().removePlayer(player);
    }

    private static void onWorldTick(final LevelTickEvent.Post evt)
    {
        for (final Player player : evt.getLevel().players())
            if (player.getVehicle() instanceof LivingEntity ridden
                    && PokemobCaps.getPokemobFor(player.getVehicle()) != null)
                EntityTools.copyRotations(ridden, player);
    }

    private static long mean(final long[] values)
    {
        long sum = 0L;
        for (final long v : values) sum += v;
        return sum / values.length;
    }

    private static void onServerTick(final ServerTickEvent.Post event)
    {
        if (!PokecubeCore.getConfig().doLoadBalancing) return;
        final MinecraftServer server = ThutCore.proxy.getServer();
        final double meanTickTime = PokemobEventsHandler.mean(server.getTickTimesNanos()) * 1.0E-6D;
        final double maxTick = PokecubeCore.getConfig().loadBalanceThreshold;
        if (meanTickTime > maxTick)
        {
            final double factor = meanTickTime / maxTick;
            RootTask.doLoadThrottling = true;
            RootTask.runRate = (int) (factor * PokecubeCore.getConfig().loadBalanceScale);
        }
        else RootTask.doLoadThrottling = false;
    }

    private static void onCopyTick(final CopyUpdateEvent evt)
    {
        final LivingEntity living = evt.getEntity();

        // This prevents double ticking when a mob is both a copy and ticking
        // elsewhere, say in a custom pokeplayer like implementation
        long tick = living.getPersistentData().getLong("__i__");
        if (tick == Tracker.instance().getTick()) return;
        living.getPersistentData().putLong("__i__", Tracker.instance().getTick());

        final IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        if (pokemob != null)
        {
            // Reset death time if we are not dead.
            if (evt.getEntity().getHealth() > 0) evt.getEntity().deathTime = 0;

            // Initialize this for client side here
            if (living.level.isClientSide() && pokemob.getTickLogic().isEmpty()) pokemob.initAI();

            // Mark copy as in world for logic checks
            living.onAddedToLevel();
            // Tick the logic stuff for this mob.
            for (final Logic l : pokemob.getTickLogic()) if (l.shouldRun()) l.tick(living.level());
            // Unmark copy as in world afterwards
            living.onRemovedFromLevel();
        }
    }

    private static void onBreakSpeed(final PlayerEvent.BreakSpeed evt)
    {
        // TODO see if this is still needed, or instead should use attributes. See Player.getDigSpeed
        Entity mount = evt.getEntity().getVehicle();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mount);
        if (pokemob == null) return;

        boolean inWater = evt.getEntity().isEyeInFluidType(NeoForgeMod.WATER_TYPE.value());
        boolean inAir = !evt.getEntity().onGround();

        if (inWater && pokemob.canUseDive())
        {
            evt.setNewSpeed(evt.getNewSpeed() * 5);
        }
        if (inAir && pokemob.canUseFly())
        {
            evt.setNewSpeed(evt.getNewSpeed() * 5);
        }
    }

    private static void onMobTick(final EntityTickEvent.Pre evt)
    {
        if (!(evt.getEntity() instanceof LivingEntity living)) return;

        if (living.isRemoved()) return;

        // Tick the genes
        IMobGenetics genes = ThutCaps.getGenetics(living);
        if (genes != null) genes.onUpdateTick(living);

        final Level dim = living.level();
        // Prevent moving if it is liable to take us out of a loaded area
        double dist = Math.sqrt(living.getDeltaMovement().x * living.getDeltaMovement().x
                + living.getDeltaMovement().z * living.getDeltaMovement().z);
        final boolean ridden = living.isVehicle();
        final boolean tooFast = ridden && !TerrainManager.isAreaLoaded(dim, living.blockPosition(),
                PokecubeCore.getConfig().movementPauseThreshold + dist);
        if (tooFast) living.setDeltaMovement(0, living.getDeltaMovement().y, 0);

        final IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        if (pokemob instanceof DefaultPokemob pokemobCap && living instanceof EntityPokemob mob
                && dim instanceof ServerLevel level)
        {
            if (pokemobCap.getOwnerId() != null) mob.setPersistenceRequired();
            final Player near = mob.level().getNearestPlayer(mob, -1);
            if (near != null && pokemob.getOwnerId() == null)
            {
                dist = near.distanceTo(mob);
                if (Config.Rules.doCull(level, dist))
                {
                    pokemobCap.onRecall();
                    evt.setCanceled(true);
                    return;
                }
            }
        }
        if (living.getPersistentData().hasUUID("old_uuid"))
        {
            final UUID id = living.getPersistentData().getUUID("old_uuid");
            living.getPersistentData().remove("old_uuid");
            if (pokemob != null) PokemobTracker.removePokemob(pokemob);
            living.setUUID(id);
            if (pokemob != null) PokemobTracker.addPokemob(pokemob);
        }

        if (pokemob != null)
        {
            if (pokemob.isRemoved())
            {
                pokemob.getEntity().remove(RemovalReason.DISCARDED);
                return;
            }
            if (pokemob.getBossInfo() != null)
                pokemob.getBossInfo().setProgress(living.getHealth() / living.getMaxHealth());
            // Reset death time if we are not dead.
            if (living.getHealth() > 0) living.deathTime = 0;
            // Tick the logic stuff for this mob, this loop is most of the time spent in this function...
//            for (final Logic l : pokemob.getTickLogic()) if (l.shouldRun()) l.tick(dim);
            pokemob.getTickLogic().stream().filter(Logic::shouldRun).forEach(logic -> logic.tick(dim));
        }
    }

    private static Map<DyeColor, TagKey<Item>> getDyeTagMap()
    {
        if (PokemobEventsHandler.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("forge", "dyes/" + colour.getName());
            PokemobEventsHandler.DYETAGS.put(colour, TagKey.create(RegHelper.ITEM_REGISTRY, tag));
        }
        return PokemobEventsHandler.DYETAGS;
    }

    private static void handleExp(final Mob pokemob, final IPokemob attacker, final LivingEntity attacked)
    {
        if (!(attacked.level() instanceof ServerLevel level)) return;
        final IPokemob attackedMob = PokemobCaps.getPokemobFor(attacked);
        if (attacked.getHealth() <= 0 && attacked.getPersistentData().getInt("lastDeathTick") != attacked.tickCount)
        {
            attacked.getPersistentData().putInt("lastDeathTick", attacked.tickCount);
            boolean giveExp = !attacker.isShadow();
            IOwnable ownable = Ownable.get(attacked);

            final boolean pvp = ownable != null && ownable.isPlayerOwned();
            if (pvp && !PokecubeCore.getConfig().pvpExp) giveExp = false;
            if (attackedMob != null && attackedMob.getGeneralState(GeneralStates.TAMED)
                    && !PokecubeCore.getConfig().trainerExp) giveExp = false;
            final KillEvent event = new KillEvent(attacker, attackedMob, attacked, giveExp);
            PokecubeAPI.POKEMOB_BUS.post(event);
            if (!event.isCanceled() && event.giveExp)
            {
                float coef = (float) (pvp
                        ? PokecubeCore.getConfig().pvpExpMultiplier
                        : PokecubeCore.getConfig().expScaleFactor);
                attacker.setExp(
                        attacker.getExp() + Tools.getExp(coef, attacked, attackedMob, level, attacker.getEntity()),
                        true);
                if (attackedMob != null)
                {
                    final byte[] evsToAdd = attackedMob.getPokedexEntry().getEVs();
                    attacker.addEVs(evsToAdd);
                }
            }
            final Entity targetOwner = ownable != null ? ownable.getOwner() : null;
            Component faintMsg = TComponent.translatable("pokemob.action.faint.enemy", attacked.getDisplayName());
            attacker.displayMessageToOwner(faintMsg);

            // If the target has an owner, divert agro over to that, as the
            // owner has now lost the fight, or should send out a new mob.
            if (targetOwner instanceof Player player && attacker.getOwner() != targetOwner)
                Battle.createOrAddToBattle(pokemob, player);

            if (attacker.getCombatState(CombatStates.HUNTING))
            {
                attacker.eat(attacked);
                attacker.setCombatState(CombatStates.HUNTING, false);
                pokemob.getNavigation().stop();
            }
        }
    }

    private static boolean tryStartRiding(final Player PlayerEntity, final IPokemob pokemob)
    {
        if (PokemobEventsHandler.isRidable(PlayerEntity, pokemob))
        {
            if (PlayerEntity.isEffectiveAi()) PlayerEntity.startRiding(pokemob.getEntity());
            return true;
        }
        return false;
    }

    private static boolean isRidable(final Entity rider, final IPokemob pokemob)
    {
        final PokedexEntry entry = pokemob.getPokedexEntry();
        if (entry == null)
        {
            PokecubeAPI.LOGGER.error("Null Entry for {}", pokemob);
            return false;
        }
        if (!entry.ridable || pokemob.getCombatState(CombatStates.GUARDING)) return false;
        if (pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        if (pokemob.getLogicState(LogicStates.SITTING)) return false;
        if (pokemob.getInventory().getItem(0).isEmpty()) return false;

        if (rider instanceof ServerPlayer player && rider == pokemob.getOwner())
        {
            if (!Permissions.canRide(pokemob, player)) return false;
        }
        final float scale = pokemob.getEntity().getScale();
        final Vec3f dims = pokemob.getPokedexEntry().getModelSize();
        return dims.y * scale + dims.x * scale > rider.getBbWidth()
                && Math.max(dims.x, dims.z) * scale > rider.getBbWidth() * 1.4;
    }

    private static void processInteract(final EntityInteractSpecific evt)
    {
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;
        var target = evt.getTarget();
        final IPokemob pokemob = PokemobCaps.getPokemobFor(target);
        if (pokemob == null) return;

        final ItemStack held = evt.getItemStack();
        final Mob entity = pokemob.getEntity();
        final InteractionHand hand = evt.getHand();

        final InteractEvent event = new InteractEvent(pokemob, player, evt);
        ThutCore.FORGE_BUS.post(event);
        if (event.getResult() != TriState.DEFAULT)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // Item has custom entity interaction, let that run instead.
        if (held.getItem().interactLivingEntity(held, player, entity, evt.getHand()) != InteractionResult.PASS)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        final PokedexEntry entry = pokemob.getPokedexEntry();

        // Check Pokedex Entry defined Interaction for player.
        if (entry.interact(player, hand, pokemob, true))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // If not alled to interact with the mob, exit here, this prevents
        // opening pokemob inventory while holding empty cubes, etc.
        if (ItemList.is(ResourceLocation.fromNamespaceAndPath("pokecube", "pokemob_no_interact"), held)) return;

        // only accept mainhand past here.
        if (hand != InteractionHand.MAIN_HAND) return;

        boolean isOwner = false;
        if (pokemob.getOwnerId() != null) isOwner = pokemob.getOwnerId().equals(player.getUUID());
        // Owner only interactions phase 1
        if (isOwner)
        {
            // Either push pokemob around, or if sneaking, make it try to
            // climb
            // on shoulder
            if (held.getItem() == Items.STICK || held.getItem() == Blocks.TORCH.asItem())
            {
                if (held.getDisplayName().getString().contains("poke"))
                {
                    final Vector3 look = new Vector3().set(player.getLookAngle()).scalarMultBy(0.5);
                    look.y = 0.2;
                    look.addVelocities(target);
                }
                return;
            }
            // Debug thing to maximize happiness
            if (held.getItem() == Items.APPLE)
                if (player.isCreative() && player.isShiftKeyDown()) pokemob.addHappiness(255);
            // Debug thing to increase hunger time
            if (held.getItem() == Items.GOLDEN_HOE)
                if (player.isCreative() && player.isShiftKeyDown()) pokemob.applyHunger(+4000);
            // Use shiny charm to make shiny
            if (ItemList.is(ResourceLocation.parse("pokecube:shiny_charm"), held))
            {
                if (player.isShiftKeyDown())
                {
                    pokemob.setShiny(!pokemob.isShiny());
                    if (!player.isCreative()) held.split(1);
                }
                evt.setCanceled(true);
                evt.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        // is Dyeable
        if (!held.isEmpty() && entry.dyeable)
        {
            final TagKey<Item> dyeTag = Tags.Items.DYES;
            DyeColor dye = null;
            if (held.is(dyeTag))
            {
                final Map<DyeColor, TagKey<Item>> tags = PokemobEventsHandler.getDyeTagMap();
                for (final DyeColor colour : DyeColor.values())
                    if (held.is(tags.get(colour)))
                    {
                        dye = colour;
                        break;
                    }
            }
            if (dye != null && (entry.validDyes.isEmpty() || entry.validDyes.contains(dye)))
            {
                pokemob.setDyeColour(dye.getId());
                if (!player.isCreative()) held.shrink(1);
                evt.setCanceled(true);
                evt.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
            else if (held.getItem() == Items.SHEARS) return;
        }

        boolean deny = pokemob.getCombatState(CombatStates.NOITEMUSE);
        final boolean hasTarget = BrainUtils.hasAttackTarget(entity);
        if (deny && !hasTarget)
        {
            deny = false;
            pokemob.setCombatState(CombatStates.NOITEMUSE, false);
        }

        if (deny)
        {
            // Add message here about cannot use items right now
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokemob.action.cannotuse"));
            return;
        }

        boolean fits = isOwner;
        if (!fits && pokemob.getEntity() instanceof EntityPokemob mob) fits = mob.canAddPassenger(player);
        final boolean saddled = PokemobEventsHandler.tryStartRiding(player, pokemob);

        boolean guiAllowed = pokemob.getPokedexEntry().stock || held.getItem() == PokecubeItems.POKEDEX.get();
        guiAllowed = guiAllowed && entity.isAlive();

        boolean saddleCheck = !player.isShiftKeyDown() && held.isEmpty() && fits && saddled;
        saddleCheck = saddleCheck && entity.isAlive();

        // Check if favourte berry and sneaking, if so, do breeding stuff.
        if (isOwner || player instanceof FakePlayer)
        {
            final int fav = Nature.getFavouriteBerryIndex(pokemob.getNature());
            if (PokecubeCore.getConfig().berryBreeding && (player.isShiftKeyDown() || player instanceof FakePlayer)
                    && !hasTarget && held.getItem() instanceof ItemBerry berry && (fav == -1
                    || fav == berry.type.index))
            {
                if (!player.isCreative())
                {
                    held.shrink(1);
                    if (held.isEmpty()) player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                }
                pokemob.setReadyToMate(player);
                BrainUtils.clearAttackTarget(entity);
                entity.level().broadcastEntityEvent(entity, (byte) 18);
                evt.setCanceled(true);
                evt.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        // Owner only interactions phase 2
        if (isOwner)
        {
            if (!held.isEmpty())
            {
                // Check if it should evolve from item, do so if yes.
                evo:
                if (pokemob.canEvolve(held))
                {
                    boolean valid = false;
                    if (pokemob.getPokedexEntry().canEvolve() && pokemob.getEntity().isEffectiveAi())
                        for (final EvolutionData d : pokemob.getPokedexEntry().getEvolutions())
                        {
                            boolean evolve = d.shouldEvolve(pokemob, held);
                            if (evolve && !d.shouldEvolve(pokemob, ItemStack.EMPTY))
                            {
                                valid = true;
                                break;
                            }
                        }
                    if (!valid) break evo;

                    boolean evolved = pokemob.evolve(true, false, held);
                    if (evolved) if (!player.isCreative())
                    {
                        held.shrink(1);
                        if (held.isEmpty())
                            player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                    }
                    evt.setCanceled(true);
                    evt.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                // Otherwise check if useable item.
                final IPokemobUseable usable = PokemobCaps.getPokemobUsable(held);
                if (usable != null)
                {
                    final InteractionResultHolder<ItemStack> result = usable.onUse(pokemob, held, player);
                    if (result.getResult() == InteractionResult.SUCCESS)
                    {
                        player.setItemInHand(hand, result.getObject());
                        pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                        evt.setCanceled(true);
                        evt.setCancellationResult(InteractionResult.SUCCESS);
                        return;
                    }
                }
            }
            // Open Gui
            if (!saddleCheck && guiAllowed)
            {
                PacketPokemobGui.sendOpenPacket(entity, player);
                evt.setCanceled(true);
                evt.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }
        // Check saddle for riding.
        if (saddleCheck)
        {
            entity.setJumping(false);
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
