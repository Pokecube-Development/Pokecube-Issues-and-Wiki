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

public class CorruptedLeaveBlock extends LeavesBlock
{
    public CorruptedLeaveBlock()
    {
        super(Properties.of(Material.LEAVES, MaterialColor.COLOR_BLACK).sound(SoundType.SOUL_SAND).strength(1, 2).noOcclusion());
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
    	if (rand.nextInt(3) == 0) {
          BlockPos blockpos = pos.below();
          if (worldIn.isEmptyBlock(blockpos)) {
             double d0 = (double)pos.getX() + rand.nextDouble();
             double d1 = (double)pos.getY() - 0.05D;
             double d2 = (double)pos.getZ() + rand.nextDouble();
             worldIn.addParticle(ParticleTypes.DRIPPING_LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
          }
       }
    }
}
