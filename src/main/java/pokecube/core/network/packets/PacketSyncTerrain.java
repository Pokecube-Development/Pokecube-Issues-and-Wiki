package pokecube.core.network.packets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.MoveAnimationHelper;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.network.Packet;

public class PacketSyncTerrain extends Packet
{
    public static void sendTerrainEffects(Entity player, int x, int y, int z, PokemobTerrainEffects terrain)
    {
        final PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.x = x;
        packet.y = y;
        packet.z = z;
        for (int i = 0; i < 16; i++)
            packet.effects[i] = terrain.effects[i];
        PokecubeCore.packets.sendToTracking(packet,
                player.getEntityWorld().getChunk(new BlockPos(x * 16, y * 16, z * 16)));
    }

    int         x;
    int         y;
    int         z;
    long[]      effects = new long[16];
    CompoundNBT data    = new CompoundNBT();

    public PacketSyncTerrain()
    {
        super(null);
    }

    public PacketSyncTerrain(PacketBuffer buf)
    {
        super(buf);
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        for (int i = 0; i < 16; i++)
            this.effects[i] = buf.readLong();
    }

    @Override
    public void handleClient()
    {
        PlayerEntity player;
        player = PokecubeCore.proxy.getPlayer();
        final TerrainSegment t = TerrainManager.getInstance().getTerrain(player.getEntityWorld(), this.x * 16,
                this.y * 16, this.z * 16);
        final PokemobTerrainEffects effect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
        boolean empty = true;
        for (int i = 0; i < 16; i++)
        {
            effect.effects[i] = this.effects[i];
            empty = empty && this.effects[i] <= 0;
        }
        if (!empty)
        {
            MoveAnimationHelper.Instance().addEffect();
            MoveAnimationHelper.Instance().terrainMap.put(t.getChunkCoords(), t);
        }
        else MoveAnimationHelper.Instance().clearEffect();
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        for (int i = 0; i < 16; i++)
            buf.writeLong(this.effects[i]);
    }

}
