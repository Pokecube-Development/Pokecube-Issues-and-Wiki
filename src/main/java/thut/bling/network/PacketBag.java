package thut.bling.network;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.large.LargeInventory;
import thut.bling.bag.large.LargeManager;
import thut.bling.bag.small.SmallContainer;
import thut.bling.bag.small.SmallInventory;
import thut.bling.bag.small.SmallManager;
import thut.wearables.network.Packet;

public class PacketBag extends Packet
{
    public static final TranslatableComponent ENDERBAG      = new TranslatableComponent(
            "item.thut_bling.bling_bag_ender_vanilla");
    public static final TranslatableComponent LARGEENDERBAG = new TranslatableComponent(
            "item.thut_bling.bling_bag_ender_large");
    public static final TranslatableComponent SMALLBAG      = new TranslatableComponent(
            "item.thut_bling.bling_bag");

    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte INIT    = 2;
    public static final byte RELEASE = 3;
    public static final byte OPEN    = 4;

    public static final String OWNER = "_owner_";

    public static void sendOpenPacket(final Player playerIn, final ItemStack heldItem)
    {
        final String item = heldItem.getItem().getRegistryName().getPath();
        if (item.equalsIgnoreCase("bling_bag_ender_large"))
        {
            PacketBag.sendOpenPacket(playerIn, playerIn.getUUID());
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag_ender_vanilla"))
        {
            final PlayerEnderChestContainer enderchestinventory = playerIn.getEnderChestInventory();
            playerIn.openMenu(new SimpleMenuProvider((id, p, e) ->
            {
                return ChestMenu.threeRows(id, p, enderchestinventory);
            }, PacketBag.ENDERBAG));
            playerIn.awardStat(Stats.OPEN_ENDERCHEST);
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag"))
        {
            if (!heldItem.hasTag()) heldItem.setTag(new CompoundTag());
            final CompoundTag tag = heldItem.getTag();
            UUID id = UUID.randomUUID();
            if (tag.hasUUID("bag_id")) id = tag.getUUID("bag_id");
            else tag.putUUID("bag_id", id);
            final SmallInventory inv = SmallManager.INSTANCE.get(id);
            playerIn.openMenu(new SimpleMenuProvider((gid, p, e) -> new SmallContainer(gid, p, inv),
                    PacketBag.SMALLBAG));
        }
    }

    public static void sendOpenPacket(final Player sendTo, final UUID owner)
    {
        final ServerPlayer player = (ServerPlayer) sendTo;
        final LargeInventory inv = LargeManager.INSTANCE.get(owner);
        final FriendlyByteBuf clt = inv.makeBuffer();
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new LargeContainer(
                i, p, inv), PacketBag.LARGEENDERBAG);
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

        LargeContainer container = null;
        if (player.containerMenu instanceof LargeContainer) container = (LargeContainer) player.containerMenu;
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
