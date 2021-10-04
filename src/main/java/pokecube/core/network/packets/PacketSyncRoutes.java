package pokecube.core.network.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.GuardAICapability.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.CapHolders;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.Packet;

public class PacketSyncRoutes extends Packet
{
    public static void applyServerPacket(final Tag tag, final Entity mob, final IGuardAICapability guard)
    {
        final CompoundTag nbt = (CompoundTag) tag;
        final int index = nbt.getInt("I");
        if (nbt.contains("V"))
        {
            // TODO generalize this maybe?
            final IGuardTask task = new GuardTask();
            task.load(nbt.get("V"));
            if (index < guard.getTasks().size()) guard.setTask(index, task);
            else guard.getTasks().add(task);
        }
        else if (nbt.contains("N"))
        {
            final int index1 = nbt.getInt("I");
            final int index2 = index1 + nbt.getInt("N");
            final IGuardTask temp = guard.getTasks().get(index1);
            guard.getTasks().set(index1, guard.getTasks().get(index2));
            guard.getTasks().set(index2, temp);
        }
        else if (index < guard.getTasks().size()) guard.getTasks().remove(index);
        EntityUpdate.sendEntityUpdate(mob);
    }

    public static void sendServerPacket(final Entity mob, final Tag tag)
    {
        final PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.entityId = mob.getId();
        if (tag instanceof CompoundTag) packet.data = (CompoundTag) tag;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendUpdateClientPacket(final Entity mob, final ServerPlayer player, final boolean gui)
    {
        final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        final PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.data.put("R", guard.serializeTasks());
        packet.data.putBoolean("O", gui);
        packet.entityId = mob.getId();
        PokecubeCore.packets.sendTo(packet, player);
    }

    public int entityId;

    public CompoundTag data = new CompoundTag();

    public PacketSyncRoutes()
    {
    }

    public PacketSyncRoutes(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        if (e == null) return;
        final IGuardAICapability guard = e.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        guard.loadTasks((ListTag) data.get("R"));
        if (data.getBoolean("O")) PacketSyncRoutes.sendServerPacket(e, null);
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        if (e == null) return;
        final IGuardAICapability guard = e.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);

        if (guard != null) if (data.isEmpty())
        {
            final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
            buffer.writeInt(e.getId());
            buffer.writeByte(PacketPokemobGui.ROUTES);
            final SimpleMenuProvider provider = new SimpleMenuProvider((i, p,
                    a) -> new ContainerPokemob(i, p, buffer), e.getDisplayName());
            NetworkHooks.openGui(player, provider, buf ->
            {
                buf.writeInt(e.getId());
                buf.writeByte(PacketPokemobGui.ROUTES);
            });
        }
        else PacketSyncRoutes.applyServerPacket(data.get("T"), e, guard);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
