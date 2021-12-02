package pokecube.core.network.packets;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCInventory;
import pokecube.core.inventory.pc.PCManager;
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

    public static void sendInitialSyncMessage(final Player sendTo)
    {
        final PCInventory inv = PCInventory.getPC(sendTo.getUUID());
        final PacketPC packet = new PacketPC(PacketPC.PCINIT, sendTo.getUUID());
        packet.data.putInt("N", inv.boxes.length);
        packet.data.putBoolean("A", inv.autoToPC);
        packet.data.putBoolean("O", inv.seenOwner);
        packet.data.putInt("C", inv.getPage());
        for (int i = 0; i < inv.boxes.length; i++)
            packet.data.putString("N" + i, inv.boxes[i]);
        PokecubeCore.packets.sendTo(packet, (ServerPlayer) sendTo);
    }

    public static void sendOpenPacket(final Player sendTo, final UUID owner, final BlockPos pcPos)
    {
        final ServerPlayer player = (ServerPlayer) sendTo;
        final PCInventory inv = PCManager.INSTANCE.get(owner);
        final FriendlyByteBuf clt = inv.makeBuffer();
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new PCContainer(i,
                p, inv), sendTo.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeBytes(clt);
        });
    }

    byte               message;
    public CompoundTag data = new CompoundTag();

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
        this.data.putUUID(PacketPC.OWNER, owner);
    }

    public PacketPC(final FriendlyByteBuf buf)
    {
        this.message = buf.readByte();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        PCInventory pc;
        switch (this.message)
        {
        case PCINIT:
            pc = PCInventory.getPC(this.data.getUUID(PacketPC.OWNER));
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
        case BIND:
            break;
        case PCOPEN:
            pc = PCInventory.getPC(this.data.getUUID(PacketPC.OWNER));
            pc.deserializeBox(this.data);
            break;
        default:
            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {

        PCContainer container = null;
        if (player.containerMenu instanceof PCContainer) container = (PCContainer) player.containerMenu;
        PCInventory pc = null;
        if (container != null) pc = container.inv;
        UUID id;
        switch (this.message)
        {
        case BIND:
            if (container != null && container.pcPos != null)
            {
                final BlockEntity tile = player.getCommandSenderWorld().getBlockEntity(container.pcPos);
                if (tile instanceof PCTile)
                {
                    final PCTile pcTile = (PCTile) tile;
                    if (pcTile.isBound()) pcTile.bind(null);
                    else pcTile.bind(player);
                }
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
            break;
        case RELEASE:
            final boolean toggle = this.data.getBoolean("T");
            id = this.data.getUUID(PacketPC.OWNER);
            if (toggle) container.setRelease(this.data.getBoolean("R"), id);
            else
            {
                final int page = this.data.getInt("page");
                if (pc == null) pc = PCInventory.getPC(id);
                container.setRelease(this.data.getBoolean("R"), id);
                for (int i = 0; i < 54; i++)
                    if (this.data.getBoolean("val" + i))
                    {
                        final int j = i + page * 54;
                        pc.setItem(j, ItemStack.EMPTY);
                    }
            }
            break;
        case TOGGLEAUTO:
            id = this.data.getUUID(PacketPC.OWNER);
            pc = PCInventory.getPC(id);
            pc.autoToPC = this.data.getBoolean("A");
            break;
        default:
            break;
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeByte(this.message);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeNbt(this.data);
    }

}
