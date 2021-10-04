package pokecube.adventures.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import pokecube.adventures.blocks.afa.AfaContainer;
import pokecube.adventures.blocks.afa.AfaTile;
import thut.core.common.network.Packet;

public class PacketAFA extends Packet
{
    public static void openGui(final ServerPlayer player, final AfaTile tile)
    {
        final TranslatableComponent name = new TranslatableComponent("block.pokecube_adventures.afa");
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new AfaContainer(i,
                p, ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos())), name);
        player.openMenu(provider);
    }

    public CompoundTag data = new CompoundTag();

    public PacketAFA()
    {
    }

    public PacketAFA(final FriendlyByteBuf buf)
    {
        this.data = buf.readNbt();
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final AbstractContainerMenu cont = player.containerMenu;
        if (cont instanceof AfaContainer)
        {
            final AfaTile tile = ((AfaContainer) cont).tile;
            final int scale = this.data.getBoolean("S") ? 10 : 1;
            tile.distance += this.data.getBoolean("U") ? 1 * scale : -1 * scale;
            tile.distance = Math.max(0, tile.distance);
            tile.refreshAbility(true);
        }
    }
}
