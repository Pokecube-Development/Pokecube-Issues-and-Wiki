package pokecube.adventures.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.blocks.afa.AfaContainer;
import pokecube.adventures.blocks.afa.AfaTile;
import thut.core.common.network.Packet;

public class PacketAFA extends Packet
{
    public static void openGui(final ServerPlayerEntity player, final AfaTile tile)
    {
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.afa");
        final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p, e) -> new AfaContainer(i,
                p, IWorldPosCallable.of(tile.getWorld(), tile.getPos())), name);
        player.openContainer(provider);
    }

    public CompoundNBT data = new CompoundNBT();

    public PacketAFA()
    {
    }

    public PacketAFA(final PacketBuffer buf)
    {
        this.data = buf.readCompoundTag();
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.data);
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Container cont = player.openContainer;
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
