package thut.api.level.terrain;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CapabilityTerrain
{
    public static class DefaultProvider implements ITerrainProvider
    {
        private BlockPos pos;
        private ChunkAccess chunk;

        Int2ObjectArrayMap<TerrainSegment> segMap = new Int2ObjectArrayMap<>();

        MutableBlockPos mutable = new MutableBlockPos();

        public DefaultProvider(final ChunkAccess chunk)
        {
            this.chunk = chunk;
        }

        @Override
        public ITerrainProvider setChunk(final ChunkAccess chunk)
        {
            if (this.chunk == null && chunk != null) this.chunk = chunk;
            return this;
        }

        @Override
        public void apply(Consumer<TerrainSegment> applier)
        {
            segMap.values().forEach(applier);
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, final CompoundTag nbt)
        {
            if (nbt.contains("segs"))
            {
                final BlockPos pos = this.getChunkPos();
                final int x = pos.getX();
                final int z = pos.getZ();
                final Int2IntMap toUpdate = new Int2IntOpenHashMap();
                ListTag tags = (ListTag) nbt.get("ids");
                for (int i = 0; i < tags.size(); i++)
                {
                    final CompoundTag tag = tags.getCompound(i);
                    final String name = tag.getString("name");
                    final int id = tag.getInt("id");
                    final BiomeType type = BiomeType.getBiome(name, true);
                    final int newId = type.getType();
                    if (newId != id) toUpdate.put(id, type.getType());
                }
                final boolean hasReplacements = !toUpdate.isEmpty();
                tags = (ListTag) nbt.get("segs");
                for (int i = 0; i < tags.size(); i++)
                {
                    TerrainSegment t;
                    final CompoundTag terrainTag = tags.getCompound(i);
                    if (!terrainTag.isEmpty() && !TerrainSegment.noLoad)
                    {
                        final int y = terrainTag.getInt("y");
                        t = new TerrainSegment(x, y, z);
                        t.chunk = this.chunk;
                        if (hasReplacements) t.idReplacements = toUpdate;
                        TerrainSegment.readFromNBT(t, terrainTag);
                        this.setTerrainSegment(t, y);
                        t.idReplacements = null;
                    }
                }
                this.chunk.setUnsaved(true);

            }
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
            final int chunkY = SectionPos.blockToSectionCoord(blockLocation.getY());
            return this.getTerrainSegment(chunkY);
        }

        @Override
        public TerrainSegment getTerrainSegment(final int chunkY)
        {
            if (this.segMap.containsKey(chunkY))
            {
                final TerrainSegment ret = this.segMap.get(chunkY);
                ret.chunk = this.chunk;
                return ret;
            }
            // The pos for this segment
            this.mutable.set(this.chunk.getPos().x, chunkY, this.chunk.getPos().z);
            // Try to pull it from our array
            TerrainSegment ret = new TerrainSegment(mutable.getX(), mutable.getY(), mutable.getZ());
            ret.chunk = this.chunk;
            this.segMap.put(chunkY, ret);
            return ret;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries)
        {
            final CompoundTag nbt = new CompoundTag();
            final IntSet ids = new IntOpenHashSet();
            final ListTag segs = new ListTag();
            for (final int i : this.segMap.keySet())
            {
                final TerrainSegment t = this.getTerrainSegment(i);
                if (t == null) continue;
                t.checkToSave();
                final CompoundTag terrainTag = new CompoundTag();
                t.saveToNBT(terrainTag);
                if (!terrainTag.isEmpty())
                {
                    for (final int id : t.biomes) ids.add(id);
                    segs.add(terrainTag);
                }
            }
            if (!segs.isEmpty())
            {
                nbt.put("segs", segs);
                final ListTag biomeList = new ListTag();
                for (final BiomeType t : BiomeType.values())
                {
                    if (!ids.contains(t.getType())) continue;
                    final CompoundTag tag = new CompoundTag();
                    tag.putString("name", t.name);
                    tag.putInt("id", t.getType());
                    biomeList.add(tag);
                }
                if (!ids.isEmpty()) nbt.put("ids", biomeList);
            }
            return nbt;
        }

        @Override
        public void setTerrainSegment(final TerrainSegment segment, final int chunkY)
        {
            this.segMap.put(chunkY, segment);
        }
    }

    public static interface ITerrainProvider extends INBTSerializable<CompoundTag>
    {
        BlockPos getChunkPos();

        TerrainSegment getTerrainSegment(BlockPos blockLocation);

        TerrainSegment getTerrainSegment(int chunkY);

        void setTerrainSegment(TerrainSegment segment, int chunkY);

        ITerrainProvider setChunk(final ChunkAccess chunk);

        void apply(Consumer<TerrainSegment> applier);
    }

    public static ITerrainProvider makeProvider(final IAttachmentHolder in)
    {
        if (!(in instanceof ChunkAccess chunk)) return null;
        return new DefaultProvider(chunk);
    }

    public static ITerrainProvider get(final IAttachmentHolder in)
    {
        return in.getData(TYPE_SAVE.get());
    }

    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:terrain");

    public static Supplier<AttachmentType<ITerrainProvider>> TYPE_SAVE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(),
                () -> AttachmentType.serializable(CapabilityTerrain::makeProvider).build());
    }
}
