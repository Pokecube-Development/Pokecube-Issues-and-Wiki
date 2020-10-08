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
    int         x;
    int         y;
    int         z;
    long[][]    weatherEffects = new long[PokemobTerrainEffects.WeatherEffectType.values().length][2];
    long[][]    terrainEffects = new long[PokemobTerrainEffects.TerrainEffectType.values().length][2];
    long[][]    entryEffects = new long[PokemobTerrainEffects.EntryEffectType.values().length][2];

    CompoundNBT data    = new CompoundNBT();

    public static void sendTerrainEffects(Entity player, int x, int y, int z, PokemobTerrainEffects terrain)
    {
        final PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.x = x;
        packet.y = y;
        packet.z = z;

        encodeActiveEffect(packet.weatherEffects, PokemobTerrainEffects.WeatherEffectType.values(), terrain);
        encodeActiveEffect(packet.terrainEffects, PokemobTerrainEffects.TerrainEffectType.values(), terrain);
        encodeActiveEffect(packet.entryEffects, PokemobTerrainEffects.EntryEffectType.values(), terrain);

        PokecubeCore.packets.sendToTracking(packet,
                player.getEntityWorld().getChunk(new BlockPos(x * 16, y * 16, z * 16)));
    }

    private static void encodeActiveEffect(final long[][] data,
                                                PokemobTerrainEffects.EffectType[] effectTypes,
                                                PokemobTerrainEffects terrain)
    {
        for (int i = 0; i < data.length; i++){
            PokemobTerrainEffects.EffectType type = effectTypes[i];
            if(terrain.isEffectActive(type)) {
                PokemobTerrainEffects.Effect effect = terrain.getEffect(type);
                data[i][0] = i;
                data[i][1] = effect.getDuration();
            }else {
                data[i][0] = i;
                data[i][1] = 0;
            }
        }
    }

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

        writeLongArray(weatherEffects, buf);
        writeLongArray(terrainEffects, buf);
        writeLongArray(entryEffects, buf);
    }

    @Override
    public void handleClient()
    {
        PlayerEntity player;
        player = PokecubeCore.proxy.getPlayer();
        final TerrainSegment t = TerrainManager.getInstance().getTerrain(player.getEntityWorld(), this.x * 16,
                this.y * 16, this.z * 16);
        final PokemobTerrainEffects effects = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");

        boolean empty = decodeEffectTypes(weatherEffects, PokemobTerrainEffects.WeatherEffectType.values(), effects);
        empty = empty && decodeEffectTypes(terrainEffects, PokemobTerrainEffects.TerrainEffectType.values(), effects);
        empty = empty && decodeEffectTypes(entryEffects, PokemobTerrainEffects.EntryEffectType.values(), effects);

        if (!empty)
        {
            MoveAnimationHelper.Instance().addEffect();
            MoveAnimationHelper.Instance().terrainMap.put(t.getChunkCoords(), t);
        }
        else MoveAnimationHelper.Instance().clearEffect();
    }

    private boolean decodeEffectTypes(final long[][] data, final PokemobTerrainEffects.EffectType[] types,
                                   final PokemobTerrainEffects effects){
        boolean empty = true;

        for (long[] longs : data) {
            boolean check = longs[1] <= 0;
            empty = empty && check;
            if (check) continue;

            effects.setEffectDuration(types[(int) longs[0]],
                    longs[1], null);
        }

        return empty;
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        writeLongArray(weatherEffects, buf);
        writeLongArray(terrainEffects, buf);
        writeLongArray(entryEffects, buf);
    }

    private void writeLongArray(long[][] array, PacketBuffer buf)
    {
        for (long[] effect : array)
            for (int s = 0; s < 2; s++)
                buf.writeLong(effect[s]);
    }
}
