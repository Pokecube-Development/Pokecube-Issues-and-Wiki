package pokecube.world.gen.structures.processors;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
import pokecube.api.events.StructureEvent;
import pokecube.core.utils.LevelSpawnData;
import thut.core.common.ThutCore;

import javax.annotation.Nullable;

public class PokecubeStructureProcessor extends StructureProcessor
{
    public static final MapCodec<StructureProcessor> CODEC;

    public static final StructureProcessor PROCESSOR = new PokecubeStructureProcessor();

    public PokecubeStructureProcessor()
    {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(final LevelReader world, final BlockPos pos1,
            final BlockPos pos2, final StructureTemplate.StructureBlockInfo rawInfo,
            final StructureTemplate.StructureBlockInfo modInfo, final StructurePlaceSettings settings,
            @Nullable final StructureTemplate template)
    {
        return modInfo;
    }

    @Override
    public StructureEntityInfo processEntity(final LevelReader world, final BlockPos seedPos,
            final StructureEntityInfo rawEntityInfo, StructureEntityInfo entityInfo,
            final StructurePlaceSettings placementSettings, final StructureTemplate template)
    {
        final BlockPos blockpos = StructureTemplate.calculateRelativePosition(placementSettings, rawEntityInfo.blockPos)
                .offset(seedPos);
        final StructureEvent.SpawnEntity event = new StructureEvent.SpawnEntity(entityInfo, rawEntityInfo, world,
                blockpos);
        ThutCore.FORGE_BUS.post(event);
        entityInfo = event.getInfo();
        if (world instanceof WorldGenLevel level)
        {
            var nbt = entityInfo.nbt.copy();
            nbt.putDouble("__x", entityInfo.pos.x);
            nbt.putDouble("__y", entityInfo.pos.y);
            nbt.putDouble("__z", entityInfo.pos.z);
            nbt.putInt("__rot", placementSettings.getRotation().ordinal());
            nbt.putInt("__mir", placementSettings.getMirror().ordinal());
            LevelSpawnData.getForLevel(level.getLevel()).add(blockpos, nbt);
        }
        return entityInfo;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.STRUCTS.get();
    }

    static
    {
        CODEC = MapCodec.unit(() -> PokecubeStructureProcessor.PROCESSOR);
    }
}
