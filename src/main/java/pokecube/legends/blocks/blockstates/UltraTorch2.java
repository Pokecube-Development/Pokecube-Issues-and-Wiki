package pokecube.legends.blocks.blockstates;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraTorch2 extends TorchBlock
{
    public UltraTorch2() {
		super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0f).lightValue(14));
	}

	@OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;
        worldIn.addParticle(ParticleTypes.TOTEM_OF_UNDYING, d0, d1, d2, 0.0D, 0.2D, 0.0D);
    }
}
