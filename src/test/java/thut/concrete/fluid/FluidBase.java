package thut.concrete.fluid;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class FluidBase extends ForgeFlowingFluid
{
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal
            .withInitial(() ->
            {
                Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(
                        CACHE_SIZE)
                {
                    /**
                     * 
                     */
                    private static final long serialVersionUID = 1L;

                    protected void rehash(int p_76102_)
                    {}
                };
                object2bytelinkedopenhashmap.defaultReturnValue((byte) 127);
                return object2bytelinkedopenhashmap;
            });

    public FluidBase(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
    {
        builder.add(FALLING);
        builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState state)
    {
        return state.getValue(LEVEL);
    }

    @Override
    public float getOwnHeight(FluidState p_76048_)
    {
        return 0.002f + (float) getAmount(p_76048_) / 15.02f;
    }

    private static boolean hasSameAbove(FluidState p_76089_, BlockGetter p_76090_, BlockPos p_76091_)
    {
        return p_76089_.getType().isSame(p_76090_.getFluidState(p_76091_.above()).getType());
    }

    @Override
    public float getHeight(FluidState p_76050_, BlockGetter p_76051_, BlockPos p_76052_)
    {
        return hasSameAbove(p_76050_, p_76051_, p_76052_) ? 1.0F : p_76050_.getOwnHeight();
    }

    @Override
    protected boolean isEmpty()
    {
        return false;
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState fluid)
    {
        if (!this.isRandomlyTicking()) return;
        if (fluid.isSource())
        {
            FluidState fluidstate = this.getNewLiquid(level, pos, level.getBlockState(pos));
            int i = this.getSpreadDelay(level, pos, fluid, fluidstate);
            if (fluidstate.isEmpty())
            {
                fluid = fluidstate;
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            else if (!fluidstate.equals(fluid))
            {
                fluid = fluidstate;
                BlockState blockstate = fluidstate.createLegacyBlock();
                level.setBlock(pos, blockstate, 2);
                level.scheduleTick(pos, fluidstate.getType(), i);
                level.updateNeighborsAt(pos, blockstate.getBlock());
            }
        }
        this.spread(level, pos, fluid);
    }

    @Override
    protected void spread(LevelAccessor p_76011_, BlockPos p_76012_, FluidState p_76013_)
    {
        if (!p_76013_.isEmpty())
        {
            BlockState blockstate = p_76011_.getBlockState(p_76012_);
            BlockPos blockpos = p_76012_.below();
            BlockState blockstate1 = p_76011_.getBlockState(blockpos);
            FluidState fluidstate = this.getNewLiquid(p_76011_, blockpos, blockstate1);
            if (this.canSpreadTo(p_76011_, p_76012_, blockstate, Direction.DOWN, blockpos, blockstate1,
                    p_76011_.getFluidState(blockpos), fluidstate.getType()))
            {
                this.spreadTo(p_76011_, blockpos, blockstate1, Direction.DOWN, fluidstate);
                if (this.sourceNeighborCount(p_76011_, p_76012_) >= 3)
                {
                    this.spreadToSides(p_76011_, p_76012_, p_76013_, blockstate);
                }
            }
            else if (p_76013_.isSource()
                    || !this.isWaterHole(p_76011_, fluidstate.getType(), p_76012_, blockstate, blockpos, blockstate1))
            {
                this.spreadToSides(p_76011_, p_76012_, p_76013_, blockstate);
            }

        }
    }

    private int sourceNeighborCount(LevelReader p_76020_, BlockPos p_76021_)
    {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = p_76021_.relative(direction);
            FluidState fluidstate = p_76020_.getFluidState(blockpos);
            if (this.isSourceBlockOfThisType(fluidstate))
            {
                ++i;
            }
        }

        return i;
    }

    private boolean canHoldFluid(BlockGetter p_75973_, BlockPos p_75974_, BlockState p_75975_, Fluid p_75976_)
    {
        Block block = p_75975_.getBlock();
        if (block instanceof LiquidBlockContainer)
        {
            return ((LiquidBlockContainer) block).canPlaceLiquid(p_75973_, p_75974_, p_75975_, p_75976_);
        }
        else if (!(block instanceof DoorBlock) && !p_75975_.is(BlockTags.SIGNS) && !p_75975_.is(Blocks.LADDER)
                && !p_75975_.is(Blocks.SUGAR_CANE) && !p_75975_.is(Blocks.BUBBLE_COLUMN))
        {
            Material material = p_75975_.getMaterial();
            if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT
                    && material != Material.REPLACEABLE_WATER_PLANT)
            {
                return !material.blocksMotion();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean isWaterHole(BlockGetter p_75957_, Fluid p_75958_, BlockPos p_75959_, BlockState p_75960_,
            BlockPos p_75961_, BlockState p_75962_)
    {
        if (!this.canPassThroughWall(Direction.DOWN, p_75957_, p_75959_, p_75960_, p_75961_, p_75962_))
        {
            return false;
        }
        else
        {
            return p_75962_.getFluidState().getType().isSame(this) ? true
                    : this.canHoldFluid(p_75957_, p_75961_, p_75962_, p_75958_);
        }
    }

    private void spreadToSides(LevelAccessor p_76015_, BlockPos p_76016_, FluidState p_76017_, BlockState p_76018_)
    {
        int i = p_76017_.getAmount() - this.getDropOff(p_76015_);
        if (p_76017_.getValue(FALLING))
        {
            i = 14;
        }

        if (i > 1)
        {
            Map<Direction, FluidState> map = this.getSpread(p_76015_, p_76016_, p_76018_);

            for (Entry<Direction, FluidState> entry : map.entrySet())
            {
                Direction direction = entry.getKey();
                FluidState fluidstate = entry.getValue();
                BlockPos blockpos = p_76016_.relative(direction);
                BlockState blockstate = p_76015_.getBlockState(blockpos);
                if (this.canSpreadTo(p_76015_, p_76016_, p_76018_, direction, blockpos, blockstate,
                        p_76015_.getFluidState(blockpos), fluidstate.getType()))
                {
                    this.spreadTo(p_76015_, blockpos, blockstate, direction, fluidstate);
                }
            }

        }
    }

    @Override
    protected Map<Direction, FluidState> getSpread(LevelReader p_76080_, BlockPos p_76081_, BlockState p_76082_)
    {
        // TODO Auto-generated method stub
        return super.getSpread(p_76080_, p_76081_, p_76082_);
    }

    @Override
    protected int getSlopeDistance(LevelReader p_76027_, BlockPos p_76028_, int p_76029_, Direction p_76030_,
            BlockState p_76031_, BlockPos p_76032_, Short2ObjectMap<Pair<BlockState, FluidState>> p_76033_,
            Short2BooleanMap p_76034_)
    {
        // TODO Auto-generated method stub
        return super.getSlopeDistance(p_76027_, p_76028_, p_76029_, p_76030_, p_76031_, p_76032_, p_76033_, p_76034_);
    }

    @Override
    protected int getSpreadDelay(Level p_75998_, BlockPos p_75999_, FluidState p_76000_, FluidState p_76001_)
    {
        // TODO Auto-generated method stub
        return super.getSpreadDelay(p_75998_, p_75999_, p_76000_, p_76001_);
    }

    @Override
    protected void spreadTo(LevelAccessor p_76005_, BlockPos p_76006_, BlockState p_76007_, Direction p_76008_,
            FluidState p_76009_)
    {
        // TODO Auto-generated method stub
        super.spreadTo(p_76005_, p_76006_, p_76007_, p_76008_, p_76009_);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluidIn,
            Direction direction)
    {
        // TODO Auto-generated method stub
        return super.canBeReplacedWith(state, world, pos, fluidIn, direction);
    }

    @Override
    protected FluidState getNewLiquid(LevelReader p_76036_, BlockPos p_76037_, BlockState p_76038_)
    {
        int i = 0;
        int j = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = p_76037_.relative(direction);
            BlockState blockstate = p_76036_.getBlockState(blockpos);
            FluidState fluidstate = blockstate.getFluidState();
            if (fluidstate.getType().isSame(this)
                    && this.canPassThroughWall(direction, p_76036_, p_76037_, p_76038_, blockpos, blockstate))
            {
                if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(p_76036_,
                        blockpos, blockstate, this.canConvertToSource()))
                {
                    ++j;
                }

                i = Math.max(i, fluidstate.getAmount());
            }
        }

        if (j >= 2)
        {
            BlockState blockstate1 = p_76036_.getBlockState(p_76037_.below());
            FluidState fluidstate1 = blockstate1.getFluidState();
            if (blockstate1.getMaterial().isSolid() || this.isSourceBlockOfThisType(fluidstate1))
            {
                return this.getSource(false);
            }
        }

        BlockPos blockpos1 = p_76037_.above();
        BlockState blockstate2 = p_76036_.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();
        if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this)
                && this.canPassThroughWall(Direction.UP, p_76036_, p_76037_, p_76038_, blockpos1, blockstate2))
        {
            return this.getFlowing(15, true);
        }
        else
        {
            int k = i - this.getDropOff(p_76036_);
            System.out.println(i + " " + k + " " + this.getDropOff(p_76036_));
            return k < 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    private boolean isSourceBlockOfThisType(FluidState p_76097_)
    {
        return p_76097_.getType().isSame(this) && p_76097_.isSource();
    }

    private boolean canPassThroughWall(Direction p_76062_, BlockGetter p_76063_, BlockPos p_76064_, BlockState p_76065_,
            BlockPos p_76066_, BlockState p_76067_)
    {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;
        if (!p_76065_.getBlock().hasDynamicShape() && !p_76067_.getBlock().hasDynamicShape())
        {
            object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
        }
        else
        {
            object2bytelinkedopenhashmap = null;
        }

        Block.BlockStatePairKey block$blockstatepairkey;
        if (object2bytelinkedopenhashmap != null)
        {
            block$blockstatepairkey = new Block.BlockStatePairKey(p_76065_, p_76067_, p_76062_);
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
            if (b0 != 127)
            {
                return b0 != 0;
            }
        }
        else
        {
            block$blockstatepairkey = null;
        }

        VoxelShape voxelshape1 = p_76065_.getCollisionShape(p_76063_, p_76064_);
        VoxelShape voxelshape = p_76067_.getCollisionShape(p_76063_, p_76066_);
        boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, p_76062_);
        if (object2bytelinkedopenhashmap != null)
        {
            if (object2bytelinkedopenhashmap.size() == CACHE_SIZE)
            {
                object2bytelinkedopenhashmap.removeLastByte();
            }

            object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte) (flag ? 1 : 0));
        }

        return flag;
    }

    @Override
    public FluidState getFlowing(int amount, boolean falling)
    {
        return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(amount)).setValue(FALLING,
                Boolean.valueOf(falling));
    }

    @Override
    protected boolean canSpreadTo(BlockGetter level, BlockPos pos1, BlockState state1, Direction direction,
            BlockPos pos2, BlockState state2, FluidState fluidstate, Fluid fluid)
    {
        if (!this.isRandomlyTicking()) return false;
        return super.canSpreadTo(level, pos1, state1, direction, pos2, state2, fluidstate, fluid);
    }

    @Override
    protected void randomTick(Level p_76132_, BlockPos p_76133_, FluidState p_76134_, Random p_76135_)
    {
        if (!this.isRandomlyTicking()) return;
        super.randomTick(p_76132_, p_76133_, p_76134_, p_76135_);
    }

    @Override
    protected boolean isRandomlyTicking()
    {
//        return !(this == Concrete.DRYCONCRETEF.get() || this == Concrete.DRYCONCRETES.get());
        return true;
    }

    @Override
    public boolean isSource(FluidState state)
    {
        return false;
    }
}
