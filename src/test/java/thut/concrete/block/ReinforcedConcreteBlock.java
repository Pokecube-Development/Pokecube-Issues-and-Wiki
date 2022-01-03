package thut.concrete.block;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;

public class ReinforcedConcreteBlock extends RebarBlock implements IDyedBlock
{
    public static final Map<ResourceLocation, RegistryObject<Block>> REGMAP = Maps.newHashMap();

    private static final Map<DyeColor, ReinforcedConcreteBlock> BYCOLOR = Maps.newHashMap();

    public static RegistryObject<Block>[] makeDry(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props, DyeColor colour)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<Block>[] arr = (RegistryObject<Block>[]) Array.newInstance(RegistryObject.class, 2);

        RegistryObject<Block> layer_reg = BLOCKS.register(layer, () -> new ReinforcedConcreteBlock(layer_props, colour)
                .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<Block> block_reg = BLOCKS.register(block, () -> new FullDry(block_props, colour));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    private Supplier<Block> convert;

    public ReinforcedConcreteBlock(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
        BYCOLOR.put(colour, this);
    }

    protected void initStateDefinition()
    {
        registerDefaultState(getStateDefinition().any().setValue(IFlowingBlock.WATERLOGGED, false).setValue(LAYERS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, IFlowingBlock.WATERLOGGED, LAYERS);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return false;
    }

    public ReinforcedConcreteBlock alternateBlock(Supplier<Block> supplier)
    {
        this.convert = supplier;
        return this;
    }

    @Override
    public BlockState getMergeResult(BlockState mergeFrom, BlockState mergeInto, BlockPos posTo, ServerLevel level)
    {
        return mergeInto;
    }

    @Override
    public boolean flows()
    {
        return false;
    }

    @Override
    public Block getAlternate()
    {
        return convert.get();
    }

    @Override
    public int getFlowRate()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getFallRate()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random)
    {
        // NO OP
    }

    @Override
    public Block getFor(DyeColor c)
    {
        return BYCOLOR.get(c);
    }

    @Override
    public DyeColor getColour()
    {
        return colour;
    }

    public static class FullDry extends Block implements IDyedBlock
    {
        private static final Map<DyeColor, FullDry> BYCOLOR = Maps.newHashMap();

        private final DyeColor colour;

        public FullDry(Properties properties, DyeColor colour)
        {
            super(properties);
            this.colour = colour;
            BYCOLOR.put(colour, this);
        }

        @Override
        public DyeColor getColour()
        {
            return colour;
        }

        @Override
        public Block getFor(DyeColor c)
        {
            return BYCOLOR.get(c);
        }
    }
}
