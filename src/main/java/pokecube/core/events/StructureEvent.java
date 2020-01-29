package pokecube.core.events;

import java.util.Random;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template.EntityInfo;
import net.minecraftforge.eventbus.api.Event;

public class StructureEvent extends Event
{
    public static class BuildStructure extends StructureEvent
    {
        private final MutableBoundingBox bounds;
        private final PlacementSettings  settings;
        private final String             structure;
        private String                   structureOverride;
        private final World              world;

        public BuildStructure(final BlockPos pos, final World world, final String name, final BlockPos size,
                final PlacementSettings settings)
        {
            this.structure = name;
            this.world = world;
            this.settings = settings;
            Direction dir = Direction.SOUTH;
            if (settings.getMirror() != null) dir = settings.getMirror().mirror(dir);
            if (settings.getRotation() != null) dir = settings.getRotation().rotate(dir);
            this.bounds = MutableBoundingBox.getComponentToAddBoundingBox(pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0,
                    size.getX(), size.getY(), size.getZ(), dir);
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

        public World getWorld()
        {
            return this.world;
        }

        public void seBiomeType(final String structureOverride)
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

    public static class ReadTag extends StructureEvent
    {
        public String             function;
        public IWorld             world;
        public BlockPos           pos;
        public MutableBoundingBox sbb;
        public Random             rand;

        public ReadTag(final String function, final BlockPos pos, final IWorld worldIn, final Random rand,
                final MutableBoundingBox sbb)
        {
            this.function = function;
            this.world = worldIn;
            this.pos = pos;
            this.sbb = sbb;
            this.rand = rand;
        }
    }
}
