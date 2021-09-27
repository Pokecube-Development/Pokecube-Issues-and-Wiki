package thut.lib;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemStackTools
{
    public static boolean addItemStackToInventory(final ItemStack itemStackIn, final IInventory toAddTo,
            final int minIndex)
    {
        return ItemStackTools.addItemStackToInventory(itemStackIn, new InvWrapper(toAddTo), minIndex);
    }

    /**
     * Adds the item stack to the inventory, returns false if it is
     * impossible.
     */
    public static boolean addItemStackToInventory(final ItemStack itemStackIn, final IItemHandlerModifiable toAddTo,
            final int minIndex)
    {
        if (!itemStackIn.isEmpty()) try
        {
            if (itemStackIn.isDamaged())
            {
                final int slot = ItemStackTools.getFirstEmptyStack(toAddTo, minIndex);

                if (slot >= minIndex)
                {
                    toAddTo.setStackInSlot(slot, itemStackIn.copy());
                    toAddTo.getStackInSlot(slot).setPopTime(5);
                    itemStackIn.setCount(0);
                    return true;
                }
                return false;
            }
            int count;

            while (true)
            {
                count = itemStackIn.getCount();
                final int num = ItemStackTools.storePartialItemStack(itemStackIn, toAddTo, minIndex);
                itemStackIn.setCount(num);
                if (num <= 0 || num >= count) break;
            }
            return itemStackIn.getCount() < count;
        }
        catch (final Throwable throwable)
        {
            final CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            crashreport.addCategory("Item being added");
            // crashreportcategory.addCrashSection("Item ID",
            // Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
            // crashreportcategory.addCrashSection("Item data",
            // Integer.valueOf(itemStackIn.getMetadata()));
            throw new ReportedException(crashreport);
        }
        return false;
    }

    private static boolean canMergeStacks(final ItemStack stack1, final ItemStack stack2)
    {
        return !stack1.isEmpty() && ItemStackTools.stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1
                .getCount() < stack1.getMaxStackSize();
    }

    public static int getFirstEmptyStack(final IInventory inventory, final int minIndex)
    {
        return ItemStackTools.getFirstEmptyStack(new InvWrapper(inventory), minIndex);
    }

    /** Returns the first item stack that is empty. */
    public static int getFirstEmptyStack(final IItemHandlerModifiable inventory, final int minIndex)
    {
        for (int index = minIndex; index < inventory.getSlots(); ++index)
            if (inventory.getStackInSlot(index).isEmpty()) return index;
        return -1;
    }

    /** Checks item, NBT, and meta if the item is not damageable */
    private static boolean stackEqualExact(final ItemStack stack1, final ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem() && ItemStack.tagMatches(stack1, stack2);
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(final ItemStack itemStackIn, final IItemHandlerModifiable inventory,
            final int minIndex)
    {
        for (int index = minIndex; index < inventory.getSlots(); ++index)
            if (ItemStackTools.canMergeStacks(inventory.getStackInSlot(index), itemStackIn)) return index;
        return -1;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over items.
     */
    private static int storePartialItemStack(final ItemStack itemStackIn, final IItemHandlerModifiable inventory,
            final int minIndex)
    {
        int count = itemStackIn.getCount();
        int index = ItemStackTools.storeItemStack(itemStackIn, inventory, minIndex);

        if (index < minIndex) index = ItemStackTools.getFirstEmptyStack(inventory, minIndex);

        if (index < minIndex) return count;
        ItemStack itemstack = inventory.getStackInSlot(index);

        if (itemstack.isEmpty())
        {
            itemstack = itemStackIn.copy();
            itemstack.setCount(0);
            if (itemStackIn.hasTag()) itemstack.setTag(itemStackIn.getTag().copy());
            inventory.setStackInSlot(index, itemstack);
        }

        int remainingCount = count;
        final int size = inventory.getStackInSlot(index).getCount();
        if (count > inventory.getStackInSlot(index).getMaxStackSize() - size) remainingCount = inventory.getStackInSlot(
                index).getMaxStackSize() - size;

        if (remainingCount > inventory.getSlotLimit(index) - size) remainingCount = inventory.getSlotLimit(index)
                - size;

        if (remainingCount == 0) return count;
        count = count - remainingCount;
        inventory.getStackInSlot(index).setCount(size + remainingCount);
        inventory.getStackInSlot(index).setPopTime(5);
        return count;
    }
}
