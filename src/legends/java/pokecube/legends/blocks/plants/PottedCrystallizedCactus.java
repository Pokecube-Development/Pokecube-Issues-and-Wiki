package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import pokecube.core.init.ItemGenerator;

import javax.annotation.Nullable;

public class PottedCrystallizedCactus extends ItemGenerator.GenericPottedPlant
{
    public PottedCrystallizedCactus(final Block pottedPlant, final BlockBehaviour.Properties properties)
    {
        super(pottedPlant, properties);
    }

    @Nullable
    @Override
    public PathType getBlockPathType(final BlockState state, final BlockGetter world, final BlockPos pos,
            @Nullable final Mob entity)
    {
        return PathType.DAMAGE_OTHER;
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        if (!world.isClientSide) entity.hurt(world.damageSources().cactus(), 1.0F);
    }
}
