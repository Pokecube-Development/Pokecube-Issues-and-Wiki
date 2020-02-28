package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class FillerProcessor extends StructureProcessor
{
    public static IStructureProcessorType  TYPE;
    public static final StructureProcessor PROCESSOR = new FillerProcessor();

    public FillerProcessor()
    {
    }

    public FillerProcessor(final Dynamic<?> p_deserialize_1_)
    {
    }

    @Override
    @Nullable
    public Template.BlockInfo process(final IWorldReader worldReaderIn, final BlockPos pos,
            final Template.BlockInfo old, final Template.BlockInfo blockInfo,
            final PlacementSettings placementSettingsIn)
    {
        return worldReaderIn.getBlockState(pos).isAir(worldReaderIn, pos) ? blockInfo : null;
    }

    @Override
    protected IStructureProcessorType getType()
    {
        return FillerProcessor.TYPE;
    }

    @Override
    protected <T> Dynamic<T> serialize0(final DynamicOps<T> ops)
    {
        return new Dynamic<>(ops, ops.emptyMap());
    }

}
