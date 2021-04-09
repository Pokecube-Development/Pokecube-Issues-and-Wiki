package pokecube.legends.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.utils.EntityTools;
import pokecube.legends.init.EntityInit;
import pokecube.legends.spawns.WormholeSpawns;
import pokecube.legends.spawns.WormholeSpawns.IWormholeWorld;
import thut.api.Tracker;
import thut.api.entity.ThutTeleporter;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.core.common.network.EntityUpdate;

public class WormholeEntity extends LivingEntity
{
    private static final List<RegistryKey<World>> _sorted = Lists.newArrayList();

    private static final Map<RegistryKey<World>, Float> _mapped = Maps.newHashMap();

    public static final Float DEFAULT_WEIGHT = 10f;

    public static final Map<RegistryKey<World>, Float> WEIGHTED_DIM_MAP = Maps.newHashMap();

    public static final Set<RegistryKey<World>> NO_HOLES = Sets.newHashSet();

    private static int lastCheck = 0;

    public static void clear()
    {
        WormholeEntity._sorted.clear();
    }

    private static RegistryKey<World> getTargetWorld(final ServerWorld source, final float rng)
    {
        final Set<RegistryKey<World>> worlds = source.getServer().levelKeys();
        if (WormholeEntity._sorted.isEmpty() || WormholeEntity.lastCheck < worlds.size())
        {
            WormholeEntity._sorted.clear();
            WormholeEntity._mapped.clear();
            float total = 0;
            for (final RegistryKey<World> world : worlds)
            {
                if (WormholeEntity.NO_HOLES.contains(world)) continue;
                total += WormholeEntity.WEIGHTED_DIM_MAP.getOrDefault(world, WormholeEntity.DEFAULT_WEIGHT);
                WormholeEntity._mapped.put(world, total);
            }
            for (final RegistryKey<World> world : worlds)
            {
                if (WormholeEntity.NO_HOLES.contains(world)) continue;
                WormholeEntity._mapped.put(world, WormholeEntity._mapped.get(world) / total);
            }
        }
        if (WormholeEntity._sorted.isEmpty()) return source.dimension();
        RegistryKey<World> dim = WormholeEntity._sorted.get(0);
        for (int i = 1; i < WormholeEntity._sorted.size(); i++)
        {
            dim = WormholeEntity._sorted.get(i - 1);
            final float prev = WormholeEntity._mapped.get(dim);
            final float here = WormholeEntity._mapped.get(WormholeEntity._sorted.get(i));
            if (prev < rng && here >= rng) return WormholeEntity._sorted.get(i);
        }
        return source.dimension();
    }

    public static class EnergyStore extends EnergyStorage implements ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public EnergyStore()
        {
            super(1000000);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putInt("E", this.energy);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.energy = nbt.getInt("E");
        }
    }

    private static final DataParameter<Byte> ACTIVE_STATE = EntityDataManager.defineId(WormholeEntity.class,
            DataSerializers.BYTE);

    private TeleDest dest = null;
    private TeleDest pos  = null;
    private Vector3d dir  = null;

    public EnergyStore energy;

    int timer = 0;
    int uses  = 0;

    public WormholeEntity(final EntityType<? extends LivingEntity> type, final World level)
    {
        super(type, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(WormholeEntity.ACTIVE_STATE, (byte) 0);
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
    public void readAdditionalSaveData(final CompoundNBT nbt)
    {
        if (nbt.contains("warp_dest"))
        {
            final CompoundNBT tag = nbt.getCompound("warp_dest");
            this.dest = TeleDest.readFromNBT(tag);
        }
        if (nbt.contains("anchor_pos"))
        {
            final CompoundNBT tag = nbt.getCompound("anchor_pos");
            this.pos = TeleDest.readFromNBT(tag);
        }
        if (nbt.contains("face_dir"))
        {
            final CompoundNBT tag = nbt.getCompound("face_dir");
            this.setDir(new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")));
        }
        this.timer = nbt.getInt("timer");
        this.uses = nbt.getInt("uses");
        this.entityData.set(WormholeEntity.ACTIVE_STATE, nbt.getByte("state"));
    }

    private boolean makingDest = false;

    public TeleDest getDest()
    {
        if (this.dest == null) if (this.level instanceof ServerWorld)
        {
            if (this.makingDest) return new TeleDest().setPos(GlobalPos.of(this.level != null ? this.level.dimension()
                    : World.OVERWORLD, this.getOnPos().above(20)));
            final Random rng = this.getRandom();
            final RegistryKey<World> key = WormholeEntity.getTargetWorld((ServerWorld) this.level, rng.nextFloat());
            final ServerWorld dest = this.getServer().getLevel(key);
            final WorldBorder border = dest.getWorldBorder();
            final IWormholeWorld holes = this.level.getCapability(WormholeSpawns.WORMHOLES_CAP).orElse(null);
            this.makingDest = true;
            EventsHandler.Schedule(dest, w ->
            {
                final int x = (int) ((border.getMaxX() - border.getMinX()) * rng.nextDouble() + border.getMinX());
                final int z = (int) ((border.getMaxZ() - border.getMinZ()) * rng.nextDouble() + border.getMinZ());

                // Ensure the chunk exists before the height check below occurs.
                dest.getChunk(new BlockPos(x, 0, z));

                final int y = dest.getHeight(Type.WORLD_SURFACE, x, z) + 2 + rng.nextInt(10);

                this.dest = new TeleDest().setPos(GlobalPos.of(key, new BlockPos(x, y, z)));

                final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(dest);
                wormhole.moveTo(this.dest.getPos().pos(), 0, 0);
                wormhole.dest = this.getPos();
                wormhole.pos = this.dest;
                holes.addWormhole(this.dest.getPos().pos());
                dest.addFreshEntity(wormhole);

                EntityUpdate.sendEntityUpdate(this);
                return true;
            });
        }
        else this.dest = new TeleDest().setPos(GlobalPos.of(this.level != null ? this.level.dimension()
                : World.OVERWORLD, this.getOnPos().above(20)));
        return this.dest;
    }

    public TeleDest getPos()
    {
        if (this.pos == null) this.pos = new TeleDest().setPos(GlobalPos.of(this.level != null ? this.level.dimension()
                : World.OVERWORLD, this.getOnPos()));
        return this.pos;
    }

    @Override
    public ActionResultType interact(final PlayerEntity p_184230_1_, final Hand p_184230_2_)
    {
        return super.interact(p_184230_1_, p_184230_2_);
    }

    @Override
    public void tick()
    {
        if (this.getDest() == null) return;
        super.tick();

        this.getPos();
        this.getDir();

        this.yRot = new Random(this.getUUID().getLeastSignificantBits()).nextFloat() * 360;

        this.yHeadRot = this.yRot;
        this.yHeadRotO = this.yRotO;
        this.yBodyRot = this.yRot;
        this.yBodyRotO = this.yRotO;

        this.setNoGravity(true);

        if (!this.isIdle() && !this.isClosing() && !this.isOpening()) this.entityData.set(WormholeEntity.ACTIVE_STATE,
                (byte) 1);

        if (this.isOpening()) if (this.timer++ > 300)
        {
            this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 2);
            this.timer = 0;
        }

        if (this.isClosing() && this.timer++ > 300)
        {
            if (this.level instanceof ServerWorld)
            {
                final IWormholeWorld holes = this.level.getCapability(WormholeSpawns.WORMHOLES_CAP).orElse(null);
                holes.removeWormhole(this.getPos().getPos().pos());
                holes.getWormholes().clear();

                final ServerWorld dest = this.getServer().getLevel(this.getDest().getPos().dimension());
                EventsHandler.Schedule(dest, w ->
                {
                    dest.getChunk(this.dest.getPos().pos());
                    final AxisAlignedBB box = new AxisAlignedBB(this.getDest().getPos().pos()).inflate(10);
                    final List<WormholeEntity> list = w.getEntitiesOfClass(WormholeEntity.class, box);
                    for (final WormholeEntity e : list)
                    {
                        e.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 4);
                        e.energy.receiveEnergy(this.energy.getEnergyStored(), false);
                        e.timer = 0;
                    }
                    return true;
                });
                final float boom = 0.5f * this.uses;
                this.level.explode(this, this.getX(), this.getY(), this.getZ(), boom, Mode.NONE);
                this.remove();
            }
            return;
        }

        this.energy.receiveEnergy(100, false);

        final BlockPos anchor = this.getPos().getPos().pos();
        final Vector3d origin = new Vector3d(anchor.getX(), anchor.getY(), anchor.getZ());
        final Vector3d here = this.position();
        final Vector3d diff = origin.subtract(here);
        final Vector3d v = this.getDeltaMovement();
        final double s = 0.01;
        this.setDeltaMovement(v.x + diff.x * s, v.y + diff.y * s, v.z + diff.z * s);

        // Collapse at full energy
        if (this.energy.getEnergyStored() > 1000000 && !this.isClosing())
        {
            this.entityData.set(WormholeEntity.ACTIVE_STATE, (byte) 4);
            this.timer = 0;
        }

    }

    @Override
    protected void pushEntities()
    {
        if (!this.isIdle()) return;

        final List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), e -> (e.getVehicle() == null
                && !e.level.isClientSide()));
        final Set<UUID> tpd = Sets.newHashSet();
        if (!list.isEmpty()) for (Entity entity : list)
        {
            entity = EntityTools.getCoreEntity(entity);
            final long lastTp = entity.getPersistentData().getLong("pokecube_legends:uwh_use") + 100;
            final long now = Tracker.instance().getTick();
            final UUID uuid = entity.getUUID();
            if (now < lastTp || tpd.contains(uuid)) continue;
            PokecubeCore.LOGGER.debug("Transfering {} through a wormhole!", entity);
            tpd.add(uuid);
            entity.getPersistentData().putLong("pokecube_legends:uwh_use", now);

            final boolean sameDim = this.getDest().getPos().dimension().equals(this.getPos().getPos().dimension());
            final List<Entity> passengers = entity.getPassengers();
            if (sameDim)
            {
                // Extra shenanigans needed for this to properly transfer the
                // mob if it has riders.
                entity.ejectPassengers();
                for (final Entity e : passengers)
                {
                    e.setDeltaMovement(0, 0, 0);
                    this.energy.receiveEnergy(10000, false);
                }

            }
            ThutTeleporter.transferTo(entity, this.getDest(), true);
            entity.setDeltaMovement(0, 0, 0);
            this.uses++;
            this.energy.receiveEnergy(10000, false);

            final Entity root = entity;
            final AtomicInteger counter = new AtomicInteger();
            if (sameDim) EventsHandler.Schedule(this.level, w ->
            {
                if (counter.getAndIncrement() < 5) return false;
                for (final Entity e : passengers)
                {
                    ThutTeleporter.transferTo(e, this.getDest());
                    e.startRiding(root, true);
                }
                return true;
            });

        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT nbt)
    {
        CompoundNBT tag = new CompoundNBT();
        this.getDest().writeToNBT(tag);
        nbt.put("warp_dest", tag);
        tag = new CompoundNBT();
        this.getPos().writeToNBT(tag);
        nbt.put("anchor_pos", tag);
        tag = new CompoundNBT();
        tag.putDouble("x", this.getDir().x);
        tag.putDouble("y", this.getDir().y);
        tag.putDouble("z", this.getDir().z);
        nbt.put("face_dir", tag);
        nbt.putInt("timer", this.timer);
        nbt.putInt("uses", this.uses);
        nbt.putByte("state", this.entityData.get(WormholeEntity.ACTIVE_STATE));
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(final EquipmentSlotType p_184582_1_)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(final EquipmentSlotType p_184201_1_, final ItemStack p_184201_2_)
    {
    }

    @Override
    public HandSide getMainArm()
    {
        return HandSide.LEFT;
    }

    public Vector3d getDir()
    {
        if (this.dir == null) this.dir = this.getLookAngle();
        return this.dir;
    }

    public void setDir(final Vector3d dir)
    {
        this.dir = dir;
    }

}
