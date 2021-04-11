package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class CorruptedLeavesBlock extends LeavesBlock
{
    public CorruptedLeavesBlock()
    {
        super(Properties.of(Material.LEAVES, MaterialColor.COLOR_BLACK).sound(SoundType.SOUL_SAND).strength(0.5f).noOcclusion());
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(16) == 0) {
            BlockPos blockpos = pos.above();
            if (worldIn.isEmptyBlock(blockpos)) {
                double d0 = (double)pos.getX() + rand.nextDouble();
                double d1 = (double)pos.getY() + (rand.nextDouble() + 1.0D);
                double d2 = (double)pos.getZ() + rand.nextDouble();
                worldIn.addParticle(ParticleTypes.SOUL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
