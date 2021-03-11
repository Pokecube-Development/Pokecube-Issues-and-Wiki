package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

public class MarkerToAirProcessor extends StructureProcessor
{
    public static final Codec<MarkerToAirProcessor> CODEC;
    public static final MarkerToAirProcessor        PROCESSOR = new MarkerToAirProcessor();

    @Override
    @Nullable
    public Template.BlockInfo processBlock(final IWorldReader p_230386_1_, final BlockPos p_230386_2_,
            final BlockPos p_230386_3_, final Template.BlockInfo raw, final Template.BlockInfo mod,
            final PlacementSettings p_230386_6_)
    {
        return mod.state.getBlock() == Blocks.STRUCTURE_BLOCK ? new Template.BlockInfo(mod.pos, Blocks.AIR
                .defaultBlockState(), mod.nbt) : mod;
    }

    @Override
    protected IStructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.MARKERAIR;
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return MarkerToAirProcessor.PROCESSOR;
        });
    }
}
