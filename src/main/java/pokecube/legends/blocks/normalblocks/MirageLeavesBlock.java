package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
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
    int particleSpawnSpeed;

    public MirageLeavesBlock(int particleSpawnSpeed, final Properties properties)
    {
        super(properties);
        this.particleSpawnSpeed = particleSpawnSpeed;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final RandomSource random)
    {
        super.animateTick(state, world, pos, random);

        if (random.nextInt(particleSpawnSpeed) == 0) {
            final BlockPos posBelow = pos.below();
            BlockState blockstate = world.getBlockState(posBelow);
            if (!isFaceFull(blockstate.getCollisionShape(world, posBelow), Direction.UP))
            {
                ParticleUtils.spawnParticleBelow(world, pos, random, ParticleInit.MIRAGE_LEAF.get());
            }
        }
    }
}
