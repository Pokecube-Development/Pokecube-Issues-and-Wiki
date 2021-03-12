package pokecube.adventures.network;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.adventures.items.bag.BagContainer;
import pokecube.adventures.items.bag.BagInventory;
import pokecube.adventures.items.bag.BagManager;
import thut.core.common.network.Packet;

public class PacketBag extends Packet
{
    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte INIT    = 2;
    public static final byte RELEASE = 3;
    public static final byte OPEN    = 4;

    public static final String OWNER = "_owner_";

    public static void sendOpenPacket(final PlayerEntity sendTo, final UUID owner)
    {
        final ServerPlayerEntity player = (ServerPlayerEntity) sendTo;
        final BagInventory inv = BagManager.INSTANCE.get(owner);
        final PacketBuffer clt = inv.makeBuffer();
        final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p, e) -> new BagContainer(i,
                p, inv), sendTo.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeBytes(clt);
        });
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
        this.data.putUUID(PacketBag.OWNER, owner);
    }

    public PacketBag(final PacketBuffer buf)
    {
        this.message = buf.readByte();
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        switch (this.message)
        {
        case OPEN:
            break;
        default:
            break;
        }
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {

        BagContainer container = null;
        if (player.containerMenu instanceof BagContainer) container = (BagContainer) player.containerMenu;
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
        buffer.writeNbt(this.data);
    }

}
