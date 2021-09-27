package pokecube.core.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.MoveAnimationHelper;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.network.Packet;

public class PacketSyncTerrain extends Packet
{
    int      x;
    int      y;
    int      z;
    long[][] weatherEffects = new long[PokemobTerrainEffects.WeatherEffectType.values().length][2];
    long[][] terrainEffects = new long[PokemobTerrainEffects.TerrainEffectType.values().length][2];
    long[][] entryEffects   = new long[PokemobTerrainEffects.EntryEffectType.values().length][2];

    CompoundNBT data = new CompoundNBT();

    public static void sendTerrainEffects(final ServerWorld world, final int x, final int y, final int z,
            final PokemobTerrainEffects terrain)
    {
        final PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.x = x;
        packet.y = y;
        packet.z = z;

        PacketSyncTerrain.encodeActiveEffect(packet.weatherEffects, PokemobTerrainEffects.WeatherEffectType.values(),
                terrain);
        PacketSyncTerrain.encodeActiveEffect(packet.terrainEffects, PokemobTerrainEffects.TerrainEffectType.values(),
                terrain);
        PacketSyncTerrain.encodeActiveEffect(packet.entryEffects, PokemobTerrainEffects.EntryEffectType.values(),
                terrain);

        PokecubeCore.packets.sendToTracking(packet, world.getChunk(new BlockPos(x * 16, y * 16, z * 16)));
    }

    private static void encodeActiveEffect(final long[][] data, final PokemobTerrainEffects.EffectType[] effectTypes,
            final PokemobTerrainEffects terrain)
    {
        for (int i = 0; i < data.length; i++)
        {
            final PokemobTerrainEffects.EffectType type = effectTypes[i];
            data[i][0] = i;
            data[i][1] = 0;
            if (terrain.isEffectActive(type))
            {
                final PokemobTerrainEffects.Effect effect = terrain.getEffect(type);
                data[i][1] = effect.getDuration();
            }
        }
    }

    public PacketSyncTerrain()
    {
        super(null);
    }

    public PacketSyncTerrain(final PacketBuffer buf)
    {
        super(buf);
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();

        this.readLongArray(this.weatherEffects, buf);
        this.readLongArray(this.terrainEffects, buf);
        this.readLongArray(this.entryEffects, buf);
    }

    @Override
    public void handleClient()
    {
        PlayerEntity player;
        player = PokecubeCore.proxy.getPlayer();
        final TerrainSegment t = TerrainManager.getInstance().getTerrain(player.getCommandSenderWorld(), this.x * 16,
                this.y * 16, this.z * 16);
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");

        boolean empty = this.decodeEffectTypes(this.weatherEffects, PokemobTerrainEffects.WeatherEffectType.values(),
                effects);
        empty = this.decodeEffectTypes(this.terrainEffects, PokemobTerrainEffects.TerrainEffectType.values(), effects)
                && empty;
        empty = this.decodeEffectTypes(this.entryEffects, PokemobTerrainEffects.EntryEffectType.values(), effects)
                && empty;
        if (!empty)
        {
            MoveAnimationHelper.Instance().addEffect();
            MoveAnimationHelper.Instance().terrainMap.put(t.getChunkCoords(), t);
        }
        else
        {
            MoveAnimationHelper.Instance().clearEffect();
            MoveAnimationHelper.Instance().terrainMap.remove(t.getChunkCoords());
        }
    }

    private boolean decodeEffectTypes(final long[][] data, final PokemobTerrainEffects.EffectType[] types,
            final PokemobTerrainEffects effects)
    {
        boolean empty = true;
        for (final long[] longs : data)
        {
            final boolean check = longs[1] <= 0;
            empty = empty && check;
            if (check) continue;
            effects.setEffectDuration(types[(int) longs[0]], longs[1], null);
        }
        return empty;
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        this.writeLongArray(this.weatherEffects, buf);
        this.writeLongArray(this.terrainEffects, buf);
        this.writeLongArray(this.entryEffects, buf);
    }

    private void writeLongArray(final long[][] array, final PacketBuffer buf)
    {
        for (final long[] effect : array)
            for (int s = 0; s < 2; s++)
                buf.writeLong(effect[s]);
    }

    private void readLongArray(final long[][] array, final PacketBuffer buf)
    {
        for (final long[] effect : array)
            for (int s = 0; s < 2; s++)
                effect[s] = buf.readLong();
    }
}
