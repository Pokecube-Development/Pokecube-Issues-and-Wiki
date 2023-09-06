package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.ParticleInit;

public class InfectedTorch extends TorchBlock
{
    protected final ParticleOptions particle;
    protected final ParticleOptions smokeParticle;
    public InfectedTorch(ParticleOptions particle, ParticleOptions smokeParticle, BlockBehaviour.Properties properties)
    {
        super(properties, particle);
        this.particle = particle;
        this.smokeParticle = smokeParticle;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand)
    {
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY() + 0.8D;
        double d2 = (double) pos.getZ() + 0.5D;
        world.addParticle(this.smokeParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        world.addParticle(ParticleInit.INFECTED_FIRE_FLAME.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }
}
