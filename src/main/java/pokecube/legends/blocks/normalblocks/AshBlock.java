package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.FarmlandWaterManager;
import pokecube.legends.blocks.FallingBlockBase;
import pokecube.legends.blocks.FallingDirtBlockBase;
import pokecube.legends.blocks.customblocks.CramomaticBlock;
import pokecube.legends.blocks.customblocks.Rotates;

public class AshBlock extends FallingDirtBlockBase implements Fallable
{
    public static final BooleanProperty WET = BooleanProperty.create("wet");
    
    public AshBlock(final int color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WET, false));
    }
    
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (isNearWater(world, pos) || world.isRainingAt(pos.above()))
        {
            world.setBlock(pos, state.setValue(WET, true), 2);
        }
        
        if (isFree(world.getBlockState(pos.below())) && pos.getY() >= world.getMinBuildHeight())
        {
            FallingBlockEntity fallingBlock = 
                    new FallingBlockEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, world.getBlockState(pos));
            this.falling(fallingBlock);
            world.addFreshEntity(fallingBlock);
        }
    }

    private static boolean isNearWater(LevelReader world, BlockPos pos)
    {
        if (world.getFluidState(pos.above()).is(FluidTags.WATER) || world.getFluidState(pos.below()).is(FluidTags.WATER)
              || world.getFluidState(pos.north()).is(FluidTags.WATER) || world.getFluidState(pos.south()).is(FluidTags.WATER)
              || world.getFluidState(pos.east()).is(FluidTags.WATER) || world.getFluidState(pos.west()).is(FluidTags.WATER))
        {
           return true;
        }
        return FarmlandWaterManager.hasBlockWaterTicket(world, pos);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
       builder.add(WET);
    }
}
