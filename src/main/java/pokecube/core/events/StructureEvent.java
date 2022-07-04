package pokecube.core.events;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.world.gen_old.WorldgenHandler.JigSawConfig;
import pokecube.world.gen_old.jigsaw.JigsawAssmbler;

public class StructureEvent extends Event
{
    @Cancelable
    public static class PickLocation extends StructureEvent
    {
        public final ChunkGenerator chunkGen;
        public final Random         rand;
        public final ChunkPos       pos;
        public final JigSawConfig   struct;

        public final LevelHeightAccessor heightAccessor;

        private ResourceKey<Level> key;

        public PickLocation(final ChunkGenerator chunkGen, final Random rand, final ChunkPos pos,
                final JigSawConfig struct, final LevelHeightAccessor heightAccessor)
        {
            this.chunkGen = chunkGen;
            this.rand = rand;
            this.pos = pos;
            this.struct = struct;
            this.heightAccessor = heightAccessor;
            final Level world = JigsawAssmbler.getForGen(chunkGen);
            if (world != null) this.key = world.dimension();
            else this.key = Level.OVERWORLD;
        }

        public ResourceKey<Level> getDimensionKey()
        {
            return this.key;
        }
    }

    public static class BuildStructure extends StructureEvent
    {
        private final BoundingBox            bounds;
        private final StructurePlaceSettings settings;
        private final String                 structure;
        private String                       structureOverride;
        private final LevelAccessor          world;

        public BuildStructure(final BoundingBox bounds, final LevelAccessor world, final String name,
                final StructurePlaceSettings settings)
        {
            this.structure = name;
            this.world = world;
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
    }

    public static class SpawnEntity extends StructureEvent
    {
        private final StructureEntityInfo info;
        private final StructureEntityInfo raw;

        public SpawnEntity(final StructureEntityInfo entity, final StructureEntityInfo raw)
        {
            this.info = entity;
            this.raw = raw;
        }

        public StructureEntityInfo getRawInfo()
        {
            return this.raw;
        }

        public StructureEntityInfo getInfo()
        {
            return this.info;
        }
    }

    @HasResult
    /**
     * This event should be given result of ALLOW if something is done.
     */
    public static class ReadTag extends StructureEvent
    {
        public String        function;
        public LevelAccessor worldBlocks;
        public ServerLevel   worldActual;
        public BlockPos      pos;
        public BoundingBox   sbb;
        public Random        rand;

        public ReadTag(final String function, final BlockPos pos, final LevelAccessor worldIn, final ServerLevel world,
                final Random rand, final BoundingBox sbb)
        {
            this.function = function;
            this.worldBlocks = worldIn;
            this.worldActual = world;
            this.pos = pos;
            this.sbb = sbb;
            this.rand = rand;
        }
    }
}
