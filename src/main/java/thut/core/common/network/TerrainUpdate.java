package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.ThutCaps;
import thut.api.level.terrain.CapabilityTerrain.ITerrainProvider;
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
        final ITerrainProvider provider = world.getChunk(pos.x, pos.z).getCapability(ThutCaps.TERRAIN_PROVIDER, null)
                .orElse(null);
        final CompoundTag terrainData = provider.serializeNBT();
        terrainData.putInt("c_x", pos.x);
        terrainData.putInt("c_z", pos.z);
        final TerrainUpdate message = new TerrainUpdate(terrainData);
        TerrainUpdate.ASSEMBLER.sendTo(message, player);
    }

    public TerrainUpdate()
    {
        super();
    }

    public TerrainUpdate(final CompoundTag nbt)
    {
        super(nbt);
    }

    public TerrainUpdate(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final Level world = net.minecraft.client.Minecraft.getInstance().level;
        final CompoundTag nbt = this.tag;
        final LevelChunk chunk = world.getChunk(nbt.getInt("c_x"), nbt.getInt("c_z"));
        final ITerrainProvider terrain = chunk.getCapability(ThutCaps.TERRAIN_PROVIDER, null).orElse(null);
        terrain.deserializeNBT(this.tag);
    }
}
