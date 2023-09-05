package thut.lib;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemStackTools
{

    public static boolean addItemStackToInventory(final ItemStack itemStackIn, final Container toAddTo,
            final int minIndex, Direction face)
    {
        if (toAddTo instanceof WorldlyContainer container && face != null)
        {
            if (!itemStackIn.isEmpty()) try
            {
                if (itemStackIn.isDamaged())
                {
                    for (int i : container.getSlotsForFace(face))
                    {
                        ItemStack s = container.getItem(i);
                        if (s.isEmpty() && container.canPlaceItemThroughFace(i, itemStackIn, face))
                        {
                            container.setItem(i, itemStackIn.copy());
                            itemStackIn.setCount(0);
                            return true;
                        }
                    }
                    return false;
                }
                int count = itemStackIn.getCount();

                for (int i : container.getSlotsForFace(face))
                {
                    ItemStack s = container.getItem(i);
                    if (container.canPlaceItemThroughFace(i, itemStackIn, face)
                            && (s.isEmpty() || canMergeStacks(s, itemStackIn)))
                    {
                        if (s.isEmpty())
                        {
                            container.setItem(i, itemStackIn.copy());
                            itemStackIn.setCount(0);
                            return true;
                        }

                        int total = s.getCount() + itemStackIn.getCount();
                        if (total <= s.getMaxStackSize())
                        {
                            s.setCount(total);
                            itemStackIn.setCount(0);
                            break;
                        }
                        else
                        {
                            int diff = s.getMaxStackSize() - s.getCount();
                            s.setCount(s.getMaxStackSize());
                            itemStackIn.setCount(itemStackIn.getCount() - diff);
                        }
                    }
                    int num = itemStackIn.getCount();
                    if (num <= 0) break;
                }
                return itemStackIn.getCount() < count;
            }
            catch (final Throwable throwable)
            {
                final CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                crashreport.addCategory("Item being added");
                throw new ReportedException(crashreport);
            }
            return false;
        }
        return ItemStackTools.addItemStackToInventory(itemStackIn, toAddTo, minIndex);
    }

    public static boolean addItemStackToInventory(ItemStack itemStackIn, Container toAddTo, int minIndex)
    {
        return ItemStackTools.addItemStackToInventory(itemStackIn, toAddTo, minIndex, toAddTo.getContainerSize());
    }

    public static boolean addItemStackToInventory(ItemStack itemStackIn, Container toAddTo, int minIndex, int maxIndex)
    {
        return ItemStackTools.addItemStackToInventory(itemStackIn, new InvWrapper(toAddTo), minIndex, maxIndex);
    }

    public static boolean addItemStackToInventory(ItemStack itemStackIn, IItemHandlerModifiable toAddTo, int minIndex)
    {
        return addItemStackToInventory(itemStackIn, toAddTo, minIndex, toAddTo.getSlots());
    }

    /**
     * Adds the item stack to the inventory, returns false if it is impossible.
     */
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IItemHandlerModifiable toAddTo, int minIndex,
            int maxIndex)
    {
        if (!itemStackIn.isEmpty()) try
        {
            if (itemStackIn.isDamaged())
            {
                final int slot = ItemStackTools.getFirstEmptyStack(toAddTo, minIndex, maxIndex);

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
                final int num = ItemStackTools.storePartialItemStack(itemStackIn, toAddTo, minIndex, maxIndex);
                itemStackIn.setCount(num);
                if (num <= 0 || num >= count) break;
            }
            return itemStackIn.getCount() < count;
        }
        catch (final Throwable throwable)
        {
            final CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
            crashreport.addCategory("Item being added");
            throw new ReportedException(crashreport);
        }
        return false;
    }

    private static boolean canMergeStacks(final ItemStack stack1, final ItemStack stack2)
    {
        return !stack1.isEmpty() && ItemStackTools.stackEqualExact(stack1, stack2) && stack1.isStackable()
                && stack1.getCount() < stack1.getMaxStackSize();
    }

    /** Returns the first item stack that is empty. */
    public static int getFirstEmptyStack(IItemHandlerModifiable inventory, int minIndex, int maxIndex)
    {
        maxIndex = Math.min(maxIndex, inventory.getSlots());
        for (int index = minIndex; index < maxIndex; ++index)
            if (inventory.getStackInSlot(index).isEmpty()) return index;
        return -1;
    }

    /** Checks item, NBT, and meta if the item is not damageable */
    private static boolean stackEqualExact(final ItemStack stack1, final ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem() && ItemStack.matches(stack1, stack2);
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(ItemStack itemStackIn, IItemHandlerModifiable inventory, int minIndex,
            int maxIndex)
    {
        maxIndex = Math.min(maxIndex, inventory.getSlots());
        for (int index = minIndex; index < inventory.getSlots(); ++index)
            if (ItemStackTools.canMergeStacks(inventory.getStackInSlot(index), itemStackIn)) return index;
        return -1;
    }

    /**
     * This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over items.
     */
    private static int storePartialItemStack(final ItemStack itemStackIn, final IItemHandlerModifiable inventory,
            final int minIndex, int maxIndex)
    {
        int count = itemStackIn.getCount();
        int index = ItemStackTools.storeItemStack(itemStackIn, inventory, minIndex, maxIndex);

        if (index < minIndex) index = ItemStackTools.getFirstEmptyStack(inventory, minIndex, maxIndex);

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
        if (count > inventory.getStackInSlot(index).getMaxStackSize() - size)
            remainingCount = inventory.getStackInSlot(index).getMaxStackSize() - size;

        if (remainingCount > inventory.getSlotLimit(index) - size)
            remainingCount = inventory.getSlotLimit(index) - size;

        if (remainingCount == 0) return count;
        count = count - remainingCount;
        inventory.getStackInSlot(index).setCount(size + remainingCount);
        inventory.getStackInSlot(index).setPopTime(5);
        return count;
    }
}
