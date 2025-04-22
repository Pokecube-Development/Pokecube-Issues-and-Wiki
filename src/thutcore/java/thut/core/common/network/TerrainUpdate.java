package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.api.ThutCaps;
import thut.api.level.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.level.terrain.TerrainSegment;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;

public class TerrainUpdate extends NBTPacket
{
    public static final PacketAssembly<TerrainUpdate> ASSEMBLER = PacketAssembly.registerAssembler(TerrainUpdate.class,
            TerrainUpdate::new, ThutCore.packets);

    public static void sendTerrainToClient(final ChunkPos pos, final ServerPlayer player)
    {
        final ServerLevel world = (ServerLevel) player.level;
        if (!world.isNaturalSpawningAllowed(pos)) return;
        var chunk = world.getChunk(pos.x, pos.z);
        final ITerrainProvider provider = ThutCaps.getTerrainProvider(chunk);
        if (provider == null)
        {
            return;
        }
        final CompoundTag terrainData = provider.serializeNBT(player.registryAccess());
        if (terrainData.isEmpty()) return;
        terrainData.putInt("c_x", pos.x);
        terrainData.putInt("c_z", pos.z);
        TerrainUpdate.ASSEMBLER.sendTo(terrainData, player);
    }

    public TerrainUpdate()
    {
        super();
    }

    public static void sendTerrainToWatching(TerrainSegment segment)
    {
        var chunk = segment.chunk;
        if (!(chunk instanceof LevelChunk lchunk)) return;
        final ITerrainProvider provider = ThutCaps.getTerrainProvider(chunk);
        final CompoundTag terrainData = provider.serializeNBT(chunk.getLevel().registryAccess());
        terrainData.putInt("c_x", segment.chunkX);
        terrainData.putInt("c_z", segment.chunkZ);
        TerrainUpdate.ASSEMBLER.sendToTracking(terrainData, lchunk);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient(Player player)
    {
        final Level world = player.level();
        final CompoundTag nbt = this.tag;
        final LevelChunk chunk = world.getChunk(nbt.getInt("c_x"), nbt.getInt("c_z"));
        final ITerrainProvider terrain = ThutCaps.getTerrainProvider(chunk);
        terrain.deserializeNBT(world.registryAccess(), this.tag);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:terrain_update"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
