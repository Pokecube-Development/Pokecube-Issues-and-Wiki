package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.eventbus.api.Cancelable;
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

        public BuildStructure(BlockPos pos, World world, String name, BlockPos size, PlacementSettings settings)
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

        public void seBiomeType(String structureOverride)
        {
            this.structureOverride = structureOverride;
        }
    }

    @Cancelable
    public static class SpawnEntity extends StructureEvent
    {
        private Entity       toSpawn;
        private final String structure;
        private final Entity original;

        public SpawnEntity(Entity entity, String structure)
        {
            this.structure = structure;
            this.original = entity;
            this.toSpawn = this.original;
        }

        public Entity getEntity()
        {
            return this.original;
        }

        public String getStructureName()
        {
            return this.structure;
        }

        public Entity getToSpawn()
        {
            return this.toSpawn;
        }

        public void setToSpawn(Entity entity)
        {
            this.toSpawn = entity;
        }
    }
}
