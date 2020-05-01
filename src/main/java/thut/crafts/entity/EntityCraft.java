package thut.crafts.entity;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.network.EntityUpdate;

public class EntityCraft extends BlockEntityBase implements IMultiplePassengerEntity
{
    public static class DismountTicker
    {
        final Entity dismounted;
        final Entity craft;
        final Seat   seat;

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
            if (event.world != this.craft.world) return;
            MinecraftForge.EVENT_BUS.unregister(this);
            final double x = this.craft.getPosX() + this.seat.seat.x;
            final double y = this.craft.getPosY() + this.seat.seat.y;
            final double z = this.craft.getPosZ() + this.seat.seat.z;
            if (this.dismounted instanceof ServerPlayerEntity) ((ServerPlayerEntity) this.dismounted).connection
                    .setPlayerLocation(x, y, z, this.dismounted.rotationYaw, this.dismounted.rotationPitch);
            else this.dismounted.setPosition(x, y, z);
        }
    }

    public static final EntityType<EntityCraft> CRAFTTYPE = new BlockEntityType<>(EntityCraft::new);

    @SuppressWarnings("unchecked")
    static final DataParameter<Seat>[]  SEAT       = new DataParameter[10];
    static final DataParameter<Integer> SEATCOUNT  = EntityDataManager.<Integer> createKey(EntityCraft.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer> MAINSEATDW = EntityDataManager.<Integer> createKey(EntityCraft.class,
            DataSerializers.VARINT);

    static
    {
        for (int i = 0; i < EntityCraft.SEAT.length; i++)
            EntityCraft.SEAT[i] = EntityDataManager.<Seat> createKey(EntityCraft.class,
                    IMultiplePassengerEntity.SEATSERIALIZER);
    }

    public static boolean ENERGYUSE  = false;
    public static int     ENERGYCOST = 100;

    public CraftController controller = new CraftController(this);
    int                    energy     = 0;
    public UUID            owner;

    EntitySize size;

    public EntityCraft(final EntityType<EntityCraft> type, final World par1World)
    {
        super(type, par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
    }

    @Override
    public void accelerate()
    {
        if (this.isServerWorld() && !this.consumePower()) this.toMoveY = this.toMoveX = this.toMoveZ = false;
        this.toMoveX = this.controller.leftInputDown || this.controller.rightInputDown;
        this.toMoveZ = this.controller.backInputDown || this.controller.forwardInputDown;

        this.speedDown = 1;
        this.speedUp = 1;
        this.speedHoriz = 1;

        // this.speedUp = 0.5;
        // this.speedDown = 0.5;
        // final int high = 30;
        // final int low = 15;
        //
        // if (this.getPosY() < high && this.getMotion().y > 0)
        // this.controller.upInputDown = true;
        //
        // if (this.getPosY() > low && this.getMotion().y < 0)
        // this.controller.downInputDown = true;
        //
        // if (this.getPosY() > high)
        // {
        // this.controller.downInputDown = true;
        // this.controller.upInputDown = false;
        // }
        // if (this.getPosY() < low)
        // {
        // this.controller.downInputDown = false;
        // this.controller.upInputDown = true;
        // }

        this.toMoveY = this.controller.upInputDown || this.controller.downInputDown;

        float destY = this.toMoveY ? this.controller.upInputDown ? 30 : -30 : 0;
        float destX = this.toMoveX ? this.controller.leftInputDown ? 30 : -30 : 0;
        float destZ = this.toMoveZ ? this.controller.forwardInputDown ? 30 : -30 : 0;
        this.toMoveY = this.toMoveX = this.toMoveZ = false;

        if (destX == destY && destY == destZ && destZ == 0)
        {
            this.setMotion(this.getMotion().mul(0.5, 0.5, 0.5));
            return;
        }

        Seat seat = null;
        for (int i = 0; i < this.getSeatCount(); i++)
            if (!this.getSeat(i).getEntityId().equals(Seat.BLANK))
            {
                seat = this.getSeat(i);
                break;
            }

        final float f = (float) Math.sqrt(destX * destX + destZ * destZ);
        // Apply rotationYaw to destination
        if (this.controller.forwardInputDown)
        {
            destX = MathHelper.sin(-this.rotationYaw * 0.017453292F) * f;
            destZ = MathHelper.cos(this.rotationYaw * 0.017453292F) * f;
        }
        else if (this.controller.backInputDown)
        {
            destX = -MathHelper.sin(-this.rotationYaw * 0.017453292F) * f;
            destZ = -MathHelper.cos(this.rotationYaw * 0.017453292F) * f;
        }
        else if (this.controller.leftInputDown)
        {
            destX = MathHelper.cos(-this.rotationYaw * 0.017453292F) * f;
            destZ = MathHelper.sin(this.rotationYaw * 0.017453292F) * f;
        }
        else if (this.controller.rightInputDown)
        {
            destX = -MathHelper.cos(-this.rotationYaw * 0.017453292F) * f;
            destZ = -MathHelper.sin(this.rotationYaw * 0.017453292F) * f;
        }
        seats:
        if (seat != null)
        {
            final Vector3 rel = Vector3.getNewVector().set(this).addTo(seat.seat.x, seat.seat.y, seat.seat.z);
            final BlockPos pos = rel.getPos();
            final BlockState block = this.getFakeWorld().getBlock(pos);
            if (block == null || !block.has(StairsBlock.FACING)) break seats;
            Vector3 dest = Vector3.getNewVector().set(destX, destY, destZ);
            switch (block.get(StairsBlock.FACING))
            {
            case DOWN:
                break;
            case EAST:
                dest = dest.rotateAboutAngles(0, -Math.PI / 2, Vector3.getNewVector(), Vector3.getNewVector());
                break;
            case NORTH:
                break;
            case SOUTH:
                dest = dest.rotateAboutAngles(0, Math.PI, Vector3.getNewVector(), Vector3.getNewVector());
                break;
            case UP:
                break;
            case WEST:
                dest = dest.rotateAboutAngles(0, Math.PI / 2, Vector3.getNewVector(), Vector3.getNewVector());
                break;
            default:
                break;
            }
            destX = (float) dest.x;
            destY = (float) dest.y;
            destZ = (float) dest.z;
        }

        destX += this.posX;
        destY += this.posY;
        destZ += this.posZ;

        final Vec3d v = this.getMotion();
        double vx = v.x;
        double vy = v.y;
        double vz = v.z;

        if (destY != this.posY)
        {
            final double dy = this.getSpeed(this.posY, destY, vy, this.speedUp, this.speedDown);
            vy = dy;
            this.toMoveY = true;
        }
        else vy *= 0.5;
        if (destX != this.posX)
        {
            final double dx = this.getSpeed(this.posX, destX, vx, this.speedHoriz, this.speedHoriz);
            vx = dx;
            this.toMoveX = true;
        }
        else vx *= 0.5;
        if (destZ != this.posZ)
        {
            final double dz = this.getSpeed(this.posZ, destZ, vz, this.speedHoriz, this.speedHoriz);
            vz = dz;
            this.toMoveZ = true;
        }
        else vz *= 0.5;
        this.setMotion(vx, vy, vz);
    }

    public void addSeat(final Vector3f seat)
    {
        final Seat toSet = this.getSeat(this.getSeatCount());
        toSet.seat.set(seat);
        this.dataManager.set(EntityCraft.SEAT[this.getSeatCount()], toSet);
        this.setSeatCount(this.getSeatCount() + 1);
    }

    @Override
    protected boolean canFitPassenger(final Entity passenger)
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
        final Vector3 bounds = Vector3.getNewVector().set(this.boundMax.subtract(this.boundMin));
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

    @Override
    public void doMotion()
    {
        this.move(MoverType.SELF, this.getMotion());
    }

    public int getEnergy()
    {
        return this.energy;
    }

    /** @return the destinationFloor */
    public int getMainSeat()
    {
        return this.dataManager.get(EntityCraft.MAINSEATDW);
    }

    @Override
    public Entity getPassenger(final Vector3f seatl)
    {
        UUID id = null;
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).seat.equals(seatl)) id = seat.getEntityId();
        }
        if (id != null) for (final Entity e : this.getPassengers())
            if (e.getUniqueID().equals(id)) return e;
        return null;
    }

    @Override
    public float getPitch()
    {
        // TODO datawatcher value of pitch.
        return this.rotationPitch;
    }

    @Override
    public float getPrevPitch()
    {
        return this.prevRotationPitch;
    }

    @Override
    public float getPrevYaw()
    {
        return this.prevRotationYaw;
    }

    @Override
    public Vector3f getSeat(final Entity passenger)
    {
        final Vector3f ret = null;
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).getEntityId().equals(passenger.getUniqueID())) return seat.seat;
        }
        return ret;
    }

    Seat getSeat(final int index)
    {
        return this.dataManager.get(EntityCraft.SEAT[index]);
    }

    int getSeatCount()
    {
        return this.dataManager.get(EntityCraft.SEATCOUNT);
    }

    @Override
    public List<Vector3f> getSeats()
    {
        final List<Vector3f> ret = Lists.newArrayList();
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            final Seat seat = this.getSeat(i);
            ret.add(seat.seat);
        }
        return null;
    }

    @Override
    public EntitySize getSize(final Pose pose)
    {
        if (this.size == null) this.size = EntitySize.fixed(1 + this.getMax().getX() - this.getMin().getX(), this
                .getMax().getY());
        return this.size;
    }

    @Override
    public float getYaw()
    {
        return this.rotationYaw;
    }

    @Override
    protected void onGridAlign()
    {
        final BlockPos pos = this.getPosition();
        double dx = this.getPosX();
        double dy = this.getPosY();
        double dz = this.getPosZ();
        this.setPosition(pos.getX() + 0.5, Math.round(this.posY), pos.getZ() + 0.5);
        dx -= this.getPosX();
        dy -= this.getPosY();
        dz -= this.getPosZ();
        if (dx * dx + dy * dy + dz * dz > 0) EntityUpdate.sendEntityUpdate(this);
    }

    @Override
    protected void preColliderTick()
    {
        this.controller.doServerTick(this.getFakeWorld());
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        this.energy = nbt.getInt("energy");
        if (nbt.contains("seats"))
        {
            final ListNBT seatsList = nbt.getList("seats", 10);
            for (int i = 0; i < seatsList.size(); ++i)
            {
                final CompoundNBT nbt1 = seatsList.getCompound(i);
                final Seat seat = Seat.readFromNBT(nbt1);
                this.dataManager.set(EntityCraft.SEAT[i], seat);
            }
        }
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        this.dataManager.register(EntityCraft.MAINSEATDW, Integer.valueOf(-1));
        for (int i = 0; i < 10; i++)
            this.dataManager.register(EntityCraft.SEAT[i], new Seat(new Vector3f(), null));
        this.dataManager.register(EntityCraft.SEATCOUNT, 0);
    }

    @Override
    protected void removePassenger(final Entity passenger)
    {
        super.removePassenger(passenger);
        if (!this.world.isRemote) for (int i = 0; i < this.getSeatCount(); i++)
            if (this.getSeat(i).getEntityId().equals(passenger.getUniqueID()))
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
    public void setItemStackToSlot(final EquipmentSlotType slotIn, final ItemStack stack)
    {
    }

    /** @return the destinationFloor */
    public void setMainSeat(final int seat)
    {
        this.dataManager.set(EntityCraft.MAINSEATDW, seat);
    }

    void setSeatCount(final int count)
    {
        this.dataManager.set(EntityCraft.SEATCOUNT, count);
    }

    void setSeatID(final int index, final UUID id)
    {
        Seat toSet = this.getSeat(index);
        final UUID old = toSet.getEntityId();
        if (!old.equals(id))
        {
            toSet = (Seat) toSet.clone();
            toSet.setEntityId(id);
            this.dataManager.set(EntityCraft.SEAT[index], toSet);
        }
    }

    @Override
    public void setSize(final EntitySize size)
    {
        this.size = size;
    }

    @Override
    public void updatePassenger(final Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            if (passenger.isShiftKeyDown()) passenger.stopRiding();
            IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
            passenger.onGround = true;
            passenger.onLivingFall(passenger.fallDistance, 0);
            passenger.fallDistance = 0;
            if (passenger instanceof ServerPlayerEntity)
            {
                ((ServerPlayerEntity) passenger).connection.vehicleFloatingTickCount = 0;
                ((ServerPlayerEntity) passenger).connection.floatingTickCount = 0;
            }
        }
    }

    @Override
    public boolean onLivingFall(final float distance, final float damageMultiplier)
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
        this.dataManager.set(EntityCraft.SEAT[index], seat);
    }

    @Override
    public void writeAdditional(final CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        nbt.putInt("energy", this.energy);
        final ListNBT seats = new ListNBT();
        for (int i = 0; i < this.getSeatCount(); i++)
        {
            final CompoundNBT tag1 = new CompoundNBT();
            this.getSeat(i).writeToNBT(tag1);
            seats.add(tag1);
        }
        nbt.put("seats", seats);
    }
}
