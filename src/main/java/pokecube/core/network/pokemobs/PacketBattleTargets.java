package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketBattleTargets extends Packet
{
    public static void cycleAlly(IPokemob pokemob, boolean up)
    {
        pokemob.getMoveStats().allyIndex += (up ? 1 : -1);
        int index = pokemob.getMoveStats().allyIndex;
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().id, TYPE_ALLY, index));
    }

    public static void cycleEnemy(IPokemob pokemob, boolean up)
    {
        pokemob.getMoveStats().enemyIndex += (up ? 1 : -1);
        int index = pokemob.getMoveStats().enemyIndex;
        PokecubeCore.packets.sendToServer(new PacketBattleTargets(pokemob.getEntity().id, TYPE_ENEMY, index));
    }

    private static final byte TYPE_ALLY = 1;
    private static final byte TYPE_ENEMY = 2;

    public int entityId;
    public byte type;
    public int order;

    public PacketBattleTargets(int id, byte type, int order)
    {
        this.entityId = id;
        this.type = type;
        this.order = order;
    }

    public PacketBattleTargets(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.type = buffer.readByte();
        this.order = buffer.readByte();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob == null || player != pokemob.getOwner()) return;
        switch (type)
        {
        // The actual setting of the IDs from this gets done in LogicMiscUpdate,
        // so we just handle changing the index here.
        case TYPE_ALLY:
            pokemob.getMoveStats().allyIndex = order;
            break;
        case TYPE_ENEMY:
            pokemob.getMoveStats().enemyIndex = order;
            break;
        default:
            return;
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeByte(type);
        buffer.writeByte(order);
    }
}
