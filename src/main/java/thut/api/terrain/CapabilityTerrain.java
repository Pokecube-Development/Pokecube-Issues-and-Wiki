package thut.api.terrain;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.ThutCaps;

public class CapabilityTerrain
{
    public static class DefaultProvider implements ITerrainProvider, ICapabilityProvider, INBTSerializable<CompoundNBT>
    {
        private final LazyOptional<ITerrainProvider> holder   = LazyOptional.of(() -> this);
        private BlockPos                             pos;
        private IChunk                               chunk;
        private final TerrainSegment[]               segments = new TerrainSegment[16];

        private final boolean[] real = new boolean[16];

        Mutable mutable = new Mutable();

        public DefaultProvider()
        {
            this.chunk = null;
        }

        public DefaultProvider(final IChunk chunk)
        {
            this.chunk = chunk;
        }

        @Override
        public ITerrainProvider setChunk(final IChunk chunk)
        {
            if (this.chunk == null) this.chunk = chunk;
            return this;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            final BlockPos pos = this.getChunkPos();
            final int x = pos.getX();
            final int z = pos.getZ();
            final Int2IntMap toUpdate = new Int2IntOpenHashMap();
            final ListNBT tags = (ListNBT) nbt.get("ids");
            for (int i = 0; i < tags.size(); i++)
            {
                final CompoundNBT tag = tags.getCompound(i);
                final String name = tag.getString("name");
                final int id = tag.getInt("id");
                final BiomeType type = BiomeType.getBiome(name, true);
                final int newId = type.getType();
                if (newId != id) toUpdate.put(id, type.getType());
            }
            final boolean hasReplacements = !toUpdate.isEmpty();
            for (int i = 0; i < 16; i++)
            {
                CompoundNBT terrainTag = null;
                terrainTag = nbt.getCompound(i + "");
                TerrainSegment t = null;
                if (!terrainTag.isEmpty() && !TerrainSegment.noLoad)
                {
                    t = new TerrainSegment(x, i, z);
                    if (hasReplacements) t.idReplacements = toUpdate;
                    TerrainSegment.readFromNBT(t, terrainTag);
                    this.setTerrainSegment(t, i);
                    t.idReplacements = null;
                    this.real[i] = true;
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
            return ThutCaps.TERRAIN_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public BlockPos getChunkPos()
        {
            if (this.pos == null) this.pos = new BlockPos(this.chunk.getPos().x, 0, this.chunk.getPos().z);
            return this.pos;
        }

        @Override
        public TerrainSegment getTerrainSegment(final BlockPos blockLocation)
        {
            final int chunkY = blockLocation.getY();
            final TerrainSegment segment = this.getTerrainSegment(chunkY);
            return segment;
        }

        @Override
        public TerrainSegment getTerrainSegment(int chunkY)
        {
            chunkY &= 15;
            if (this.real[chunkY]) return this.segments[chunkY];

            // The pos for this segment
            this.mutable.set(this.chunk.getPos().x, chunkY, this.chunk.getPos().z);
            final BlockPos pos = this.mutable;

            // Try to pull it from our array
            TerrainSegment ret = this.segments[chunkY];
            // try to find any cached variants if they exist
            final TerrainSegment cached = thut.api.terrain.ITerrainProvider.removeCached(((World) this.chunk
                    .getWorldForge()).dimension(), this.chunk.getPos(), chunkY);

            // If not found, make a new one, or use cached
            if (ret == null)
            {
                if (cached != null) ret = this.segments[chunkY] = cached;
                else ret = this.segments[chunkY] = new TerrainSegment(pos.getX(), pos.getY(), pos.getZ());
            }
            // If there is a cached version, lets merge over into it.
            else if (cached != null) for (int i = 0; i < cached.biomes.length; i++)
                if (ret.biomes[i] == -1) ret.biomes[i] = cached.biomes[i];

            // Let the segment know what chunk it goes with, and that it is
            // actually real.
            ret.chunk = this.chunk;
            ret.real = true;
            this.real[chunkY] = true;

            return ret;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            final IntSet ids = new IntOpenHashSet();
            for (int i = 0; i < 16; i++)
            {
                final TerrainSegment t = this.getTerrainSegment(i);
                if (t == null) continue;
                if (!t.toSave) continue;
                for (final int id : t.biomes)
                    ids.add(id);
                final CompoundNBT terrainTag = new CompoundNBT();
                t.saveToNBT(terrainTag);
                nbt.put("" + i, terrainTag);
            }
            final ListNBT biomeList = new ListNBT();
            for (final BiomeType t : BiomeType.values())
            {
                if (!ids.contains(t.getType())) continue;
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

        TerrainSegment getTerrainSegment(BlockPos blockLocation);

        TerrainSegment getTerrainSegment(int chunkY);

        void setTerrainSegment(TerrainSegment segment, int chunkY);

        ITerrainProvider setChunk(final IChunk chunk);
    }

    public static class Storage implements Capability.IStorage<ITerrainProvider>
    {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<ITerrainProvider> capability, final ITerrainProvider instance,
                final Direction side, final INBT base)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(base);
        }

        @Override
        public INBT writeNBT(final Capability<ITerrainProvider> capability, final ITerrainProvider instance,
                final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }
    }
}
