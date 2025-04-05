package thut.bling.network;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import thut.bling.ThutBling;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.large.LargeInventory;
import thut.bling.bag.large.LargeManager;
import thut.bling.bag.small.SmallContainer;
import thut.bling.bag.small.SmallInventory;
import thut.bling.bag.small.SmallManager;
import thut.bling.data.SmallBagData;
import thut.lib.RegHelper;
import thut.lib.TComponent;
import thut.wearables.network.Packet;

public class PacketBag extends Packet
{
    public static final MutableComponent ENDERBAG = TComponent.translatable("item.thut_bling.bling_bag_ender_vanilla");
    public static final MutableComponent LARGEENDERBAG = TComponent
            .translatable("item.thut_bling.bling_bag_ender_large");
    public static final MutableComponent SMALLBAG = TComponent.translatable("item.thut_bling.bling_bag");

    public static final byte SETPAGE = 0;
    public static final byte RENAME = 1;
    public static final byte INIT = 2;
    public static final byte RELEASE = 3;
    public static final byte OPEN = 4;

    public static final String OWNER = "_owner_";

    public static void sendOpenPacket(final Player playerIn, final ItemStack heldItem)
    {
        final String item = RegHelper.getKey(heldItem).getPath();
        if (item.equalsIgnoreCase("bling_bag_ender_large"))
        {
            PacketBag.sendOpenPacket(playerIn, playerIn.getUUID());
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag_ender_vanilla"))
        {
            final PlayerEnderChestContainer enderchestinventory = playerIn.getEnderChestInventory();
            playerIn.openMenu(new SimpleMenuProvider((id, p, e) -> {
                return ChestMenu.threeRows(id, p, enderchestinventory);
            }, PacketBag.ENDERBAG));
            playerIn.awardStat(Stats.OPEN_ENDERCHEST);
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag"))
        {
            var data = heldItem.get(ThutBling.SMALL_BAG_DATA);
            if (data == null)
            {
                data = new SmallBagData(UUID.randomUUID(), "", "", false);
                heldItem.set(ThutBling.SMALL_BAG_DATA, data);
            }
            UUID id = data.uuid();
            final SmallInventory inv = SmallManager.INSTANCE.get(playerIn.registryAccess(), id);
            playerIn.openMenu(
                    new SimpleMenuProvider((gid, p, e) -> new SmallContainer(gid, p, inv), PacketBag.SMALLBAG));
        }
    }

    public static void sendOpenPacket(final Player sendTo, final UUID owner)
    {
        final ServerPlayer player = (ServerPlayer) sendTo;
        final LargeInventory inv = LargeManager.INSTANCE.get(sendTo.registryAccess(), owner);
        final FriendlyByteBuf clt = inv.makeBuffer();
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new LargeContainer(i, p, inv),
                PacketBag.LARGEENDERBAG);
        player.openMenu(provider, buf -> {
            buf.writeBytes(clt);
        });
    }

    byte message;
    public CompoundTag data = new CompoundTag();

    public PacketBag()
    {}

    public PacketBag(final byte message)
    {
        this.message = message;
    }

    public PacketBag(final byte message, final UUID owner)
    {
        this(message);
        this.data.putUUID(PacketBag.OWNER, owner);
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.message = buf.readByte();
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient(Player player)
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
        if (!(player.containerMenu instanceof LargeContainer menu)) return;
        switch (this.message)
        {
        case SETPAGE:
            menu.gotoInventoryPage(this.data.getInt("P"));
            break;
        case RENAME:
            final String name = this.data.getString("N");
            menu.changeName(name);
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

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thut_bling:bag_access"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
