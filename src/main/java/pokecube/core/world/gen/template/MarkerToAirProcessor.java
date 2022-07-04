package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class MarkerToAirProcessor extends StructureProcessor
{
    public static final Codec<MarkerToAirProcessor> CODEC;
    public static final MarkerToAirProcessor        PROCESSOR = new MarkerToAirProcessor();

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(final LevelReader p_230386_1_, final BlockPos p_230386_2_,
            final BlockPos p_230386_3_, final StructureTemplate.StructureBlockInfo raw, final StructureTemplate.StructureBlockInfo mod,
            final StructurePlaceSettings p_230386_6_)
    {
        return mod.state.getBlock() == Blocks.STRUCTURE_BLOCK ? new StructureTemplate.StructureBlockInfo(mod.pos, Blocks.AIR
                .defaultBlockState(), mod.nbt) : mod;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.MARKERAIR.get();
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return MarkerToAirProcessor.PROCESSOR;
        });
    }
}
