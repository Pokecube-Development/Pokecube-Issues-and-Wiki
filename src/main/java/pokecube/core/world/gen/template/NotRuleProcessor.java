package pokecube.core.world.gen.template;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
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
    public BlockInfo func_230386_a_(final IWorldReader worldReaderIn, final BlockPos pos, final BlockPos pos2,
            final BlockInfo blockInfo1, final BlockInfo blockInfo2, final PlacementSettings placementSettingsIn)
    {
        final Random random = new Random(MathHelper.getPositionRandom(blockInfo2.pos));
        final BlockState blockstate = worldReaderIn.getBlockState(blockInfo2.pos);
        if (blockstate != null && blockstate.getBlock() != Blocks.AIR) for (final RuleEntry ruleentry : this.rules)
            if (!ruleentry.func_237110_a_(blockInfo2.state, blockstate, blockInfo1.pos, blockInfo2.pos, pos2, random))
            {
                final BlockState output = ruleentry.getOutputState();
                if (output == null || output.getBlock() == Blocks.STRUCTURE_VOID) return null;
                return new Template.BlockInfo(blockInfo2.pos, output, ruleentry.getOutputNbt());

            }
        return blockInfo2;
    }

    @Override
    protected IStructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.NOTRULE;
    }
}
