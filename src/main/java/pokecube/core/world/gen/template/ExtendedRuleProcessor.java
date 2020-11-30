package pokecube.core.world.gen.template;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class ExtendedRuleProcessor extends RuleStructureProcessor
{
    private final ImmutableList<RuleEntry> rules;

    public ExtendedRuleProcessor(final List<RuleEntry> rules)
    {
        super(rules);
        this.rules = ImmutableList.copyOf(rules);
    }

    @Override
    public BlockInfo func_230386_a_(final IWorldReader worldReaderIn, final BlockPos pos, final BlockPos pos2,
            final BlockInfo blockInfo1, final BlockInfo blockInfo2, final PlacementSettings placementSettingsIn)
    {
        final Random random = new Random(MathHelper.getPositionRandom(blockInfo2.pos));
        final BlockState blockstate = worldReaderIn.getBlockState(blockInfo2.pos);
        final BlockState state_below = worldReaderIn.getBlockState(blockInfo2.pos.down());
        for (final RuleEntry ruleentry : this.rules)
        {
            if (ruleentry.func_237110_a_(blockInfo2.state, blockstate, blockInfo1.pos, blockInfo2.pos, pos2, random))
                return new Template.BlockInfo(blockInfo2.pos, ruleentry.getOutputState(), ruleentry.getOutputNbt());
            if (ruleentry.func_237110_a_(blockInfo2.state, state_below, blockInfo1.pos, blockInfo2.pos, pos2, random))
                return new Template.BlockInfo(blockInfo2.pos, ruleentry.getOutputState(), ruleentry.getOutputNbt());
        }
        return blockInfo2;
    }

    @Override
    protected IStructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.EXTENDED;
    }
}
