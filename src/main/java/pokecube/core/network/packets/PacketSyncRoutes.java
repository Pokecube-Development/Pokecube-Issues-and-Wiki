package pokecube.core.network.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkHooks;
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
    public static void applyServerPacket(final INBT tag, final Entity mob, final IGuardAICapability guard)
    {
        final CompoundNBT nbt = (CompoundNBT) tag;
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

    public static void sendServerPacket(final Entity mob, final INBT tag)
    {
        final PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.entityId = mob.getEntityId();
        if (tag instanceof CompoundNBT) packet.data = (CompoundNBT) tag;
        PokecubeCore.packets.sendToServer(packet);
    }

    public static void sendUpdateClientPacket(final Entity mob, final ServerPlayerEntity player, final boolean gui)
    {
        final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        final PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.data.put("R", guard.serializeTasks());
        packet.data.putBoolean("O", gui);
        packet.entityId = mob.getEntityId();
        PokecubeCore.packets.sendTo(packet, player);
    }

    public int entityId;

    public CompoundNBT data = new CompoundNBT();

    public PacketSyncRoutes()
    {
    }

    public PacketSyncRoutes(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        this.entityId = buffer.readInt();
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final CompoundNBT data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        if (e == null) return;
        final IGuardAICapability guard = e.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        guard.loadTasks((ListNBT) data.get("R"));
        if (data.getBoolean("O")) PacketSyncRoutes.sendServerPacket(e, null);
    }

    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final int id = this.entityId;
        final CompoundNBT data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        if (e == null) return;
        final IGuardAICapability guard = e.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);

        if (guard != null) if (data.isEmpty())
        {
            final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
            buffer.writeInt(e.getEntityId());
            buffer.writeByte(PacketPokemobGui.ROUTES);
            final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                    a) -> new ContainerPokemob(i, p, buffer), e.getDisplayName());
            NetworkHooks.openGui(player, provider, buf ->
            {
                buf.writeInt(e.getEntityId());
                buf.writeByte(PacketPokemobGui.ROUTES);
            });
        }
        else PacketSyncRoutes.applyServerPacket(data.get("T"), e, guard);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        buffer.writeCompoundTag(this.data);
    }
}
