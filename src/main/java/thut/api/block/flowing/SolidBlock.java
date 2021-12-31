package thut.api.block.flowing;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class SolidBlock extends DustBlock
{
    public static final Map<ResourceLocation, RegistryObject<DustBlock>> REGMAP = Maps.newHashMap();

    public static RegistryObject<DustBlock>[] makeSolid(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<DustBlock>[] arr = (RegistryObject<DustBlock>[]) Array.newInstance(RegistryObject.class, 2);

        RegistryObject<DustBlock> layer_reg = BLOCKS.register(layer,
                () -> new SolidBlock(layer_props).alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<DustBlock> block_reg = BLOCKS.register(block,
                () -> new FullSolid(block_props).alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    public SolidBlock(Properties properties)
    {
        super(properties);
        this.flows = false;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        // No tick
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        // NO OP
    }

    public static class FullSolid extends SolidBlock
    {

        public FullSolid(Properties properties)
        {
            super(properties);
        }

        protected int getExistingAmount(BlockState state, BlockPos pos, ServerLevel level)
        {
            if (state.getBlock() == this) return -1;
            if (state.getBlock() == getAlternate().get()) return state.getValue(LAYERS);
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
