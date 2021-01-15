package pokecube.core.inventory;

import java.util.function.Predicate;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import thut.api.item.ItemList;

public class InvHelper
{
    public static class ItemCap extends ItemStackHandler implements ICapabilitySerializable<CompoundNBT>
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

    public static void load(final IInventory inven, final CompoundNBT tag)
    {
        final ListNBT listnbt = tag.getList("Items", 10);
        for (int i = 0; i < listnbt.size(); ++i)
        {
            final CompoundNBT compoundnbt = listnbt.getCompound(i);
            final int j = compoundnbt.getByte("Slot") & 255;
            if (j >= 0 && j < inven.getSizeInventory()) inven.setInventorySlotContents(j, ItemStack.read(compoundnbt));
        }
    }

    public static void save(final IInventory inven, final CompoundNBT tag)
    {
        final ListNBT listnbt = new ListNBT();
        for (int i = 0; i < inven.getSizeInventory(); ++i)
        {
            final ItemStack itemstack = inven.getStackInSlot(i);
            if (!itemstack.isEmpty())
            {
                final CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putByte("Slot", (byte) i);
                itemstack.write(compoundnbt);
                listnbt.add(compoundnbt);
            }
        }
        tag.put("Items", listnbt);
    }

}
