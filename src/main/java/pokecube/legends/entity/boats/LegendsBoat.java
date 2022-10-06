package pokecube.legends.entity.boats;

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
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.EntityInit;
import pokecube.legends.init.ItemInit;

public class LegendsBoat extends Boat
{
    private static final EntityDataAccessor<Integer> BOAT_ID_TYPE = SynchedEntityData.defineId(LegendsBoat.class, EntityDataSerializers.INT);

    public LegendsBoat(EntityType<? extends Boat> boatType, Level world)
    {
        super(boatType, world);
        this.blocksBuilding = true;
    }

    public LegendsBoat(Level world, double x, double y, double z)
    {
        this(EntityInit.BOAT.get(), world);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public Item getDropItem()
    {
        switch(this.getLegendsBoatType())
        {
            case AGED:
            default:
                return ItemInit.AGED_BOAT.get();
            case CONCRETE:
                return ItemInit.CONCRETE_BOAT.get();
            case CORRUPTED:
                return ItemInit.CORRUPTED_BOAT.get();
            case DISTORTIC:
                return ItemInit.DISTORTIC_BOAT.get();
            case INVERTED:
                return ItemInit.INVERTED_BOAT.get();
            case MIRAGE:
                return ItemInit.MIRAGE_BOAT.get();
            case TEMPORAL:
                return ItemInit.TEMPORAL_BOAT.get();
        }
    }

    public Type getLegendsBoatType()
    {
        return Type.byId(this.entityData.get(BOAT_ID_TYPE));
    }

    public void setLegendsBoatType(Type boatType)
    {
        this.entityData.set(BOAT_ID_TYPE, boatType.ordinal());
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(BOAT_ID_TYPE, Type.AGED.ordinal());
        super.defineSynchedData();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putString("Type", this.getLegendsBoatType().getName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if (compound.contains("Type", 8))
        {
            this.setLegendsBoatType(Type.byName(compound.getString("Type")));
        }
    }

    public void setType(Type type)
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
        AGED(BlockInit.AGED_PLANKS.get(), "aged"),
        CONCRETE(BlockInit.CONCRETE_PLANKS.get(), "concrete"),
        CORRUPTED(BlockInit.CORRUPTED_PLANKS.get(), "corrupted"),
        DISTORTIC(BlockInit.DISTORTIC_PLANKS.get(), "distortic"),
        INVERTED(BlockInit.INVERTED_PLANKS.get(), "inverted"),
        MIRAGE(BlockInit.MIRAGE_PLANKS.get(), "mirage"),
        TEMPORAL(BlockInit.TEMPORAL_PLANKS.get(), "temporal");

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

        public static Type byId(int id)
        {
            Type[] types = values();
            if (id < 0 || id >= types.length)
            {
                id = 0;
            }

            return types[id];
        }

        public static Type byName(String name)
        {
            Type[] types = values();

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