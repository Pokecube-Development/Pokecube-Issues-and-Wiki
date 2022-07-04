package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FillerProcessor extends StructureProcessor
{
    public static final Codec<StructureProcessor> CODEC;

    public static final StructureProcessor PROCESSOR = new FillerProcessor();

    public FillerProcessor()
    {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(final LevelReader worldReaderIn, final BlockPos pos, final BlockPos otherPos,
            final StructureTemplate.StructureBlockInfo old, final StructureTemplate.StructureBlockInfo blockInfo,
            final StructurePlaceSettings placementSettingsIn, final StructureTemplate ref)
    {
        return worldReaderIn.isEmptyBlock(pos) ? blockInfo : null;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.FILLER.get();
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return FillerProcessor.PROCESSOR;
        });
    }
}
