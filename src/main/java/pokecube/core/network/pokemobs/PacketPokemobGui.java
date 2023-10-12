package pokecube.core.network.pokemobs;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkHooks;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.inventory.pokemob.PokemobContainer;
import pokecube.core.network.packets.PacketSyncRoutes;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.network.Packet;

public class PacketPokemobGui extends Packet
{
    public static final byte MAIN = 0;
    public static final byte AI = 1;
    public static final byte STORAGE = 2;
    public static final byte ROUTES = 3;

    public static void sendOpenPacket(final Entity target, final ServerPlayer player)
    {
        sendOpenPacket(target, player, MAIN);
    }

    public static void sendOpenPacket(Entity target, ServerPlayer player, byte mode)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
        buffer.writeInt(target.getId());
        buffer.writeByte(mode);

        IPokemob pokemob = PokemobCaps.getPokemobFor(target);
        if (pokemob != null && !(player.containerMenu instanceof PokemobContainer))
        {
            StoreTask ai = null;
            for (final IAIRunnable run : pokemob.getTasks()) if (run instanceof StoreTask task) ai = task;
            final StoreTask toSend = ai;
            buffer.writeNbt(toSend.serializeNBT());
            String megaMode = target.getPersistentData().getString("pokecube:mega_mode");
            buffer.writeUtf(megaMode);
            PacketSyncRoutes.sendUpdateClientPacket(target, player, false);
            final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new PokemobContainer(i, p, buffer),
                    target.getDisplayName());
            NetworkHooks.openGui(player, provider, buf -> {
                buf.writeInt(target.getId());
                buf.writeByte(mode);
                buf.writeNbt(toSend.serializeNBT());
                buf.writeUtf(megaMode);
            });
        }
    }

    public static void sendPagePacket(final byte page, final int id)
    {
        PokecubeCore.packets.sendToServer(new PacketPokemobGui(page, id));
    }

    byte message;

    int id;

    public PacketPokemobGui()
    {}

    public PacketPokemobGui(final byte message, final int id)
    {
        this.message = message;
        this.id = id;
    }

    public PacketPokemobGui(final FriendlyByteBuf buf)
    {
        this.message = buf.readByte();
        this.id = buf.readInt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final Entity entity = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.id, true);

        // First check if maybe player already has this open.
        if (player.containerMenu instanceof PokemobContainer container)
        {
            container.setMode(message);
        }
        else sendOpenPacket(entity, player, this.message);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeByte(this.message);
        buf.writeInt(this.id);
    }
}
