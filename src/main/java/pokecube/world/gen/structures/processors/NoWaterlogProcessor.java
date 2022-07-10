package pokecube.world.gen.structures.processors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class NoWaterlogProcessor extends StructureProcessor
{
    public static final Codec<NoWaterlogProcessor> CODEC;
    public static final NoWaterlogProcessor PROCESSOR = new NoWaterlogProcessor();

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(final LevelReader level, final BlockPos pos_a,
            final BlockPos pos_b, final StructureTemplate.StructureBlockInfo raw,
            final StructureTemplate.StructureBlockInfo mod, final StructurePlaceSettings settings)
    {
        return mod;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.NOWATERLOG.get();
    }

    static
    {
        CODEC = Codec.unit(() -> {
            return NoWaterlogProcessor.PROCESSOR;
        });
    }
}
