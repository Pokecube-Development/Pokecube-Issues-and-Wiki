package pokecube.legends.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.utils.EntityTools;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.init.EntityInit;
import pokecube.legends.spawns.WormholeSpawns;
import pokecube.legends.spawns.WormholeSpawns.IWormholeWorld;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.attachments.Energy;
import thut.api.attachments.Linkable.ILinkStorage;
import thut.api.entity.teleporting.TeleDest;
import thut.api.entity.teleporting.ThutTeleporter;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@EventBusSubscriber(modid = Reference.ID)
public class WormholeEntity extends LivingEntity implements IEntityWithComplexSpawn
{
    private static final List<ResourceKey<Level>> _sorted = Lists.newArrayList();

    private static final Map<ResourceKey<Level>, Float> _mapped = Maps.newHashMap();

    public static final Float DEFAULT_WEIGHT = 10f;

    public static final Map<ResourceKey<Level>, Float> WEIGHTED_DIM_MAP = Maps.newHashMap();

    public static final Set<ResourceKey<Level>> NO_HOLES = Sets.newHashSet();

    private static int lastCheck = 0;

    public static int maxWormholeEnergy = 1000000;
    public static int wormholeEnergyPerTick = 1000;
    public static int wormholeEntityPerTP = 100000;
    public static int wormholeReUseDelay = 10;

    public static void clear()
    {
        WormholeEntity._sorted.clear();
    }

    private static ResourceKey<Level> getTargetWorld(final ServerLevel source, final float rng)
    {
        final Set<ResourceKey<Level>> worlds = source.getServer().levelKeys();
        if (WormholeEntity._sorted.isEmpty() || WormholeEntity.lastCheck < worlds.size())
        {
            WormholeEntity._sorted.clear();
            WormholeEntity._mapped.clear();
            lastCheck = worlds.size();
            float total = 0;
            for (final ResourceKey<Level> world : WormholeEntity.WEIGHTED_DIM_MAP.keySet())
            {
                total += WormholeEntity.WEIGHTED_DIM_MAP.getOrDefault(world, WormholeEntity.DEFAULT_WEIGHT);
                WormholeEntity._mapped.put(world, total);
            }
            for (final ResourceKey<Level> world : worlds)
            {
                if (WormholeEntity.NO_HOLES.contains(world) || WormholeEntity._mapped.containsKey(world)) continue;
                total += WormholeEntity.WEIGHTED_DIM_MAP.getOrDefault(world, WormholeEntity.DEFAULT_WEIGHT);
                WormholeEntity._mapped.put(world, total);
            }
            float current = 0;
            for (final ResourceKey<Level> world : Sets.newHashSet(WormholeEntity._mapped.keySet()))
            {
                if (WormholeEntity.NO_HOLES.contains(world)) continue;
                current += WormholeEntity._mapped.get(world) / total;
                WormholeEntity._mapped.put(world, current);
                WormholeEntity._sorted.add(world);
            }
        }
        if (WormholeEntity._sorted.isEmpty()) return source.dimension();
        ResourceKey<Level> dim = WormholeEntity._sorted.getFirst();
        for (int i = 1; i < WormholeEntity._sorted.size(); i++)
        {
            dim = WormholeEntity._sorted.get(i - 1);
            final float prev = WormholeEntity._mapped.get(dim);
            final float here = WormholeEntity._mapped.get(WormholeEntity._sorted.get(i));
            if (prev < rng && here >= rng)
            {
                dim = WormholeEntity._sorted.get(i);
                break;
            }
        }
        return dim;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseGeneral(PlayerInteractEvent.EntityInteract event)
    {
        // Cancel interaction if wormhole is on cooldown
        if(event.getTarget() instanceof WormholeEntity wormhole)
        {
            if(wormhole.interact_timer>0) event.setCanceled(true);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemUseSpecfic(PlayerInteractEvent.EntityInteractSpecific event)
    {
        // Cancel interaction if wormhole is on cooldown
        if(event.getTarget() instanceof WormholeEntity wormhole)
        {
            if(wormhole.interact_timer>0) event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTeleport(final EntityTeleportEvent event)
    {
        Entity entity = event.getEntity();
        final Level world = entity.level;
        if (world.isClientSide()) return;
        if (!(world instanceof ServerLevel)) return;

        final long lastTp = entity.getPersistentData().getLong("pokecube_legends:uwh_use")
                + WormholeEntity.wormholeReUseDelay;
        final long now = Tracker.instance().getTick();

        if (now < lastTp) return;

        final IWormholeWorld holes = WormholeSpawns.getWormholes(world);
        if (holes == null) return;

        final double chance = ItemList.is(WormholeSpawns.SPACE_WORMS, entity) ? WormholeSpawns.teleWormholeChanceWorms
                : WormholeSpawns.teleWormholeChanceNormal;

        final RandomSource rand = world.getRandom();
        if (rand.nextDouble() > chance) return;

        final Vector3 pos = new Vector3().set(event.getPrevX(), event.getPrevY()+1, event.getPrevZ());
        final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(world);
        pos.moveEntity(wormhole);
        holes.addWormhole(wormhole.getAnchorPos().getPos().pos());

        // If it is a pokemob, check if holding a location linker, if so, use
        // that for destination of the wormhole!
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null)
        {
            ILinkStorage link = null;
            for (int i = 0; i < pokemob.getInventory().getContainerSize() && link == null; i++)
            {
                final ItemStack test = pokemob.getInventory().getItem(i);
                var storage = ThutCaps.getLinkStorage(test);
                if (storage != null)
                {
                    link = ThutCaps.getLinkStorage(test).withContext(entity.registryAccess()).link();
                    if(link != null) break;
                }
            }
            if (link != null)
            {
                GlobalPos linked_pos = link.getLinkedPos(entity);
                if (linked_pos != null)
                {
                    linked_pos = GlobalPos.of(linked_pos.dimension(), linked_pos.pos());
                    wormhole.setDest(new TeleDest().setPos(linked_pos));
                }
            }
        }
        world.addFreshEntity(wormhole);
    }

    public static final LoadingValidationCallback TICKET_VALIDATOR = (level, helper)->{
        for(UUID uuid: helper.getEntityTickets().keySet().stream().toList()){
            if(level.getEntity(uuid)==null||!PokecubeLegends.config.wormholesChunkload)
            {
                helper.removeAllTickets(uuid);
            }
        }
    };
    public static final TicketController WORMHOLETICKETS = new TicketController(ResourceLocation.fromNamespaceAndPath(Reference.ID,"wormholes"), TICKET_VALIDATOR);

    @SubscribeEvent
    public static void onRegisterTicketControllersEvent(RegisterTicketControllersEvent event)
    {
        if(PokecubeLegends.config.wormholesChunkload) event.register(WORMHOLETICKETS);
    }

    private static final EntityDataAccessor<Byte> ACTIVE_STATE = SynchedEntityData.defineId(WormholeEntity.class,
            EntityDataSerializers.BYTE);

    private TeleDest dest = null;
    private TeleDest anchorPos = null;
    private Vec3 dir = null;

    public EnergyStorage energy;

    int timer = 0, uses = 0, exit_id=0, interact_timer;

    private boolean stable = false, forced=false;
    WormholeEntity exit_entity = null;

    public WormholeEntity(final EntityType<? extends LivingEntity> type, final Level level)
    {
        super(type, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(WormholeEntity.ACTIVE_STATE, (byte) 0);
    }

    public boolean isOpening()
    {
        return (this.entityData.get(WormholeEntity.ACTIVE_STATE) & 1) != 0;
    }

    public boolean isIdle()
    {
        return (this.entityData.get(WormholeEntity.ACTIVE_STATE) & 2) != 0;
    }

    public boolean isClosing()
    {
        return (this.entityData.get(WormholeEntity.ACTIVE_STATE) & 4) != 0;
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        if (nbt.contains("warp_dest"))
        {
            final CompoundTag tag = nbt.getCompound("warp_dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        if (nbt.contains("anchor_pos"))
        {
            final CompoundTag tag = nbt.getCompound("anchor_pos");
            this.anchorPos = TeleDest.readFromNBT(tag);
        }
        if (nbt.contains("face_dir"))
        {
            final CompoundTag tag = nbt.getCompound("face_dir");
            this.setDir(new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")));
        }
        this.stable = nbt.getBoolean("stable");
        this.timer = nbt.getInt("timer");
        this.uses = nbt.getInt("uses");
        this.entityData.set(WormholeEntity.ACTIVE_STATE, nbt.getByte("state"));
    }

    private boolean makingDest = false;

    public void setDest(final TeleDest d)
    {
        final ResourceKey<Level> key = d.getPos().dimension();
        final ServerLevel dest = this.getServer().getLevel(key);
        final IWormholeWorld holes = WormholeSpawns.getWormholes(level);
        this.makingDest = true;
        EventsHandler.Schedule(dest, w -> {
            this.dest = d;
            final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(dest);
            wormhole.moveTo(this.dest.getPos().pos(), 0, 0);
            wormhole.dest = this.getAnchorPos();
            wormhole.anchorPos = this.dest;
            holes.addWormhole(this.dest.getPos().pos());
            dest.addFreshEntity(wormhole);
            EntityUpdate.sendEntityUpdate(this);
            return true;
        });
    }

    public TeleDest getDest()
    {
        if (this.dest == null) if (this.level instanceof ServerLevel)
        {
            if (this.makingDest) return new TeleDest().setPos(GlobalPos
                    .of(this.level != null ? this.level.dimension() : Level.OVERWORLD, this.getOnPos().above(20)));
            final RandomSource rng = this.getRandom();
            final ResourceKey<Level> key = WormholeEntity.getTargetWorld((ServerLevel) this.level, rng.nextFloat());
            ServerLevel dest = this.getServer().getLevel(key);
            if (dest == null)
            {
                dest = (ServerLevel) this.level;
                PokecubeAPI.LOGGER.error("Warning, Wormhole had invalid exit dimension {}", key);
            }
            final WorldBorder border = dest.getWorldBorder();
            final IWormholeWorld holes = WormholeSpawns.getWormholes(level);
            this.makingDest = true;
            EventsHandler.Schedule(dest, w -> {
                final int x = (int) ((border.getMaxX() - border.getMinX()) * rng.nextDouble() + border.getMinX());
                final int z = (int) ((border.getMaxZ() - border.getMinZ()) * rng.nextDouble() + border.getMinZ());
                final ServerLevel world = (ServerLevel) w;
                this.dest = new TeleDest()
                        .setPos(GlobalPos.of(key, WormholeSpawns.getWormholePos(world, new BlockPos(x, 0, z))));

                final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(world);
                wormhole.moveTo(this.dest.getPos().pos(), 0, 0);
                wormhole.dest = this.getAnchorPos();
                wormhole.anchorPos = this.dest;
                holes.addWormhole(this.dest.getPos().pos());
                world.addFreshEntity(wormhole);

                EntityUpdate.sendEntityUpdate(this);
                return true;
            });
        }
        else this.dest = new TeleDest().setPos(
                GlobalPos.of(this.level != null ? this.level.dimension() : Level.OVERWORLD, this.getOnPos().above(20)));
        return this.dest;
    }

    public TeleDest getAnchorPos()
    {
        if (this.anchorPos == null) this.anchorPos = new TeleDest()
                .setPos(GlobalPos.of(this.level != null ? this.level.dimension() : Level.OVERWORLD, this.getOnPos()));
        return this.anchorPos;
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        ResourceLocation LINKER = ResourceLocation.parse("pokecube_adventures:linker");
        ResourceLocation ENDERPEARL = ResourceLocation.parse("minecraft:ender_pearl");
        ItemStack stack = player.getItemInHand(InteractionHand.OFF_HAND);
        boolean pearl = ItemList.is(ENDERPEARL, stack);
        boolean linker = ItemList.is(LINKER, player.getItemInHand(InteractionHand.MAIN_HAND));
        // Allow rotating the wormhole
        if (linker && pearl)
        {
            this.setDir(player.position().subtract(this.position()).normalize());
            return InteractionResult.CONSUME;
        }
        // 3s delay before another interaction, this allows placing blocks in the space of the wormhole
        interact_timer = 60;
        return super.interact(player, hand);
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force)
    {
        return false;
    }

    @Override
    public boolean hurt(final DamageSource source, final float amount)
    {
        this.interact_timer = 60;
        // Allow a /kill command to work
        if (amount == Float.MAX_VALUE)
        {
            this.discard();
        }
        return amount == Float.MAX_VALUE;
    }

    @Override
    public void tick()
    {
        if (this.getDest() == null) return;
        this.energy = Energy.get(this);
        super.tick();

        interact_timer--;
        // When on cooldown, allow building inside the hitbox
        this.blocksBuilding = interact_timer<0;

        this.getAnchorPos();
        this.getDir();

        if(this.getVehicle()!=null)
        {
            var _pos = this.getAnchorPos().getPos().pos();
            int x = _pos.getX(), y = _pos.getY(), z = _pos.getZ();
            this.dismountTo(x, y, z);
        }

        if (!this.level.isClientSide())
        {
            float old_rot = this.yRotO;
            // Only do this rotation server side, let it sync to client.
            float yRot = (float) -Mth.atan2(this.getDir().x, this.getDir().z) * (180F / (float) Math.PI);
            float xRot = 0;

            if(yRot != old_rot)
            {
                this.setXRot(this.xRotO = xRot);
                this.setYRot(this.yRotO = yRot);
                this.yBodyRot = this.yBodyRotO = yRot;
                this.yHeadRot = this.yHeadRotO = yRot;
                EntityUpdate.sendEntityUpdate(this);
            }
        }

        this.setNoGravity(true);

        if (!this.isIdle() && !this.isClosing() && !this.isOpening())
            if (this.level() instanceof ServerLevel) this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 1);

        if (this.isOpening()) if (this.timer++ > 300)
        {
            if (this.level() instanceof ServerLevel) this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 2);
            this.timer = 0;
        }

        if (this.isClosing() && this.timer++ > 300)
        {
            if (this.level instanceof ServerLevel)
            {
                final IWormholeWorld holes = WormholeSpawns.getWormholes(level);
                holes.removeWormhole(this.getAnchorPos().getPos().pos());
                holes.getWormholes().clear();

                final ServerLevel dest = this.getServer().getLevel(this.getDest().getPos().dimension());
                EventsHandler.Schedule(dest, w -> {
                    dest.getChunk(this.dest.getPos().pos());
                    final AABB box = new AABB(this.getDest().getPos().pos()).inflate(10);
                    final List<WormholeEntity> list = w.getEntitiesOfClass(WormholeEntity.class, box);
                    for (final WormholeEntity e : list)
                    {
                        if (this.level() instanceof ServerLevel)
                            e.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 4);
                        e.energy.receiveEnergy(this.energy.getEnergyStored(), false);
                        e.timer = 0;
                    }
                    return true;
                });
                final float boom = 0.5f * this.uses;
                if (boom > 0) this.level.explode(this, this.getX(), this.getY(), this.getZ(), boom, Level.ExplosionInteraction.NONE);
                this.discard();
            }
            return;
        }

        this.energy.receiveEnergy(WormholeEntity.wormholeEnergyPerTick, false);
        // Stable wormholes just lose all of their energy.
        if (this.stable)
        {
            this.energy.extractEnergy(Integer.MAX_VALUE, false);
            if (this.level() instanceof ServerLevel) this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 2);
            this.timer = 0;
        }

        final Vector3 anchor = this.getAnchorPos().getTeleLoc();
        // Uses width, as we have smaller height for block placements
        final Vec3 origin = new Vec3(anchor.x, anchor.y, anchor.z);
        final Vec3 here = this.position();
        final Vec3 diff = origin.subtract(here);
        final Vec3 v = this.getDeltaMovement();
        final double s = 0.05;
        this.setDeltaMovement(v.x + diff.x * s, v.y + diff.y * s, v.z + diff.z * s);

        // Check if destination hole exists, if so, we will average our energy with theirs.
        var other = this.getExitEntity();
        if(other != null && other.energy != null)
        {
            other.energy = this.energy;
            Energy.set(other, this.energy);
            // Once per second update the wormholes
            if(Tracker.instance().getTick()%20==this.getRandom().nextInt(20))
                EntityUpdate.sendEntityUpdate(other);
        }

        // Collapse at full energy
        if (!this.stable && this.energy.getEnergyStored() >= WormholeEntity.maxWormholeEnergy && !this.isClosing())
        {
            this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 4);
            this.timer = 0;
        }
    }

    public WormholeEntity getExitEntity()
    {
        if(!(this.level() instanceof ServerLevel serverLevel)) return null;

        WormholeEntity other = exit_entity;
        if(other!=null && !other.isAlive())
        {
            other = exit_entity = null;
            exit_id = 0;
        }
        if(other == null)
        {
            var dest = getDest();
            ServerLevel otherLevel = serverLevel.getServer().getLevel(dest.getPos().dimension());
            if(!forced && PokecubeLegends.config.wormholesChunkload)
            {
                var pos = new ChunkPos(dest.getPos().pos());
                forced = true;
                int loadR = PokecubeLegends.config.wormholesChunkloadRadius;
                // Load chunks around the wormhole
                for(int i = -loadR; i<=loadR;i++)
                    for(int j = -loadR; j<=loadR;j++)
                        forced = WORMHOLETICKETS.forceChunk(otherLevel, this, pos.x+i, pos.z+j, true, true) && forced;
            }
            if (exit_id > 0)
            {
                var test = otherLevel.getEntity(exit_id);
                if (test instanceof WormholeEntity e) other = e;
            }
            else if (otherLevel.isAreaLoaded(dest.getTeleLoc().getPos(), 5))
            {
                var box = dest.getTeleLoc().getAABB();
                var _box = box.inflate(5);
                Predicate<WormholeEntity> valid = h-> h.getAnchorPos().withinDist(dest, 5) && h!=this;
                EntityTypeTest<Entity, WormholeEntity> test = EntityInit.WORMHOLE.get();
                List<WormholeEntity> opts = new ArrayList<>();
                otherLevel.getEntities(test, _box, valid, opts, 10);
                if (!opts.isEmpty())
                {
                    other = opts.getFirst();
                }
            }
        }
        exit_entity = other;
        exit_id = other==null?0:other.getId();
        if (other != null)
        {
            // Sync destinations
            other.dest = this.getAnchorPos();
            this.dest = other.getAnchorPos();

            other.exit_entity = this;
            other.exit_id = this.getId();
            // If any are stable, both are stable
            other.stable = this.stable = this.stable|other.stable;
            other.uses = this.uses = Math.max(this.uses, other.uses);
            other.timer = this.timer = Math.max(other.timer, this.timer);
        }
        return exit_entity;
    }

    @Override
    public PushReaction getPistonPushReaction()
    {
        return PushReaction.IGNORE;
    }

    @Override
    protected void pushEntities()
    {
        if (!this.isIdle()||this.level().isClientSide()) return;

        AABB box = this.getBoundingBox();
        Vec3 _centre = box.getCenter(), // Up is 0,1,0 for now, later we can add pitch if needed
                localUp = new Vec3(0,1,0),
                _localFwd = this.getDir().normalize(),
                localLeft = _localFwd.cross(localUp).normalize();
        Vec3 localFwd = localLeft.cross(localUp).normalize();
        // move the teleport plane back a bit from centre
        Vec3 centre = _centre.add(localFwd.scale(box.getXsize()/4));

        final List<Entity> list = this.level.getEntities(this, box,
                e -> (e.getVehicle() == null));
        final Set<UUID> tpd = Sets.newHashSet();

        if (!list.isEmpty()) for (Entity _entity : list)
        {
            Entity entity = EntityTools.getCoreEntity(_entity);

            // These cannot go through a wormhole.
            if (ItemList.is(WormholeSpawns.SPACE_ANCHORED, entity)) continue;
            // These could be ones teleported as riders of existing things
            if (entity.level() != this.level() || !entity.isAddedToLevel())
            {
                continue;
            }

            var track = ThutCaps.getPositionTracker(entity);

            var entityBox = entity.getBoundingBox();
            final Vec3 entityCentre = entityBox.getCenter(), entityV = track.getVelocity();
            final Vec3 dMidV = entityCentre.subtract(centre), dMidVNext=dMidV.add(entityV);
            // Ensure the middle of intersects our midplane, plus or minus a block
            double dMid = dMidV.dot(localFwd)+1, dMidNext=dMidVNext.dot(localFwd)+1;
            // If we are not within 1 block of the centre, and will pass it next tick, skip
            if(Math.abs(dMid) > 1 && Math.signum(dMidNext)==Math.signum(dMid)) continue;
            // Only transport things going the correct direction
            if(entityV.dot(localFwd)<=0) continue;


            final long lastTp = entity.getPersistentData().getLong("pokecube_legends:uwh_use")
                    + WormholeEntity.wormholeReUseDelay;
            final long now = Tracker.instance().getTick();
            final UUID uuid = entity.getUUID();
            if (now < lastTp || tpd.contains(uuid)) continue;
            if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Transfering {} through a wormhole!", entity);
            tpd.add(uuid);
            entity.getPersistentData().putLong("pokecube_legends:uwh_use", now);

            TeleDest dest = this.getDest();
            dest = new TeleDest().setLoc(dest.getPos(), dest.getLoc().copy());

            Vec3 postV = Vec3.ZERO;
            // Recompute this post transfer, so we are probably loaded if a player went through at least?
            WormholeEntity other = getExitEntity();
            float dYaw = 0;
            if (other != null)
            {
                // Eject target in direction wormhole faces.
                Vec3 newV;
                // Construct the two orthonormal coordinate systems

                Vec3 newFwd = other.getDir().normalize(),
                        newUp = new Vec3(0, 1, 0),
                        newLeft = newFwd.cross(newUp).scale(-1).normalize();
                newFwd = newLeft.cross(newUp).normalize(); // New direction is flipped

                // Initial shift is forwards 1 block, no vertical shift
                Vec3 _dMidV = entityCentre.subtract(this.getAnchorPos().getTeleLoc().toVec3d());
                Vec3 shift = transform(_dMidV, localFwd, localUp, localLeft, newFwd, newUp, newLeft).add(newFwd);
                dest.shift(shift.x, shift.y, shift.z);

                // Project velocity onto it
                newV = transform(entityV, localFwd, localUp, localLeft, newFwd, newUp, newLeft);
                if (newV.lengthSqr() < 0.06)
                    newV.add(newFwd.scale(0.25)); // Then add some ejection speed if too slow
                entity.setDeltaMovement(postV=newV);

                // Project rotation
                Vec3 oldEntityFwd = entity.getForward();
                newV = transform(oldEntityFwd, localFwd, localUp, localLeft, newFwd, newUp, newLeft);

                float yRot = (float) -Mth.atan2(newV.x, newV.z) * (180F / (float) Math.PI);
                float oldRot = entity.getYRot();
                dYaw = yRot - oldRot;

                if(other.level()==this.level())
                {
                    entity.hasImpulse = true;
                    if(entity instanceof Projectile)
                    {
                        // Recomputes from velocity
                        entity.setXRot(0.0f);
                        entity.setYRot(0.0f);
                    }
                    else
                    {
                        entity.setYBodyRot(oldRot+dYaw);
                        entity.setYHeadRot(entity.getYHeadRot() + dYaw);
                        entity.setYRot(yRot);
                        entity.yRotO += dYaw;
                    }
                }
            }
            else entity.setDeltaMovement(0, 0, 0);
            Vec3 _postV = postV;
            float dy = dYaw;
            Consumer<Entity> postTransfer = (e2)-> {
                if(e2==entity) e2.setDeltaMovement(_postV);
                e2.hasImpulse = true;
                if(e2 instanceof Projectile)
                {
                    // Recomputes from velocity
                    e2.setXRot(0.0f);
                    e2.setYRot(0.0f);
                }
                else if(other.level()!=this.level())
                {
                    float yRot = e2.getYRot() + dy;
                    e2.setYBodyRot(yRot);
                    e2.setYHeadRot(e2.getYHeadRot() + dy);
                    e2.setYRot(yRot);
                    e2.yRotO += dy;
                }
            };

            Set<Entity> involved = new HashSet<>();
            getRecursivePassengers(entity, involved);
            this.energy.receiveEnergy(WormholeEntity.wormholeEntityPerTP * involved.size(), false);
            ThutTeleporter.transferTo(entity, dest, true, postTransfer);
            for(Entity e2:involved) e2.getPersistentData().putLong("pokecube_legends:uwh_use", now);

            this.uses++;
            this.energy.receiveEnergy(WormholeEntity.wormholeEntityPerTP, false);
        }
    }

    void getRecursivePassengers(Entity entity, Set<Entity> found)
    {
        if(found.add(entity)) for(var e: entity.getPassengers())
        {
            if(!found.contains(e)) getRecursivePassengers(e, found);
        }
    }

    public Vec3 transform(Vec3 v, Vec3 fwdO, Vec3 upO, Vec3 leftO, Vec3 fwd, Vec3 up, Vec3 left)
    {
        return fwd.scale(fwdO.dot(v)).add(left.scale(leftO.dot(v))).add(up.scale(upO.dot(v)));
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag nbt)
    {
        CompoundTag tag = new CompoundTag();
        var dest = this.getDest();
        if (dest != null)
        {
            dest.writeToNBT(tag);
            nbt.put("warp_dest", tag);
        }
        tag = new CompoundTag();
        this.getAnchorPos().writeToNBT(tag);
        nbt.put("anchor_pos", tag);
        tag = new CompoundTag();
        tag.putDouble("x", this.getDir().x);
        tag.putDouble("y", this.getDir().y);
        tag.putDouble("z", this.getDir().z);
        nbt.put("face_dir", tag);
        nbt.putInt("timer", this.timer);
        nbt.putInt("uses", this.uses);
        nbt.putBoolean("stable", this.stable);
        nbt.putByte("state", this.entityData.get(WormholeEntity.ACTIVE_STATE));
    }

    @Override
    public float getHealth()
    {
        return Float.MAX_VALUE;
    }

    @Override
    public boolean isAffectedByPotions()
    {
        return false;
    }

    @Override
    public boolean attackable()
    {
        return false;
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    public boolean isPickable()
    {
        // This needs to be true for ability to right click for
        // changing direction, as well as for nbt editing to stable
        return interact_timer<=0;
    }

    @Override
    public boolean isDeadOrDying()
    {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(final DamageSource source)
    {
        return true;
    }

    public static final ProjectileDeflection WORMHOLE = (projectile, wormhole, rng) -> {
    };
    @Override
    public ProjectileDeflection deflection(Projectile projectile)
    {
        return WORMHOLE;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(final EquipmentSlot p_184582_1_)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(final EquipmentSlot p_184201_1_, final ItemStack p_184201_2_)
    {}

    @Override
    public HumanoidArm getMainArm()
    {
        return HumanoidArm.LEFT;
    }

    public Vec3 getDir()
    {
        if (this.dir == null)
        {
            this.setYRot(new Random(this.getUUID().getLeastSignificantBits()).nextFloat() * 360);
            this.yRotO = this.getYRot();
            this.yHeadRot = this.getYRot();
            this.yHeadRotO = this.yRotO;
            this.yBodyRot = this.getYRot();
            this.yBodyRotO = this.yRotO;
            this.dir = this.getLookAngle();
        }
        return this.dir;
    }

    public void setDir(final Vec3 dir)
    {
        this.dir = dir;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer)
    {
        var tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        buffer.writeNbt(tag);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData)
    {
        var tag = additionalData.readNbt();
        this.readAdditionalSaveData(tag);
    }
}
