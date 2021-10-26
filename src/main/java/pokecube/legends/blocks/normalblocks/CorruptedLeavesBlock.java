package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.handlers.ItemGenerator;

public class CorruptedLeavesBlock extends LeavesBlock
{
    public CorruptedLeavesBlock()
    {
        super(Properties.of(Material.LEAVES, MaterialColor.COLOR_GRAY).sound(SoundType.SOUL_SAND).strength(0.5f).noOcclusion()
            .isSuffocating((s, r, p)-> false).isValidSpawn(ItemGenerator::ocelotOrParrot).isViewBlocking((s, r, p) -> false));
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
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
