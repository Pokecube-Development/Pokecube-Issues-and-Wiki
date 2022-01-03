package thut.api.block.flowing;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainChecker;

public interface IFlowingBlock
{
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 16);
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty VISCOSITY = IntegerProperty.create("viscosity", 0, 15);

    public static VoxelShape[] makeShapes()
    {
        VoxelShape[] SHAPES = new VoxelShape[16];
        for (int i = 0; i < 16; i++)
        {
            SHAPES[i] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, (i + 1), 16.0D);
        }
        return SHAPES;
    }

    public static final VoxelShape[] SHAPES = makeShapes();

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public static BlockState copyValidTo(BlockState from, BlockState to)
    {
        for (Property p : from.getProperties())
        {
            if (to.hasProperty(p)) to = to.setValue(p, from.getValue(p));
        }
        return to;
    }

    default Block thisBlock()
    {
        return (Block) this;
    }

    Block getAlternate();

    int getFlowRate();

    int getFallRate();

    default int getSlope(BlockState state)
    {
        return state.hasProperty(VISCOSITY) ? state.getValue(VISCOSITY) : 0;
    }

    default boolean isFullBlock()
    {
        return false;
    }

    default boolean flows()
    {
        return true;
    }

    default boolean isFalling(BlockState state)
    {
        if (!state.hasProperty(FALLING)) return false;
        return state.getValue(FALLING);
    }

    default boolean isStableBelow(BlockState state, BlockPos pos, ServerLevel level)
    {
        int amt = getExistingAmount(state, pos, level);
        if (TerrainChecker.isLeaves(state) || TerrainChecker.isWood(state)) return false;
        if (amt == 16 && state.getBlock() instanceof IFlowingBlock b && b.isFullBlock() && !b.flows()) return true;
        return (amt == -1);
    }

    default BlockState makeFalling(BlockState state, boolean falling)
    {
        if (this.isFullBlock())
        {
            if (!falling) return thisBlock().defaultBlockState();
            BlockState b = this.getAlternate().defaultBlockState();
            b = copyValidTo(state, b);
            return b.setValue(LAYERS, 16).setValue(FALLING, falling);
        }
        return state.setValue(FALLING, falling);
    }

    default BlockState setAmount(BlockState state, int amt)
    {
        if (state.getBlock() instanceof IFlowingBlock b && b != this)
        {
            return b.setAmount(state, amt);
        }
        if (this.isFullBlock())
        {
            if (amt == 16) return thisBlock().defaultBlockState();
            BlockState b = this.getAlternate().defaultBlockState();
            b = copyValidTo(state, b);
            return b.setValue(LAYERS, amt);
        }
        if (amt == 16 && !isFalling(state))
        {
            BlockState b = this.getAlternate().defaultBlockState();
            b = copyValidTo(state, b);
            return b;
        }
        // TODO check if it is waterlogged, if so, return water instead.
        if (amt == 0) return Blocks.AIR.defaultBlockState();
        return state.setValue(LAYERS, amt);
    }

    default void onStableTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        int dust = getExistingAmount(state, pos, level);
        if (dust == 16 && state.hasProperty(LAYERS) && getAlternate() != thisBlock())
        {
            level.setBlock(pos, getAlternate().defaultBlockState(), 2);
        }
    }

    default boolean tryFall(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        boolean falling = isFalling(state);

        // Try down first;
        int dust = getExistingAmount(state, pos, level);

        BlockPos belowPos = pos.below();
        BlockState b = level.getBlockState(belowPos);
        int below = getExistingAmount(b, belowPos, level);
        boolean belowFalling = isFalling(b);

        if (falling)
        {
            if ((below < 0 || below == 16))
            {
                if (!belowFalling) level.setBlock(pos, makeFalling(state, false), 2);
                level.scheduleTick(pos.immutable(), thisBlock(), getFallRate());
                return true;
            }
            else
            {
                int total = dust + below;
                int diff = 16 - below;
                BlockState newBelow;
                if (total <= 16)
                {
                    newBelow = getMergeResult(setAmount(state, total), b, belowPos, level);
                    if (newBelow != b)
                    {
                        level.setBlock(belowPos, newBelow, 2);
                        level.scheduleTick(belowPos, newBelow.getBlock(), getFallRate());
                        level.removeBlock(pos, false);
                    }
                    else return false;
                }
                else if (dust - diff >= 0)
                {
                    BlockState b2 = getAlternate().defaultBlockState();
                    b2 = copyValidTo(state, b2);
                    newBelow = getMergeResult(b2, b, belowPos, level);
                    if (newBelow != b)
                    {
                        level.setBlock(belowPos, newBelow, 2);
                        level.setBlock(pos, setAmount(state, dust - diff), 2);
                        level.scheduleTick(pos.immutable(), thisBlock(), getFallRate());
                        level.scheduleTick(belowPos, newBelow.getBlock(), getFallRate());
                    }
                    else return false;
                }
                return true;
            }
        }
        if (below >= 0 && below < 16)
        {
            level.setBlock(pos, makeFalling(state, true), 2);
            level.scheduleTick(pos.immutable(), thisBlock(), getFallRate());
            return true;
        }
        return false;
    }

    default boolean trySpread(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        int dust = getExistingAmount(state, pos, level);
        int slope = getSlope(state);

        if (dust >= slope)
        {
            Vector3 v = Vector3.getNewVector().set(pos);
            BlockState b = null;
            Direction dir = null;

            int existing = dust;
            int amt = 0;

            int rng = random.nextInt(100);

            for (int i = 0; i < Direction.values().length; i++)
            {
                int index = (i + rng) % Direction.values().length;
                Direction d = Direction.values()[index];
                if (d == Direction.DOWN || d == Direction.UP) continue;
                v.set(d).addTo(pos.getX(), pos.getY(), pos.getZ());
                b = v.getBlockState(level);
                amt = getExistingAmount(b, v.getPos(), level);
                if (amt == -1 || amt > dust - slope) continue;
                existing += amt;
                dir = d;
                break;
            }
            if (dir != null && amt != dust)
            {
                int left = existing;
                int next = 0;

                next = (existing - slope) / 2;
                left = existing - next;

                if (slope == 0 && dust > 1 && left - next == 1)
                {
                    int tmp = left;
                    left = next;
                    next = tmp;
                }
                if (next > 0)
                {
                    BlockState oldState = setAmount(state, left);
                    BlockPos pos2 = v.getPos();

                    BlockState nextState = setAmount(state, next);
                    BlockState newState = getMergeResult(nextState, b, pos2, level);
                    if (newState != b)
                    {
                        level.setBlock(pos, oldState, 2);
                        level.setBlock(pos2, newState, 2);
                        level.scheduleTick(pos.immutable(), oldState.getBlock(), getFlowRate());
                        level.scheduleTick(pos2, newState.getBlock(), getFlowRate());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    default int getExistingAmount(BlockState state, BlockPos pos, ServerLevel level)
    {
        return getAmount(state);
    }

    default int getAmount(BlockState state)
    {
        if (state.getBlock() instanceof IFlowingBlock b)
        {
            if (b != this) return b.getAmount(state);
            if (b.isFullBlock()) return 16;
            return state.hasProperty(LAYERS) ? state.getValue(LAYERS) : b.flows() ? 16 : -1;
        }
        return canReplace(state) ? 0 : -1;
    }

    default boolean canReplace(BlockState state, BlockPos pos, ServerLevel level)
    {
        return canReplace(state);
    }

    default boolean canReplace(BlockState state)
    {
        if (state.canBeReplaced(Fluids.FLOWING_WATER)) return true;
        if (state.isAir()) return true;
        return false;
    }

    default BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        FluidState into = mergeInto.getFluidState();
        if ((into.is(Fluids.WATER) || (mergeInto.hasProperty(WATERLOGGED) && mergeInto.getValue(WATERLOGGED)))
                && mergeFrom.hasProperty(WATERLOGGED))
        {
            mergeFrom = mergeFrom.setValue(WATERLOGGED, true);
        }
        boolean replacelable = canReplace(mergeInto, posTo, level);
        if (replacelable) return mergeFrom;
        if (mergeFrom.getBlock() == mergeInto.getBlock()) return mergeFrom;
        return mergeInto;
    }

    default void updateNearby(BlockPos centre, ServerLevel level, int tickRate)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction d : Direction.values())
        {
            pos.set(centre).move(d);

            ChunkPos c = new ChunkPos(pos);
            LevelChunk chunk = level.getChunkSource().getChunkNow(c.x, c.z);
            if (chunk == null) continue;

            BlockState block = level.getBlockState(pos);
            if (block.getBlock() instanceof IFlowingBlock b && b.flows()
                    && !level.getBlockTicks().hasScheduledTick(pos, block.getBlock()))
                level.scheduleTick(pos, block.getBlock(), tickRate);
        }
    }

    default void doTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        level.getProfiler().push("flowing_block:" + this.getClass());
        tick:
        {
            // Try down first;
            level.getProfiler().push("fall_check");
            if (tryFall(state, level, pos, random))
            {
                level.getProfiler().pop();
                break tick;
            }
            level.getProfiler().pop();
            // Next try spreading sideways
            level.getProfiler().push("spread_check");
            if (trySpread(state, level, pos, random))
            {
                level.getProfiler().pop();
                break tick;
            }
            level.getProfiler().pop();
            // Then apply any checks for if we were stable
            level.getProfiler().push("stability_check:" + this.getClass());
            onStableTick(state, level, pos, random);
            level.getProfiler().pop();
        }
        level.getProfiler().pop();
    }

}
