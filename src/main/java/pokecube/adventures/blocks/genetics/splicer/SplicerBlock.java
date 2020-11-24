package pokecube.adventures.blocks.genetics.splicer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableBlock;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class SplicerBlock extends InteractableHorizontalBlock
{
    private static final Map<Direction, VoxelShape> SPLICER = new HashMap<>();
    public static final DirectionProperty           FACING  = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty             FIXED   = BooleanProperty.create("fixed");

    // Precise selection box
    static
    {// @formatter:off
    	SplicerBlock.SPLICER.put(Direction.NORTH,
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.07933286941284567, 0, 0.9293328694128444, 16.079332869412845, 10, 14.929332869412843),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.07933286941284567, 10, 9.929332869412843, 8.079332869412845, 16, 14.929332869412843),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0.07933286941284567, 10, 1.9293328694128444, 8.079332869412845, 11, 9.929332869412843),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.079332869412845, 10, 6.929332869412844, 14.079332869412845, 11, 11.929332869412843),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.079332869412845, 10, 1.9293328694128444, 14.079332869412845, 11, 5.929332869412844),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.079332869412845, 11, 8.929332869412843, 14.079332869412845, 12, 10.929332869412843),
          VoxelShapes.combineAndSimplify(Block.makeCuboidShape(11.079332869412845, 10.98096988312782, 8.629332869412842, 13.079332869412845, 15.48096988312782, 10.629332869412842),
    	  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.079332869412845, 13.5, 3.5293328694128423, 14.079332869412845, 15, 9.929332869412843),
		  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.779332869412844, 11.7, 2.8686726976330217, 13.279332869412844, 15.7, 4.868672697633022),
		  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.0793328694128457, 10.03806023374436, 0.9293328694128444, 7.079332869412846, 15.03806023374436, 1.9293328694128444),
		  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.079332869412846, 11, 7.9293328694128435, 6.079332869412846, 15, 8.929332869412843),
		  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.079332869412846, 11, 5.929332869412844, 7.079332869412846, 15, 6.929332869412844),
		  VoxelShapes.combineAndSimplify(Block.makeCuboidShape(5.079332869412846, 11, 3.9293328694128444, 6.079332869412846, 15, 4.929332869412844),
        		  Block.makeCuboidShape(1.0793328694128457, 11, 5.929332869412844, 2.0793328694128457, 15, 6.929332869412844),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR),IBooleanFunction.OR),
    				IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR),
                IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR)
        );
    	SplicerBlock.SPLICER.put(Direction.EAST,
    	        Stream.of(
    	                Block.makeCuboidShape(1, 0, 0, 15, 10, 16),
    	                Block.makeCuboidShape(1, 10, 0, 6, 16, 8),
    	                Block.makeCuboidShape(6, 10, 0, 14, 11, 8),
    	                Block.makeCuboidShape(4, 10, 10, 9, 11, 14),
    	                Block.makeCuboidShape(10, 10, 10, 14, 11, 14),
    	                Block.makeCuboidShape(5, 11, 10, 7, 12, 14),
    	                Block.makeCuboidShape(5.3, 10.98096988312782, 11, 7.3, 15.48096988312782, 13),
    	                Block.makeCuboidShape(6, 13.5, 10, 12.4, 15, 14),
    	                Block.makeCuboidShape(11.060660171779823, 11.7, 10.7, 13.060660171779823, 15.7, 13.2),
    	                Block.makeCuboidShape(14, 10.03806023374436, 1, 15, 15.03806023374436, 7),
    	                Block.makeCuboidShape(7, 11, 5, 8, 15, 6),
    	                Block.makeCuboidShape(9, 11, 6, 10, 15, 7),
    	                Block.makeCuboidShape(11, 11, 5, 12, 15, 6),
    	                Block.makeCuboidShape(9, 11, 1, 10, 15, 2)
    	                ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.SOUTH,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(-0.07066713058715557, 0, 0.920667130587157, 15.929332869412843, 10, 14.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.9293328694128435, 10, 0.920667130587157, 15.929332869412843, 16, 5.920667130587157),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.9293328694128435, 10, 5.920667130587157, 15.929332869412843, 11, 13.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.9293328694128435, 10, 3.920667130587157, 5.9293328694128435, 11, 8.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.9293328694128435, 10, 9.920667130587155, 5.9293328694128435, 11, 13.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.9293328694128435, 11, 4.920667130587157, 5.9293328694128435, 12, 6.920667130587157),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.9293328694128435, 10.98096988312782, 5.220667130587158, 4.9293328694128435, 15.48096988312782, 7.220667130587158),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.9293328694128435, 13.5, 5.920667130587157, 5.9293328694128435, 15, 12.320667130587157),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.7293328694128443, 11.7, 10.981327302366978, 5.229332869412844, 15.7, 12.981327302366978),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.929332869412843, 10.03806023374436, 13.920667130587155, 14.929332869412843, 15.03806023374436, 14.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.929332869412843, 11, 6.920667130587157, 10.929332869412843, 15, 7.920667130587156),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.929332869412843, 11, 8.920667130587155, 9.929332869412843, 15, 9.920667130587155),
				VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.929332869412843, 11, 10.920667130587155, 10.929332869412843, 15, 11.920667130587155),
						Block.makeCuboidShape(13.929332869412843, 11, 8.920667130587155, 14.929332869412843, 15, 9.920667130587155),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
				IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
				IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	SplicerBlock.SPLICER.put(Direction.WEST,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.0086657388256883, 0, -0.14999999999999947, 15.008665738825687, 10, 15.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.008665738825687, 10, 7.85, 15.008665738825687, 16, 15.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.0086657388256883, 10, 7.85, 10.008665738825687, 11, 15.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(7.008665738825688, 10, 1.8499999999999996, 12.008665738825687, 11, 5.85),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.0086657388256883, 10, 1.8499999999999996, 6.008665738825688, 11, 5.85),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(9.008665738825687, 11, 1.8499999999999996, 11.008665738825687, 12, 5.85),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.708665738825687, 10.98096988312782, 2.8499999999999996, 10.708665738825687, 15.48096988312782, 4.85),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.608665738825686, 13.5, 1.8499999999999996, 10.008665738825687, 15, 5.85),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.9480055670458656, 11.7, 2.6500000000000004, 4.948005567045866, 15.7, 5.15),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1.0086657388256883, 10.03806023374436, 8.849999999999998, 2.0086657388256883, 15.03806023374436, 14.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(8.008665738825687, 11, 9.849999999999998, 9.008665738825687, 15, 10.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(6.008665738825688, 11, 8.849999999999998, 7.008665738825688, 15, 9.849999999999998),
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.008665738825688, 11, 9.849999999999998, 5.008665738825688, 15, 10.849999999999998),
    					Block.makeCuboidShape(6.008665738825688, 11, 13.849999999999998, 7.008665738825688, 15, 14.849999999999998),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR),IBooleanFunction.OR),
    			IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR),
    			IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR),IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return SplicerBlock.SPLICER.get(state.get(SplicerBlock.FACING));
    }

    public SplicerBlock(final Properties properties)
    {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(SplicerBlock.FACING, Direction.NORTH).with(
                SplicerBlock.FIXED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new SplicerTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(SplicerBlock.FACING);
        builder.add(SplicerBlock.FIXED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(SplicerBlock.FACING, context.getPlacementHorizontalFacing().getOpposite())
                .with(SplicerBlock.FIXED, false);
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return InteractableBlock.RENDERSHAPE;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
