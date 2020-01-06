package pokecube.core.network.pokemobs;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.utility.AIStoreStuff;
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

    public static void sendOpenPacket(final Entity target, final ServerPlayerEntity player)
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
        buffer.writeInt(target.getEntityId());
        buffer.writeByte(PacketPokemobGui.MAIN);
        final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                e) -> new ContainerPokemob(i, p, buffer), target.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeInt(target.getEntityId());
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

    public PacketPokemobGui(final PacketBuffer buf)
    {
        this.message = buf.readByte();
        this.id = buf.readInt();
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final Entity entity = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.id, true);
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
        buffer.writeInt(entity.getEntityId());
        buffer.writeByte(this.message);
        final byte mode = this.message;
        SimpleNamedContainerProvider provider;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);

        switch (this.message)
        {
        case ROUTES:
            PacketSyncRoutes.sendUpdateClientPacket(entity, player, true);
            return;
        case STORAGE:
            AIStoreStuff ai = null;
            for (final IAIRunnable run : pokemob.getTasks())
                if (run instanceof AIStoreStuff) ai = (AIStoreStuff) run;
            final AIStoreStuff toSend = ai;
            buffer.writeCompoundTag(toSend.serializeNBT());
            provider = new SimpleNamedContainerProvider((i, p, e) -> new ContainerPokemob(i, p, buffer), entity
                    .getDisplayName());
            NetworkHooks.openGui(player, provider, buf ->
            {
                buf.writeInt(entity.getEntityId());
                buf.writeByte(mode);
                buf.writeCompoundTag(toSend.serializeNBT());
            });
            return;
        }
        provider = new SimpleNamedContainerProvider((i, p, e) -> new ContainerPokemob(i, p, buffer), entity
                .getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeInt(entity.getEntityId());
            buf.writeByte(mode);
        });
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeByte(this.message);
        buf.writeInt(this.id);
    }
}
