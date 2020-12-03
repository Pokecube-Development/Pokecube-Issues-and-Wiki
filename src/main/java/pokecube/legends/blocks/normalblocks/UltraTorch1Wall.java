package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UltraTorch1Wall extends WallTorchBlock
{	
    public UltraTorch1Wall() {
    	super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0f),
    			ParticleTypes.CAMPFIRE_COSY_SMOKE);
	}
    
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
       Direction direction = stateIn.get(HORIZONTAL_FACING);
       double d0 = (double)pos.getX() + 0.5D;
       double d1 = (double)pos.getY() + 0.7D;
       double d2 = (double)pos.getZ() + 0.5D;
       Direction direction1 = direction.getOpposite();
       worldIn.addParticle(ParticleTypes.DRAGON_BREATH, d0 + 0.27D * (double)direction1.getXOffset(), d1 + 0.22D, d2 + 0.27D * (double)direction1.getZOffset(), 0.0D, 0.0D, 0.0D);
    }
}
