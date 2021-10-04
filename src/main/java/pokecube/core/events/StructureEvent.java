package pokecube.core.events;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;
import pokecube.core.world.gen.jigsaw.JigsawAssmbler;

public class StructureEvent extends Event
{
    @Cancelable
    public static class PickLocation extends StructureEvent
    {
        public final ChunkGenerator chunkGen;
        public final Random         rand;
        public final int            chunkPosX;
        public final int            chunkPosZ;
        public final JigSawConfig   struct;

        private ResourceKey<Level> key;

        public PickLocation(final ChunkGenerator chunkGen, final Random rand, final int chunkPosX, final int chunkPosZ,
                final JigSawConfig struct)
        {
            this.chunkGen = chunkGen;
            this.rand = rand;
            this.chunkPosX = chunkPosX;
            this.chunkPosZ = chunkPosZ;
            this.struct = struct;
            final Level world = JigsawAssmbler.getForGen(chunkGen);
            if(world!=null) this.key = world.dimension();
            else this.key = Level.OVERWORLD;
        }

        public ResourceKey<Level> getDimensionKey()
        {
            return this.key;
        }
    }

    public static class BuildStructure extends StructureEvent
    {
        private final BoundingBox bounds;
        private final StructurePlaceSettings  settings;
        private final String             structure;
        private String                   structureOverride;
        private final LevelAccessor             world;

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
        public String             function;
        public LevelAccessor             worldBlocks;
        public ServerLevel        worldActual;
        public BlockPos           pos;
        public BoundingBox sbb;
        public Random             rand;

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
