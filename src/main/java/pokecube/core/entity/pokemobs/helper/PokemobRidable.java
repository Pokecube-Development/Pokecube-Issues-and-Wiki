package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.entity.multipart.GenericPartEntity.BodyPart;
import thut.api.maths.vecmath.Mat3f;
import thut.api.maths.vecmath.Vec3f;
import thut.core.common.world.mobs.data.types.Data_Seat;

public abstract class PokemobRidable extends PokemobHasParts
        implements IMultiplePassengerEntity, PlayerRideableJumping, Saddleable
{

    public PokemobRidable(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
        // Define the seats
        for (int i = 0; i < SEAT.length; i++)
            SEAT[i] = this.pokemobCap.dataSync().register(new Data_Seat().setRealtime(), new Seat(new Vec3f(), null));
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
    final Integer[] SEAT = new Integer[10];

    private boolean init = false;
    private String lastPose = "";
    protected int seatCount = 0;

    @Override
    public Entity getPassenger(final Vec3f seatl)
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
    public Vec3f getSeat(final Entity passenger)
    {
        this.initSeats();
        final Vec3f ret = null;
        for (int i = 0; i < this.seatCount; i++)
        {
            Seat seat;
            if ((seat = this.getSeat(i)).getEntityId().equals(passenger.getUUID())) return seat.seat;
        }
        return ret;
    }

    @Override
    public List<Vec3f> getSeats()
    {
        this.initSeats();
        final List<Vec3f> ret = Lists.newArrayList();
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
            this.pokemobCap.dataSync().set(SEAT[index], toSet);
        }
    }

    @Override
    protected void initSizes(final float size)
    {
        float a = 1, b = 1, c = 1;
        final PokedexEntry entry = pokemobCap.getPokedexEntry();
        float h = size;
        if (entry != null)
        {
            a = entry.width * size;
            b = entry.height * size;
            c = entry.length * size;
            h = Math.max(a, Math.max(b, c));
        }
        if (h == getHolder().holder().last_size) return;
        getHolder().holder().last_size = h;
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
        if (!(this.getLevel() instanceof ServerLevel)) return;
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
                final Vec3f seat = new Vec3f();
                final BodyPart part = bodySeats.get(index);
                seat.x = (float) (part.__ride__.x) * size;
                seat.y = (float) (part.__ride__.y) * size;
                seat.z = (float) (part.__ride__.z) * size;
                final Seat newSeat = (Seat) this.getSeat(index).clone();
                newSeat.seat = seat;
                this.pokemobCap.dataSync().set(SEAT[index], newSeat);
            }
        }
        else
        {
            this.seatCount = entry.passengerOffsets.length;
            for (int index = 0; index < this.seatCount; index++)
            {
                final Vec3f seat = new Vec3f();
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
                this.pokemobCap.dataSync().set(SEAT[index], newSeat);
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
        return this.pokemobCap.getPitch();
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
        return this.pokemobCap.dataSync().get(SEAT[index]);
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
        this.initSeats();
        if (!this.level.isClientSide)
            for (int i = 0; i < this.seatCount; i++) if (this.getSeat(i).getEntityId().equals(passenger.getUUID()))
        {
            this.updateSeat(i, Seat.BLANK);
            break;
        }
        if (passenger instanceof ServerPlayer player)
        {
            player.getServer().tell(new TickTask(player.getServer().getTickCount() + 1, () -> {
                passenger.moveTo(x, y, z);
            }));
        }
        else passenger.moveTo(x, y, z);
    }

    @Override
    public boolean canAddPassenger(final Entity passenger)
    {
        if (this.getPassengers().isEmpty()) return passenger == this.pokemobCap.getOwner();
        return this.getPassengers().size() < this.seatCount;
    }

    @Override
    public void setPos(double x, double y, double z)
    {
        if (this.getVehicle() instanceof Player player)
        {
            final float yaw = -player.yBodyRot * 0.017453292F;
            final float pitch = 0;
            final float sinYaw = Mth.sin(yaw);
            final float cosYaw = Mth.cos(yaw);
            final float sinPitch = Mth.sin(pitch);
            final float cosPitch = Mth.cos(pitch);
            final Mat3f matrixYaw = new Mat3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
            final Mat3f matrixPitch = new Mat3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
            final Mat3f transform = new Mat3f();
            transform.mul(matrixYaw, matrixPitch);

            float dx = this == player.getPassengers().get(0) ? 0.2f + this.getBbWidth() / 2
                    : -(0.4f + this.getBbWidth() / 2);

            this.setOrderedToSit(true);

            Vec3f v = new Vec3f(dx, -0.1f, 0);
            transform.transform(v);
            x += v.x;
            y += v.y;
            z += v.z;

            this.yBodyRot = player.yBodyRot;
            this.yBodyRotO = player.yBodyRotO;
        }
        super.setPos(x, y, z);
    }
}
