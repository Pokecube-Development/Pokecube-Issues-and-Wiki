package pokecube.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.TriState;

import javax.annotation.Nullable;
import java.util.Random;

public class StructureEvent extends Event
{
    public static class PickLocation extends StructureEvent implements ICancellableEvent
    {
        public final ChunkGenerator chunkGen;
        public final Random rand;
        public final ChunkPos pos;

        public final LevelHeightAccessor heightAccessor;

        private final ResourceKey<Level> key;

        public PickLocation(final ChunkGenerator chunkGen, final Random rand, final ChunkPos pos,
                final LevelHeightAccessor heightAccessor)
        {
            this.chunkGen = chunkGen;
            this.rand = rand;
            this.pos = pos;
            this.heightAccessor = heightAccessor;
            this.key = Level.OVERWORLD;
        }

        public ResourceKey<Level> getDimensionKey()
        {
            return this.key;
        }
    }

    public static class BuildStructure extends StructureEvent
    {
        private final BoundingBox bounds;
        private final StructurePlaceSettings settings;
        private final String structure;
        private String structureOverride;
        private final LevelAccessor world;
        private final WorldGenLevel worldGen;

        public BuildStructure(final BoundingBox bounds, final WorldGenLevel world, final String name,
                final StructurePlaceSettings settings)
        {
            this.structure = name;
            this.worldGen = world;
            this.world = world.getLevel();
            this.settings = settings;
            this.bounds = bounds;
        }

        public String getBiomeType()
        {
            return this.structureOverride;
        }

        public BoundingBox getBoundingBox()
        {
            return this.bounds;
        }

        public StructurePlaceSettings getSettings()
        {
            return this.settings;
        }

        public String getStructure()
        {
            return this.structure;
        }

        public LevelAccessor getWorld()
        {
            return this.world;
        }

        public void setBiomeType(final String structureOverride)
        {
            this.structureOverride = structureOverride;
        }

        public WorldGenLevel getWorldGen()
        {
            return worldGen;
        }
    }

    public static class SpawnEntity extends StructureEvent
    {
        private StructureEntityInfo info;
        private final StructureEntityInfo raw;
        public final LevelReader worldBlocks;
        public final BlockPos pos;

        public SpawnEntity(final StructureEntityInfo entity, final StructureEntityInfo raw, LevelReader world,
                BlockPos pos)
        {
            this.info = entity;
            this.raw = raw;
            this.worldBlocks = world;
            this.pos = pos;
        }

        public StructureEntityInfo getRawInfo()
        {
            return this.raw;
        }

        public StructureEntityInfo getInfo()
        {
            return this.info;
        }

        public void setInfo(StructureEntityInfo info)
        {
            this.info = info;
        }
    }

    /**
     * This event should be given result of ALLOW if something is done.
     */
    public static class ReadTag extends StructureEvent implements ICancellableEvent
    {
        public String function;
        public LevelAccessor worldBlocks;
        public ServerLevel worldActual;
        public BlockPos pos;
        public BoundingBox sbb;
        public RandomSource rand;
        public boolean duringWorldgen;
        public CompoundTag nbt;
        @Nullable
        public StructureTemplate.StructureBlockInfo info;

        public ReadTag(String function, BlockPos pos, LevelAccessor worldIn, ServerLevel world, RandomSource rand,
                BoundingBox sbb, boolean duringWorldgen)
        {
            this.function = function;
            this.worldBlocks = worldIn;
            this.worldActual = world;
            this.pos = pos;
            this.sbb = sbb;
            this.rand = rand;
            this.duringWorldgen = duringWorldgen;
            this.nbt = new CompoundTag();
            this.nbt.putString("pokecube:spawn_function", function);
        }

        private TriState result = TriState.DEFAULT;

        public void setResult(TriState result)
        {
            this.result = result;
        }

        public TriState getResult()
        {
            return this.result;
        }
    }
}
