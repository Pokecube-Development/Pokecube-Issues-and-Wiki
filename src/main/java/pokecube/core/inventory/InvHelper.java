package pokecube.core.inventory;

import java.util.function.Predicate;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thut.api.item.ItemList;

public class InvHelper
{
    public static class ItemCap extends ItemStackHandler implements ICapabilitySerializable<CompoundTag>
    {
        private final int                        stackSize;
        private final LazyOptional<IItemHandler> holder     = LazyOptional.of(() -> this);
        private final ResourceLocation           mask;
        public Predicate<ItemStack>              stackCheck = (s) -> true;

        public ItemCap(final int slotCount, final int stackSize)
        {
            this(slotCount, stackSize, null);
        }

        public ItemCap(final int slotCount, final int stackSize, final ResourceLocation mask)
        {
            super(slotCount);
            this.stackSize = stackSize;
            this.mask = mask;
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack)
        {
            if (this.mask != null) return ItemList.is(this.mask, stack) && this.stackCheck.test(stack);
            return super.isItemValid(slot, stack) && this.stackCheck.test(stack);
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return this.stackSize;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, this.holder);
        }
    }

    public static void load(final Container inven, final CompoundTag tag)
    {
        final ListTag listnbt = tag.getList("Items", 10);
        for (int i = 0; i < listnbt.size(); ++i)
        {
            final CompoundTag compoundnbt = listnbt.getCompound(i);
            final int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < inven.getContainerSize()) inven.setItem(j, ItemStack.of(compoundnbt));
        }
    }

    public static void save(final Container inven, final CompoundTag tag)
    {
        final ListTag listnbt = new ListTag();
        for (int i = 0; i < inven.getContainerSize(); ++i)
        {
            final ItemStack itemstack = inven.getItem(i);
            if (!itemstack.isEmpty())
            {
                final CompoundTag compoundnbt = new CompoundTag();
                compoundnbt.putByte("Slot", (byte) i);
                itemstack.save(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }
        tag.put("Items", listnbt);
    }

}
