package thut.bling.bag.small;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import thut.api.item.ItemList;
import thut.bling.ThutBling;

public class SmallContainer extends ChestMenu
{
    public static final ResourceLocation INVALID = new ResourceLocation(ThutBling.MODID, "not_bagable");

    public static int STACKLIMIT = 64;
    public static int yOffset;
    public static int xOffset;

    /**
     * Returns true if the item is valid for the bag
     *
     * @param itemstack the itemstack to test
     * @return true if the item is valid
     */
    public static boolean isItemValid(final ItemStack itemstack)
    {
        if (ItemList.is(SmallContainer.INVALID, itemstack)) return false;
        return true;
    }

    public final SmallInventory inv;

    public final Inventory invPlayer;

    public SmallContainer(final int id, final Inventory ivplay, final FriendlyByteBuf data)
    {
        this(id, ivplay, new SmallInventory(SmallManager.INSTANCE, data));
    }

    public SmallContainer(final int id, final Inventory ivplay, final SmallInventory pc)
    {
        super(ThutBling.SMALL_BAG.get(), id, ivplay, pc, 3);
        SmallContainer.xOffset = 0;
        SmallContainer.yOffset = 0;
        this.inv = pc;
        this.invPlayer = ivplay;

        // Replace the default slots.
        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                int i = k + j * 9;
                var replacement = new BagSlot(this.inv, i, 8 + k * 18, 18 + j * 18);
                replacement.index = i;
                this.slots.set(i, replacement);
            }
        }
    }

    @Override
    public boolean stillValid(final Player PlayerEntity)
    {
        return true;
    }
}
