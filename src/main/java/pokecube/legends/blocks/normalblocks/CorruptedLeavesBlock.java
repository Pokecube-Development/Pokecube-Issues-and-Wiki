package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.ParticleInit;

public class CorruptedLeavesBlock extends LeavesBlock
{
    public CorruptedLeavesBlock(final BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final Random random)
    {
        if (random.nextInt(16) == 0)
        {
            final BlockPos pos1 = pos.above();
            if (world.isEmptyBlock(pos1))
            {
                final double d0 = pos.getX() + random.nextDouble();
                final double d1 = pos.getY() + (random.nextDouble() + 1.0D);
                final double d2 = pos.getZ() + random.nextDouble();
                world.addParticle(ParticleInit.INFECTED_SOUL.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
