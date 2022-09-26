package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureFeatureManager;

@Mixin(WorldGenRegion.class)
public interface WorldGenRegionAccessor
{
    @Accessor("structureFeatureManager")
    StructureFeatureManager getStructureFeatureManager();

    @Accessor("level")
    ServerLevel getServerLevel();
}
