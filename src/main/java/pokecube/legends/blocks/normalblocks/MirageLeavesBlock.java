package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirageLeavesBlock extends LeavesBlock
{
    public MirageLeavesBlock(final int color, final Properties properties)
    {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState stateIn, final Level worldIn, final BlockPos pos, final Random rand)
    {
        if (rand.nextInt(3) == 0) {
            final BlockPos blockpos = pos.below();
            if (worldIn.isEmptyBlock(blockpos)) {
                final double d0 = pos.getX() + rand.nextDouble();
                final double d1 = pos.getY() - 0.05D;
                final double d2 = pos.getZ() + rand.nextDouble();
                worldIn.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, stateIn), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
