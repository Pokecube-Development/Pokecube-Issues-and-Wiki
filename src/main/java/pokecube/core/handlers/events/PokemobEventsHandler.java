package pokecube.core.handlers.events;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.BossEvent;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.Logic;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.BrainInitEvent;
import pokecube.core.events.CustomInteractEvent;
import pokecube.core.events.pokemob.InteractEvent;
import pokecube.core.events.pokemob.combat.KillEvent;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityInhabitor;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.moves.damage.IPokedamage;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketSyncGene;
import pokecube.core.utils.AITools;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.ai.RootTask;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.event.CopyUpdateEvent;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.api.terrain.TerrainManager;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;

public class PokemobEventsHandler
{
    private static Map<DyeColor, TagKey<Item>> DYETAGS = Maps.newHashMap();

    public static void register()
    {
        // This handles exp yield from lucky eggs and exp_shares.
        PokecubeCore.POKEMOB_BUS.addListener(PokemobEventsHandler::onKillEvent);

        // Highest to prevent other things from trying to do things with our
        // drops if we cancel them, and to allow us to add things properly to
        // the drops. This adds the inventory items to the drops list for wild
        // pokemobs, and prevents drops for pokemobs which have been revived or
        // are tame
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PokemobEventsHandler::onLivingDrops);
        // This is done twice as some events only send one rather than the other
        // from client side!
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onInteract);
        // This handles pokemob damage stuff. It deals with: cancelling damage
        // on invalid targets, adjusting damage amount by the scaling configs
        // and preventing player suffocating while riding a pokemob into a
        // cieling.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onLivingHurt);
        // Used to reset the "NOITEMUSE" flag, which controls using healing
        // items, the capture delay, etc.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onLivingAttacked);

        // This ensures that the damage sources apply for the correct entity,
        // this part is for support for mods like customnpcs
        // It also handles exp gain for the pokemobs when they kill something.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onLivingDeath);
        // This deals with pokemob initialization, it initializes the AI, and
        // also does some checks for things like evolution, etc
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onJoinWorld);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, PokemobEventsHandler::onJoinWorldLast);
        // This synchronizes genetics over to the clients when they start
        // tracking the mob locally.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onStopTracking);
        // This syncs rotation of the ridden pokemob with the rider.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onWorldTick);
        // Monitors sim speed and reduces idle tick rate if lagging too much
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onServerTick);
        // This pauses the pokemobs if too close to the edge of the loaded area,
        // preventing them from chunkloading during their AI. It also then
        // ensures their UUID is correct after evolution, and then ticks the
        // "logic" section of their AI.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onMobTick);
        // Similar as the above, except only for "logic" on the copied state
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onCopyTick);
        // Called by MixinMobEntity before the first brain tick, to ensure the
        // brain has AI setup, etc.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onBrainInit);

        // This checks if we are an inhabitor of a nest, and we just left it. if
        // this is the case, then some extra processing is done related to
        // finishing tasks, etc upon leaving the nest.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onMobAddedToWorld);

        // Checks to see if we are diving mob+dive, or flyingmob+fly, and if so,
        // we speed back up breaking.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onBreakSpeed);
    }

    /**
     * Here we will check if it was a bee, added from a bee-hive, and if so, we
     * will increment the honey level as needed.
     */
    private static void onMobAddedToWorld(final EntityJoinWorldEvent event)
    {
        // We only consider MobEntities
        if (!(event.getEntity() instanceof Mob)) return;

        final Mob mob = (Mob) event.getEntity();

        if (mob.getLevel().isClientSide()) return;

        // We only want to run this from execution thread.
        if (!mob.getServer().isSameThread() || !(mob.level instanceof ServerLevel world)) return;

        final IInhabitor inhabitor = mob.getCapability(CapabilityInhabitor.CAPABILITY).orElse(null);
        // Not a valid inhabitor of things, so return.
        if (inhabitor == null) return;

        // Vanilla breaks things here, by deleting the memory tag in the brain,
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
        }

        // No Home spot, so definitely not leaving home
        if (inhabitor.getHome() == null) return;

        final GlobalPos pos = inhabitor.getHome();
        // not same dimension, not a bee leaving hive
        if (pos.dimension() != world.dimension()) return;

        // This will indicate if the tile did actually cause the spawn.
        boolean fromHive = false;
        int n = 0;
        Class<?> c = null;
        // Check the stack to see if tile resulted in our spawn, if not, then we
        // are not from it either!
        for (final StackTraceElement element : Thread.currentThread().getStackTrace())
        {
            try
            {
                c = Class.forName(element.getClassName());
                fromHive = BlockEntity.class.isAssignableFrom(c);
            }
            catch (final ClassNotFoundException e)
            {
                // NOOP, why would this happen anyway?
                PokecubeCore.LOGGER.error("Error with class for {}??", element.getClassName());
            }
            if (fromHive || n++ > 100) break;
        }
        // was not from the hive, so exit
//        if (!fromHive) return;
//        final Class<?> clss = c;
        // not loaded, definitely not a bee leaving hive
        if (!world.isPositionEntityTicking(pos.pos())) return;
        final BlockEntity tile = world.getBlockEntity(pos.pos());
        // No tile entity here? also not a bee leaving hive!
        if (tile == null) return;
        // Not the same class, so return as well.
//        if (tile.getClass() != clss) return;
        final IInhabitable habitat = tile.getCapability(CapabilityInhabitable.CAPABILITY).orElse(null);
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
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
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

    private static void onInteract(final CustomInteractEvent evt)
    {
        PokemobEventsHandler.processInteract(evt, evt.getTarget());
    }

    private static void onLivingHurt(final LivingHurtEvent evt)
    {
        // Only process these server side
        if (!(evt.getEntity().getLevel() instanceof ServerLevel)) return;
        /*
         * No harming invalid targets, only apply this to pokemob related damage
         * sources
         */
        if (evt.getSource() instanceof IPokedamage && !AITools.validTargets.test(evt.getEntity()))
        {
            evt.setCanceled(true);
            return;
        }
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        // check if configs say this damage can't happen
        if (pokemob != null && !AITools.validToHitPokemob.test(evt.getSource()))
        {
            evt.setCanceled(true);
            return;
        }
        // Apply scaling from config for this
        if (pokemob != null && evt.getSource().getEntity() instanceof Player)
            evt.setAmount((float) (evt.getAmount() * PokecubeCore.getConfig().playerToPokemobDamageScale));

        // Some special handling for in wall stuff
        if (evt.getSource() == DamageSource.IN_WALL)
        {
            Mob toPush = pokemob != null ? pokemob.getEntity() : null;

            // Check if a player riding something, if so, compute a larger
            // hitbox and try to push out of the wall
            pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity().getVehicle());
            final boolean playerRiding = evt.getEntity() instanceof Player && pokemob != null;
            if (playerRiding) toPush = pokemob.getEntity();

            if (toPush != null)
            {
                evt.setCanceled(true);
                final ServerLevel world = (ServerLevel) evt.getEntity().getLevel();
                final AABB oldBox = evt.getEntity().getBoundingBox();
                final AABB newBox = toPush.getBoundingBox();

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
                if (hits.size() > 0)
                {
                    VoxelShape total = Shapes.empty();
                    // Merge the found shapes into a single one
                    for (final VoxelShape s : hits) total = Shapes.joinUnoptimized(total, s, BooleanOp.OR);
                    final List<AABB> aabbs = Lists.newArrayList();
                    // Convert to colliding AABBs
                    BlockEntityUpdater.fill(aabbs, biggerBox, total);
                    // Push off the AABBS if needed
                    final boolean col = BlockEntityUpdater.applyEntityCollision(toPush, biggerBox, aabbs, Vec3.ZERO);

                    // This gives us an indication if if we did actually
                    // collide, if this occured, then we need to do some extra
                    // processing to make sure that we fit properly
                    if (col)
                    {
                        Vector3 v = new Vector3().set(toPush);
                        v = SendOutManager.getFreeSpot(toPush, world, v, false);
                        if (v != null) v.moveEntity(toPush);
                    }
                }
            }
        }
    }

    private static void onKillEvent(final KillEvent evt)
    {
        final IPokemob killer = evt.killer;
        final IPokemob killed = evt.killed;
        // Handles extra EXP gain from lucky egg and exp share.
        if (killer != null && evt.giveExp)
        {
            final LivingEntity owner = killer.getOwner();
            final ItemStack stack = killer.getHeldItem();
            if (ItemList.is(new ResourceLocation("pokecube", "luckyegg"), stack))
            {
                final int exp = killer.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor,
                        killed.getBaseXP(), killed.getLevel());
                killer.setExp(exp, true);
            }
            if (owner != null)
            {
                final List<Entity> pokemobs = PCEventsHandler.getOutMobs(owner, false);
                pokemobs.removeIf(e -> !e.isAlive());
                for (final Entity mob : pokemobs)
                {
                    final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                    if (poke != null && poke.getEntity().getHealth() > 0
                            && ItemList.is(new ResourceLocation("pokecube", "exp_share"), poke.getHeldItem())
                            && !poke.getLogicState(LogicStates.SITTING))
                    {
                        final int exp = poke.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor,
                                killed.getBaseXP(), killed.getLevel());
                        poke.setExp(exp, true);
                    }
                }
            }
        }
    }

    private static void onLivingAttacked(final LivingAttackEvent event)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getSource().getDirectEntity());
        if (pokemob != null) pokemob.setCombatState(CombatStates.NOITEMUSE, false);

        if (event.getSource().getDirectEntity() != null
                && event.getSource().getDirectEntity().isPassengerOfSameVehicle(event.getEntity()))
        {
            event.setCanceled(true);
        }
    }

    private static void onLivingDeath(final LivingDeathEvent evt)
    {
        // If the thing that died was a pokemob, ensure no boss bar left
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (pokemob != null && pokemob.getBossInfo() != null)
        {
            pokemob.getBossInfo().removeAllPlayers();
            pokemob.getBossInfo().setVisible(false);
        }

        final DamageSource damageSource = evt.getSource();
        // Handle transferring the kill info over, This is in place for mod
        // support.
        if (damageSource instanceof PokemobDamageSource && evt.getEntity().getLevel() instanceof ServerLevel)
            damageSource.getDirectEntity().killed((ServerLevel) evt.getEntity().getLevel(),
                    (LivingEntity) evt.getEntity());

        // Handle exp gain for the mob.
        final IPokemob attacker = CapabilityPokemob.getPokemobFor(damageSource.getDirectEntity());
        if (attacker != null && damageSource.getDirectEntity() instanceof Mob) PokemobEventsHandler
                .handleExp((Mob) damageSource.getDirectEntity(), attacker, (LivingEntity) evt.getEntity());
    }

    private static void onJoinWorldLast(final EntityJoinWorldEvent event)
    {
        final Entity mob = event.getEntity();
        if (!(mob instanceof final EntityPokemob pokemob)) return;
        PokemobTracker.addPokemob(pokemob.pokemobCap);
        if (pokemob.pokemobCap.isPlayerOwned() && pokemob.pokemobCap.getOwnerId() != null)
            PlayerPokemobCache.UpdateCache(pokemob.pokemobCap);
    }

    private static void onJoinWorld(final EntityJoinWorldEvent event)
    {
        final Entity mob = event.getEntity();
        final Level world = mob.level;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setEntity((Mob) mob);
        final IPokemob modified = pokemob.onAddedInit();
        if (modified.getEntity() != mob)
        {
            pokemob.markRemoved();
            final Mob newMob = modified.getEntity();
            if (world instanceof ServerLevel && !newMob.isAddedToWorld()) world.addFreshEntity(newMob);
        }
        // This init stage involves block checks, etc, so do that here
        pokemob.postInitAI();
    }

    private static void onBrainInit(final BrainInitEvent event)
    {
        final Entity mob = event.getEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.preInitAI();
    }

    private static void onStartTracking(final StartTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        final PokedexEntry entry = pokemob.getPokedexEntry();

        final IMobGenetics genes = event.getTarget().getCapability(ThutCaps.GENETICS_CAP).orElse(null);
        for (final Alleles<?, ?> allele : genes.getAlleles().values())
            PacketSyncGene.syncGene(event.getTarget(), allele, (ServerPlayer) event.getPlayer());

        // Send the whole thing over in this case, as it means it won't
        // auto-sync things like IPokemob, etc.
        // TODO special packet for just our capabiltiies instead!
        if (!entry.stock) EntityUpdate.sendEntityUpdate(event.getTarget());

        // If the mob has a boss bar, add the player to track from that as well
        if (pokemob.getBossInfo() != null) pokemob.getBossInfo().addPlayer((ServerPlayer) event.getEntity());
    }

    private static void onStopTracking(final StopTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        if (pokemob.getBossInfo() != null) pokemob.getBossInfo().removePlayer((ServerPlayer) event.getEntity());
    }

    private static void onWorldTick(final WorldTickEvent evt)
    {
        for (final Player player : evt.world.players()) if (player.getVehicle() instanceof LivingEntity
                && CapabilityPokemob.getPokemobFor(player.getVehicle()) != null)
        {
            final LivingEntity ridden = (LivingEntity) player.getVehicle();
            EntityTools.copyRotations(ridden, player);
        }
    }

    private static long mean(final long[] values)
    {
        long sum = 0L;
        for (final long v : values) sum += v;
        return sum / values.length;
    }

    private static void onServerTick(final ServerTickEvent event)
    {
        if (event.phase != Phase.END || !PokecubeCore.getConfig().doLoadBalancing) return;
        final MinecraftServer server = ThutCore.proxy.getServer();
        final double meanTickTime = PokemobEventsHandler.mean(server.tickTimes) * 1.0E-6D;
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
        final LivingEntity living = evt.getEntityLiving();

        // This prevents double ticking when a mob is both a copy and ticking
        // elsewhere, say in a custom pokeplayer like implementation
        long tick = living.getPersistentData().getLong("__i__");
        if (tick == Tracker.instance().getTick()) return;
        living.getPersistentData().putLong("__i__", Tracker.instance().getTick());

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(living);
        if (pokemob != null)
        {
            // Reset death time if we are not dead.
            if (evt.getEntityLiving().getHealth() > 0) evt.getEntityLiving().deathTime = 0;

            // Initialize this for client side here
            if (living.level.isClientSide() && pokemob.getTickLogic().isEmpty()) pokemob.initAI();

            // Tick the logic stuff for this mob.
            for (final Logic l : pokemob.getTickLogic()) if (l.shouldRun()) l.tick(living.getLevel());
        }
    }

    private static void onBreakSpeed(final PlayerEvent.BreakSpeed evt)
    {
        Entity mount = evt.getPlayer().getVehicle();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mount);
        if (pokemob == null) return;

        boolean inWater = evt.getPlayer().isEyeInFluid(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(evt.getPlayer());
        boolean inAir = !evt.getPlayer().onGround;

        if (inWater && pokemob.canUseDive())
        {
            evt.setNewSpeed(evt.getNewSpeed() * 5);
        }
        if (inAir && pokemob.canUseFly())
        {
            evt.setNewSpeed(evt.getNewSpeed() * 5);
        }
    }

    private static void onMobTick(final LivingUpdateEvent evt)
    {
        final LivingEntity living = evt.getEntityLiving();

        // This prevents double ticking when a mob is both a copy and ticking
        // elsewhere, say in a custom pokeplayer like implementation
        long tick = living.getPersistentData().getLong("__i__");
        if (tick == Tracker.instance().getTick()) return;
        living.getPersistentData().putLong("__i__", Tracker.instance().getTick());

        final Level dim = living.getLevel();
        // Prevent moving if it is liable to take us out of a loaded area
        double dist = Math.sqrt(living.getDeltaMovement().x * living.getDeltaMovement().x
                + living.getDeltaMovement().z * living.getDeltaMovement().z);
        final boolean ridden = living.isVehicle();
        final boolean tooFast = ridden && !TerrainManager.isAreaLoaded(dim, living.blockPosition(),
                PokecubeCore.getConfig().movementPauseThreshold + dist);
        if (tooFast) living.setDeltaMovement(0, living.getDeltaMovement().y, 0);

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(living);
        if (pokemob instanceof DefaultPokemob && living instanceof EntityPokemob && dim instanceof ServerLevel level)
        {
            final DefaultPokemob pokemobCap = (DefaultPokemob) pokemob;
            final EntityPokemob mob = (EntityPokemob) living;
            if (pokemobCap.returning)
            {
                mob.remove(RemovalReason.DISCARDED);
                evt.setCanceled(true);
                return;
            }
            if (pokemobCap.getOwnerId() != null) mob.setPersistenceRequired();
            final Player near = mob.getLevel().getNearestPlayer(mob, -1);
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
            else if (pokemob.getOwnerId() == null && pokemob.getCombatState(CombatStates.DYNAMAX)
                    && dim instanceof ServerLevel)
                pokemob.setBossInfo(new ServerBossEvent(living.getDisplayName(), BossEvent.BossBarColor.RED,
                        BossEvent.BossBarOverlay.PROGRESS));

            // Reset death time if we are not dead.
            if (evt.getEntityLiving().getHealth() > 0) evt.getEntityLiving().deathTime = 0;
            // Tick the logic stuff for this mob.
            for (final Logic l : pokemob.getTickLogic()) if (l.shouldRun()) l.tick(living.getLevel());
        }
    }

    private static Map<DyeColor, TagKey<Item>> getDyeTagMap()
    {
        if (PokemobEventsHandler.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            PokemobEventsHandler.DYETAGS.put(colour,TagKey.create(Registry.ITEM_REGISTRY, tag));
        }
        return PokemobEventsHandler.DYETAGS;
    }

    private static void handleExp(final Mob pokemob, final IPokemob attacker, final LivingEntity attacked)
    {
        final IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (PokecubeCore.getConfig().nonPokemobExp && attackedMob == null)
        {
            final JEP parser = new JEP();
            parser.initFunTab(); // clear the contents of the function table
            parser.addStandardFunctions();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addStandardConstants();
            parser.addComplex();
            parser.addVariable("h", 0);
            parser.addVariable("a", 0);
            parser.parseExpression(PokecubeCore.getConfig().nonPokemobExpFunction);
            parser.setVarValue("h", attacked.getMaxHealth());
            parser.setVarValue("a", attacked.getArmorValue());
            int exp = (int) parser.getValue();
            if (parser.hasError()) exp = 0;
            attacker.setExp(attacker.getExp() + exp, true);
            return;
        }
        if (attackedMob != null && attacked.getHealth() <= 0
                && attacked.getPersistentData().getInt("lastDeathTick") != attacked.tickCount)
        {
            attacked.getPersistentData().putInt("lastDeathTick", attacked.tickCount);
            boolean giveExp = !attackedMob.isShadow();
            final boolean pvp = attackedMob.getGeneralState(GeneralStates.TAMED)
                    && attackedMob.getOwner() instanceof Player;
            if (pvp && !PokecubeCore.getConfig().pvpExp) giveExp = false;
            if (attackedMob.getGeneralState(GeneralStates.TAMED) && !PokecubeCore.getConfig().trainerExp)
                giveExp = false;
            final KillEvent event = new KillEvent(attacker, attackedMob, giveExp);
            PokecubeCore.POKEMOB_BUS.post(event);
            giveExp = event.giveExp;
            if (event.isCanceled())
            {

            }
            else if (giveExp)
            {
                attacker.setExp(attacker.getExp() + Tools.getExp(
                        (float) (pvp ? PokecubeCore.getConfig().pvpExpMultiplier
                                : PokecubeCore.getConfig().expScaleFactor),
                        attackedMob.getBaseXP(), attackedMob.getLevel()), true);
                final byte[] evsToAdd = attackedMob.getPokedexEntry().getEVs();
                attacker.addEVs(evsToAdd);
            }
            final Entity targetOwner = attackedMob.getOwner();
            attacker.displayMessageToOwner(
                    new TranslatableComponent("pokemob.action.faint.enemy", attackedMob.getDisplayName()));
            if (targetOwner instanceof Player && attacker.getOwner() != targetOwner)
                BrainUtils.initiateCombat(pokemob, (LivingEntity) targetOwner);
            else BrainUtils.deagro(pokemob);
            if (attacker.getPokedexEntry().isFood(attackedMob.getPokedexEntry())
                    && attacker.getCombatState(CombatStates.HUNTING))
            {
                attacker.eat(attackedMob.getEntity());
                attacker.setCombatState(CombatStates.HUNTING, false);
                pokemob.getNavigation().stop();
            }
        }
    }

    private static boolean handleHmAndSaddle(final Player PlayerEntity, final IPokemob pokemob)
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
            System.err.println("Null Entry for " + pokemob);
            return false;
        }
        if (!entry.ridable || pokemob.getCombatState(CombatStates.GUARDING)) return false;
        if (pokemob.getGeneralState(GeneralStates.STAYING)) return false;
        if (pokemob.getLogicState(LogicStates.SITTING)) return false;
        if (pokemob.getInventory().getItem(0).isEmpty()) return false;

        if (rider instanceof ServerPlayer player && rider == pokemob.getOwner())
        {
            final Config config = PokecubeCore.getConfig();
            if (config.permsRide && !PermNodes.getBooleanPerm(player, Permissions.RIDEPOKEMOB)) return false;
            if (config.permsRideSpecific && !PermNodes.getBooleanPerm(player, Permissions.RIDESPECIFIC.get(entry)))
                return false;
        }
        final float scale = pokemob.getSize();
        final Vec3f dims = pokemob.getPokedexEntry().getModelSize();
        return dims.y * scale + dims.x * scale > rider.getBbWidth()
                && Math.max(dims.x, dims.z) * scale > rider.getBbWidth() * 1.4;
    }

    private static void processInteract(final PlayerInteractEvent evt, final Entity target)
    {
        if (!(evt.getPlayer() instanceof ServerPlayer)) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(target);
        if (pokemob == null) return;

        final ServerPlayer player = (ServerPlayer) evt.getPlayer();
        final InteractionHand hand = evt.getHand();
        final ItemStack held = player.getItemInHand(hand);
        final Mob entity = pokemob.getEntity();

        final InteractEvent event = new InteractEvent(pokemob, player, evt);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() != Result.DEFAULT)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // Item has custom entity interaction, let that run instead.
        if (held.getItem().interactLivingEntity(held, player, entity, hand) != InteractionResult.PASS)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        final PokedexEntry entry = pokemob.getPokedexEntry();

        // Check Pokedex Entry defined Interaction for player.
        if (entry.interact(player, pokemob, true))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // If not alled to interact with the mob, exit here, this prevents
        // opening pokemob inventory while holding empty cubes, etc.
        if (ItemList.is(new ResourceLocation("pokecube", "pokemob_no_interact"), held)) return;
        // check of other hand is holding a blackisted item as well.
        final InteractionHand other = InteractionHand.values()[(hand.ordinal() + 1) % 2];
        final ItemStack otherheld = player.getItemInHand(other);
        if (ItemList.is(new ResourceLocation("pokecube", "pokemob_no_interact"), otherheld)) return;

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
                if (player.isShiftKeyDown())
                {
                    if (pokemob.getEntity().isAlive()) pokemob.moveToShoulder(player);
                    return;
                }
                final Vector3 look = new Vector3().set(player.getLookAngle()).scalarMultBy(1);
                look.y = 0.2;
                look.addVelocities(target);
                return;
            }
            // Debug thing to maximize happiness
            if (held.getItem() == Items.APPLE)
                if (player.isCreative() && player.isShiftKeyDown()) pokemob.addHappiness(255);
            // Debug thing to increase hunger time
            if (held.getItem() == Items.GOLDEN_HOE)
                if (player.isCreative() && player.isShiftKeyDown()) pokemob.applyHunger(+4000);
            // Use shiny charm to make shiny
            if (ItemList.is(new ResourceLocation("pokecube:shiny_charm"), held))
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
                for (final DyeColor colour : DyeColor.values()) if (held.is(tags.get(colour)))
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
            player.sendMessage(new TranslatableComponent("pokemob.action.cannotuse"), Util.NIL_UUID);
            return;
        }

        boolean fits = isOwner;
        if (!fits && pokemob.getEntity() instanceof EntityPokemob)
            fits = ((EntityPokemob) pokemob.getEntity()).canAddPassenger(player);
        final boolean saddled = PokemobEventsHandler.handleHmAndSaddle(player, pokemob);

        final boolean guiAllowed = pokemob.getPokedexEntry().stock || held.getItem() == PokecubeItems.POKEDEX.get();

        final boolean saddleCheck = !player.isShiftKeyDown() && held.isEmpty() && fits && saddled;

        // Check if favourte berry and sneaking, if so, do breeding stuff.
        if (isOwner || player instanceof FakePlayer)
        {
            final int fav = Nature.getFavouriteBerryIndex(pokemob.getNature());
            if (PokecubeCore.getConfig().berryBreeding && (player.isShiftKeyDown() || player instanceof FakePlayer)
                    && !hasTarget && held.getItem() instanceof ItemBerry
                    && (fav == -1 || fav == ((ItemBerry) held.getItem()).type.index))
            {
                if (!player.isCreative())
                {
                    held.shrink(1);
                    if (held.isEmpty()) player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                }
                pokemob.setReadyToMate(player);
                BrainUtils.clearAttackTarget(entity);
                entity.getLevel().broadcastEntityEvent(entity, (byte) 18);
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
                        boolean hasItem = !d.item.isEmpty();
                        hasItem = hasItem || d.preset != null;
                        if (hasItem && d.shouldEvolve(pokemob, held))
                        {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) break evo;

                    final IPokemob evolution = pokemob.evolve(true, false, held);
                    if (evolution != null) if (!player.isCreative())
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
                final IPokemobUseable usable = IPokemobUseable.getUsableFor(held);
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
            return;
        }
    }
}
