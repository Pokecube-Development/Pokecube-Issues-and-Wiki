package thut.core.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.ThutCaps;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.core.common.ThutCore;

public class TerrainUpdate extends NBTPacket
{
    public static final PacketAssembly<TerrainUpdate> ASSEMBLER = PacketAssembly.registerAssembler(TerrainUpdate.class,
            TerrainUpdate::new, ThutCore.packets);

    public static void sendTerrainToClient(final ChunkPos pos, final ServerPlayerEntity player)
    {
        final World world = player.getCommandSenderWorld();
        if (!world.isAreaLoaded(pos.getWorldPosition(), 0)) return;
        final ITerrainProvider provider = world.getChunk(pos.x, pos.z).getCapability(ThutCaps.TERRAIN_CAP, null).orElse(
                null);
        final CompoundNBT terrainData = (CompoundNBT) ThutCaps.TERRAIN_CAP.writeNBT(provider, null);
        terrainData.putInt("c_x", pos.x);
        terrainData.putInt("c_z", pos.z);
        final TerrainUpdate message = new TerrainUpdate(terrainData);
        TerrainUpdate.ASSEMBLER.sendTo(message, player);
    }

    public TerrainUpdate()
    {
        super();
    }

    public TerrainUpdate(final CompoundNBT nbt)
    {
        super(nbt);
    }

    public TerrainUpdate(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final World world = net.minecraft.client.Minecraft.getInstance().level;
        final CompoundNBT nbt = this.tag;
        final Chunk chunk = world.getChunk(nbt.getInt("c_x"), nbt.getInt("c_z"));
        final ITerrainProvider terrain = chunk.getCapability(ThutCaps.TERRAIN_CAP, null).orElse(null);
        ThutCaps.TERRAIN_CAP.readNBT(terrain, null, nbt);
    }
}
