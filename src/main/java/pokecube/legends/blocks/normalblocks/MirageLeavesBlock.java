package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.ParticleInit;

public class MirageLeavesBlock extends LeavesBlock
{
    public MirageLeavesBlock(final int color, final Properties properties)
    {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final RandomSource random)
    {
        super.animateTick(state, world, pos, random);

        if (random.nextInt(3) == 0) {
            final BlockPos posBelow = pos.below();
            BlockState blockstate = world.getBlockState(posBelow);
            if (!isFaceFull(blockstate.getCollisionShape(world, posBelow), Direction.UP))
            {
                ParticleUtils.spawnParticleBelow(world, pos, random, ParticleInit.MIRAGE_LEAVES.get());
            }
        }
    }
}
