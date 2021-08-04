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

public class PottedCrystallizedCactus extends ItemGenerator.GenericPottedPlant {
    public PottedCrystallizedCactus(Block pottedPlant, AbstractBlock.Properties properties) {
        super(pottedPlant, properties);
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        if (!world.isClientSide) {
            entity.hurt(DamageSource.CACTUS, 1.0F);
        }
    }
}
