package thut.api.terrain;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityTerrain
{
    public static class DefaultProvider implements ITerrainProvider, ICapabilityProvider, INBTSerializable<CompoundNBT>
    {
        private final LazyOptional<ITerrainProvider> holder   = LazyOptional.of(() -> this);
        private BlockPos                             pos;
        private IChunk                               chunk;
        private final TerrainSegment[]               segments = new TerrainSegment[16];

        public DefaultProvider()
        {
            this.chunk = null;
        }

        public DefaultProvider(final IChunk chunk)
        {
            this.chunk = chunk;
        }

        public DefaultProvider setChunk(final IChunk chunk)
        {
            this.chunk = chunk;
            return this;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            final BlockPos pos = this.getChunkPos();
            final int x = pos.getX();
            final int z = pos.getZ();
            final Map<Integer, Integer> idReplacements = Maps.newHashMap();
            final ListNBT tags = (ListNBT) nbt.get("ids");
            for (int i = 0; i < tags.size(); i++)
            {
                final CompoundNBT tag = tags.getCompound(i);
                final String name = tag.getString("name");
                final int id = tag.getInt("id");
                final BiomeType type = BiomeType.getBiome(name, false);
                if (type.getType() != id) idReplacements.put(id, type.getType());
            }
            final boolean hasReplacements = !idReplacements.isEmpty();
            for (int i = 0; i < 16; i++)
            {
                CompoundNBT terrainTag = null;
                try
                {
                    terrainTag = nbt.getCompound(i + "");
                }
                catch (final Exception e)
                {

                }
                TerrainSegment t = null;
                if (terrainTag != null && !terrainTag.isEmpty() && !TerrainSegment.noLoad)
                {
                    t = new TerrainSegment(x, i, z);
                    if (hasReplacements) t.idReplacements = idReplacements;
                    TerrainSegment.readFromNBT(t, terrainTag);
                    this.setTerrainSegment(t, i);
                    t.idReplacements = null;
                }
                if (t == null)
                {
                    t = new TerrainSegment(x, i, z);
                    this.setTerrainSegment(t, i);
                }
            }
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityTerrain.TERRAIN_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public BlockPos getChunkPos()
        {
            if (this.pos == null) this.pos = new BlockPos(this.chunk.getPos().x, 0, this.chunk.getPos().z);
            return this.pos;
        }

        @Override
        public TerrainSegment getTerrainSegement(final BlockPos blockLocation)
        {
            final int chunkY = blockLocation.getY() / 16 & 15;
            final TerrainSegment segment = this.getTerrainSegment(chunkY);
            segment.getCentre().addTo(0, 256 * (blockLocation.getY() / 256), 0);
            return segment;
        }

        @Override
        public TerrainSegment getTerrainSegment(int chunkY)
        {
            chunkY &= 15;
            TerrainSegment ret = this.segments[chunkY];
            if (ret == null)
            {
                ret = this.segments[chunkY] = new TerrainSegment(this.getChunkPos().getX(), chunkY, this.getChunkPos()
                        .getZ());
                ret.chunk = this.chunk;
            }
            return ret;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            for (int i = 0; i < 16; i++)
            {
                final TerrainSegment t = this.getTerrainSegment(i);
                if (t == null) continue;
                t.checkToSave();
                if (!t.toSave) continue;
                final CompoundNBT terrainTag = new CompoundNBT();
                t.saveToNBT(terrainTag);
                nbt.put("" + i, terrainTag);
            }
            final ListNBT biomeList = new ListNBT();
            for (final BiomeType t : BiomeType.values())
            {
                final CompoundNBT tag = new CompoundNBT();
                tag.putString("name", t.name);
                tag.putInt("id", t.getType());
                biomeList.add(tag);
            }
            nbt.put("ids", biomeList);
            return nbt;
        }

        @Override
        public void setTerrainSegment(final TerrainSegment segment, int chunkY)
        {
            chunkY &= 15;
            this.segments[chunkY] = segment;
        }

    }

    public static interface ITerrainProvider
    {
        BlockPos getChunkPos();

        TerrainSegment getTerrainSegement(BlockPos blockLocation);

        TerrainSegment getTerrainSegment(int chunkY);

        void setTerrainSegment(TerrainSegment segment, int chunkY);
    }

    public static class Storage implements Capability.IStorage<ITerrainProvider>
    {

        @Override
        public void readNBT(final Capability<ITerrainProvider> capability, final ITerrainProvider instance,
                final Direction side, final INBT base)
        {
            if (instance instanceof DefaultProvider && base instanceof CompoundNBT) ((DefaultProvider) instance)
                    .deserializeNBT((CompoundNBT) base);
        }

        @Override
        public INBT writeNBT(final Capability<ITerrainProvider> capability, final ITerrainProvider instance,
                final Direction side)
        {
            if (instance instanceof DefaultProvider) return ((DefaultProvider) instance).serializeNBT();
            return null;
        }
    }

    @CapabilityInject(ITerrainProvider.class)
    public static final Capability<ITerrainProvider> TERRAIN_CAP = null;
}
