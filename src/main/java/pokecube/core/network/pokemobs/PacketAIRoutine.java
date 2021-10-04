package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketAIRoutine extends Packet
{

    public static void sentCommand(final IPokemob pokemob, final AIRoutine routine, final boolean state)
    {
        final PacketAIRoutine packet = new PacketAIRoutine();
        packet.entityId = pokemob.getEntity().getId();
        packet.routine = routine;
        packet.state = state;
        PokecubeCore.packets.sendToServer(packet);
    }

    int       entityId;
    AIRoutine routine;

    boolean state;

    public PacketAIRoutine()
    {
        super(null);
    }

    public PacketAIRoutine(final FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.routine = AIRoutine.values()[buf.readByte()];
        this.state = buf.readBoolean();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final Entity user = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        pokemob.setRoutineState(this.routine, this.state);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.routine.ordinal());
        buf.writeBoolean(this.state);
    }

}
