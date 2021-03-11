package pokecube.core.handlers.events;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
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
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.RootTask;
import pokecube.core.ai.logic.Logic;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.BrainInitEvent;
import pokecube.core.events.CustomInteractEvent;
import pokecube.core.events.pokemob.InteractEvent;
import pokecube.core.events.pokemob.combat.KillEvent;
import pokecube.core.handlers.Config;
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
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.api.terrain.TerrainManager;
import thut.core.common.network.EntityUpdate;

public class PokemobEventsHandler
{
    private static Map<DyeColor, ITag<Item>> DYETAGS = Maps.newHashMap();

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
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onLivingAttack);

        // This ensures that the damage sources apply for the correct entity,
        // this part is for support for mods like customnpcs
        // It also handles exp gain for the pokemobs when they kill something.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onLivingDeath);
        // This deals with pokemob initialization, it initializes the AI, and
        // also does some checks for things like evolution, etc
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onJoinWorld);
        // This synchronizes genetics over to the clients when they start
        // tracking the mob locally.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onMobTracking);
        // This syncs rotation of the ridden pokemob with the rider.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onWorldTick);
        // Monitors sim speed and reduces idle tick rate if lagging too much
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onServerTick);
        // This pauses the pokemobs if too close to the edge of the loaded area,
        // preventing them from chunkloading during their AI. It also then
        // ensures their UUID is correct after evolution, and then ticks the
        // "logic" section of their AI.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onMobTick);
        // Called by MixinMobEntity before the first brain tick, to ensure the
        // brain has AI setup, etc.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onBrainInit);

        // This checks if we are an inhabitor of a nest, and we just left it. if
        // this is the case, then some extra processing is done related to
        // finishing tasks, etc upon leaving the nest.
        MinecraftForge.EVENT_BUS.addListener(PokemobEventsHandler::onMobAddedToWorld);
    }

    /**
     * Here we will check if it was a bee, added from a bee-hive, and if so, we
     * will increment the honey level as needed.
     */
    private static void onMobAddedToWorld(final EntityJoinWorldEvent event)
    {
        // We only consider MobEntities
        if (!(event.getEntity() instanceof MobEntity)) return;

        final MobEntity mob = (MobEntity) event.getEntity();

        if (mob.getCommandSenderWorld().isClientSide()) return;

        // We only want to run this from execution thread.
        if (!mob.getServer().isSameThread()) return;

        final IInhabitor inhabitor = mob.getCapability(CapabilityInhabitor.CAPABILITY).orElse(null);
        // Not a valid inhabitor of things, so return.
        if (inhabitor == null) return;

        // No Home spot, so definitely not leaving home
        if (inhabitor.getHome() == null) return;
        final World world = event.getEntity().getCommandSenderWorld();

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
                fromHive = TileEntity.class.isAssignableFrom(c);
            }
            catch (final ClassNotFoundException e)
            {
                // NOOP, why would this happen anyway?
                PokecubeCore.LOGGER.error("Error with class for {}??", element.getClassName());
            }
            if (fromHive || n++ > 100) break;
        }
        // was not from the hive, so exit
        if (!fromHive) return;
        final Class<?> clss = c;
        // not loaded, definitely not a bee leaving hive
        if (!world.isAreaLoaded(pos.pos(), 8)) return;
        final TileEntity tile = world.getBlockEntity(pos.pos());
        // No tile entity here? also not a bee leaving hive!
        if (tile == null) return;
        // Not the same class, so return as well.
        if (tile.getClass() != clss) return;
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
            if (!pokemob.getGeneralState(GeneralStates.TAMED)) for (int i = 0; i < pokemob.getInventory()
                    .getContainerSize(); i++)
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
        if (!(evt.getEntity().getCommandSenderWorld() instanceof ServerWorld)) return;
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
        if (pokemob != null && evt.getSource().getEntity() instanceof PlayerEntity) evt.setAmount((float) (evt
                .getAmount() * PokecubeCore.getConfig().playerToPokemobDamageScale));

        // Some special handling for in wall stuff
        if (evt.getSource() == DamageSource.IN_WALL)
        {
            MobEntity toPush = pokemob != null ? pokemob.getEntity() : null;

            // Check if a player riding something, if so, compute a larger
            // hitbox and try to push out of the wall
            pokemob = CapabilityPokemob.getPokemobFor(evt.getEntity().getVehicle());
            final boolean playerRiding = evt.getEntity() instanceof PlayerEntity && pokemob != null;
            if (playerRiding) toPush = pokemob.getEntity();

            if (toPush != null)
            {
                evt.setCanceled(true);
                final ServerWorld world = (ServerWorld) evt.getEntity().getCommandSenderWorld();
                final AxisAlignedBB oldBox = evt.getEntity().getBoundingBox();
                final AxisAlignedBB newBox = toPush.getBoundingBox();

                // Take the larger of the boxes, collide off that.
                final AxisAlignedBB biggerBox = oldBox.minmax(newBox);

                final List<VoxelShape> hits = Lists.newArrayList();
                // Find all voxel shapes in the area
                BlockPos.betweenClosedStream(biggerBox).forEach(pos ->
                {
                    final BlockState state = world.getBlockState(pos);
                    final VoxelShape shape = state.getCollisionShape(world, pos);
                    if (!shape.isEmpty()) hits.add(shape.move(pos.getX(), pos.getY(), pos.getZ()));
                });

                // If there were any voxel shapes, then check if we need to
                // collidedw
                if (hits.size() > 0)
                {
                    VoxelShape total = VoxelShapes.empty();
                    // Merge the found shapes into a single one
                    for (final VoxelShape s : hits)
                        total = VoxelShapes.joinUnoptimized(total, s, IBooleanFunction.OR);
                    final List<AxisAlignedBB> aabbs = Lists.newArrayList();
                    // Convert to colliding AABBs
                    BlockEntityUpdater.fill(aabbs, biggerBox, total);
                    // Push off the AABBS if needed
                    final boolean col = BlockEntityUpdater.applyEntityCollision(toPush, biggerBox, aabbs,
                            Vector3d.ZERO);

                    // This gives us an indication if if we did actually
                    // collide, if this occured, then we need to do some extra
                    // processing to make sure that we fit properly
                    if (col)
                    {
                        Vector3 v = Vector3.getNewVector().set(toPush);
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
                final int exp = killer.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor, killed
                        .getBaseXP(), killed.getLevel());
                killer.setExp(exp, true);
            }
            if (owner != null)
            {
                final List<Entity> pokemobs = PCEventsHandler.getOutMobs(owner, false);
                pokemobs.removeIf(e -> !e.isAlive());
                for (final Entity mob : pokemobs)
                {
                    final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                    if (poke != null && poke.getEntity().getHealth() > 0 && ItemList.is(new ResourceLocation("pokecube",
                            "exp_share"), poke.getHeldItem()) && !poke.getLogicState(LogicStates.SITTING))
                    {
                        final int exp = poke.getExp() + Tools.getExp((float) PokecubeCore.getConfig().expScaleFactor,
                                killed.getBaseXP(), killed.getLevel());
                        poke.setExp(exp, true);
                    }
                }
            }
        }
    }

    private static void onLivingAttack(final LivingAttackEvent event)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getSource().getDirectEntity());
        if (pokemob != null) pokemob.setCombatState(CombatStates.NOITEMUSE, false);
    }

    private static void onLivingDeath(final LivingDeathEvent evt)
    {
        final DamageSource damageSource = evt.getSource();
        // Handle transferring the kill info over, This is in place for mod
        // support.
        if (damageSource instanceof PokemobDamageSource && evt.getEntity().getCommandSenderWorld() instanceof ServerWorld)
            damageSource.getDirectEntity().killed((ServerWorld) evt.getEntity().getCommandSenderWorld(),
                    (LivingEntity) evt.getEntity());

        // Handle exp gain for the mob.
        final IPokemob attacker = CapabilityPokemob.getPokemobFor(damageSource.getDirectEntity());
        if (attacker != null && damageSource.getDirectEntity() instanceof MobEntity) PokemobEventsHandler.handleExp(
                (MobEntity) damageSource.getDirectEntity(), attacker, (LivingEntity) evt.getEntity());
    }

    private static void onJoinWorld(final EntityJoinWorldEvent event)
    {
        final Entity mob = event.getEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setEntity((MobEntity) mob);
        final IPokemob modified = pokemob.onAddedInit();
        if (modified != pokemob)
        {
            pokemob.markRemoved();
            if (mob.getCommandSenderWorld() instanceof ServerWorld) mob.getCommandSenderWorld().addFreshEntity(modified.getEntity());
        }
        // This initializes logics on the client side.
        if (!(mob.getCommandSenderWorld() instanceof ServerWorld)) pokemob.initAI();
    }

    private static void onBrainInit(final BrainInitEvent event)
    {
        final Entity mob = event.getEntity();
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.initAI();
    }

    private static void onMobTracking(final StartTracking event)
    {
        // Sync genes over to players when they start tracking a pokemob
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getTarget());
        if (pokemob == null) return;
        if (!(event.getEntity() instanceof ServerPlayerEntity)) return;
        final PokedexEntry entry = pokemob.getPokedexEntry();

        final IMobGenetics genes = event.getTarget().getCapability(GeneRegistry.GENETICS_CAP).orElse(null);
        for (final Alleles<?, ?> allele : genes.getAlleles().values())
            PacketSyncGene.syncGene(event.getTarget(), allele, (ServerPlayerEntity) event.getPlayer());

        // Send the whole thing over in this case, as it means it won't
        // auto-sync things like IPokemob, etc.
        // TODO special packet for just our capabiltiies instead!
        if (!entry.stock) EntityUpdate.sendEntityUpdate(event.getTarget());
    }

    private static void onWorldTick(final WorldTickEvent evt)
    {
        for (final PlayerEntity player : evt.world.players())
            if (player.getVehicle() instanceof LivingEntity && CapabilityPokemob.getPokemobFor(player
                    .getVehicle()) != null)
            {
                final LivingEntity ridden = (LivingEntity) player.getVehicle();
                EntityTools.copyRotations(ridden, player);
            }
    }

    private static long mean(final long[] values)
    {
        long sum = 0L;
        for (final long v : values)
            sum += v;
        return sum / values.length;
    }

    private static void onServerTick(final ServerTickEvent event)
    {
        if (event.phase != Phase.END || !PokecubeCore.getConfig().doLoadBalancing) return;
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        final double meanTickTime = PokemobEventsHandler.mean(server.tickTimes) * 1.0E-6D;
        final double maxTick = 2;
        if (meanTickTime > maxTick)
        {
            final double factor = meanTickTime / maxTick;
            RootTask.doLoadThrottling = true;
            RootTask.runRate = (int) factor;
        }
        else RootTask.doLoadThrottling = false;
    }

    private static void onMobTick(final LivingUpdateEvent evt)
    {
        final LivingEntity living = evt.getEntityLiving();
        final World dim = living.getCommandSenderWorld();
        // Prevent moving if it is liable to take us out of a loaded area
        double dist = Math.sqrt(living.getDeltaMovement().x * living.getDeltaMovement().x + living.getDeltaMovement().z * living
                .getDeltaMovement().z);
        final boolean ridden = living.isVehicle();
        final boolean tooFast = ridden && !TerrainManager.isAreaLoaded(dim, living.blockPosition(), PokecubeCore
                .getConfig().movementPauseThreshold + dist);
        if (tooFast) living.setDeltaMovement(0, living.getDeltaMovement().y, 0);

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(living);
        if (pokemob instanceof DefaultPokemob && living instanceof EntityPokemob && dim instanceof ServerWorld)
        {
            final DefaultPokemob pokemobCap = (DefaultPokemob) pokemob;
            final EntityPokemob mob = (EntityPokemob) living;
            if (pokemobCap.returning)
            {
                mob.remove(false);
                evt.setCanceled(true);
                return;
            }
            if (pokemobCap.getOwnerId() != null) mob.setPersistenceRequired();
            final PlayerEntity near = mob.getCommandSenderWorld().getNearestPlayer(mob, -1);
            if (near != null && pokemob.getOwnerId() == null)
            {
                dist = near.distanceTo(mob);
                if (PokecubeCore.getConfig().cull && dist > PokecubeCore.getConfig().cullDistance)
                {
                    pokemobCap.onRecall();
                    evt.setCanceled(true);
                    return;
                }
                if (dist > PokecubeCore.getConfig().aiDisableDistance)
                {
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
                pokemob.getEntity().remove(false);
                return;
            }

            // Reset death time if we are not dead.
            if (evt.getEntityLiving().getHealth() > 0) evt.getEntityLiving().deathTime = 0;
            // Tick the logic stuff for this mob.
            for (final Logic l : pokemob.getTickLogic())
                if (l.shouldRun()) l.tick(living.getCommandSenderWorld());
        }
    }

    private static Map<DyeColor, ITag<Item>> getDyeTagMap()
    {
        if (PokemobEventsHandler.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            PokemobEventsHandler.DYETAGS.put(colour, ItemTags.getAllTags().getTagOrEmpty(tag));
        }
        return PokemobEventsHandler.DYETAGS;
    }

    private static void handleExp(final MobEntity pokemob, final IPokemob attacker, final LivingEntity attacked)
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
        if (attackedMob != null && attacked.getHealth() <= 0 && attacked.getPersistentData().getInt(
                "lastDeathTick") != attacked.tickCount)
        {
            attacked.getPersistentData().putInt("lastDeathTick", attacked.tickCount);
            boolean giveExp = !attackedMob.isShadow();
            final boolean pvp = attackedMob.getGeneralState(GeneralStates.TAMED) && attackedMob
                    .getOwner() instanceof PlayerEntity;
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
                attacker.setExp(attacker.getExp() + Tools.getExp((float) (pvp ? PokecubeCore
                        .getConfig().pvpExpMultiplier : PokecubeCore.getConfig().expScaleFactor), attackedMob
                                .getBaseXP(), attackedMob.getLevel()), true);
                final byte[] evsToAdd = attackedMob.getPokedexEntry().getEVs();
                attacker.addEVs(evsToAdd);
            }
            final Entity targetOwner = attackedMob.getOwner();
            attacker.displayMessageToOwner(new TranslationTextComponent("pokemob.action.faint.enemy", attackedMob
                    .getDisplayName()));
            if (targetOwner instanceof PlayerEntity && attacker.getOwner() != targetOwner) BrainUtils.initiateCombat(
                    pokemob, (LivingEntity) targetOwner);
            else BrainUtils.deagro(pokemob);
            if (attacker.getPokedexEntry().isFood(attackedMob.getPokedexEntry()) && attacker.getCombatState(
                    CombatStates.HUNTING))
            {
                attacker.eat(attackedMob.getEntity());
                attacker.setCombatState(CombatStates.HUNTING, false);
                pokemob.getNavigation().stop();
            }
        }
    }

    private static boolean handleHmAndSaddle(final PlayerEntity PlayerEntity, final IPokemob pokemob)
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

        if (rider instanceof ServerPlayerEntity && rider == pokemob.getOwner())
        {
            final PlayerEntity player = (PlayerEntity) rider;
            final IPermissionHandler handler = PermissionAPI.getPermissionHandler();
            final PlayerContext context = new PlayerContext(player);
            final Config config = PokecubeCore.getConfig();
            if (config.permsRide && !handler.hasPermission(player.getGameProfile(), Permissions.RIDEPOKEMOB, context))
                return false;
            if (config.permsRideSpecific && !handler.hasPermission(player.getGameProfile(), Permissions.RIDESPECIFIC
                    .get(entry), context)) return false;
        }
        final float scale = pokemob.getSize();
        final Vector3f dims = pokemob.getPokedexEntry().getModelSize();
        return dims.y * scale + dims.x * scale > rider.getBbWidth() && Math.max(dims.x, dims.z) * scale > rider.getBbWidth()
                * 1.4;
    }

    private static void processInteract(final PlayerInteractEvent evt, final Entity target)
    {
        if (!(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(target);
        if (pokemob == null) return;

        final ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        final Hand hand = evt.getHand();
        final ItemStack held = player.getItemInHand(hand);
        final MobEntity entity = pokemob.getEntity();

        final InteractEvent event = new InteractEvent(pokemob, player, evt);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() != Result.DEFAULT)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        // Item has custom entity interaction, let that run instead.
        if (held.getItem().interactLivingEntity(held, player, entity, hand) != ActionResultType.PASS)
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

        final PokedexEntry entry = pokemob.getPokedexEntry();

        // Check Pokedex Entry defined Interaction for player.
        if (entry.interact(player, pokemob, true))
        {
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }

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
                final Vector3 look = Vector3.getNewVector().set(player.getLookAngle()).scalarMultBy(1);
                look.y = 0.2;
                look.addVelocities(target);
                return;
            }
            // Debug thing to maximize happiness
            if (held.getItem() == Items.APPLE) if (player.abilities.instabuild && player.isShiftKeyDown()) pokemob
                    .addHappiness(255);
            // Debug thing to increase hunger time
            if (held.getItem() == Items.GOLDEN_HOE) if (player.abilities.instabuild && player.isShiftKeyDown()) pokemob
                    .applyHunger(+4000);
            // Use shiny charm to make shiny
            if (ItemList.is(new ResourceLocation("pokecube:shiny_charm"), held))
            {
                if (player.isShiftKeyDown())
                {
                    pokemob.setShiny(!pokemob.isShiny());
                    if (!player.abilities.instabuild) held.split(1);
                }
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
        }

        // is Dyeable
        if (!held.isEmpty() && entry.dyeable)
        {
            final IOptionalNamedTag<Item> dyeTag = Tags.Items.DYES;
            DyeColor dye = null;
            if (held.getItem().is(dyeTag))
            {
                final Map<DyeColor, ITag<Item>> tags = PokemobEventsHandler.getDyeTagMap();
                for (final DyeColor colour : DyeColor.values())
                    if (held.getItem().is(tags.get(colour)))
                    {
                        dye = colour;
                        break;
                    }
            }
            if (dye != null && (entry.validDyes.isEmpty() || entry.validDyes.contains(dye)))
            {
                pokemob.setDyeColour(dye.getId());
                if (!player.abilities.instabuild) held.shrink(1);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
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
            player.sendMessage(new TranslationTextComponent("pokemob.action.cannotuse"), Util.NIL_UUID);
            return;
        }

        boolean fits = isOwner;
        if (!fits && pokemob.getEntity() instanceof EntityPokemob) fits = ((EntityPokemob) pokemob.getEntity())
                .canAddPassenger(player);
        final boolean saddled = PokemobEventsHandler.handleHmAndSaddle(player, pokemob);

        final boolean guiAllowed = pokemob.getPokedexEntry().stock || held.getItem() == PokecubeItems.POKEDEX.get();

        final boolean saddleCheck = !player.isShiftKeyDown() && held.isEmpty() && fits && saddled;

        // Check if favourte berry and sneaking, if so, do breeding stuff.
        if (isOwner || player instanceof FakePlayer)
        {
            final int fav = Nature.getFavouriteBerryIndex(pokemob.getNature());
            if (PokecubeCore.getConfig().berryBreeding && (player.isShiftKeyDown() || player instanceof FakePlayer)
                    && !hasTarget && held.getItem() instanceof ItemBerry && (fav == -1 || fav == ((ItemBerry) held
                            .getItem()).type.index))
            {
                if (!player.abilities.instabuild)
                {
                    held.shrink(1);
                    if (held.isEmpty()) player.inventory.setItem(player.inventory.selected,
                            ItemStack.EMPTY);
                }
                pokemob.setReadyToMate(player);
                BrainUtils.clearAttackTarget(entity);
                entity.getCommandSenderWorld().broadcastEntityEvent(entity, (byte) 18);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
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
                    if (evolution != null) if (!player.abilities.instabuild)
                    {
                        held.shrink(1);
                        if (held.isEmpty()) player.inventory.setItem(player.inventory.selected,
                                ItemStack.EMPTY);
                    }
                    evt.setCanceled(true);
                    evt.setCancellationResult(ActionResultType.SUCCESS);
                    return;
                }
                // Otherwise check if useable item.
                final IPokemobUseable usable = IPokemobUseable.getUsableFor(held);
                if (usable != null)
                {
                    final ActionResult<ItemStack> result = usable.onUse(pokemob, held, player);
                    if (result.getResult() == ActionResultType.SUCCESS)
                    {
                        player.setItemInHand(hand, result.getObject());
                        pokemob.setCombatState(CombatStates.NOITEMUSE, true);
                        evt.setCanceled(true);
                        evt.setCancellationResult(ActionResultType.SUCCESS);
                        return;
                    }
                }
            }
            // Open Gui
            if (!saddleCheck && guiAllowed)
            {
                PacketPokemobGui.sendOpenPacket(entity, player);
                evt.setCanceled(true);
                evt.setCancellationResult(ActionResultType.SUCCESS);
                return;
            }
        }
        // Check saddle for riding.
        if (saddleCheck)
        {
            entity.setJumping(false);
            evt.setCanceled(true);
            evt.setCancellationResult(ActionResultType.SUCCESS);
            return;
        }
    }
}
