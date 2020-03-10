package pokecube.core.world.gen.template;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class NotRuleProcessor extends RuleStructureProcessor
{
    private final ImmutableList<RuleEntry> rules;

    public NotRuleProcessor(final ImmutableList<RuleEntry> rules)
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
        if (blockstate != null && blockstate.getBlock() != Blocks.AIR) for (final RuleEntry ruleentry : this.rules)
            if (!ruleentry.test(blockInfo.state, blockstate, random))
            {
                final BlockState output = ruleentry.getOutputState();
                if (output == null || output.getBlock() == Blocks.STRUCTURE_VOID) return null;
                return new Template.BlockInfo(blockInfo.pos, output, ruleentry.getOutputNbt());

            }
        return blockInfo;
    }
}
