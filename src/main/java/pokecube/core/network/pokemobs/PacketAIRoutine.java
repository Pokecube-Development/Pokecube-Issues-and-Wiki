package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.core.PokecubeCore;
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
        final Entity user = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.entityId, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(user);
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
