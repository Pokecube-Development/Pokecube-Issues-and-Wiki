package pokecube.core.inventory.pc;

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
import net.minecraft.item.WritableBookItem;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.BaseContainer;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.packets.PacketPC;
import thut.core.common.ThutCore;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

// TODO inventory tweaks
// @ChestContainer(isLargeChest = true, showButtons = false)
public class PCContainer extends BaseContainer
{
    public static final ContainerType<PCContainer> TYPE = new ContainerType<>(PCContainer::new);

    public static Set<Predicate<ItemStack>> CUSTOMPCWHILTELIST = Sets.newHashSet();

    public static int STACKLIMIT = 64;
    public static int yOffset;
    public static int xOffset;

    /**
     * Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise
     */
    public static boolean isItemValid(final ItemStack itemstack)
    {
        if (itemstack.isEmpty()) return false;
        final LazyOptional<IMegaCapability> mega = itemstack.getCapability(MegaCapability.MEGA_CAP);
        final LazyOptional<IActiveWearable> worn = itemstack.getCapability(ThutWearables.WEARABLE_CAP);

        final boolean eggorCube = !PokecubeCore.getConfig().pcHoldsOnlyPokecubes || PokecubeManager.isFilled(itemstack)
                || itemstack.getItem() instanceof WritableBookItem || itemstack.getItem() instanceof ItemPokemobEgg
                || itemstack.getItem() instanceof ItemPokedex || mega.isPresent() && mega.orElse(null).getEntry(
                        itemstack) != null || worn.isPresent();
        if (!eggorCube) for (final Predicate<ItemStack> tester : PCContainer.CUSTOMPCWHILTELIST)
            if (tester.test(itemstack)) return true;
        return eggorCube;
    }

    public final PCInventory inv;

    public final PlayerInventory invPlayer;
    public final BlockPos        pcPos;
    public boolean               release = false;
    // private GuiPC gpc;

    public boolean[] toRelease = new boolean[54];

    public PCContainer(final int id, final PlayerInventory ivplay)
    {
        this(id, ivplay, PCInventory.getPC(ivplay.player));
    }

    public PCContainer(final int id, final PlayerInventory ivplay, final PCInventory pc)
    {
        this(id, ivplay, pc, null);
    }

    public PCContainer(final int id, final PlayerInventory ivplay, final PCInventory pc, final BlockPos pcPos)
    {
        super(PCContainer.TYPE, id);
        PCContainer.xOffset = 0;
        PCContainer.yOffset = 0;
        this.inv = pc;
        this.invPlayer = ivplay;
        this.bindInventories();
        this.pcPos = pcPos;
    }

    protected void bindInventories()
    {
        this.clearSlots();
        this.bindPCInventory();
        this.bindPlayerInventory(this.invPlayer, 45);
    }

    protected void bindPCInventory()
    {
        int n = 0;
        n = this.inv.getPage() * 54;
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 9; j++)
                this.addSlot(new PCSlot(this.inv, n + j + i * 9, 8 + j * 18 + PCContainer.xOffset, 18 + i * 18
                        + PCContainer.yOffset));
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
            final PacketPC packet = new PacketPC(PacketPC.RENAME, this.inv.owner);
            packet.data.putString("N", name);
            PokecubeCore.packets.sendToServer(packet);
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

    public boolean getRelease()
    {
        return this.release;
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
            final PacketPC packet = new PacketPC(PacketPC.SETPAGE, this.inv.owner);
            packet.data.putInt("P", page);
            PokecubeCore.packets.sendToServer(packet);
        }
        this.bindInventories();
    }

    @Override
    public void onContainerClosed(final PlayerEntity player)
    {
        super.onContainerClosed(player);
        this.inv.closeInventory(player);
    }

    public void setRelease(final boolean bool)
    {
        if (this.release && !bool) if (ThutCore.proxy.isClientSide())
        {
            final PacketPC packet = new PacketPC(PacketPC.RELEASE, this.inv.owner);
            packet.data.putBoolean("T", false);
            packet.data.putInt("page", this.inv.getPage());
            for (int i = 0; i < 54; i++)
                if (this.toRelease[i]) packet.data.putBoolean("val" + i, true);
            PokecubeCore.packets.sendToServer(packet);
        }
        this.release = bool;
    }

    @Override
    public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickTypeIn,
            final PlayerEntity player)
    {
        if (this.release)
        {
            if (slotId < 54 && slotId >= 0) this.toRelease[slotId] = !this.toRelease[slotId];
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    public void toggleAuto()
    {
        this.inv.autoToPC = !this.inv.autoToPC;
        if (ThutCore.proxy.isClientSide())
        {
            final PacketPC packet = new PacketPC(PacketPC.TOGGLEAUTO, this.inv.owner);
            packet.data.putBoolean("A", this.inv.autoToPC);
            PokecubeCore.packets.sendToServer(packet);
        }
    }

    @Override
    public ItemStack transferStackInSlot(final PlayerEntity player, final int index)
    {
        return super.transferStackInSlot(player, index);
    }

    public void updateInventoryPages(final int dir, final PlayerInventory invent)
    {
        int page = this.inv.getPage() == 0 && dir == -1 ? PCInventory.PAGECOUNT - 1
                : (this.inv.getPage() + dir) % PCInventory.PAGECOUNT;
        page += 1;
        this.gotoInventoryPage(page);
    }
}
