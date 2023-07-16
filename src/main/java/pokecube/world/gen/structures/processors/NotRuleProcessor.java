package pokecube.world.gen.structures.processors;

import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class NotRuleProcessor extends RuleProcessor
{
    public static final Codec<NotRuleProcessor> CODEC;

    private final List<ProcessorRule> rules;

    public NotRuleProcessor(final List<ProcessorRule> rules)
    {
        super(rules);
        this.rules = rules;
    }

    @Override
    public StructureBlockInfo processBlock(final LevelReader worldReaderIn, final BlockPos pos, final BlockPos pos2,
            final StructureBlockInfo blockInfo1, final StructureBlockInfo blockInfo2, final StructurePlaceSettings placementSettingsIn)
    {
        final Random random = new Random(Mth.getSeed(blockInfo2.pos));
        final BlockState blockstate = worldReaderIn.getBlockState(blockInfo2.pos);
        if (blockstate != null && blockstate.getBlock() != Blocks.AIR) for (final ProcessorRule ruleentry : this.rules)
            if (!ruleentry.test(blockInfo2.state, blockstate, blockInfo1.pos, blockInfo2.pos, pos2, random))
            {
                final BlockState output = ruleentry.getOutputState();
                if (output == null || output.getBlock() == Blocks.STRUCTURE_VOID) return null;
                return new StructureTemplate.StructureBlockInfo(blockInfo2.pos, output, ruleentry.getOutputTag());

            }
        return blockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.NOTRULE.get();
    }

    static
    {
        CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(NotRuleProcessor::new, (p_237126_0_) ->
        {
            return p_237126_0_.rules;
        }).codec();
    }
}
