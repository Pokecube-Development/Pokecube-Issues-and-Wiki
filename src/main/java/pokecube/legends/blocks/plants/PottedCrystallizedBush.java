package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import pokecube.core.handlers.ItemGenerator;

public class PottedCrystallizedBush extends ItemGenerator.GenericPottedPlant
{
    public PottedCrystallizedBush(final Block pottedPlant, final BlockBehaviour.Properties properties)
    {
        super(pottedPlant, properties);
    }

    @Nullable
    @Override
    public BlockPathTypes getAiPathNodeType(final BlockState state, final BlockGetter world, final BlockPos pos, @Nullable final Mob entity)
    {
        return BlockPathTypes.DAMAGE_OTHER;
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        if (entity instanceof LivingEntity)
        {
            entity.makeStuckInBlock(state, new Vec3(0.9D, 0.75D, 0.9D));
            if (!world.isClientSide) entity.hurt(DamageSource.CACTUS, 1.0F);
        }
    }
}
