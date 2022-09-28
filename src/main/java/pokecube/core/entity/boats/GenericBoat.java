package pokecube.core.entity.boats;

import com.mojang.math.Vector3d;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeItems;
import pokecube.core.init.EntityTypes;
import pokecube.core.items.berries.BerryManager;
import pokecube.legends.init.EntityInit;

public class GenericBoat extends Boat
{
    private static final EntityDataAccessor<Integer> BOAT_ID_TYPE = SynchedEntityData.defineId(GenericBoat.class, EntityDataSerializers.INT);

    public GenericBoat(EntityType<? extends Boat> boatType, Level world)
    {
        super(boatType, world);
        this.blocksBuilding = true;
    }

    public GenericBoat(Level world, double x, double y, double z)
    {
        this(EntityTypes.BOAT.get(), world);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public Item getDropItem()
    {
        switch(this.getGenericBoatType())
        {
            case ENIGMA:
            default:
                return PokecubeItems.ENIGMA_BOAT.get();
            case LEPPA:
                return PokecubeItems.LEPPA_BOAT.get();
            case NANAB:
                return PokecubeItems.NANAB_BOAT.get();
            case ORAN:
                return PokecubeItems.ORAN_BOAT.get();
            case PECHA:
                return PokecubeItems.PECHA_BOAT.get();
            case SITRUS:
                return PokecubeItems.SITRUS_BOAT.get();
        }
    }

    public GenericBoat.Type getGenericBoatType()
    {
        return GenericBoat.Type.byId(this.entityData.get(BOAT_ID_TYPE));
    }

    public void setGenericBoatType(Type boatType)
    {
        this.entityData.set(BOAT_ID_TYPE, boatType.ordinal());
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(BOAT_ID_TYPE, GenericBoat.Type.ENIGMA.ordinal());
        super.defineSynchedData();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putString("Type", this.getGenericBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if (compound.contains("Type", 8))
        {
            this.setGenericBoatType(GenericBoat.Type.byName(compound.getString("Type")));
        }
    }

    public void setType(GenericBoat.Type type)
    {
        this.entityData.set(BOAT_ID_TYPE, type.ordinal());
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    public enum Type
    {
        ENIGMA(BerryManager.berryPlanks.get(60), "enigma"),
        LEPPA(BerryManager.berryPlanks.get(6), "leppa"),
        NANAB(BerryManager.berryPlanks.get(18), "nanab"),
        ORAN(BerryManager.berryPlanks.get(7), "oran"),
        PECHA(BerryManager.berryPlanks.get(3), "pecha"),
        SITRUS(BerryManager.berryPlanks.get(10), "sitrus");

        private final String name;
        private final Block planks;

        Type(Block block, String name)
        {
            this.name = name;
            this.planks = block;
        }

        public String getName()
        {
            return this.name;
        }

        public Block getPlanks()
        {
            return this.planks;
        }

        public String toString()
        {
            return this.name;
        }

        public static GenericBoat.Type byId(int id)
        {
            GenericBoat.Type[] aboat$type = values();
            if (id < 0 || id >= aboat$type.length)
            {
                id = 0;
            }

            return aboat$type[id];
        }

        public static GenericBoat.Type byName(String name)
        {
            GenericBoat.Type[] types = values();

            for(int i = 0; i < types.length; ++i)
            {
                if (types[i].getName().equals(name))
                {
                    return types[i];
                }
            }

            return types[0];
        }
    }
}
