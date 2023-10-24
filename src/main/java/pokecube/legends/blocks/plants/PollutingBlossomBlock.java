package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
   public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random)
   {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      double d0 = i + random.nextDouble();
      double d1 = j + 0.7D;
      double d2 = k + random.nextDouble();
      world.addParticle(ParticleTypes.ASH, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

      for(int l = 0; l < 14; ++l)
      {
         mutablePos.set(i + Mth.nextInt(random, -10, 10), j - random.nextInt(10), k + Mth.nextInt(random, -10, 10));
         BlockState mutableState = world.getBlockState(mutablePos);
         if (!mutableState.isCollisionShapeFullBlock(world, mutablePos))
         {
            world.addParticle(ParticleTypes.LARGE_SMOKE, mutablePos.getX() + random.nextDouble(), 
                    mutablePos.getY() + random.nextDouble(), mutablePos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
         }
      }
   }
}