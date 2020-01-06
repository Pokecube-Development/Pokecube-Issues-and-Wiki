package thut.lib;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemStackTools
{
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IInventory toAddTo, int minIndex)
    {
        return ItemStackTools.addItemStackToInventory(itemStackIn, new InvWrapper(toAddTo), minIndex);
    }

    /**
     * Adds the item stack to the inventory, returns false if it is
     * impossible.
     */
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IItemHandlerModifiable toAddTo, int minIndex)
    {
        if (!itemStackIn.isEmpty()) try
        {
            if (itemStackIn.isDamaged())
            {
                final int j = ItemStackTools.getFirstEmptyStack(toAddTo, minIndex);

                if (j >= 0)
                {
                    toAddTo.setStackInSlot(j, itemStackIn.copy());
                    toAddTo.getStackInSlot(j).setAnimationsToGo(5);
                    itemStackIn.setCount(0);
                    return true;
                }
                return false;
            }
            int i;

            while (true)
            {
                i = itemStackIn.getCount();
                final int num = ItemStackTools.storePartialItemStack(itemStackIn, toAddTo, minIndex);
                itemStackIn.setCount(num);
                if (num <= 0 || num >= i) break;
            }
            return itemStackIn.getCount() < i;
        }
        catch (final Throwable throwable)
        {
            final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
            crashreport.makeCategory("Item being added");
            // crashreportcategory.addCrashSection("Item ID",
            // Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
            // crashreportcategory.addCrashSection("Item data",
            // Integer.valueOf(itemStackIn.getMetadata()));
            throw new ReportedException(crashreport);
        }
        return false;
    }

    private static boolean canMergeStacks(ItemStack stack1, ItemStack stack2)
    {
        return !stack1.isEmpty() && ItemStackTools.stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1
                .getCount() < stack1.getMaxStackSize();
    }

    public static int getFirstEmptyStack(IInventory inventory, int minIndex)
    {
        return ItemStackTools.getFirstEmptyStack(new InvWrapper(inventory), minIndex);
    }

    /** Returns the first item stack that is empty. */
    public static int getFirstEmptyStack(IItemHandlerModifiable inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSlots(); ++i)
            if (inventory.getStackInSlot(i).isEmpty()) return i;
        return -1;
    }

    /** Checks item, NBT, and meta if the item is not damageable */
    private static boolean stackEqualExact(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem() && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(ItemStack itemStackIn, IItemHandlerModifiable inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSlots(); ++i)
            if (ItemStackTools.canMergeStacks(inventory.getStackInSlot(i), itemStackIn)) return i;
        return -1;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over items.
     */
    private static int storePartialItemStack(ItemStack itemStackIn, IItemHandlerModifiable inventory, int minIndex)
    {
        int i = itemStackIn.getCount();
        int j = ItemStackTools.storeItemStack(itemStackIn, inventory, minIndex);

        if (j < 0) j = ItemStackTools.getFirstEmptyStack(inventory, minIndex);

        if (j < 0) return i;
        ItemStack itemstack = inventory.getStackInSlot(j);

        if (itemstack.isEmpty())
        {
            itemstack = itemStackIn.copy();
            itemstack.setCount(0);
            if (itemStackIn.hasTag()) itemstack.setTag(itemStackIn.getTag().copy());
            inventory.setStackInSlot(j, itemstack);
        }

        int k = i;
        final int size = inventory.getStackInSlot(j).getCount();
        if (i > inventory.getStackInSlot(j).getMaxStackSize() - size) k = inventory.getStackInSlot(j).getMaxStackSize()
                - size;

        if (k > inventory.getSlotLimit(j) - size) k = inventory.getSlotLimit(j) - size;

        if (k == 0) return i;
        i = i - k;
        inventory.getStackInSlot(j).setCount(size + k);
        inventory.getStackInSlot(j).setAnimationsToGo(5);
        return i;
    }
}
