package pokecube.core.entity.boats;

import java.util.Collection;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import pokecube.core.init.EntityTypes;

public class GenericBoat extends Boat
{
    private static final EntityDataAccessor<String> BOAT_ID_TYPE = SynchedEntityData.defineId(GenericBoat.class,
            EntityDataSerializers.STRING);

    private static final Object2ObjectOpenHashMap<String, BoatType> BOAT_REGISTRY = new Object2ObjectOpenHashMap<>();
    private static final BoatType NULL_TYPE = new BoatType(() -> Blocks.DIAMOND_BLOCK, () -> Items.DIAMOND, "null");

    static
    {
        BOAT_REGISTRY.put(NULL_TYPE.name(), NULL_TYPE);
        BOAT_REGISTRY.defaultReturnValue(NULL_TYPE);
    }

    public static final BoatType registerBoat(Supplier<Block> block, Supplier<Item> item, String name)
    {
        BoatType type = new BoatType(block, item, name);
        BOAT_REGISTRY.put(name, type);
        if (BOAT_REGISTRY.defaultReturnValue() == NULL_TYPE)
        {
            BOAT_REGISTRY.defaultReturnValue(type);
            BOAT_REGISTRY.remove(NULL_TYPE.name());
        }
        return type;
    }

    public static Collection<BoatType> getTypes()
    {
        return BOAT_REGISTRY.values();
    }

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
        return this.getGenericBoatType().item.get();
    }

    public BoatType getGenericBoatType()
    {
        return BOAT_REGISTRY.get(this.entityData.get(BOAT_ID_TYPE));
    }

    public void setGenericBoatType(BoatType boatType)
    {
        this.entityData.set(BOAT_ID_TYPE, boatType.name());
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(BOAT_ID_TYPE, "enigma");
        super.defineSynchedData();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putString("Type", this.getGenericBoatType().name());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        if (compound.contains("Type", 8))
        {
            this.setGenericBoatType(BOAT_REGISTRY.get(compound.getString("Type")));
        }
    }

    public void setType(BoatType type)
    {
        this.entityData.set(BOAT_ID_TYPE, type.name());
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return new ClientboundAddEntityPacket(this);
    }

    public static record BoatType(Supplier<Block> block, Supplier<Item> item, String name)
    {
    }

    public static record BoatRegister(Supplier<Block> block, String name, CreativeModeTab tab,
            DeferredRegister<Item> register)
    {
    }
}
