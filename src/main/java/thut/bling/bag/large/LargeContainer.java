package thut.bling.bag.large;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.IContainerFactory;
import thut.api.inventory.BaseContainer;
import thut.api.item.ItemList;
import thut.bling.ThutBling;
import thut.bling.network.PacketBag;
import thut.core.common.ThutCore;

public class LargeContainer extends BaseContainer
{
    public static final ResourceLocation INVALID = new ResourceLocation(ThutBling.MODID, "not_bagable");

    public static final ContainerType<LargeContainer> TYPE = new ContainerType<>(
            (IContainerFactory<LargeContainer>) LargeContainer::new);

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
        if (ItemList.is(LargeContainer.INVALID, itemstack)) return false;
        return true;
    }

    public final LargeInventory inv;

    public final PlayerInventory invPlayer;

    public LargeContainer(final int id, final PlayerInventory ivplay, final PacketBuffer data)
    {
        this(id, ivplay, new LargeInventory(LargeManager.INSTANCE, data));
    }

    public LargeContainer(final int id, final PlayerInventory ivplay, final LargeInventory pc)
    {
        super(LargeContainer.TYPE, id);
        LargeContainer.xOffset = 0;
        LargeContainer.yOffset = 0;
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
        final int n = this.inv.getPage() * 54;
        for (int i = 0; i < 6; i++)
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

    public void changeName(final String name)
    {
        this.inv.boxes[this.inv.getPage()] = name;
        if (ThutCore.proxy.isClientSide())
        {
            final PacketBag packet = new PacketBag(PacketBag.RENAME, this.inv.getOwner());
            packet.data.putString("N", name);
            ThutBling.packets.sendToServer(packet);
        }
    }

    protected void clearSlots()
    {
        this.slots.clear();
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
        return this.slots.get(par1);
    }

    public void gotoInventoryPage(final int page)
    {
        if (page - 1 == this.inv.getPage()) return;
        this.inv.setPage(page - 1);
        if (ThutCore.proxy.isClientSide())
        {
            final PacketBag packet = new PacketBag(PacketBag.SETPAGE, this.inv.getOwner());
            packet.data.putInt("P", page);
            ThutBling.packets.sendToServer(packet);
        }
        this.bindInventories();
    }

    @Override
    public void removed(final PlayerEntity player)
    {
        super.removed(player);
        this.inv.stopOpen(player);
    }

    @Override
    public ItemStack clicked(final int slotId, final int dragType, final ClickType clickTypeIn,
            final PlayerEntity player)
    {
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void updateInventoryPages(final int dir, final PlayerInventory invent)
    {
        int page = this.inv.getPage() == 0 && dir == -1 ? this.inv.boxCount() - 1
                : (this.inv.getPage() + dir) % this.inv.boxCount();
        page += 1;
        this.gotoInventoryPage(page);
    }
}
