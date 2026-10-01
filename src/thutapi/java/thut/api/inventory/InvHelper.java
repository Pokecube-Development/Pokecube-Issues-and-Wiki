package thut.api.inventory;

import java.util.function.BiFunction;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import thut.api.item.ItemList;

public class InvHelper
{
    public static class ItemCap extends ItemStackHandler
    {
        private final int stackSize;
        public ResourceLocation mask;
        public BiFunction<Integer, ItemStack, Boolean> stackCheck = (c, s) -> true;

        public ItemCap(int slotCount)
        {
            this(slotCount, 64, null);
        }

        public ItemCap(int slotCount, int stackSize)
        {
            this(slotCount, stackSize, null);
        }

        public ItemCap(int slotCount, int stackSize, ResourceLocation mask)
        {
            super(slotCount);
            this.stackSize = stackSize;
            this.mask = mask;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack)
        {
            if (this.mask != null) return ItemList.is(this.mask, stack) && this.stackCheck.apply(slot, stack);
            return super.isItemValid(slot, stack) && this.stackCheck.apply(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return this.stackSize;
        }

        @Override
        public void deserializeNBT(Provider provider, CompoundTag nbt)
        {
            super.deserializeNBT(provider, nbt);
            if (nbt.contains("mask")) mask = ResourceLocation.parse(nbt.getString("mask"));
        }

        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            var tag = super.serializeNBT(provider);
            if (this.mask != null) tag.putString("mask", this.mask.toString());
            return tag;
        }
    }

    public static void load(final Container inven, final CompoundTag tag, HolderLookup.Provider registries)
    {
        final ListTag listnbt = tag.getList("Items", 10);
        for (int i = 0; i < listnbt.size(); ++i)
        {
            final CompoundTag compoundnbt = listnbt.getCompound(i);
            final int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < inven.getContainerSize())
                inven.setItem(j, ItemStack.parseOptional(registries, compoundnbt));
        }
    }

    public static void save(final Container inven, final CompoundTag tag, HolderLookup.Provider registries)
    {
        final ListTag listnbt = new ListTag();
        for (int i = 0; i < inven.getContainerSize(); ++i)
        {
            final ItemStack itemstack = inven.getItem(i);
            if (!itemstack.isEmpty())
            {
                final CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                listnbt.add(itemstack.save(registries, compoundnbt));
            }
        }
        tag.put("Items", listnbt);
    }

}
