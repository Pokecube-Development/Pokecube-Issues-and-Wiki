package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class FillerProcessor extends StructureProcessor
{
    public static final Codec<StructureProcessor> CODEC;

    public static final StructureProcessor PROCESSOR = new FillerProcessor();

    public FillerProcessor()
    {
    }

    @Override
    @Nullable
    public Template.BlockInfo process(final IWorldReader worldReaderIn, final BlockPos pos, final BlockPos otherPos,
            final Template.BlockInfo old, final Template.BlockInfo blockInfo,
            final PlacementSettings placementSettingsIn, final Template ref)
    {
        return worldReaderIn.isEmptyBlock(pos) ? blockInfo : null;
    }

    @Override
    protected IStructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.FILLER;
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return FillerProcessor.PROCESSOR;
        });
    }
}
