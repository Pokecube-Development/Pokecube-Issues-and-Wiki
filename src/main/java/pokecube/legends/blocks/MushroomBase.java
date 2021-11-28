package pokecube.legends.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;

public class MushroomBase extends MushroomBlock
{
    protected static final VoxelShape              SHAPE               = Block.box(2, 0, 2, 14, 15, 14);
    public final Supplier<ConfiguredFeature<?, ?>> featureSupplier;
    public boolean                                 validBonemealTarget = true;

    public MushroomBase(final BlockBehaviour.Properties properties, final Supplier<ConfiguredFeature<?, ?>> supplier)
    {
        super(properties, supplier);
        this.featureSupplier = supplier;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return MushroomBase.SHAPE;
    }

    @Override
    public PlantType getPlantType(final BlockGetter world, final BlockPos pos)
    {
        return PlantType.PLAINS;
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter block, final BlockPos pos, final BlockState state,
            final boolean b)
    {
        return this.validBonemealTarget;
    }

    public MushroomBase bonemealTarget(final Boolean isValidBonemealTarget)
    {
        this.validBonemealTarget = isValidBonemealTarget;
        return this;
    }
}
