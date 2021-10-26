package thut.api.entity;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.MathHelper;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;

public interface IMultiplePassengerEntity
{
    public static class MultiplePassengerManager
    {
        public static void managePassenger(final Entity passenger, final IMultiplePassengerEntity multipassenger)
        {
            final Entity entity = (Entity) multipassenger;
            if (!entity.hasPassenger(passenger)) return;
            Vector3f v = multipassenger.getSeat(passenger);
            final float yaw = -multipassenger.getYaw() * 0.017453292F;
            final float pitch = -multipassenger.getPitch() * 0.017453292F;
            final float sinYaw = MathHelper.sin(yaw);
            final float cosYaw = MathHelper.cos(yaw);
            final float sinPitch = MathHelper.sin(pitch);
            final float cosPitch = MathHelper.cos(pitch);
            final Matrix3f matrixYaw = new Matrix3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
            final Matrix3f matrixPitch = new Matrix3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
            final Matrix3f transform = new Matrix3f();
            transform.mul(matrixYaw, matrixPitch);
            if (v == null) v = new Vector3f();
            else
            {
                v = (Vector3f) v.clone();
                transform.transform(v);
            }
            passenger.setPos(entity.getX() + v.x, entity.getY() + passenger.getMyRidingOffset() + v.y, entity
                    .getZ() + v.z);
        }
    }

    public static class Seat
    {
        public static final UUID BLANK = new UUID(0, 0);

        public static Seat readFromNBT(final CompoundNBT tag)
        {
            final byte[] arr = tag.getByteArray("v");
            final PacketBuffer buf = new PacketBuffer(Unpooled.copiedBuffer(arr));
            return new Seat(buf);
        }

        public Vector3f seat;

        private UUID entityId;

        public Seat(final PacketBuffer buf)
        {
            this.seat = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
            this.setEntityId(new UUID(buf.readLong(), buf.readLong()));
        }

        public Seat(final Vector3f vector3f, final UUID readInt)
        {
            this.seat = vector3f;
            this.setEntityId(readInt != null ? readInt : Seat.BLANK);
        }

        @Override
        public Object clone()
        {
            return new Seat((Vector3f) this.seat.clone(), this.getEntityId());
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
         * @param entityId
         *            the entityId to set
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

        public void writeToBuf(final PacketBuffer buf)
        {
            buf.writeFloat(this.seat.x);
            buf.writeFloat(this.seat.y);
            buf.writeFloat(this.seat.z);
            buf.writeLong(this.getEntityId().getMostSignificantBits());
            buf.writeLong(this.getEntityId().getLeastSignificantBits());
        }

        public void writeToNBT(final CompoundNBT tag)
        {
            final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(8));
            this.writeToBuf(buffer);
            tag.putByteArray("v", buffer.array());
        }
    }

    public static final IDataSerializer<Seat> SEATSERIALIZER = new IDataSerializer<Seat>()
    {
        @Override
        public Seat copy(final Seat value)
        {
            return new Seat((Vector3f) value.seat.clone(), value.getEntityId());
        }

        @Override
        public DataParameter<Seat> createAccessor(final int id)
        {
            return new DataParameter<>(id, this);
        }

        @Override
        public Seat read(final PacketBuffer buf)
        {
            return new Seat(buf);
        }

        @Override
        public void write(final PacketBuffer buf, final Seat value)
        {
            value.writeToBuf(buf);
        }
    };

    /**
     * Gets the passenger for a seat, if this returns null, it may attempt to
     * add someone to this seat. The seat given here will always be from the
     * contents of the return of getSeats()
     *
     * @param seat
     * @return
     */
    Entity getPassenger(Vector3f seat);

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
     * Gets the seated location of this passenger, used for properly
     * translating onto the seat.
     *
     * @param passenger
     * @return
     */
    Vector3f getSeat(Entity passenger);

    /**
     * List of seats on this entity;
     *
     * @return
     */
    List<Vector3f> getSeats();

    /**
     * Current rotation yaw, for offsetting of the ridden entitites.
     *
     * @return
     */
    float getYaw();

    void updateSeat(int index, UUID id);
}
