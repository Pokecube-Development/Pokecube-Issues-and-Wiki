package thut.tech.common.entity;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.IEnergyStorage;
import thut.api.ThutCaps;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;

public class EntityLift extends BlockEntityBase
{
    public static class LiftTracker
    {
        protected static final Map<UUID, EntityLift> liftMap = Maps.newHashMap();
    }

    static final EntityDataAccessor<Integer> DESTINATIONFLOORDW;
    static final EntityDataAccessor<Float> DESTINATIONYDW;
    static final EntityDataAccessor<Float> DESTINATIONXDW;
    static final EntityDataAccessor<Float> DESTINATIONZDW;
    static final EntityDataAccessor<Integer> CURRENTFLOORDW;
    static final EntityDataAccessor<Boolean> CALLEDDW;

    static final EntityDataAccessor<Float> SPEEDUP;
    static final EntityDataAccessor<Float> SPEEDDOWN;
    static final EntityDataAccessor<Float> SPEEDSIDE;
    static final EntityDataAccessor<Float> ACCEL;

    static
    {
        DESTINATIONFLOORDW = SynchedEntityData.<Integer>defineId(EntityLift.class, EntityDataSerializers.INT);
        DESTINATIONYDW = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        DESTINATIONXDW = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        DESTINATIONZDW = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        CURRENTFLOORDW = SynchedEntityData.<Integer>defineId(EntityLift.class, EntityDataSerializers.INT);
        CALLEDDW = SynchedEntityData.<Boolean>defineId(EntityLift.class, EntityDataSerializers.BOOLEAN);

        SPEEDUP = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        SPEEDDOWN = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        SPEEDSIDE = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
        ACCEL = SynchedEntityData.<Float>defineId(EntityLift.class, EntityDataSerializers.FLOAT);
    }

    public static boolean ENERGYUSE = false;

    public static int ENERGYCOST = 100;

    public static EntityLift getLiftFromUUID(final UUID liftID, final Level world)
    {
        if (world instanceof ServerLevel)
        {
            final Entity e = ((ServerLevel) world).getEntity(liftID);
            if (e instanceof EntityLift) return (EntityLift) e;
        }
        return LiftTracker.liftMap.get(liftID);
    }

    public IEnergyStorage energy = null;
    public UUID owner;
    public double prevFloorY = 0;
    public double prevFloor = 0;
    private final int[] floors = new int[128];

    private final int[] hasFloors = new int[128];

    private final Vector3 velocity = new Vector3();

    private Vec3 motion = new Vec3(0, 0, 0);

    EntityDimensions size;

    public EntityLift(final EntityType<EntityLift> type, final Level par1World)
    {
        super(type, par1World);
    }

    @Override
    public void accelerate()
    {
        // These elevators shouldn't be able to rotate, set this here incase
        // someone else has tried to rotate it.
        this.setYRot(0);
        // Only should run the consume power check on servers.
        if (this.isServerWorld() && !this.consumePower())
        {
            this.toMoveY = this.toMoveX = this.toMoveZ = false;
            this.setDestX((float) this.getX());
            this.setCalled(false);
        }
        else
        {
            // Otherwise set it to move if it has a destination.
            this.toMoveX = this.getDestX() != this.getX();
            this.toMoveY = this.getDestY() != this.getY();
            this.toMoveZ = this.getDestZ() != this.getZ();
        }
        if (!(this.toMoveX || this.toMoveY || this.toMoveZ)) this.setCalled(false);

        // Apply damping to velocities if no destination.
        if (!this.toMoveX) this.velocity.x *= 0.5;
        if (!this.toMoveZ) this.velocity.z *= 0.5;
        if (!this.toMoveY) this.velocity.y *= 0.5;

        if (this.getCalled())
        {
            final double speedDown = this.getSpeedDown();
            final double speedHoriz = this.getSpeedHoriz();
            final double speedUp = this.getSpeedUp();

            if (this.toMoveY)
            {
                final float destY = this.getDestY();
                // If Sufficiently close (0,01 blocks) just snap the elevator to
                // the destination.
                if (Math.abs(destY - this.getY()) < 0.01)
                {
                    this.setPos(this.getX(), destY, this.getZ());
                    this.toMoveY = false;
                    this.velocity.y = 0;
                }
                else
                {
                    // Otherwise accelerate accordingly.
                    final double dy = this.getSpeed(this.getY(), destY, this.velocity.y, speedUp, speedDown);
                    this.velocity.y = (float) dy;
                }
            }
            if (this.toMoveX)
            {
                final float destX = this.getDestX();
                if (Math.abs(destX - this.getX()) < 0.01)
                {
                    this.setPos(destX, this.getY(), this.getZ());
                    this.toMoveX = false;
                    this.velocity.x = 0;
                }
                else
                {
                    final double dx = this.getSpeed(this.getX(), destX, this.velocity.x, speedHoriz, speedHoriz);
                    this.velocity.x = (float) dx;
                }
            }
            if (this.toMoveZ)
            {
                final float destZ = this.getDestZ();
                if (Math.abs(destZ - this.getZ()) < 0.01)
                {
                    this.setPos(this.getX(), this.getY(), destZ);
                    this.toMoveZ = false;
                    this.velocity.z = 0;
                }
                else
                {
                    final double dz = this.getSpeed(this.getZ(), destZ, this.velocity.z, speedHoriz, speedHoriz);
                    this.velocity.z = (float) dz;
                }
            }
        }
        this.setDeltaMovement(this.velocity.x, this.velocity.y, this.velocity.z);

    }

    @Override
    public Vec3 getDeltaMovement()
    {
        return this.motion;
    }

    @Override
    public void setDeltaMovement(final Vec3 vec)
    {
        this.motion = vec;
    }

    public void call(final int floor)
    {
        if (floor > 0 && floor <= this.floors.length)
        {
            if (!this.isServerWorld()) return;
            if (this.hasFloor(floor))
            {
                this.callYValue(this.getFloorPos(floor));
                this.setDestinationFloor(floor);
                ThutCore.LOGGER.debug("Lift Called to floor: " + floor);
            }
        }
        else ThutCore.LOGGER.error("Set floor out of range!");
    }

    public void callYValue(final int yValue)
    {
        this.setDestY(yValue);
    }

    @Override
    protected boolean checkAccelerationConditions()
    {
        return this.consumePower();
    }

    private boolean consumePower()
    {
        if (!EntityLift.ENERGYUSE || !this.getCalled()) return true;
        if (this.energy == null) this.energy = this.getCapability(ThutCaps.ENERGY, null).orElse(null);
        if (this.energy == null) return true;

        boolean power = false;
        final Vector3 bounds = new Vector3().set(this.boundMax.subtract(this.boundMin));
        final double volume = bounds.x * bounds.y * bounds.z;
        int energyCost = (int) (Math.abs(this.getDestY() - this.getY()) * EntityLift.ENERGYCOST * volume * 0.01);
        energyCost = Math.max(energyCost, 1);
        final int canExtract = this.energy.extractEnergy(energyCost, true);
        if (canExtract == energyCost)
        {
            power = true;
            this.energy.extractEnergy(energyCost, false);
        }
        MinecraftForge.EVENT_BUS.post(new EventLiftConsumePower(this, energyCost));
        if (!power)
        {
            this.setDestinationFloor(-1);
            this.setDestY((float) this.getY());
            this.setCalled(false);
            this.toMoveY = false;
        }
        return power;
    }

    @Override
    protected BlockEntityInteractHandler createInteractHandler()
    {
        return new LiftInteractHandler(this);
    }

    // @Override
    // public void doMotion()
    // {
    // if (!this.toMoveX) this.velocity.x = 0;
    // if (!this.toMoveY) this.velocity.y = 0;
    // if (!this.toMoveZ) this.velocity.z = 0;
    // this.setMotion(this.velocity.x, this.velocity.y, this.velocity.z);
    // this.move(MoverType.SELF, this.getMotion());
    // }

    public boolean getCalled()
    {
        return this.entityData.get(EntityLift.CALLEDDW);
    }

    /** @return the destinationFloor */
    public int getCurrentFloor()
    {
        return this.entityData.get(EntityLift.CURRENTFLOORDW);
    }

    /** @return the destinationFloor */
    public int getDestinationFloor()
    {
        return this.entityData.get(EntityLift.DESTINATIONFLOORDW);
    }

    /** @return the destinationFloor */
    public float getDestX()
    {
        return this.entityData.get(EntityLift.DESTINATIONXDW);
    }

    /** @return the destinationFloor */
    public float getDestY()
    {
        return this.entityData.get(EntityLift.DESTINATIONYDW);
    }

    /** @return the destinationFloor */
    public float getDestZ()
    {
        return this.entityData.get(EntityLift.DESTINATIONZDW);
    }

    @Override
    public float getSpeedUp()
    {
        return this.entityData.get(EntityLift.SPEEDUP);
    }

    @Override
    public float getSpeedDown()
    {
        return this.entityData.get(EntityLift.SPEEDDOWN);
    }

    @Override
    public float getSpeedHoriz()
    {
        return this.entityData.get(EntityLift.SPEEDSIDE);
    }

    @Override
    public float getAccel()
    {
        return this.entityData.get(EntityLift.ACCEL);
    }

    @Override
    public EntityDimensions getDimensions(final Pose pose)
    {
        if (this.size == null)
            this.size = EntityDimensions.fixed(1 + this.getMax().getX() - this.getMin().getX(), this.getMax().getY());
        return this.size;
    }

    @Override
    public void onAddedToWorld()
    {
        super.onAddedToWorld();
        LiftTracker.liftMap.put(this.getUUID(), this);
    }

    @Override
    public void onRemovedFromWorld()
    {
        super.onRemovedFromWorld();
        LiftTracker.liftMap.remove(this.getUUID(), this);
    }

    @Override
    protected void onGridAlign()
    {
        this.setCalled(false);
    }

    @Override
    protected void preColliderTick()
    {}

    @Override
    public void readAdditionalSaveData(final CompoundTag arg0)
    {
        super.readAdditionalSaveData(arg0);
        final CompoundTag tag = arg0.getCompound("floors");
        for (int i = 0; i < this.hasFloors.length; i++) if (tag.contains("" + i))
        {
            final int floor = tag.getInt("" + i);
            final int num = tag.getInt("_" + i);
            this.hasFloors[i] = num;
            this.floors[i] = floor;
        }
        if (arg0.hasUUID("owner")) this.owner = arg0.getUUID("owner");
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(EntityLift.DESTINATIONFLOORDW, Integer.valueOf(0));
        this.entityData.define(EntityLift.DESTINATIONYDW, Float.valueOf(0));
        this.entityData.define(EntityLift.DESTINATIONXDW, Float.valueOf(0));
        this.entityData.define(EntityLift.DESTINATIONZDW, Float.valueOf(0));
        this.entityData.define(EntityLift.CURRENTFLOORDW, Integer.valueOf(-1));
        this.entityData.define(EntityLift.CALLEDDW, Boolean.FALSE);

        this.entityData.define(EntityLift.SPEEDUP, Float.valueOf((float) TechCore.config.LiftSpeedUp));
        this.entityData.define(EntityLift.SPEEDDOWN, Float.valueOf((float) TechCore.config.LiftSpeedDown));
        this.entityData.define(EntityLift.SPEEDSIDE, Float.valueOf((float) TechCore.config.LiftSpeedSideways));
        this.entityData.define(EntityLift.ACCEL, Float.valueOf((float) TechCore.config.LiftAcceleration));
    }

    private void setCalled(final boolean called)
    {
        this.entityData.set(EntityLift.CALLEDDW, called);
    }

    /**
     * @param currentFloor the destinationFloor to set
     */
    public void setCurrentFloor(final int currentFloor)
    {
        this.entityData.set(EntityLift.CURRENTFLOORDW, Integer.valueOf(currentFloor));
    }

    /**
     * @param destinationFloor the destinationFloor to set
     */
    public void setDestinationFloor(final int destinationFloor)
    {
        this.entityData.set(EntityLift.DESTINATIONFLOORDW, Integer.valueOf(destinationFloor));
    }

    /**
     * @param dest the destinationFloor to set
     */
    public void setDestX(final float dest)
    {
        this.entityData.set(EntityLift.DESTINATIONXDW, Float.valueOf(dest));
        this.entityData.set(EntityLift.DESTINATIONYDW, Float.valueOf((float) this.getY()));
        this.entityData.set(EntityLift.DESTINATIONZDW, Float.valueOf((float) this.getZ()));
        this.setCalled(true);
    }

    /**
     * @param dest the destinationFloor to set
     */
    public void setDestY(final float dest)
    {
        this.entityData.set(EntityLift.DESTINATIONYDW, Float.valueOf(dest));
        this.entityData.set(EntityLift.DESTINATIONXDW, Float.valueOf((float) this.getX()));
        this.entityData.set(EntityLift.DESTINATIONZDW, Float.valueOf((float) this.getZ()));
        this.setCalled(true);
    }

    /**
     * @param dest the destinationFloor to set
     */
    public void setDestZ(final float dest)
    {
        this.entityData.set(EntityLift.DESTINATIONZDW, Float.valueOf(dest));
        this.entityData.set(EntityLift.DESTINATIONYDW, Float.valueOf((float) this.getY()));
        this.entityData.set(EntityLift.DESTINATIONXDW, Float.valueOf((float) this.getX()));
        this.setCalled(true);
    }

    public boolean setFoor(final ControllerTile te, int floor)
    {
        floor--;
        if (floor < 0 || floor >= this.floors.length) return false;
        if (te != null)
        {
            boolean changed = false;
            // If we don't have a floor, then actually set the position.
            // If we did have one, it was already set, and not cleared, so
            // we do not want to reset it!
            if (this.hasFloors[floor] <= 0) this.floors[floor] = te.getBlockPos().getY() - 2;
            this.hasFloors[floor]++;
            changed = true;

            if (changed)
            {
                final int prev = te.floor - 1;
                // Reduce the number of previous floors if they existed.
                if (prev != -1 && prev != floor && this.hasFloors[prev] > 0) this.hasFloors[prev]--;
            }
        }
        else if (this.hasFloors[floor] > 0)
        {
            this.hasFloors[floor]--;
            // If no tiles left for this floor, clear the position
            if (this.hasFloors[floor] <= 0) this.floors[floor] = 0;
        }
        if (this.isServerWorld()) EntityUpdate.sendEntityUpdate(this);
        return true;
    }

    public void setFloorPos(int floor, final int posY)
    {
        floor--;
        if (floor >= 0 && floor < this.floors.length) this.floors[floor] = posY;
        ThutCore.LOGGER.error("Set floor out of range!");
    }

    public int getFloorPos(int floor)
    {
        floor--;
        if (floor >= 0 && floor < this.floors.length) return this.floors[floor];
        ThutCore.LOGGER.error("Requested floor out of range!");
        return 0;
    }

    public boolean hasFloor(int floor)
    {
        floor--;
        if (floor >= 0 && floor < this.hasFloors.length) return this.hasFloors[floor] > 0;
        ThutCore.LOGGER.error("Checked floor out of range!");
        return false;
    }

    public int maxFloors()
    {
        return this.hasFloors.length;
    }

    @Override
    public void setItemSlot(final EquipmentSlot slotIn, final ItemStack stack)
    {}

    @Override
    public void setSize(final EntityDimensions size)
    {
        this.size = size;
    }

    @Override
    public void setTiles(final BlockEntity[][][] tiles)
    {
        super.setTiles(tiles);
        for (final BlockEntity[][] tileArrArr : tiles) for (final BlockEntity[] tileArr : tileArrArr)
            for (final BlockEntity tile : tileArr) if (tile instanceof ControllerTile)
        {
            ((ControllerTile) tile).setLift(this);
            ((ControllerTile) tile).setWorldObj((Level) this.getFakeWorld());
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag arg0)
    {
        super.addAdditionalSaveData(arg0);
        final CompoundTag tag = new CompoundTag();
        for (int i = 0; i < this.hasFloors.length; i++) if (this.hasFloors[i] > 0)
        {
            tag.putInt("" + i, this.floors[i]);
            tag.putInt("_" + i, this.hasFloors[i]);
        }
        arg0.put("floors", tag);
        if (this.owner != null) arg0.putUUID("owner", this.owner);
    }
}
