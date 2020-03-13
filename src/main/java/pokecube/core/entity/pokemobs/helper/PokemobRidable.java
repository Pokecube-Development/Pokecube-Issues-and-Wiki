package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.database.PokedexEntry;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.maths.vecmath.Vector3f;

public abstract class PokemobRidable extends PokemobBase implements IMultiplePassengerEntity
{

    public PokemobRidable(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean canPassengerSteer()
    {
        if (this.getPassengers().isEmpty()) return false;
        return this.getPassengers().get(0).getUniqueID().equals(this.pokemobCap.getOwnerId());
    }

    @Override
    public Entity getControllingPassenger()
    {
        final List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) return null;
        return this.getPassengers().get(0).getUniqueID().equals(this.pokemobCap.getOwnerId()) ? this.getPassengers()
                .get(0) : null;
    }

    @Override
    public boolean canBeRiddenInWater()
    {
        return this.pokemobCap.canUseSurf() || this.pokemobCap.canUseDive();
    }

    // ========== IMultipassenger stuff below here ==============
    @SuppressWarnings("unchecked")
    static final DataParameter<Seat>[] SEAT = new DataParameter[10];

    private boolean      init      = false;
    private PokedexEntry lastCheck = null;
    protected int        seatCount = 0;

    static
    {
        for (int i = 0; i < PokemobRidable.SEAT.length; i++)
            PokemobRidable.SEAT[i] = EntityDataManager.<Seat> createKey(PokemobRidable.class,
                    IMultiplePassengerEntity.SEATSERIALIZER);
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        for (int i = 0; i < 10; i++)
            this.dataManager.register(PokemobRidable.SEAT[i], new Seat(new Vector3f(), null));
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
        if (id != null) for (final Entity e : this.getPassengers())
            if (e.getUniqueID().equals(id)) return e;
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
            if ((seat = this.getSeat(i)).getEntityId().equals(passenger.getUniqueID())) return seat.seat;
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
            this.dataManager.set(PokemobRidable.SEAT[index], toSet);

        }
    }

    protected void initSeats()
    {
        if (!(this.getEntityWorld() instanceof ServerWorld)) return;
        if (this.init && this.lastCheck == this.pokemobCap.getPokedexEntry()) return;
        this.lastCheck = this.pokemobCap.getPokedexEntry();
        this.init = true;
        this.seatCount = this.pokemobCap.getPokedexEntry().passengerOffsets.length;
        for (int index = 0; index < this.seatCount; index++)
        {
            final Vector3f seat = new Vector3f();
            final double[] offset = this.pokemobCap.getPokedexEntry().passengerOffsets[index];
            seat.x = (float) offset[0];
            seat.y = (float) offset[1];
            seat.z = (float) offset[2];
            final float dx = this.pokemobCap.getPokedexEntry().width * this.pokemobCap.getSize(), dz = this.pokemobCap
                    .getPokedexEntry().length * this.pokemobCap.getSize();
            seat.x *= dx;
            seat.y *= this.getHeight();
            seat.z *= dz;
            this.getSeat(index).seat = seat;
        }
    }

    @Override
    public float getYaw()
    {
        return this.renderYawOffset;
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
        return this.prevRenderYawOffset;
    }

    // We do our own rendering, so don't need this.
    @Override
    public float getPrevPitch()
    {
        return this.getPitch();
    }

    Seat getSeat(final int index)
    {
        return this.dataManager.get(PokemobRidable.SEAT[index]);
    }

    @Override
    public void updatePassenger(final Entity passenger)
    {
        if (this.isPassenger(passenger)) IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger,
                this);
    }

    @Override
    protected void addPassenger(final Entity passenger)
    {
        super.addPassenger(passenger);
        this.initSeats();
        if (!this.world.isRemote) for (int i = 0; i < this.seatCount; i++)
            if (this.getSeat(i).getEntityId() == Seat.BLANK)
            {
                this.updateSeat(i, passenger.getUniqueID());
                break;
            }
    }

    @Override
    protected void removePassenger(final Entity passenger)
    {
        super.removePassenger(passenger);
        this.initSeats();
        if (!this.world.isRemote) for (int i = 0; i < this.seatCount; i++)
            if (this.getSeat(i).getEntityId().equals(passenger.getUniqueID()))
            {
                this.updateSeat(i, Seat.BLANK);
                break;
            }
    }

    @Override
    public boolean canFitPassenger(final Entity passenger)
    {
        if (this.getPassengers().isEmpty()) return passenger == this.pokemobCap.getOwner();
        return this.getPassengers().size() < this.seatCount;
    }

}
