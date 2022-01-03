package thut.concrete.block;

import java.lang.reflect.Array;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.SolidBlock;

public class ConcreteBlock extends SolidBlock implements IDyedBlock
{
    public static RegistryObject<FlowingBlock>[] makeDry(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props, DyeColor colour)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<FlowingBlock>[] arr = (RegistryObject<FlowingBlock>[]) Array.newInstance(RegistryObject.class,
                2);

        RegistryObject<FlowingBlock> layer_reg = BLOCKS.register(layer,
                () -> new ConcreteBlock(layer_props, colour).alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);
        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block,
                () -> new FullDry(block_props, colour).alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    private static final Map<DyeColor, ConcreteBlock> BYCOLOR = Maps.newHashMap();

    private final DyeColor colour;

    public ConcreteBlock(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
        if (this.getClass() == ConcreteBlock.class) BYCOLOR.put(colour, this);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
        builder.add(LAYERS);
        builder.add(WATERLOGGED);
    }

    @Override
    protected void initStateDefinition()
    {
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)).setValue(WATERLOGGED,
                Boolean.valueOf(false)));
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

    public static class FullDry extends ConcreteBlock
    {

        private static final Map<DyeColor, FullDry> BYCOLOR = Maps.newHashMap();

        public FullDry(Properties properties, DyeColor colour)
        {
            super(properties, colour);
            BYCOLOR.put(colour, this);
        }

        @Override
        public boolean isFullBlock()
        {
            return true;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
        }

        protected void initStateDefinition()
        {
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
        {
            return Shapes.block();
        }

        @Override
        public Block getFor(DyeColor c)
        {
            return BYCOLOR.get(c);
        }
    }
}
