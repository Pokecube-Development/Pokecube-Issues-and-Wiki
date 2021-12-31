package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.database.PokedexEntry;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.entity.multipart.GenericPartEntity.BodyPart;
import thut.api.maths.vecmath.Vector3f;

public abstract class PokemobRidable extends PokemobHasParts
        implements IMultiplePassengerEntity, PlayerRideableJumping, Saddleable
{

    public PokemobRidable(final EntityType<? extends ShoulderRidingEntity> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean isControlledByLocalInstance()
    {
        if (this.getPassengers().isEmpty()) return false;
        return this.getPassengers().get(0).getUUID().equals(this.pokemobCap.getOwnerId());
    }

    @Override
    public Entity getControllingPassenger()
    {
        final List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) return null;
        return this.getPassengers().get(0).getUUID().equals(this.pokemobCap.getOwnerId()) ? this.getPassengers().get(0)
                : null;
    }

    @Override
    public boolean canBeRiddenInWater(final Entity rider)
    {
        return this.pokemobCap.canUseSurf() || this.pokemobCap.canUseDive();
    }

    // ========== Jumping Mount and Equipable stuff here ==========
    protected float jumpPower;

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPlayerJump(int jumpPowerIn)
    {
        if (jumpPowerIn < 0) jumpPowerIn = 0;
        if (jumpPowerIn >= 90) this.jumpPower = 1;
        else this.jumpPower = 0.4F + 0.4F * jumpPowerIn / 90.0F;
    }

    @Override
    public boolean canJump()
    {
        return true;
    }

    @Override
    public void handleStartJump(final int jumpPower)
    {
        // Horse plays a sdound here
        this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
    }

    @Override
    public void handleStopJump()
    {
        // Horse does nothing here
    }

    @Override
    /**
     * This is "can have saddle equipped
     */
    public boolean isSaddleable()
    {
        return this.isAlive() && this.getOwnerUUID() != null;
    }

    @Override
    /**
     * This is "add saddle"
     */
    public void equipSaddle(@Nullable final SoundSource sound)
    {
        this.pokemobCap.getInventory().setItem(0, new ItemStack(Items.SADDLE));
        if (sound != null) this.level.playSound((Player) null, this, SoundEvents.HORSE_SADDLE, sound, 0.5F, 1.0F);
    }

    @Override
    public boolean isSaddled()
    {
        return !this.pokemobCap.getInventory().getItem(0).isEmpty();
    }

    // ========== IMultipassenger stuff below here ==============
    @SuppressWarnings("unchecked")
    static final EntityDataAccessor<Seat>[] SEAT = new EntityDataAccessor[10];

    private boolean init = false;
    private String lastPose = "";
    protected int seatCount = 0;

    static
    {
        for (int i = 0; i < PokemobRidable.SEAT.length; i++) PokemobRidable.SEAT[i] = SynchedEntityData
                .<Seat>defineId(PokemobRidable.class, IMultiplePassengerEntity.SEATSERIALIZER);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        for (int i = 0; i < 10; i++) this.entityData.define(PokemobRidable.SEAT[i], new Seat(new Vector3f(), null));
    }

    @Override
    public Entity getPassenger(final Vector3f seatl)
    {
        this.initSeats();
        UUID id = null;
        for (int i = 0; i < this.seatCount; i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).seat.equals(seatl)) id = seat.getEntityId();
        }
        if (id != null) for (final Entity e : this.getPassengers()) if (e.getUUID().equals(id)) return e;
        return null;
    }

    @Override
    public Vector3f getSeat(final Entity passenger)
    {
        this.initSeats();
        final Vector3f ret = null;
        for (int i = 0; i < this.seatCount; i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).getEntityId().equals(passenger.getUUID())) return seat.seat;
        }
        return ret;
    }

    @Override
    public List<Vector3f> getSeats()
    {
        this.initSeats();
        final List<Vector3f> ret = Lists.newArrayList();
        for (int i = 0; i < this.seatCount; i++)
        {
            final Seat seat = this.getSeat(i);
            ret.add(seat.seat);
        }
        return null;
    }

    @Override
    public void updateSeat(final int index, final UUID id)
    {
        Seat toSet = this.getSeat(index);
        final UUID old = toSet.getEntityId();
        if (!old.equals(id))
        {
            toSet = new Seat(toSet.seat, id);
            this.entityData.set(PokemobRidable.SEAT[index], toSet);
        }
    }

    @Override
    protected void initSizes(final float size)
    {
        if (size == getHolder().holder().last_size) return;
        getHolder().holder().last_size = size;
        this.init = false;
        this.initSeats();
        super.initSizes(size);
    }

    @Override
    public void updatePartsPos()
    {
        super.updatePartsPos();
        this.initSeats();
    }

    protected void initSeats()
    {
        if (!(this.getCommandSenderWorld() instanceof ServerLevel)) return;
        if (this.init && this.lastPose.equals(getHolder().holder().effective_pose)) return;
        final PokedexEntry entry = this.pokemobCap.getPokedexEntry();
        this.lastPose = getHolder().holder().effective_pose;
        this.init = true;
        final List<BodyPart> bodySeats = Lists.newArrayList();
        BodyNode body;
        if (entry.poseShapes != null && (body = entry.poseShapes.get(this.lastPose)) != null)
            for (final BodyPart part : body.parts) if (part.__ride__ != null) bodySeats.add(part);
        final float size = this.pokemobCap.getSize();
        if (!bodySeats.isEmpty())
        {
            this.seatCount = bodySeats.size();
            for (int index = 0; index < this.seatCount; index++)
            {
                final Vector3f seat = new Vector3f();
                final BodyPart part = bodySeats.get(index);
                seat.x = (float) (part.__pos__.x + part.__ride__.x) * size;
                seat.y = (float) (part.__pos__.y + part.__ride__.y) * size;
                seat.z = (float) (part.__pos__.z + part.__ride__.z) * size;
                final Seat newSeat = (Seat) this.getSeat(index).clone();
                newSeat.seat = seat;
                this.entityData.set(PokemobRidable.SEAT[index], newSeat);
            }
        }
        else
        {
            this.seatCount = entry.passengerOffsets.length;
            for (int index = 0; index < this.seatCount; index++)
            {
                final Vector3f seat = new Vector3f();
                final double[] offset = entry.passengerOffsets[index];
                seat.x = (float) offset[0];
                seat.y = (float) offset[1];
                seat.z = (float) offset[2];
                final float dx = entry.width * size, dz = entry.length * size;
                seat.x *= dx;
                seat.y *= entry.height * size;
                seat.z *= dz;
                final Seat newSeat = (Seat) this.getSeat(index).clone();
                newSeat.seat = seat;
                this.entityData.set(PokemobRidable.SEAT[index], newSeat);
            }
        }
    }

    @Override
    public float getYaw()
    {
        return this.yBodyRot;
    }

    @Override
    public float getPitch()
    {
        return this.pokemobCap.getDirectionPitch();
    }

    // We do our own rendering, so don't need this.
    @Override
    public float getPrevYaw()
    {
        return this.yBodyRotO;
    }

    // We do our own rendering, so don't need this.
    @Override
    public float getPrevPitch()
    {
        return this.getPitch();
    }

    Seat getSeat(final int index)
    {
        return this.entityData.get(PokemobRidable.SEAT[index]);
    }

    @Override
    public void positionRider(final Entity passenger)
    {
        if (this.hasPassenger(passenger))
            IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
    }

    @Override
    protected void addPassenger(final Entity passenger)
    {
        super.addPassenger(passenger);
        this.initSeats();
        if (!this.level.isClientSide)
            for (int i = 0; i < this.seatCount; i++) if (this.getSeat(i).getEntityId() == Seat.BLANK)
        {
            this.updateSeat(i, passenger.getUUID());
            break;
        }
    }

    @Override
    protected void removePassenger(final Entity passenger)
    {
        super.removePassenger(passenger);
        final double x = this.getX();
        final double y = this.getY();
        final double z = this.getZ();
        passenger.setPos(x, y, z);
        this.initSeats();
        if (!this.level.isClientSide)
            for (int i = 0; i < this.seatCount; i++) if (this.getSeat(i).getEntityId().equals(passenger.getUUID()))
        {
            this.updateSeat(i, Seat.BLANK);
            break;
        }
    }

    @Override
    public boolean canAddPassenger(final Entity passenger)
    {
        if (this.getPassengers().isEmpty()) return passenger == this.pokemobCap.getOwner();
        return this.getPassengers().size() < this.seatCount;
    }

}
