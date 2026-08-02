package thut.api.attachments;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.data.HolderProvider;

import java.util.function.Supplier;

public class Energy
{
    public static class CustomeStorage extends EnergyStorage
    {

        public CustomeStorage(int capacity)
        {
            super(capacity);
        }

        public CustomeStorage(int capacity, int maxTransfer)
        {
            super(capacity, maxTransfer, maxTransfer, 0);
        }

        public CustomeStorage(int capacity, int maxReceive, int maxExtract)
        {
            super(capacity, maxReceive, maxExtract, 0);
        }

        public CustomeStorage(int capacity, int maxReceive, int maxExtract, int energy)
        {
            super(capacity, maxReceive, maxExtract, energy);
        }

        public int maxReceive()
        {
            return maxReceive;
        }

        public int maxExtract()
        {
            return maxExtract;
        }

    }

    public static class Wrapping extends CustomeStorage
    {
        final IEnergyStorage wrapped;

        public Wrapping(IEnergyStorage wrap)
        {
            super(0);
            wrapped = wrap;
        }

        @Override
        public boolean canExtract()
        {
            return wrapped.canExtract();
        }

        @Override
        public boolean canReceive()
        {
            return wrapped.canReceive();
        }

        @Override
        public int getEnergyStored()
        {
            return wrapped.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored()
        {
            return wrapped.getMaxEnergyStored();
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate)
        {
            return wrapped.extractEnergy(toExtract, simulate);
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate)
        {
            return wrapped.receiveEnergy(toReceive, simulate);
        }
    }

    public static record EnergyHolder(CustomeStorage energy, CompoundTag tag)
    {
        public EnergyHolder(CompoundTag tag)
        {
            this(null, tag);
        }

        public EnergyHolder withContext(HolderLookup.Provider context)
        {
            int C = this.tag.getInt("C");
            int MR = this.tag.getInt("MR");
            int ME = this.tag.getInt("ME");
            CustomeStorage contents = new CustomeStorage(C, MR, ME);

            contents.deserializeNBT(context, this.tag().get("E"));
            return new EnergyHolder(contents, this.tag);
        }

        public EnergyHolder saveHolder(HolderLookup.Provider context)
        {
            var saved = this.energy.serializeNBT(context);
            var tag = new CompoundTag();
            tag.put("E", saved);
            tag.putInt("C", this.energy.getMaxEnergyStored());
            tag.putInt("MR", this.energy.maxReceive());
            tag.putInt("ME", this.energy.maxExtract());
            return new EnergyHolder(energy, tag);
        }

        public static final Codec<EnergyHolder> CODEC = CompoundTag.CODEC.<EnergyHolder>comapFlatMap(EnergyHolder::read,
                EnergyHolder::tag).stable();
        public static final StreamCodec<ByteBuf, EnergyHolder> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
                EnergyHolder::parse, EnergyHolder::tag);

        public static DataResult<EnergyHolder> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(parse(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid itemholder tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }

        public static EnergyHolder parse(CompoundTag tag)
        {
            return new EnergyHolder(tag);
        }
    }

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:energy");

    // ITEM DATA

    public static Supplier<DataComponentType<EnergyHolder>> INVENTORY_STORE;

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        INVENTORY_STORE = registry.register("energy_storage",
                name -> new DataComponentType.Builder<EnergyHolder>().persistent(EnergyHolder.CODEC)
                        .networkSynchronized(EnergyHolder.STREAM_CODEC).build());
    }

    // ENTITY/TILE ENTITY ATTACHMENT

    public static Supplier<AttachmentType<EnergyStorage>> TYPE;

    public static final HolderProvider<EnergyStorage> REGISTRY = new HolderProvider<>(
            ResourceLocation.parse("thutcore:energy"));

    public static EnergyStorage makeProvider(final IAttachmentHolder in)
    {
        return new EnergyStorage(1);
    }

    public static EnergyStorage get_raw(final IAttachmentHolder in, Direction d)
    {
        return in.getData(TYPE);
    }

    public static EnergyStorage get(final IAttachmentHolder in, Direction d)
    {
        IEnergyStorage toWrap = null;
        if(in instanceof Entity e){
            toWrap =e.getCapability(Capabilities.EnergyStorage.ENTITY, d);
        }
        if(in instanceof BlockEntity b && b.getLevel()!=null){
            toWrap = b.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, b.getBlockPos(), d);
        }
        if(toWrap!=null) return new Wrapping(toWrap);
        return get_raw(in, d);
    }

    public static boolean has(final IAttachmentHolder in, Direction d)
    {
        IEnergyStorage toWrap = null;
        if(in instanceof Entity e){
            toWrap =e.getCapability(Capabilities.EnergyStorage.ENTITY, d);
        }
        if(in instanceof BlockEntity b){
            toWrap =b.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, b.getBlockPos(), d);
        }
        if(toWrap!=null) return true;
        return in.hasData(TYPE);
    }

    public static EnergyStorage get(final IAttachmentHolder in)
    {
        return get(in, Direction.DOWN);
    }

    public static boolean has(final IAttachmentHolder in)
    {
        return has(in, Direction.DOWN);
    }

    public static void set(final IAttachmentHolder in, EnergyStorage storage)
    {
        in.setData(TYPE, storage);
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register("energy", () -> AttachmentType.serializable(REGISTRY::make).build());
    }
}
