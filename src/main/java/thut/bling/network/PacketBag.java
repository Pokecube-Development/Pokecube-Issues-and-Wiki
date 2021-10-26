package thut.bling.network;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import thut.bling.bag.large.LargeContainer;
import thut.bling.bag.large.LargeInventory;
import thut.bling.bag.large.LargeManager;
import thut.bling.bag.small.SmallContainer;
import thut.bling.bag.small.SmallInventory;
import thut.bling.bag.small.SmallManager;
import thut.wearables.network.Packet;

public class PacketBag extends Packet
{
    public static final TranslationTextComponent ENDERBAG      = new TranslationTextComponent(
            "item.thut_bling.bling_bag_ender_vanilla");
    public static final TranslationTextComponent LARGEENDERBAG = new TranslationTextComponent(
            "item.thut_bling.bling_bag_ender_large");
    public static final TranslationTextComponent SMALLBAG      = new TranslationTextComponent(
            "item.thut_bling.bling_bag");

    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte INIT    = 2;
    public static final byte RELEASE = 3;
    public static final byte OPEN    = 4;

    public static final String OWNER = "_owner_";

    public static void sendOpenPacket(final PlayerEntity playerIn, final ItemStack heldItem)
    {
        final String item = heldItem.getItem().getRegistryName().getPath();
        if (item.equalsIgnoreCase("bling_bag_ender_large"))
        {
            PacketBag.sendOpenPacket(playerIn, playerIn.getUUID());
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag_ender_vanilla"))
        {
            final EnderChestInventory enderchestinventory = playerIn.getEnderChestInventory();
            playerIn.openMenu(new SimpleNamedContainerProvider((id, p, e) ->
            {
                return ChestContainer.threeRows(id, p, enderchestinventory);
            }, PacketBag.ENDERBAG));
            playerIn.awardStat(Stats.OPEN_ENDERCHEST);
            return;
        }
        else if (item.equalsIgnoreCase("bling_bag"))
        {
            if (!heldItem.hasTag()) heldItem.setTag(new CompoundNBT());
            final CompoundNBT tag = heldItem.getTag();
            UUID id = UUID.randomUUID();
            if (tag.hasUUID("bag_id")) id = tag.getUUID("bag_id");
            else tag.putUUID("bag_id", id);
            final SmallInventory inv = SmallManager.INSTANCE.get(id);
            playerIn.openMenu(new SimpleNamedContainerProvider((gid, p, e) -> new SmallContainer(gid, p, inv),
                    PacketBag.SMALLBAG));
        }
    }

    public static void sendOpenPacket(final PlayerEntity sendTo, final UUID owner)
    {
        final ServerPlayerEntity player = (ServerPlayerEntity) sendTo;
        final LargeInventory inv = LargeManager.INSTANCE.get(owner);
        final PacketBuffer clt = inv.makeBuffer();
        final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p, e) -> new LargeContainer(
                i, p, inv), PacketBag.LARGEENDERBAG);
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
    public void write(final PacketBuffer buf)
    {
        buf.writeByte(this.message);
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNbt(this.data);
    }

}
