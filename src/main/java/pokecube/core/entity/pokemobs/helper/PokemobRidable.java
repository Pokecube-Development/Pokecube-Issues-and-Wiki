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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
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
    public LivingEntity getControllingPassenger()
    {
        final List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) return null;
        return this.getPassengers().get(0).getUUID().equals(this.pokemobCap.getOwnerId())
                ? (LivingEntity) this.getPassengers().get(0)
                : null;
    }

    @Override
    public boolean canBeRiddenUnderFluidType(FluidType type, Entity rider)
    {
        // TODO add lava check as well!
        return super.canBeRiddenUnderFluidType(type, rider);
    }

    @Override
    public boolean dismountsUnderwater()
    {
        return !this.pokemobCap.canUseSurf() && !this.pokemobCap.canUseDive()
                && !this.getType().is(EntityTypeTags.DISMOUNTS_UNDERWATER);
    }

    // ========== Jumping Mount and Equipable stuff here ==========
    protected float jumpPower;

    @Override
    public void onPlayerJump(int jumpPowerIn)
    {
        boolean shouldFly = this.pokemobCap.getController().verticalControl;
        if(shouldFly) return;
        
        if (jumpPowerIn < 0) jumpPowerIn = 0;
        if (jumpPowerIn >= 90) this.jumpPower = 1;
        else this.jumpPower = 0.4F + 0.4F * jumpPowerIn / 90.0F;

        if (jumpPowerIn >= 90)
        {
            this.playerJumpPendingScale = 1.0F;
        }
        else
        {
            this.playerJumpPendingScale = 0.4F + 0.4F * (float) jumpPowerIn / 90.0F;
        }
    }
    
    @Override
    public int getJumpCooldown()
    {
        boolean shouldFly = this.pokemobCap.getController().verticalControl;
        if(shouldFly) return 10;
        return 0;
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

    protected boolean isJumping;
    protected float playerJumpPendingScale;
    protected int gallopSoundCounter;
    protected boolean canGallop = true;

    public boolean isJumping()
    {
        return this.isJumping;
    }

    public void setIsJumping(boolean p_30656_)
    {
        this.isJumping = p_30656_;
    }

    protected void executeRidersJump(float p_248808_, Vec3 p_275435_)
    {
        double d0 = 1.0 * (double) p_248808_ * (double) this.getBlockJumpFactor();
        double d1 = d0 + (double) this.getJumpBoostPower();
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, d1, vec3.z);
        this.setIsJumping(true);
        this.hasImpulse = true;
        net.minecraftforge.common.ForgeHooks.onLivingJump(this);
        if (p_275435_.z > 0.0D)
        {
            float f = Mth.sin(this.getYRot() * ((float) Math.PI / 180F));
            float f1 = Mth.cos(this.getYRot() * ((float) Math.PI / 180F));
            this.setDeltaMovement(this.getDeltaMovement().add((double) (-0.4F * f * p_248808_), 0.0D,
                    (double) (0.4F * f1 * p_248808_)));
        }

    }

    @Override
    protected void tickRidden(Player player, Vec3 input_direction)
    {
        super.tickRidden(player, input_direction);
        Vec2 vec2 = this.getRiddenRotation(player);
        this.setRot(vec2.y, vec2.x);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        boolean shouldFly = this.pokemobCap.getController().verticalControl;
        if (this.isControlledByLocalInstance() && !shouldFly)
        {
            if (input_direction.z <= 0.0D)
            {
                this.gallopSoundCounter = 0;
            }

            if (this.onGround())
            {
                this.setIsJumping(false);
                if (this.playerJumpPendingScale > 0.0F && !this.isJumping())
                {
                    this.executeRidersJump(this.playerJumpPendingScale, input_direction);
                }
                this.playerJumpPendingScale = 0.0F;
            }
        }

    }

    protected Vec2 getRiddenRotation(LivingEntity p_275502_)
    {
        return new Vec2(p_275502_.getXRot() * 0.5F, p_275502_.getYRot());
    }

    @Override
    protected float getRiddenSpeed(Player p_278286_)
    {
        double scale = PokecubeCore.getConfig().groundSpeedFactor;
        double base = this.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
        if (this.pokemobCap.getController().inFluid) scale = PokecubeCore.getConfig().surfSpeedFactor;
        else if (this.pokemobCap.getController().canFly && !this.pokemobCap.onGround())
        {
            scale = PokecubeCore.getConfig().surfSpeedFactor;
            base = this.getAttribute(Attributes.FLYING_SPEED).getValue();
        }
        return (float) (scale * base);
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 original_v)
    {
        boolean shouldFly = this.pokemobCap.getController().verticalControl;
        if (shouldFly)
        {
            float f = player.xxa * 0.5F;
            float f1 = player.zza;
            if (f1 <= 0.0F)
            {
                f1 *= 0.25F;
            }
            return new Vec3((double) f, this.pokemobCap.getController().moveUp, (double) f1);
        }
        
        float f = player.xxa * 0.5F;
        float f1 = player.zza;
        if (f1 <= 0.0F)
        {
            f1 *= 0.25F;
        }
        return new Vec3((double) f, 0.0D, (double) f1);
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
        if (!(this.level instanceof ServerLevel)) return;
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
                seat.x = (float) (part.__pos__.x + part.__ride__.x) * size;
                seat.y = (float) (part.__pos__.y + part.__ride__.y) * size;
                seat.z = (float) (part.__pos__.z + part.__ride__.z) * size;
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
    public void positionRider(final Entity passenger, Entity.MoveFunction moveFunction)
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
