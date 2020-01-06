package thut.core.common.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import thut.api.terrain.CapabilityTerrain;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.core.common.ThutCore;

public class TerrainUpdate extends Packet
{
    public static void sendTerrainToClient(World world, ChunkPos pos, ServerPlayerEntity player)
    {
        final ITerrainProvider provider = world.getChunk(pos.x, pos.z).getCapability(CapabilityTerrain.TERRAIN_CAP,
                null).orElse(null);
        final CompoundNBT terrainData = (CompoundNBT) CapabilityTerrain.TERRAIN_CAP.writeNBT(provider, null);
        terrainData.putInt("c_x", pos.x);
        terrainData.putInt("c_z", pos.z);
        final TerrainUpdate message = new TerrainUpdate(terrainData);
        ThutCore.packets.sendTo(message, player);
    }

    CompoundNBT tag;

    public TerrainUpdate(CompoundNBT tag)
    {
        super(null);
        this.tag = tag;
    }

    public TerrainUpdate(PacketBuffer buffer)
    {
        super(buffer);
        this.tag = buffer.readCompoundTag();
    }

    @Override
    public void handle(Supplier<Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            final PlayerEntity player = ThutCore.proxy.getPlayer();
            final CompoundNBT nbt = this.tag;
            final Chunk chunk = player.getEntityWorld().getChunk(nbt.getInt("c_x"), nbt.getInt("c_z"));
            final ITerrainProvider terrain = chunk.getCapability(CapabilityTerrain.TERRAIN_CAP, null).orElse(null);

            CapabilityTerrain.TERRAIN_CAP.readNBT(terrain, null, nbt);

        });
        ctx.get().setPacketHandled(true);
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeCompoundTag(this.tag);
    }

}
