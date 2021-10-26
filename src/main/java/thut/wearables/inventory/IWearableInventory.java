package thut.wearables.inventory;

import java.util.Set;

import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;

public interface IWearableInventory
{
    /**
     * Return the wearable of the given type, returns first not-null one for
     * types with multiple slots.
     *
     * @param type
     * @return
     */
    ItemStack getWearable(EnumWearable type);

    /**
     * Returns the wearable of the given type in the given slot.
     *
     * @param type
     * @param slot
     * @return
     */
    ItemStack getWearable(EnumWearable type, int slot);

    /**
     * Returns all wearables worn.
     *
     * @return
     */
    Set<ItemStack> getWearables();

    /**
     * tries to set the stack as a wearable for that type, returns false if it
     * doesn't fit, or returns false if stack is null, and there is nothing to
     * remove.
     *
     * @param type
     * @param stack
     * @return
     */
    boolean setWearable(EnumWearable type, ItemStack stack);

    /**
     * tries to set the given itemstack as worn item for the given type and
     * slot. Returns false if the stack doesn't fit, or returns false if there
     * is nothing to remove.
     *
     * @param type
     * @param stack
     * @param slot
     * @return returns if the item went into the slot
     */
    boolean setWearable(EnumWearable type, ItemStack stack, int slot);

}
