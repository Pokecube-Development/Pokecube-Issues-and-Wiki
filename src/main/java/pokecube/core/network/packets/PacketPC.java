package pokecube.core.network.packets;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeCore;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCInventory;
import thut.core.common.network.Packet;

public class PacketPC extends Packet
{
    public static final byte SETPAGE    = 0;
    public static final byte RENAME     = 1;
    public static final byte PCINIT     = 2;
    public static final byte RELEASE    = 3;
    public static final byte TOGGLEAUTO = 4;
    public static final byte BIND       = 5;
    public static final byte PCOPEN     = 6;

    public static final String OWNER = "_owner_";

    public static void sendInitialSyncMessage(final PlayerEntity sendTo)
    {
        final PCInventory inv = PCInventory.getPC(sendTo.getUniqueID());
        final PacketPC packet = new PacketPC(PacketPC.PCINIT, sendTo.getUniqueID());
        packet.data.putInt("N", inv.boxes.length);
        packet.data.putBoolean("A", inv.autoToPC);
        packet.data.putBoolean("O", inv.seenOwner);
        packet.data.putInt("C", inv.getPage());
        for (int i = 0; i < inv.boxes.length; i++)
            packet.data.putString("N" + i, inv.boxes[i]);
        PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) sendTo);
    }

    public static void sendOpenPacket(final PlayerEntity sendTo, final UUID owner, final BlockPos pcPos)
    {
        final PCInventory inv = PCInventory.getPC(owner);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            final PacketPC packet = new PacketPC(PacketPC.PCOPEN, owner);
            packet.data = inv.serializeBox(i);
            packet.data.putUniqueId(PacketPC.OWNER, owner);
            PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) sendTo);
        }
        sendTo.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new PCContainer(id,
                playerInventory, PCInventory.getPC(playerIn)), sendTo.getDisplayName()));
    }

    byte               message;
    public CompoundNBT data = new CompoundNBT();

    public PacketPC()
    {
    }

    public PacketPC(final byte message)
    {
        this.message = message;
    }

    public PacketPC(final byte message, final UUID owner)
    {
        this(message);
        this.data.putUniqueId(PacketPC.OWNER, owner);
    }

    public PacketPC(final PacketBuffer buf)
    {
        this.message = buf.readByte();
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        PCInventory pc;
        switch (this.message)
        {
        case BIND:
        case PCOPEN:
            pc = PCInventory.getPC(this.data.getUniqueId(PacketPC.OWNER));
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
        PCInventory pc;
        switch (this.message)
        {
        case BIND:
            if (container != null && container.pcPos != null)
            {
                // boolean owned = this.data.getBoolean("O");
                // TODO handle bind packet.
                // if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Bind PC
                // Packet: " + owned + " " + player);
                // if (owned)
                // {
                // container.pcTile.toggleBound();
                // }
                // else
                // {
                // container.pcTile.setBoundOwner(player);
                // }
            }
            break;
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
        case PCINIT:
            PCInventory.blank = new PCInventory(PCInventory.defaultId);
            pc = PCInventory.getPC(this.data.getUniqueId(PacketPC.OWNER));
            pc.seenOwner = this.data.getBoolean("O");
            pc.autoToPC = this.data.getBoolean("A");
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
                pc = PCInventory.getPC(this.data.getUniqueId(PacketPC.OWNER));
                for (int i = 0; i < 54; i++)
                    if (this.data.getBoolean("val" + i))
                    {
                        final int j = i + page * 54;
                        pc.setInventorySlotContents(j, ItemStack.EMPTY);
                    }
            }
            break;
        case TOGGLEAUTO:
            pc = PCInventory.getPC(this.data.getUniqueId(PacketPC.OWNER));
            pc.autoToPC = this.data.getBoolean("A");
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
