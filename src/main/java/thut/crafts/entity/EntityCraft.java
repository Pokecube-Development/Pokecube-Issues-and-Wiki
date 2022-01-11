package thut.crafts.entity;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;

public class EntityCraft extends BlockEntityBase implements IMultiplePassengerEntity
{
    public static class DismountTicker
    {
        final Entity dismounted;
        final Entity craft;
        final Seat seat;

        public DismountTicker(final Entity dismounted, final Entity craft, final Seat seat)
        {
            this.dismounted = dismounted;
            this.craft = craft;
            this.seat = seat;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(final WorldTickEvent event)
        {
            if (event.world != this.craft.level) return;
            MinecraftForge.EVENT_BUS.unregister(this);
            final double x = this.craft.getX() + this.seat.seat.x;
            final double y = this.craft.getY() + this.seat.seat.y;
            final double z = this.craft.getZ() + this.seat.seat.z;
            if (this.dismounted instanceof ServerPlayer) ((ServerPlayer) this.dismounted).connection.teleport(x, y, z,
                    this.dismounted.yRot, this.dismounted.xRot);
            else this.dismounted.setPos(x, y, z);
        }
    }

    public static final EntityType<EntityCraft> CRAFTTYPE = new BlockEntityType<>(EntityCraft::new);

    @SuppressWarnings("unchecked")
    static final EntityDataAccessor<Seat>[] SEAT = new EntityDataAccessor[10];
    static final EntityDataAccessor<Integer> SEATCOUNT = SynchedEntityData.<Integer>defineId(EntityCraft.class,
            EntityDataSerializers.INT);
    static final EntityDataAccessor<Integer> MAINSEATDW = SynchedEntityData.<Integer>defineId(EntityCraft.class,
            EntityDataSerializers.INT);

    static
    {
        for (int i = 0; i < EntityCraft.SEAT.length; i++) EntityCraft.SEAT[i] = SynchedEntityData
                .<Seat>defineId(EntityCraft.class, IMultiplePassengerEntity.SEATSERIALIZER);
    }

    public static boolean ENERGYUSE = false;
    public static int ENERGYCOST = 100;

    public CraftController controller = new CraftController(this);
    int energy = 0;
    public UUID owner;

    EntityDimensions size;

    public EntityCraft(final EntityType<EntityCraft> type, final Level par1World)
    {
        super(type, par1World);
    }

    @Override
    public void accelerate()
    {
        this.noPhysics = false;
        if (this.isServerWorld() && !this.consumePower())
        {
            this.setPos(this.position());
            Vec3 v = this.getDeltaMovement();
            double v2 = v.lengthSqr();
            if (v2 > 0)
            {
                if (v2 > 1e-4) this.setDeltaMovement(v.multiply(0.5, 0.5, 0.5));
                else this.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        this.toMoveX = this.controller.leftInputDown || this.controller.rightInputDown;
        this.toMoveZ = this.controller.backInputDown || this.controller.forwardInputDown;
        this.toMoveY = this.controller.upInputDown || this.controller.downInputDown;

        float destY = this.toMoveY ? this.controller.upInputDown ? 30 : -30 : 0;
        float destX = this.toMoveX ? this.controller.leftInputDown ? 30 : -30 : 0;
        float destZ = this.toMoveZ ? this.controller.forwardInputDown ? 30 : -30 : 0;

        // debug movement
        final boolean dbug_move = false;

        if (dbug_move)
        {
            this.toMoveY = true;
            destY = 1;
        }

        if (!(this.toMoveY || this.toMoveX || this.toMoveZ))
        {
            Vec3 v = this.getDeltaMovement();
            double v2 = v.lengthSqr();
            if (v2 > 0)
            {
                if (v2 > 1e-4) this.setDeltaMovement(v.multiply(0.5, 0.5, 0.5));
                else this.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        Seat seat = null;
        for (int i = 0; i < this.getSeatCount(); i++) if (!this.getSeat(i).getEntityId().equals(Seat.BLANK))
        {
            seat = this.getSeat(i);
            break;
        }

        final float f = (float) Math.sqrt(destX * destX + destZ * destZ);

        float dx = 0;
        float dz = 0;
        // Apply rotationYaw to destination
        if (this.controller.forwardInputDown)
        {
            dx = Mth.sin(-this.yRot * 0.017453292F);
            dz = Mth.cos(this.yRot * 0.017453292F);
        }
        else if (this.controller.backInputDown)
        {
            dx = -Mth.sin(-this.yRot * 0.017453292F);
            dz = -Mth.cos(this.yRot * 0.017453292F);
        }
        else if (this.controller.leftInputDown)
        {
            dx = Mth.cos(-this.yRot * 0.017453292F);
            dz = Mth.sin(this.yRot * 0.017453292F);
        }
        else if (this.controller.rightInputDown)
        {
            dx = -Mth.cos(-this.yRot * 0.017453292F);
            dz = -Mth.sin(this.yRot * 0.017453292F);
        }

        if (Mth.equal(dx, 0)) dx = 0;
        if (Mth.equal(dz, 0)) dz = 0;

        destX += dx * f;
        destZ += dz * f;

        seats:
        if (seat != null)
        {
            final Vector3 rel = new Vector3().addTo(seat.seat.x, seat.seat.y, seat.seat.z);
            final BlockPos pos = rel.getPos();
            BlockState block = this.getFakeWorld().getBlockRelative(pos);
            if (block == null || !block.hasProperty(StairBlock.FACING)) break seats;
            Vector3 dest = new Vector3().set(destX, destY, destZ);
            switch (block.getValue(StairBlock.FACING))
            {
            case DOWN:
                break;
            case EAST:
                dest = dest.rotateAboutAngles(0, -Math.PI / 2, new Vector3(), new Vector3());
                break;
            case NORTH:
                break;
            case SOUTH:
                dest = dest.rotateAboutAngles(0, Math.PI, new Vector3(), new Vector3());
                break;
            case UP:
                break;
            case WEST:
                dest = dest.rotateAboutAngles(0, Math.PI / 2, new Vector3(), new Vector3());
                break;
            default:
                break;
            }
            destX = (float) dest.x;
            destY = (float) dest.y;
            destZ = (float) dest.z;
        }
        //
        // // // debug movement
        if (dbug_move)
        {
            this.speedUp = 0.1f;
            this.speedDown = -0.1f;
            this.acceleration = 0.25f;
            this.toMoveY = true;
            if (this.getY() < 20) this.energy = 10;
            if (this.getY() > 30) this.energy = -10;
            destY = this.energy > 0 ? 10 : -10;
        }

        if (Mth.equal(destX, 0)) destX = 0;
        if (Mth.equal(destY, 0)) destY = 0;
        if (Mth.equal(destZ, 0)) destZ = 0;

        final Vec3 v = this.getDeltaMovement();

        double vx = v.x;
        double vy = v.y;
        double vz = v.z;

        if (destY != 0)
        {
            final double dy = this.getSpeed(0, destY, vy, this.getSpeedUp(), this.getSpeedDown());
            vy = dy;
        }
        else vy *= 0.5;

        if (destX != 0)
        {
            dx = (float) this.getSpeed(0, destX, vx, this.getSpeedHoriz(), this.getSpeedHoriz());
            vx = dx;
        }
        else vx *= 0.5;

        if (destZ != 0)
        {
            dz = (float) this.getSpeed(0, destZ, vz, this.getSpeedHoriz(), this.getSpeedHoriz());
            vz = dz;
        }
        else vz *= 0.5;
        this.setDeltaMovement(vx, vy, vz);
    }

    public void addSeat(final Vec3f seat)
    {
        final Seat toSet = this.getSeat(this.getSeatCount());
        toSet.seat.set(seat);
        this.entityData.set(EntityCraft.SEAT[this.getSeatCount()], toSet);
        this.setSeatCount(this.getSeatCount() + 1);
    }

    @Override
    protected boolean canAddPassenger(final Entity passenger)
    {
        return this.getPassengers().size() < this.getSeatCount();
    }

    /**
     * If a rider of this entity can interact with this entity. Should return
     * true on the ridden entity if so.
     *
     * @return if the entity can be interacted with from a rider
     */
    @Override
    public boolean canRiderInteract()
    {
        return true;
    }

    @Override
    protected boolean checkAccelerationConditions()
    {
        return this.consumePower();
    }

    private boolean consumePower()
    {
        if (!EntityCraft.ENERGYUSE) return true;
        boolean power = false;
        final Vector3 bounds = new Vector3().set(this.boundMax.subtract(this.boundMin));
        final double volume = bounds.x * bounds.y * bounds.z;
        final float speed = 10;
        double energyCost = Math.abs(speed) * EntityCraft.ENERGYCOST * volume * 0.01;
        energyCost = Math.max(energyCost, 1);
        power = (this.energy = (int) (this.energy - energyCost)) > 0;
        if (this.energy < 0) this.energy = 0;
        MinecraftForge.EVENT_BUS.post(new EventCraftConsumePower(this, (long) energyCost));
        if (!power) this.toMoveY = false;
        return power;
    }

    @Override
    protected BlockEntityInteractHandler createInteractHandler()
    {
        return new CraftInteractHandler(this);
    }

    public int getEnergy()
    {
        return this.energy;
    }

    /** @return the destinationFloor */
    public int getMainSeat()
    {
        return this.entityData.get(EntityCraft.MAINSEATDW);
    }

    @Override
    public Entity getPassenger(final Vec3f seatl)
    {
        UUID id = null;
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).seat.equals(seatl)) id = seat.getEntityId();
        }
        if (id != null) for (final Entity e : this.getPassengers()) if (e.getUUID().equals(id)) return e;
        return null;
    }

    @Override
    public float getPitch()
    {
        // TODO datawatcher value of pitch.
        return this.xRot;
    }

    @Override
    public float getPrevPitch()
    {
        return this.xRotO;
    }

    @Override
    public float getPrevYaw()
    {
        return this.yRotO;
    }

    @Override
    public Vec3f getSeat(final Entity passenger)
    {
        final Vec3f ret = null;
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).getEntityId().equals(passenger.getUUID())) return seat.seat;
        }
        return ret;
    }

    Seat getSeat(final int index)
    {
        return this.entityData.get(EntityCraft.SEAT[index]);
    }

    int getSeatCount()
    {
        return this.entityData.get(EntityCraft.SEATCOUNT);
    }

    @Override
    public List<Vec3f> getSeats()
    {
        final List<Vec3f> ret = Lists.newArrayList();
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            final Seat seat = this.getSeat(i);
            ret.add(seat.seat);
        }
        return null;
    }

    @Override
    public EntityDimensions getDimensions(final Pose pose)
    {
        if (this.size == null)
            this.size = EntityDimensions.fixed(1 + this.getMax().getX() - this.getMin().getX(), this.getMax().getY());
        return this.size;
    }

    @Override
    public float getYaw()
    {
        return this.yRot;
    }

    @Override
    protected void onGridAlign()
    {}

    @Override
    protected void preColliderTick()
    {
        this.controller.doServerTick(this.getFakeWorld());
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.energy = nbt.getInt("energy");
        if (nbt.contains("seats"))
        {
            final ListTag seatsList = nbt.getList("seats", 10);
            for (int i = 0; i < seatsList.size(); ++i)
            {
                final CompoundTag nbt1 = seatsList.getCompound(i);
                final Seat seat = Seat.readFromNBT(nbt1);
                this.entityData.set(EntityCraft.SEAT[i], seat);
            }
        }
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(EntityCraft.MAINSEATDW, Integer.valueOf(-1));
        for (int i = 0; i < 10; i++) this.entityData.define(EntityCraft.SEAT[i], new Seat(new Vec3f(), null));
        this.entityData.define(EntityCraft.SEATCOUNT, 0);
    }

    @Override
    protected void removePassenger(final Entity passenger)
    {
        super.removePassenger(passenger);
        if (!this.level.isClientSide)
            for (int i = 0; i < this.getSeatCount(); i++) if (this.getSeat(i).getEntityId().equals(passenger.getUUID()))
        {
            this.setSeatID(i, Seat.BLANK);
            new DismountTicker(passenger, this, this.getSeat(i));
            break;
        }
    }

    public void setEnergy(final int energy)
    {
        this.energy = energy;
    }

    @Override
    public void setItemSlot(final EquipmentSlot slotIn, final ItemStack stack)
    {}

    /** @return the destinationFloor */
    public void setMainSeat(final int seat)
    {
        this.entityData.set(EntityCraft.MAINSEATDW, seat);
    }

    void setSeatCount(final int count)
    {
        this.entityData.set(EntityCraft.SEATCOUNT, count);
    }

    void setSeatID(final int index, final UUID id)
    {
        Seat toSet = this.getSeat(index);
        final UUID old = toSet.getEntityId();
        if (!old.equals(id))
        {
            toSet = (Seat) toSet.clone();
            toSet.setEntityId(id);
            this.entityData.set(EntityCraft.SEAT[index], toSet);
        }
    }

    @Override
    public void setSize(final EntityDimensions size)
    {
        this.size = size;
    }

    @Override
    public void positionRider(final Entity passenger)
    {
        if (this.hasPassenger(passenger))
        {
            if (passenger.isShiftKeyDown()) passenger.stopRiding();
            IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
            passenger.setOnGround(true);
            passenger.causeFallDamage(passenger.fallDistance, 0, DamageSource.GENERIC);
            passenger.fallDistance = 0;
            if (passenger instanceof ServerPlayer)
            {
                ((ServerPlayer) passenger).connection.aboveGroundVehicleTickCount = 0;
                ((ServerPlayer) passenger).connection.aboveGroundTickCount = 0;
            }
        }
    }

    @Override
    public boolean causeFallDamage(final float distance, final float damageMultiplier, final DamageSource source)
    {
        // Do nothing here, the supoer method will call this to all passengers
        // as well!
        return false;
    }

    @Override
    public void updateSeat(final int index, final UUID id)
    {
        final Seat seat = (Seat) this.getSeat(index).clone();
        seat.setEntityId(id);
        this.entityData.set(EntityCraft.SEAT[index], seat);
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("energy", this.energy);
        final ListTag seats = new ListTag();
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            final CompoundTag tag1 = new CompoundTag();
            this.getSeat(i).writeToNBT(tag1);
            seats.add(tag1);
        }
        nbt.put("seats", seats);
    }
}
