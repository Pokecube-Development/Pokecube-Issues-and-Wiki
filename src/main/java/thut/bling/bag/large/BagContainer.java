package thut.bling.bag.large;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.inventory.BaseContainer;
import thut.bling.network.PacketBag;
import thut.core.common.ThutCore;
import thut.wearables.ThutWearables;

public class BagContainer extends BaseContainer
{
    public static final ContainerType<BagContainer> TYPE = new ContainerType<>(BagContainer::new);

    public static Set<Predicate<ItemStack>> CUSTOMPCWHILTELIST = Sets.newHashSet();

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
        if (itemstack.isEmpty()) return false;
        // TODO check tags on the item to see if it is valid
        return true;
    }

    public final BagInventory inv;

    public final PlayerInventory invPlayer;

    public BagContainer(final int id, final PlayerInventory ivplay)
    {
        this(id, ivplay, BagInventory.getPC(ivplay.player));
    }

    public BagContainer(final int id, final PlayerInventory ivplay, final BagInventory pc)
    {
        super(BagContainer.TYPE, id);
        BagContainer.xOffset = 0;
        BagContainer.yOffset = 0;
        this.inv = pc;
        this.invPlayer = ivplay;
        this.bindInventories();
    }

    protected void bindInventories()
    {
        this.clearSlots();
        this.bindBagInventory();
        this.bindPlayerInventory(this.invPlayer, 45);
    }

    protected void bindBagInventory()
    {
        int n = 0;
        n = this.inv.getPage() * 54;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new BagSlot(this.inv, n + j + i * 9, 8 + j * 18 + BagContainer.xOffset, 18 + i * 18
                        + BagContainer.yOffset));
        // int k = 0;
        for (final Object o : this.inventorySlots)
            if (o instanceof Slot) ((Slot) o).onSlotChanged();
    }

    @Override
    public boolean canInteractWith(final PlayerEntity PlayerEntity)
    {
        return true;
    }

    public void changeName(final String name)
    {
        this.inv.boxes[this.inv.getPage()] = name;
        if (ThutCore.proxy.isClientSide())
        {
            final PacketBag packet = new PacketBag(PacketBag.RENAME, this.inv.owner);
            packet.data.putString("N", name);
            ThutWearables.packets.sendToServer(packet);
        }
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 54;
    }

    @OnlyIn(Dist.CLIENT)
    public String getPage()
    {
        return this.inv.boxes[this.inv.getPage()];
    }

    @OnlyIn(Dist.CLIENT)
    public String getPageNb()
    {
        return Integer.toString(this.inv.getPage() + 1);
    }

    @Override
    public Slot getSlot(final int par1)
    {
        return this.inventorySlots.get(par1);
    }

    public void gotoInventoryPage(final int page)
    {
        if (page - 1 == this.inv.getPage()) return;
        this.inv.setPage(page - 1);
        if (ThutCore.proxy.isClientSide())
        {
            final PacketBag packet = new PacketBag(PacketBag.SETPAGE, this.inv.owner);
            packet.data.putInt("P", page);
            ThutWearables.packets.sendToServer(packet);
        }
        this.bindInventories();
    }

    @Override
    public void onContainerClosed(final PlayerEntity player)
    {
        super.onContainerClosed(player);
        this.inv.closeInventory(player);
    }

    @Override
    public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn,
            final PlayerEntity player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    public void updateInventoryPages(final int dir, final PlayerInventory invent)
    {
        int page = this.inv.getPage() == 0 && dir == -1 ? BagInventory.PAGECOUNT - 1
                : (this.inv.getPage() + dir) % BagInventory.PAGECOUNT;
        page += 1;
        this.gotoInventoryPage(page);
    }
}
