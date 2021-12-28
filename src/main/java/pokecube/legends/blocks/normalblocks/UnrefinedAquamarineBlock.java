package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class UnrefinedAquamarineBlock extends RotatedPillarBlock
{
    public UnrefinedAquamarineBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }
    
    public void onProjectileHit(Level world, BlockState state, BlockHitResult block, Projectile projectile)
    {
       if (!world.isClientSide)
       {
          BlockPos pos = block.getBlockPos();
          world.playSound((Player)null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
          world.playSound((Player)null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
       }
    }
}
