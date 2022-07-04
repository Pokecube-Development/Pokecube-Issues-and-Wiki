package pokecube.legends.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.PlantsInit;

public class MushroomBase extends MushroomBlock
{
    protected static final VoxelShape LARGE_SHAPE = Block.box(2, 0, 2, 14, 15, 14);
    protected static final VoxelShape SMALL_SHAPE = Block.box(4, 0, 4, 12, 9, 12);
    public final Supplier<Holder<? extends ConfiguredFeature<?, ?>>> featureSupplier;
    public boolean validBonemealTarget = true;

    public MushroomBase(final BlockBehaviour.Properties properties,
            final Supplier<Holder<? extends ConfiguredFeature<?, ?>>> supplier)
    {
        super(properties, supplier);
        this.featureSupplier = supplier;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        if (state.getBlock() == PlantsInit.COMPRECED_MUSHROOM.get()
                || state.getBlock() == PlantsInit.DISTORCED_MUSHROOM.get())
        {
            return LARGE_SHAPE;
        }
        else return SMALL_SHAPE;
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
