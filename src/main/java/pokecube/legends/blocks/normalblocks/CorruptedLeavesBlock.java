package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.handlers.ItemGenerator;
import pokecube.legends.init.ParticleInit;

public class CorruptedLeavesBlock extends LeavesBlock
{
    public CorruptedLeavesBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random)
    {
        if (random.nextInt(16) == 0)
        {
            BlockPos pos1 = pos.above();
            if (world.isEmptyBlock(pos1))
            {
                double d0 = (double)pos.getX() + random.nextDouble();
                double d1 = (double)pos.getY() + (random.nextDouble() + 1.0D);
                double d2 = (double)pos.getZ() + random.nextDouble();
                world.addParticle(ParticleInit.INFECTED_SOUL.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
