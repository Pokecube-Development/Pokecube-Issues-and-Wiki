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

public class UltraTorch3 extends TorchBlock
{
    public UltraTorch3() {
		super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0f),
				ParticleTypes.CAMPFIRE_COSY_SMOKE);
	}

	@OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.8D;
        double d2 = (double)pos.getZ() + 0.5D;
        worldIn.addParticle(ParticleTypes.SQUID_INK, d0, d1, d2, 0.0D, 0.3D, 0.0D);
    }
}
