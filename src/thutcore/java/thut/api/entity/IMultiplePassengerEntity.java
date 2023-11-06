package thut.api.entity;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import thut.api.maths.vecmath.Mat3f;
import thut.api.maths.vecmath.Vec3f;

public interface IMultiplePassengerEntity
{
    public static class MultiplePassengerManager
    {
        public static void managePassenger(final Entity passenger, final IMultiplePassengerEntity multipassenger)
        {
            final Entity entity = (Entity) multipassenger;
            if (!entity.hasPassenger(passenger)) return;
            Vec3f v = multipassenger.getSeat(passenger);
            final float yaw = -multipassenger.getYaw() * 0.017453292F;
            final float pitch = -multipassenger.getPitch() * 0.017453292F;
            final float sinYaw = Mth.sin(yaw);
            final float cosYaw = Mth.cos(yaw);
            final float sinPitch = Mth.sin(pitch);
            final float cosPitch = Mth.cos(pitch);
            final Mat3f matrixYaw = new Mat3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
            final Mat3f matrixPitch = new Mat3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
            final Mat3f transform = new Mat3f();
            transform.mul(matrixYaw, matrixPitch);
            if (v == null) v = new Vec3f();
            else
            {
                v = (Vec3f) v.clone();
                transform.transform(v);
            }
            passenger.setPos(entity.getX() + v.x, entity.getY() + passenger.getMyRidingOffset() + v.y,
                    entity.getZ() + v.z);
        }
    }

    public static class Seat
    {
        public static final UUID BLANK = new UUID(0, 0);

        public static Seat readFromNBT(final CompoundTag tag)
        {
            final byte[] arr = tag.getByteArray("v");
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copiedBuffer(arr));
            return new Seat(buf);
        }

        public Vec3f seat;

        private UUID entityId;

        public Seat(final ByteBuf buf)
        {
            this.seat = new Vec3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
            this.setEntityId(new UUID(buf.readLong(), buf.readLong()));
        }

        public Seat(final Vec3f vector3f, final UUID readInt)
        {
            this.seat = vector3f;
            this.setEntityId(readInt != null ? readInt : Seat.BLANK);
        }

        @Override
        public Object clone()
        {
            return new Seat((Vec3f) this.seat.clone(), this.getEntityId());
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof Seat)) return false;
            final Seat other = (Seat) obj;
            return this.getEntityId().equals(other.getEntityId()) && this.seat.epsilonEquals(other.seat, 0.1f);
        }

        /**
         * @return the entityId
         */
        public UUID getEntityId()
        {
            return this.entityId;
        }

        /**
         * @param entityId the entityId to set
         */
        public void setEntityId(final UUID entityId)
        {
            this.entityId = entityId;
        }

        @Override
        public String toString()
        {
            return this.seat + " " + this.getEntityId();
        }

        public void writeToBuf(final ByteBuf buf)
        {
            buf.writeFloat(this.seat.x);
            buf.writeFloat(this.seat.y);
            buf.writeFloat(this.seat.z);
            buf.writeLong(this.getEntityId().getMostSignificantBits());
            buf.writeLong(this.getEntityId().getLeastSignificantBits());
        }

        public void writeToNBT(final CompoundTag tag)
        {
            final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(8));
            this.writeToBuf(buffer);
            tag.putByteArray("v", buffer.array());
        }
    }

    /**
     * Gets the passenger for a seat, if this returns null, it may attempt to
     * add someone to this seat. The seat given here will always be from the
     * contents of the return of getSeats()
     *
     * @param seat
     * @return
     */
    Entity getPassenger(Vec3f seat);

    /**
     * Current pitch rotation for offsetting the ridden entitites
     *
     * @return
     */
    float getPitch();

    /**
     * for rendering interpolation.
     *
     * @return
     */
    float getPrevPitch();

    /**
     * for rendering interpolation.
     *
     * @return
     */
    float getPrevYaw();

    /**
     * Gets the seated location of this passenger, used for properly translating
     * onto the seat.
     *
     * @param passenger
     * @return
     */
    Vec3f getSeat(Entity passenger);

    /**
     * List of seats on this entity;
     *
     * @return
     */
    List<Vec3f> getSeats();

    /**
     * Current rotation yaw, for offsetting of the ridden entitites.
     *
     * @return
     */
    float getYaw();

    void updateSeat(int index, UUID id);
}
