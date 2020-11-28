package pokecube.core.world.gen.template;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class ExtendedRuleProcessor extends RuleStructureProcessor
{
    private final ImmutableList<RuleEntry> rules;

    public ExtendedRuleProcessor(final ImmutableList<RuleEntry> rules)
    {
        super(rules);
        this.rules = rules;
    }

    @Override
    public BlockInfo process(final IWorldReader worldReaderIn, final BlockPos pos, final BlockInfo p_215194_3_,
            final BlockInfo blockInfo, final PlacementSettings placementSettingsIn)
    {
        final Random random = new Random(MathHelper.getPositionRandom(blockInfo.pos));
        final BlockState blockstate = worldReaderIn.getBlockState(blockInfo.pos);
        final BlockState state_below = worldReaderIn.getBlockState(blockInfo.pos.down());
        for (final RuleEntry ruleentry : this.rules)
        {
            if (ruleentry.test(blockInfo.state, blockstate, random)) return new Template.BlockInfo(blockInfo.pos,
                    ruleentry.getOutputState(), ruleentry.getOutputNbt());
            if (ruleentry.test(blockInfo.state, state_below, random)) return new Template.BlockInfo(blockInfo.pos,
                    ruleentry.getOutputState(), ruleentry.getOutputNbt());
        }
        return blockInfo;
    }
}
