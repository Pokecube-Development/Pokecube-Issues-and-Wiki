package pokecube.legends.blocks.plants;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import pokecube.core.handlers.ItemGenerator;

public class PottedCrystallizedBush extends ItemGenerator.GenericPottedPlant {
    public PottedCrystallizedBush(Block pottedPlant, AbstractBlock.Properties properties) {
        super(pottedPlant, properties);
    }

    @Override
    public void stepOn(final World world, final BlockPos pos, final Entity entity) {
        if (entity instanceof LivingEntity) {
            final BlockState state = world.getBlockState(pos);
            entity.makeStuckInBlock(state, new Vector3d(0.9D, 0.75D, 0.9D));
            if (!world.isClientSide) {
                entity.hurt(DamageSource.CACTUS, 1.0F);
            }
        }
    }
}
