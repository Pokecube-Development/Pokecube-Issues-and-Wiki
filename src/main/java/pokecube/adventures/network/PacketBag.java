package pokecube.adventures.network;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;
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

    public static void sendOpenPacket(final Player sendTo, final UUID owner)
    {
        final ServerPlayer player = (ServerPlayer) sendTo;
        final BagInventory inv = BagManager.INSTANCE.get(owner);
        final FriendlyByteBuf clt = inv.makeBuffer();
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new BagContainer(i,
                p, inv), sendTo.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeBytes(clt);
        });
    }

    byte               message;
    public CompoundTag data = new CompoundTag();

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

    public PacketBag(final FriendlyByteBuf buf)
    {
        this.message = buf.readByte();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
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
    public void handleServer(final ServerPlayer player)
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
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeByte(this.message);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeNbt(this.data);
    }

}
