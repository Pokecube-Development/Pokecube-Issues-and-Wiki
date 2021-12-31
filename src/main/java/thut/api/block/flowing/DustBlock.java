package thut.api.block.flowing;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class DustBlock extends Block implements SimpleWaterloggedBlock
{
    private static final Map<ResourceLocation, RegistryObject<DustBlock>> REGMAP = Maps.newHashMap();

    public static RegistryObject<DustBlock>[] makeDust(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<DustBlock>[] arr = (RegistryObject<DustBlock>[]) Array.newInstance(RegistryObject.class, 2);

        RegistryObject<DustBlock> layer_reg = BLOCKS.register(layer,
                () -> new DustBlock(layer_props).alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<DustBlock> block_reg = BLOCKS.register(block,
                () -> new FullDust(block_props).alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 16);
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape[] SHAPES = new VoxelShape[16];
    static
    {
        for (int i = 0; i < 16; i++)
        {
            SHAPES[i] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, (i + 1), 16.0D);
        }
    }

    protected int slope = 4;
    protected int tickRateFall = 50;
    protected int tickRateFlow = 5;
    protected boolean flows = true;
    private Supplier<Block> convert;

    public DustBlock(Properties properties)
    {
        super(properties);
        initStateDefinition();
    }

    protected void initStateDefinition()
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(16))
                .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FALLING, Boolean.valueOf(false)));
    }

    public DustBlock alternateBlock(Supplier<Block> supplier)
    {
        this.convert = supplier;
        return this;
    }

    public Supplier<Block> getAlternate()
    {
        return convert;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LAYERS);
        builder.add(WATERLOGGED);
        builder.add(FALLING);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return isFalling(state);
    }

    @Override
    public void neighborChanged(BlockState us, Level level, BlockPos here, Block other, BlockPos changed, boolean bool)
    {
        level.scheduleTick(here, this, isFalling(us) ? tickRateFall : tickRateFlow);
        DebugPackets.sendNeighborsUpdatePacket(level, here);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean bool)
    {
        level.scheduleTick(pos, this, tickRateFlow);
        if (level instanceof ServerLevel slevel) updateNearby(pos, slevel, tickRateFlow);
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    protected int getExistingAmount(BlockState state, BlockPos pos, ServerLevel level)
    {
        if (state.getBlock() instanceof DustBlock b)
            return state.hasProperty(LAYERS) ? state.getValue(LAYERS) : b.flows ? 16 : -1;
        return canReplace(state, pos, level) ? 0 : -1;
    }

    protected boolean canReplace(BlockState state, BlockPos pos, ServerLevel level)
    {
        if (state.canBeReplaced(Fluids.FLOWING_WATER)) return true;
        if (state.isAir()) return true;
        return false;
    }

    protected BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        FluidState into = mergeInto.getFluidState();
        if (into.is(Fluids.WATER))
        {
            mergeFrom = mergeFrom.setValue(WATERLOGGED, true);
        }
        boolean replacelable = canReplace(mergeInto, posTo, level);
        if (replacelable) return mergeFrom;
        if (mergeFrom.getBlock() == mergeInto.getBlock()) return mergeFrom;
        return mergeInto;
    }

    private void updateNearby(BlockPos centre, ServerLevel level, int tickRate)
    {
        for (Direction d : Direction.values())
        {
            BlockPos pos = centre.relative(d);

            ChunkPos c = new ChunkPos(pos);
            LevelChunk chunk = level.getChunkSource().getChunkNow(c.x, c.z);
            if (chunk == null) continue;

            BlockState block = level.getBlockState(pos);
            level.scheduleTick(pos, block.getBlock(), tickRate);
        }
    }

    @Override
    public void onRemove(BlockState p_60516_, Level level, BlockPos pos, BlockState p_60518_, boolean p_60519_)
    {
        if (level instanceof ServerLevel slevel) updateNearby(pos, slevel, tickRateFlow);
    }

    protected boolean isFalling(BlockState state)
    {
        if (!state.hasProperty(FALLING)) return false;
        return state.getValue(FALLING);
    }

    protected boolean isStableBelow(BlockState state, BlockPos pos, ServerLevel level)
    {
        int amt = getExistingAmount(state, pos, level);
        if (PokecubeTerrainChecker.isLeaves(state) || PokecubeTerrainChecker.isWood(state)) return false;
        return (amt == -1);
    }

    protected BlockState makeFalling(BlockState state, boolean falling)
    {
        return state.setValue(FALLING, falling);
    }

    protected BlockState setAmount(BlockState state, int amt)
    {
        if (amt == 16 && !isFalling(state)) return convert.get().defaultBlockState();
        // TODO check if it is waterlogged, if so, return water instead.
        if (amt == 0) return Blocks.AIR.defaultBlockState();
        return state.setValue(LAYERS, amt);
    }

    protected void onStableTick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        int dust = getExistingAmount(state, pos, level);
        if (dust == 16 && state.hasProperty(LAYERS))
        {
            level.setBlock(pos, convert.get().defaultBlockState(), 2);
        }
    }

    protected boolean tryFall(BlockState state, ServerLevel level, BlockPos pos, Random random)
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
                level.scheduleTick(pos.immutable(), this, tickRateFall);
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
                        updateNearby(pos, level, tickRateFall);
                        level.removeBlock(pos, false);
                    }
                    else return false;
                }
                else if (dust - diff >= 0)
                {
                    BlockState b2 = convert.get().defaultBlockState();
                    newBelow = getMergeResult(b2, b, belowPos, level);
                    if (newBelow != b)
                    {
                        level.setBlock(belowPos, newBelow, 2);
                        level.setBlock(pos, setAmount(state, dust - diff), 2);
                        level.scheduleTick(pos.immutable(), this, tickRateFall);
                        updateNearby(pos, level, tickRateFall);
                    }
                    else return false;
                }
                return true;
            }
        }
        if (below >= 0 && below < 16)
        {
            level.setBlock(pos, makeFalling(state, true), 2);
            level.scheduleTick(pos.immutable(), this, tickRateFall);
            return true;
        }
        return false;
    }

    protected boolean trySpread(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        int dust = getExistingAmount(state, pos, level);
        if (dust >= slope)
        {
            Vector3 v = Vector3.getNewVector().set(pos);
            BlockState b = level.getBlockState(pos.below());
            Set<Direction> options = Sets.newHashSet();

            int existing = dust;

            int rng = random.nextInt(100);

            for (int i = 0; i < Direction.values().length; i++)
            {
                int index = (i + rng) % Direction.values().length;
                Direction d8 = Direction.values()[index];
                if (d8 == Direction.DOWN || d8 == Direction.UP) continue;
                v.set(d8).addTo(pos.getX(), pos.getY(), pos.getZ());
                b = v.getBlockState(level);
                int amt = getExistingAmount(b, v.getPos(), level);
                if (amt == -1 || amt >= dust - slope) continue;

                existing += amt;
                options.add(d8);
                break;
            }
            if (!options.isEmpty())
            {
                int per = (existing) / (options.size() + 1);
                int rem = (existing) - (per * (options.size()));

                if (per > 0)
                {
                    level.setBlock(pos, setAmount(state, rem), 2);
                    level.scheduleTick(pos.immutable(), this, tickRateFlow);
                    updateNearby(pos, level, tickRateFall);
                    for (Direction d8 : options)
                    {
                        v.set(d8).addTo(pos.getX(), pos.getY(), pos.getZ());
                        v.setBlock(level, setAmount(state, per));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        // Try down first;
        if (tryFall(state, level, pos, random)) return;
        // Next try spreading sideways
        if (trySpread(state, level, pos, random)) return;
        // Then apply any checks for if we were stable
        onStableTick(state, level, pos, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
    {
        int level = state.getValue(LAYERS);
        boolean falling = state.getValue(FALLING);
        if (falling) return Shapes.empty();
        return SHAPES[level - 1];
    }

    public static class FullDust extends DustBlock
    {

        public FullDust(Properties properties)
        {
            super(properties);
        }

        protected int getExistingAmount(BlockState state, BlockPos pos, ServerLevel level)
        {
            if (state.getBlock() == this) return 16;
            if (state.getBlock() instanceof DustBlock && state.hasProperty(LAYERS)) return state.getValue(LAYERS);
            if (state.canBeReplaced(Fluids.FLOWING_WATER)) return 0;
            if (state.isAir()) return 0;
            return -1;
        }

        protected BlockState makeFalling(BlockState state, boolean falling)
        {
            if (!falling) return this.defaultBlockState();
            return getAlternate().get().defaultBlockState().setValue(LAYERS, 16).setValue(FALLING, falling);
        }

        protected BlockState setAmount(BlockState state, int amt)
        {
            if (amt == 16) return this.defaultBlockState();
            return getAlternate().get().defaultBlockState().setValue(LAYERS, amt);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            builder.add(WATERLOGGED);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
        {
            return Shapes.block();
        }
    }
}
