package pokecube.core.network.pokemobs;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.packets.PacketSyncRoutes;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.network.Packet;

public class PacketPokemobGui extends Packet
{
    public static final byte MAIN    = 0;
    public static final byte AI      = 1;
    public static final byte STORAGE = 2;
    public static final byte ROUTES  = 3;

    public static void sendOpenPacket(final Entity target, final ServerPlayer player)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
        buffer.writeInt(target.getId());
        buffer.writeByte(PacketPokemobGui.MAIN);
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p,
                e) -> new ContainerPokemob(i, p, buffer), target.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeInt(target.getId());
            buf.writeByte(PacketPokemobGui.MAIN);
        });
    }

    @OnlyIn(value = Dist.CLIENT)
    public static void sendPagePacket(final byte page, final int id)
    {
        PokecubeCore.packets.sendToServer(new PacketPokemobGui(page, id));
    }

    byte message;

    int id;

    public PacketPokemobGui()
    {
    }

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
        final Entity entity = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.id, true);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
        buffer.writeInt(entity.getId());
        buffer.writeByte(this.message);
        final byte mode = this.message;
        SimpleMenuProvider provider;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return;

        switch (this.message)
        {
        case ROUTES:
            provider = new SimpleMenuProvider((i, p, e) -> new ContainerPokemob(i, p, buffer), entity
                    .getDisplayName());
            PacketSyncRoutes.sendUpdateClientPacket(entity, player, true);
            NetworkHooks.openGui(player, provider, buf ->
            {
                buf.writeInt(entity.getId());
                buf.writeByte(mode);
            });
            return;
        case STORAGE:
            StoreTask ai = null;
            for (final IAIRunnable run : pokemob.getTasks())
                if (run instanceof StoreTask) ai = (StoreTask) run;
            final StoreTask toSend = ai;
            buffer.writeNbt(toSend.serializeNBT());
            provider = new SimpleMenuProvider((i, p, e) -> new ContainerPokemob(i, p, buffer), entity
                    .getDisplayName());
            NetworkHooks.openGui(player, provider, buf ->
            {
                buf.writeInt(entity.getId());
                buf.writeByte(mode);
                buf.writeNbt(toSend.serializeNBT());
            });
            return;
        }
        provider = new SimpleMenuProvider((i, p, e) -> new ContainerPokemob(i, p, buffer), entity
                .getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeInt(entity.getId());
            buf.writeByte(mode);
        });
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeByte(this.message);
        buf.writeInt(this.id);
    }
}
