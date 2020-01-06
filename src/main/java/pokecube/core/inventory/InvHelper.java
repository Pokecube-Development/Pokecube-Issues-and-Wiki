package pokecube.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class InvHelper
{
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
