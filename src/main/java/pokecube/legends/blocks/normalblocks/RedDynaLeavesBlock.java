package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;
import pokecube.legends.init.ParticleInit;

public class RedDynaLeavesBlock extends DynaLeavesBlock implements IForgeShearable
{
    int particleSpawnSpeed;

    public RedDynaLeavesBlock(int particleSpawnSpeed, final Properties properties)
    {
        super(properties);
        this.particleSpawnSpeed = particleSpawnSpeed;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final RandomSource random)
    {
        super.animateTick(state, world, pos, random);

        if (random.nextInt(particleSpawnSpeed) == 0) {
            final BlockPos posBelow = pos.below();
            BlockState blockstate = world.getBlockState(posBelow);
            if (!isFaceFull(blockstate.getCollisionShape(world, posBelow), Direction.UP))
            {
                ParticleUtils.spawnParticleBelow(world, pos, random, ParticleInit.DYNA_LEAF_RED.get());
            }
        }
    }
}
