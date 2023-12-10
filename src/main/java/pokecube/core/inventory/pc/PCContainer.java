package pokecube.core.inventory.pc;

import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeCore;
import pokecube.core.init.MenuTypes;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.packets.PacketPC;
import thut.api.inventory.BaseContainer;
import thut.core.common.ThutCore;
import thut.wearables.ThutWearables;

public class PCContainer extends BaseContainer
{
    public static Set<Predicate<ItemStack>> CUSTOMPCWHILTELIST = Sets.newHashSet();

    public static int STACKLIMIT = 64;
    public static int yOffset;
    public static int xOffset;

    /**
     * Returns true if the item is a filled pokecube.
     *
     * @param itemstack the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise
     */
    public static boolean isItemValid(final ItemStack itemstack)
    {
        if (itemstack.isEmpty()) return false;

        final boolean eggorCube = !PokecubeCore.getConfig().pcHoldsOnlyPokecubes || PokecubeManager.isFilled(itemstack)
                || itemstack.getItem() instanceof WritableBookItem || itemstack.getItem() instanceof ItemPokemobEgg
                || itemstack.getItem() instanceof ItemPokedex || ThutWearables.getWearable(itemstack) != null;
        if (!eggorCube) for (final Predicate<ItemStack> tester : PCContainer.CUSTOMPCWHILTELIST)
            if (tester.test(itemstack)) return true;
        return eggorCube;
    }

    public final PCInventory inv;

    public final Inventory invPlayer;
    public final BlockPos pcPos;
    public boolean release = false;
    // private GuiPC gpc;

    public boolean[] toRelease = new boolean[54];

    public PCContainer(final int id, final Inventory ivplay, final FriendlyByteBuf buffer)
    {
        this(id, ivplay, new PCInventory(PCManager.INSTANCE, buffer));
    }

    public PCContainer(final int id, final Inventory ivplay, final PCInventory pc)
    {
        this(id, ivplay, pc, null);
    }

    public PCContainer(final int id, final Inventory ivplay, final PCInventory pc, final BlockPos pcPos)
    {
        super(MenuTypes.PC.get(), id);
        PCContainer.xOffset = 0;
        PCContainer.yOffset = 0;
        this.inv = pc;
        this.invPlayer = ivplay;
        this.bindInventories();
        this.pcPos = pcPos;
    }

    protected void bindInventories()
    {
        this.bindPCInventory();
        this.bindPlayerInventory(this.invPlayer, 45);
    }

    protected void bindPCInventory()
    {
        boolean boundSlots = !this.slots.isEmpty();
        final int n = this.inv.getPage() * 54;
        for (int i = 0; i < 6; i++) for (int j = 0; j < 9; j++)
        {
            int slotIndex = j + i * 9;
            int bagIndex = n + slotIndex;
            if (!boundSlots) this.addSlot(new PCSlot(this.inv, bagIndex, 8 + j * 18 + PCContainer.xOffset,
                    18 + i * 18 + PCContainer.yOffset));
            else if (this.slots.get(slotIndex) instanceof PCSlot slot) slot.setSlotIndex(bagIndex);
        }
        for (final Slot s : this.slots) s.setChanged();
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
            final PacketPC packet = new PacketPC(PacketPC.RENAME, this.inv.getOwner());
            packet.data.putString("N", name);
            PokecubeCore.packets.sendToServer(packet);
        }
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

    public boolean getRelease()
    {
        return this.release;
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
            final PacketPC packet = new PacketPC(PacketPC.SETPAGE, this.inv.getOwner());
            packet.data.putInt("P", page);
            PokecubeCore.packets.sendToServer(packet);
        }
        this.bindInventories();
    }

    @Override
    public void removed(final Player player)
    {
        super.removed(player);
        this.inv.stopOpen(player);
    }

    public void setRelease(final boolean bool, final UUID id)
    {
        if (this.release && !bool) if (ThutCore.proxy.isClientSide())
        {
            final PacketPC packet = new PacketPC(PacketPC.RELEASE, id);
            packet.data.putBoolean("T", false);
            packet.data.putInt("page", this.inv.getPage());
            for (int i = 0; i < 54; i++) if (this.toRelease[i]) packet.data.putBoolean("val" + i, true);
            PokecubeCore.packets.sendToServer(packet);
        }
        this.release = bool;
    }

    @Override
    public void clicked(final int slotId, final int dragType, final ClickType clickTypeIn, final Player player)
    {
        if (this.release)
        {
            if (slotId < 54 && slotId >= 0)
            {
                this.toRelease[slotId] = !this.toRelease[slotId];
            }
            return;
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    public void toggleAuto()
    {
        this.inv.setAutoToPC(!this.inv.isAutoToPC());
        if (ThutCore.proxy.isClientSide())
        {
            final PacketPC packet = new PacketPC(PacketPC.TOGGLEAUTO, this.inv.getOwner());
            packet.data.putBoolean("A", this.inv.isAutoToPC());
            PokecubeCore.packets.sendToServer(packet);
        }
    }

    public void updateInventoryPages(final int dir, final Inventory invent)
    {
        int page = this.inv.getPage() == 0 && dir == -1 ? this.inv.boxCount() - 1
                : (this.inv.getPage() + dir) % this.inv.boxCount();
        page += 1;
        this.gotoInventoryPage(page);
    }
}
