package thut.bling.bag.small;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.IContainerFactory;
import thut.api.item.ItemList;
import thut.bling.ThutBling;
import thut.bling.bag.large.BagSlot;
import thut.bling.bag.large.LargeContainer;

public class SmallContainer extends ChestContainer
{
    public static final ResourceLocation INVALID = new ResourceLocation(ThutBling.MODID, "not_bagable");

    public static final ContainerType<SmallContainer> TYPE = new ContainerType<>(
            (IContainerFactory<SmallContainer>) SmallContainer::new);

    public static int STACKLIMIT = 64;
    public static int yOffset;
    public static int xOffset;

    /**
     * Returns true if the item is valid for the bag
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the item is valid
     */
    public static boolean isItemValid(final ItemStack itemstack)
    {
        if (ItemList.is(SmallContainer.INVALID, itemstack)) return false;
        return true;
    }

    public final SmallInventory inv;

    public final PlayerInventory invPlayer;

    public SmallContainer(final int id, final PlayerInventory ivplay, final PacketBuffer data)
    {
        this(id, ivplay, new SmallInventory(SmallManager.INSTANCE, data));
    }

    public SmallContainer(final int id, final PlayerInventory ivplay, final SmallInventory pc)
    {
        super(SmallContainer.TYPE, id, ivplay, pc, 3);
        SmallContainer.xOffset = 0;
        SmallContainer.yOffset = 0;
        this.inv = pc;
        this.invPlayer = ivplay;
        this.bindInventories();
    }

    protected void clearSlots()
    {
        this.slots.clear();
    }

    protected void bindInventories()
    {
        this.clearSlots();
        this.bindBagInventory();
        this.bindPlayerInventory(this.invPlayer, -18);
    }

    public void bindPlayerInventory(final PlayerInventory playerInv, final int yOffset)
    {
        for (int i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(playerInv, i1, 8 + i1 * 18, 161 + yOffset));

        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                this.addSlot(new Slot(playerInv, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + yOffset));
    }

    protected void bindBagInventory()
    {
        final int n = 27;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new BagSlot(this.inv, n + j + i * 9, 8 + j * 18 + LargeContainer.xOffset, 18 + i * 18
                        + LargeContainer.yOffset));
        // int k = 0;
        for (final Object o : this.slots)
            if (o instanceof Slot) ((Slot) o).setChanged();
    }

    @Override
    public boolean stillValid(final PlayerEntity PlayerEntity)
    {
        return true;
    }
}
