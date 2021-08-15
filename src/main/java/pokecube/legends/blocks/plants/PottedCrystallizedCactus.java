package pokecube.legends.blocks.plants;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.handlers.ItemGenerator;

import javax.annotation.Nullable;

public class PottedCrystallizedCactus extends ItemGenerator.GenericPottedPlant {
    public PottedCrystallizedCactus(Block pottedPlant, AbstractBlock.Properties properties) {
        super(pottedPlant, properties);
    }

    public PathNodeType getAIPathNodeType(BlockState state, IBlockReader worldIn, BlockPos pos, @Nullable MobEntity entity)
    {
        return PathNodeType.DAMAGE_OTHER;
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide) {
            entity.hurt(DamageSource.CACTUS, 1.0F);
        }
    }
}
