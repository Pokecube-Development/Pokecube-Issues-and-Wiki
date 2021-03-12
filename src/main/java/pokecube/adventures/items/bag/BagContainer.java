package pokecube.adventures.items.bag;

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.network.PacketBag;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

public class BagContainer extends BaseContainer
{
    public static final ResourceLocation VALID = new ResourceLocation(PokecubeAdv.MODID, "bagable");

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
        // No placing bags in self.
        if (itemstack.getItem() == PokecubeAdv.BAG.get()) return false;
        // Config option to hold anything.
        if (PokecubeAdv.config.bagsHoldEverything) return true;
        // Specifically ban filled cubes
        if (!PokecubeAdv.config.bagsHoldFilledCubes && PokecubeManager.isFilled(itemstack)) return false;
        // Otherwise check the tag
        return ItemList.is(BagContainer.VALID, itemstack);
    }

    public final BagInventory inv;

    public final PlayerInventory invPlayer;

    public BagContainer(final int id, final PlayerInventory ivplay, final PacketBuffer data)
    {
        this(id, ivplay, new BagInventory(BagManager.INSTANCE, data));
    }

    public BagContainer(final int id, final PlayerInventory ivplay, final BagInventory pc)
    {
        super(PokecubeAdv.BAG_CONT.get(), id);
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
        final int n = this.inv.getPage() * 54;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new BagSlot(this.inv, n + j + i * 9, 8 + j * 18 + BagContainer.xOffset, 18 + i * 18
                        + BagContainer.yOffset));
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
            PokecubeAdv.packets.sendToServer(packet);
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
            PokecubeAdv.packets.sendToServer(packet);
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
