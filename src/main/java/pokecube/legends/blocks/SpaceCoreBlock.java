package pokecube.legends.blocks;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class SpaceCoreBlock extends Rotates
{

    public SpaceCoreBlock(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) worldIn.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F,
                false);
    }
}
