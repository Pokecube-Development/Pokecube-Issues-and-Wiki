package pokecube.adventures.network;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagInventory;
import pokecube.core.inventory.pc.PCContainer;
import thut.core.common.network.Packet;

public class PacketBag extends Packet
{
    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte INIT    = 2;
    public static final byte RELEASE = 3;
    public static final byte OPEN    = 4;

    public static final String OWNER = "_owner_";

    public static void sendInitialSyncMessage(final PlayerEntity sendTo)
    {
        final BagInventory inv = BagInventory.getPC(sendTo.getUniqueID());
        final PacketBag packet = new PacketBag(PacketBag.INIT, sendTo.getUniqueID());
        packet.data.putInt("N", inv.boxes.length);
        packet.data.putInt("C", inv.getPage());
        for (int i = 0; i < inv.boxes.length; i++)
            packet.data.putString("N" + i, inv.boxes[i]);
        PokecubeAdv.packets.sendTo(packet, (ServerPlayerEntity) sendTo);
    }

    public static void sendOpenPacket(final PlayerEntity sendTo, final UUID owner)
    {
        final BagInventory inv = BagInventory.getPC(owner);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            final PacketBag packet = new PacketBag(PacketBag.OPEN, owner);
            packet.data = inv.serializeBox(i);
            packet.data.putUniqueId(PacketBag.OWNER, owner);
            PokecubeAdv.packets.sendTo(packet, (ServerPlayerEntity) sendTo);
        }
        sendTo.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new BagContainer(id,
                playerInventory, BagInventory.getPC(playerIn)), sendTo.getDisplayName()));
    }

    byte               message;
    public CompoundNBT data = new CompoundNBT();

    public PacketBag()
    {
    }

    public PacketBag(final byte message)
    {
        this.message = message;
    }

    public PacketBag(final byte message, final UUID owner)
    {
        this(message);
        this.data.putUniqueId(PacketBag.OWNER, owner);
    }

    public PacketBag(final PacketBuffer buf)
    {
        this.message = buf.readByte();
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        BagInventory pc;
        switch (this.message)
        {
        case OPEN:
            pc = BagInventory.getPC(this.data.getUniqueId(PacketBag.OWNER));
            pc.deserializeBox(this.data);
            break;
        default:
            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {

        PCContainer container = null;
        if (player.openContainer instanceof PCContainer) container = (PCContainer) player.openContainer;
        BagInventory pc;
        switch (this.message)
        {
        case SETPAGE:
            if (container != null) container.gotoInventoryPage(this.data.getInt("P"));
            break;
        case RENAME:
            if (container != null)
            {
                final String name = this.data.getString("N");
                container.changeName(name);
            }
            break;
        case INIT:
            BagInventory.blank = new BagInventory(BagInventory.defaultId);
            pc = BagInventory.getPC(this.data.getUniqueId(PacketBag.OWNER));
            if (this.data.contains("C")) pc.setPage(this.data.getInt("C"));
            if (this.data.contains("N"))
            {
                final int num = this.data.getInt("N");
                pc.boxes = new String[num];
                for (int i = 0; i < pc.boxes.length; i++)
                    pc.boxes[i] = this.data.getString("N" + i);
            }
            break;
        case RELEASE:
            final boolean toggle = this.data.getBoolean("T");
            if (toggle) container.setRelease(this.data.getBoolean("R"));
            else
            {
                final int page = this.data.getInt("page");
                pc = BagInventory.getPC(this.data.getUniqueId(PacketBag.OWNER));
                for (int i = 0; i < 54; i++)
                    if (this.data.getBoolean("val" + i))
                    {
                        final int j = i + page * 54;
                        pc.setInventorySlotContents(j, ItemStack.EMPTY);
                    }
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeByte(this.message);
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(this.data);
    }

}
