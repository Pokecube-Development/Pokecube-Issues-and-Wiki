package thut.bling.bag.large;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import thut.api.inventory.BaseContainer;
import thut.api.item.ItemList;
import thut.bling.ThutBling;
import thut.bling.network.PacketBag;
import thut.core.common.ThutCore;

public class LargeContainer extends BaseContainer
{
    public static final ResourceLocation INVALID = new ResourceLocation(ThutBling.MODID, "not_bagable");

    public static final MenuType<LargeContainer> TYPE = new MenuType<>(
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

    public final Inventory invPlayer;

    public LargeContainer(final int id, final Inventory ivplay, final FriendlyByteBuf data)
    {
        this(id, ivplay, new LargeInventory(LargeManager.INSTANCE, data));
    }

    public LargeContainer(final int id, final Inventory ivplay, final LargeInventory pc)
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
    public boolean stillValid(final Player PlayerEntity)
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
    public Container getInv()
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
    public void removed(final Player player)
    {
        super.removed(player);
        this.inv.stopOpen(player);
    }

    public void updateInventoryPages(final int dir, final Inventory invent)
    {
        int page = this.inv.getPage() == 0 && dir == -1 ? this.inv.boxCount() - 1
                : (this.inv.getPage() + dir) % this.inv.boxCount();
        page += 1;
        this.gotoInventoryPage(page);
    }
}
