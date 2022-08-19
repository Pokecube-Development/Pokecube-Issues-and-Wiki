package pokecube.legends.blocks.flowing;

import java.lang.reflect.Array;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.block.flowing.SolidBlock;

public class MoltenMeteorBlock extends MoltenBlock
{
    public static RegistryObject<FlowingBlock>[] makeLava(DeferredRegister<Block> BLOCKS, String modid, String layer,
            String block, BlockBehaviour.Properties layer_props, BlockBehaviour.Properties block_props,
            ResourceLocation solid_layer, ResourceLocation solid_block)
    {
        ResourceLocation layer_id = new ResourceLocation(modid, layer);
        ResourceLocation block_id = new ResourceLocation(modid, block);

        @SuppressWarnings("unchecked")
        RegistryObject<FlowingBlock>[] arr = (RegistryObject<FlowingBlock>[]) Array.newInstance(RegistryObject.class,
                2);

        RegistryObject<FlowingBlock> layer_reg = BLOCKS.register(layer,
                () -> new PartialMolten(layer_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_layer).get())
                        .alternateBlock(() -> REGMAP.get(block_id).get()));
        REGMAP.put(layer_id, layer_reg);

        RegistryObject<FlowingBlock> block_reg = BLOCKS.register(block,
                () -> new FullMolten(block_props).solidBlock(() -> SolidBlock.REGMAP.get(solid_block).get())
                        .alternateBlock(() -> REGMAP.get(layer_id).get()));
        REGMAP.put(block_id, block_reg);

        arr[0] = layer_reg;
        arr[1] = block_reg;

        return arr;
    }

    protected MoltenMeteorBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void onHarden(BlockState state, BlockState solidTo, ServerLevel level, BlockPos pos, Random random)
    {
        super.onHarden(state, solidTo, level, pos, random);
    }
    
    public static class FullMolten extends MoltenMeteorBlock
    {

        public FullMolten(Properties properties)
        {
            super(properties);
        }

        @Override
        public boolean isFullBlock()
        {
            return true;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            builder.add(WATERLOGGED);
            builder.add(HEATED);
            builder.add(VISCOSITY);
        }

        protected void initStateDefinition()
        {
            this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false))
                    .setValue(HEATED, Boolean.valueOf(false)).setValue(VISCOSITY, 4));
        }

        @Override
        public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos)
        {
            return Shapes.block();
        }
    }

    public static class PartialMolten extends MoltenMeteorBlock
    {
        public PartialMolten(Properties properties)
        {
            super(properties);
        }
    }
}
