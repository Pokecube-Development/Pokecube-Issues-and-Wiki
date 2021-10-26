package thut.wearables.inventory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;

public class PlayerWearables implements IWearableInventory, IItemHandlerModifiable, ICapabilitySerializable<CompoundTag>
{
    private static class WearableSlot
    {
        final EnumWearable    type;
        final List<ItemStack> slots;

        WearableSlot(final EnumWearable type)
        {
            this.type = type;
            this.slots = NonNullList.withSize(type.slots, ItemStack.EMPTY);
        }

        boolean addStack(final ItemStack stack)
        {
            for (int i = 0; i < this.slots.size(); i++)
                if (this.slots.get(i).isEmpty())
                {
                    this.setStack(i, stack);
                    return true;
                }
            return false;
        }

        ItemStack getStack()
        {
            for (final ItemStack element : this.slots)
                if (!element.isEmpty()) return element;
            return ItemStack.EMPTY;
        }

        ItemStack getStack(final int slot)
        {
            return this.slots.get(slot);
        }

        void loadFromNBT(final CompoundTag tag)
        {
            for (int n = 0; n < this.slots.size(); n++)
            {
                final Tag temp = tag.get("slot" + n);
                if (temp instanceof CompoundTag)
                {
                    final CompoundTag tag1 = (CompoundTag) temp;
                    this.setStack(n, ItemStack.of(tag1));
                }
            }
        }

        public ItemStack removeStack(final int subIndex)
        {
            if (!this.slots.get(subIndex).isEmpty())
            {
                final ItemStack stack = this.slots.get(subIndex);
                this.setStack(subIndex, ItemStack.EMPTY);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        CompoundTag saveToNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putByte("type", (byte) this.type.ordinal());
            for (int n = 0; n < this.slots.size(); n++)
            {
                final ItemStack i = this.getStack(n);
                if (!i.isEmpty())
                {
                    final CompoundTag tag1 = new CompoundTag();
                    i.save(tag1);
                    tag.put("slot" + n, tag1);
                }
            }
            return tag;
        }

        void setStack(final int slot, final ItemStack stack)
        {
            this.slots.set(slot, stack);
        }
    }

    private final Map<EnumWearable, WearableSlot> slots = Maps.newHashMap();

    private final LazyOptional<IWearableInventory> holder = LazyOptional.of(() -> this);

    public PlayerWearables()
    {
        for (final EnumWearable type : EnumWearable.values())
            this.slots.put(type, new WearableSlot(type));
    }

    public String dataFileName()
    {
        return "wearables";
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.readFromNBT(nbt);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (this.getStackInSlot(slot).isEmpty()) return ItemStack.EMPTY;
        if (simulate) return amount > 0 ? this.getStackInSlot(slot) : ItemStack.EMPTY;
        return this.slots.get(EnumWearable.getWearable(slot)).removeStack(EnumWearable.getSubIndex(slot));
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        return ThutWearables.WEARABLES_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        return 1;
    }

    @Override
    public int getSlots()
    {
        return 13;
    }

    @Override
    public ItemStack getStackInSlot(final int index)
    {
        return this.slots.get(EnumWearable.getWearable(index)).getStack(EnumWearable.getSubIndex(index));
    }

    @Override
    public ItemStack getWearable(final EnumWearable type)
    {
        return this.slots.get(type).getStack();
    }

    @Override
    public ItemStack getWearable(final EnumWearable type, final int slot)
    {
        return this.slots.get(type).getStack(slot);
    }

    @Override
    public Set<ItemStack> getWearables()
    {
        final Set<ItemStack> ret = Sets.newHashSet();
        for (final WearableSlot slot : this.slots.values())
            for (final ItemStack element : slot.slots)
                if (!element.isEmpty()) ret.add(element);
        return ret;
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate)
    {
        if (!this.getStackInSlot(slot).isEmpty()) return stack;
        if (simulate) return ItemStack.EMPTY;
        this.setStackInSlot(slot, stack);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack)
    {
        final EnumWearable wear = EnumWearable.getWearable(slot);
        final IWearable worn = stack.getItem() instanceof IWearable ? (IWearable) stack.getItem() : null;
        if (worn != null) return worn.getSlot(stack) == wear;
        final IActiveWearable worn2 = stack.getCapability(ThutWearables.WEARABLE_CAP).orElse(null);
        if (worn2 != null) return worn2.getSlot(stack) == wear;
        return false;
    }

    public void readFromNBT(final CompoundTag tag)
    {
        for (final EnumWearable type : EnumWearable.values())
            this.slots.put(type, new WearableSlot(type));
        for (final EnumWearable slot : this.slots.keySet())
        {
            final CompoundTag compound = tag.getCompound(slot.ordinal() + "");
            this.slots.get(slot).loadFromNBT(compound);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        return this.writeToNBT(new CompoundTag());
    }

    @Override
    public void setStackInSlot(final int slot, final ItemStack stack)
    {
        this.slots.get(EnumWearable.getWearable(slot)).setStack(EnumWearable.getSubIndex(slot), stack);
    }

    @Override
    public boolean setWearable(final EnumWearable type, final ItemStack stack)
    {
        final WearableSlot wSlot = this.slots.get(type);
        return wSlot.addStack(stack);
    }

    @Override
    public boolean setWearable(final EnumWearable type, final ItemStack stack, final int slot)
    {
        final WearableSlot wSlot = this.slots.get(type);
        wSlot.setStack(slot, stack);
        return true;
    }

    public CompoundTag writeToNBT(final CompoundTag tag)
    {
        for (final EnumWearable slot : this.slots.keySet())
        {
            final CompoundTag compound = this.slots.get(slot).saveToNBT();
            tag.put(slot.ordinal() + "", compound);
        }
        return tag;
    }
}
