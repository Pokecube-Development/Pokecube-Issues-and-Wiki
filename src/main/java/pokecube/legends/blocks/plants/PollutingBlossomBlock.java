package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PollutingBlossomBlock extends SporeBlossomBlock
{
   public PollutingBlossomBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public void animateTick(BlockState state, Level world, BlockPos pos, Random random)
   {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      double d0 = (double)i + random.nextDouble();
      double d1 = (double)j + 0.7D;
      double d2 = (double)k + random.nextDouble();
      world.addParticle(ParticleTypes.WHITE_ASH, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

      for(int l = 0; l < 14; ++l)
      {
         mutablePos.set(i + Mth.nextInt(random, -10, 10), j - random.nextInt(10), k + Mth.nextInt(random, -10, 10));
         BlockState mutableState = world.getBlockState(mutablePos);
         if (!mutableState.isCollisionShapeFullBlock(world, mutablePos))
         {
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, (double)mutablePos.getX() + random.nextDouble(), 
                    (double)mutablePos.getY() + random.nextDouble(), (double)mutablePos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
         }
      }
   }
}