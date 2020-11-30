package pokecube.core.events;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template.EntityInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.worldgen.WorldgenHandler.JigSawConfig;

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

        public PickLocation(final ChunkGenerator chunkGen, final Random rand, final int chunkPosX, final int chunkPosZ,
                final JigSawConfig struct)
        {
            this.chunkGen = chunkGen;
            this.rand = rand;
            this.chunkPosX = chunkPosX;
            this.chunkPosZ = chunkPosZ;
            this.struct = struct;
        }
    }

    public static class BuildStructure extends StructureEvent
    {
        private final MutableBoundingBox bounds;
        private final PlacementSettings  settings;
        private final String             structure;
        private String                   structureOverride;
        private final IWorld             world;

        public BuildStructure(final MutableBoundingBox bounds, final IWorld world, final String name,
                final PlacementSettings settings)
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

        public MutableBoundingBox getBoundingBox()
        {
            return this.bounds;
        }

        public PlacementSettings getSettings()
        {
            return this.settings;
        }

        public String getStructure()
        {
            return this.structure;
        }

        public IWorld getWorld()
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
        private final EntityInfo info;
        private final EntityInfo raw;

        public SpawnEntity(final EntityInfo entity, final EntityInfo raw)
        {
            this.info = entity;
            this.raw = raw;
        }

        public EntityInfo getRawInfo()
        {
            return this.raw;
        }

        public EntityInfo getInfo()
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
        public IWorld             worldBlocks;
        public ServerWorld        worldActual;
        public BlockPos           pos;
        public MutableBoundingBox sbb;
        public Random             rand;

        public ReadTag(final String function, final BlockPos pos, final IWorld worldIn, final ServerWorld world,
                final Random rand, final MutableBoundingBox sbb)
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
