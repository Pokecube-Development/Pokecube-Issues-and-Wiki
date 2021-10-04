package pokecube.core.world.gen.template;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class ExtendedRuleProcessor extends RuleProcessor
{
    public static final Codec<ExtendedRuleProcessor> CODEC;

    private final ImmutableList<ProcessorRule> rules;

    public ExtendedRuleProcessor(final List<ProcessorRule> rules)
    {
        super(rules);
        this.rules = ImmutableList.copyOf(rules);
    }

    @Override
    public StructureBlockInfo processBlock(final LevelReader worldReaderIn, final BlockPos pos, final BlockPos pos2,
            final StructureBlockInfo blockInfo1, final StructureBlockInfo blockInfo2, final StructurePlaceSettings placementSettingsIn)
    {
        final Random random = new Random(Mth.getSeed(blockInfo2.pos));
        final BlockState blockstate = worldReaderIn.getBlockState(blockInfo2.pos);
        final BlockState state_below = worldReaderIn.getBlockState(blockInfo2.pos.below());
        for (final ProcessorRule ruleentry : this.rules)
        {
            if (ruleentry.test(blockInfo2.state, blockstate, blockInfo1.pos, blockInfo2.pos, pos2, random))
                return new StructureTemplate.StructureBlockInfo(blockInfo2.pos, ruleentry.getOutputState(), ruleentry.getOutputTag());
            if (ruleentry.test(blockInfo2.state, state_below, blockInfo1.pos, blockInfo2.pos, pos2, random))
                return new StructureTemplate.StructureBlockInfo(blockInfo2.pos, ruleentry.getOutputState(), ruleentry.getOutputTag());
        }
        return blockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.EXTENDED;
    }

    static
    {
        CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(ExtendedRuleProcessor::new, (p_237126_0_) ->
        {
            return p_237126_0_.rules;
        }).codec();
    }
}
