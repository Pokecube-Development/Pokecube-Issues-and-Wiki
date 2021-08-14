package pokecube.core.blocks.berries;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.items.berries.BerryManager;

public class BerryFruit extends BushBlock
{
    public static final VoxelShape BERRY_UP   = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    public static final VoxelShape BERRY_DOWN = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 6.0D, 13.5D);

    // Precise selection box
    private static final VoxelShape PECHA_BERRY = VoxelShapes.or(
      Block.box(5.5, 12, 6.5, 10.5, 16, 9.5),
      Block.box(6, 10.5, 6.5, 10, 12, 9.5))
      .optimize();

    private static final VoxelShape ASPEAR_BERRY = VoxelShapes.or(
      Block.box(5, 0, 5, 11, 6, 11))
      .optimize();

    private static final VoxelShape LEPPA_BERRY = VoxelShapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(6, 10.5, 6, 10, 15.5, 10))
      .optimize();

    private static final VoxelShape ORAN_BERRY = VoxelShapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(5.5, 10.5, 5.5, 10.5, 15.5, 10.5))
      .optimize();

    private static final VoxelShape LUM_BERRY = VoxelShapes.or(
      Block.box(2, 0, 2, 14, 6, 14))
      .optimize();

    private static final VoxelShape SITRUS_BERRY = VoxelShapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(6, 13.5, 6, 10, 15.5, 10),
      Block.box(5.5, 9.5, 5.5, 10.5, 13.5, 10.5))
      .optimize();

    private static final VoxelShape NANAB_BERRY = VoxelShapes.or(
      Block.box(4, 7.6, 4, 12, 16, 12))
      .optimize();

    private static final VoxelShape PINAP_BERRY = VoxelShapes.or(
      Block.box(5, 0, 5, 11, 15, 11))
      .optimize();

    private static final VoxelShape POMEG_BERRY = VoxelShapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(6, 14, 6, 10, 15, 10),
      Block.box(5.5, 10, 5.5, 10.5, 14, 10.5),
      Block.box(6, 9, 6, 10, 10, 10))
      .optimize();

    private static final VoxelShape KELPSY_BERRY = VoxelShapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(6.5, 7, 6.5, 9.5, 15, 9.5),
      Block.box(7.5, 6, 7.5, 8.5, 7, 8.5))
      .optimize();

    private static final VoxelShape QUALOT_BERRY = VoxelShapes.or(
      Block.box(7.5, 14.5, 7.5, 8.5, 16, 8.5),
      Block.box(6, 13.5, 6, 10, 14.5, 10),
      Block.box(5.5, 8.5, 5.5, 10.5, 13.5, 10.5),
      Block.box(6, 7.5, 6, 10, 8.5, 10))
      .optimize();

    private static final VoxelShape HONDEW_BERRY = VoxelShapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(5.5, 9, 5.5, 10.5, 15, 10.5))
      .optimize();

    private static final VoxelShape GREPA_BERRY = VoxelShapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(5.5, 10, 5.5, 10.5, 15, 10.5))
      .optimize();

    private static final VoxelShape TAMATO_BERRY = VoxelShapes.or(
      Block.box(6, 11.5, 6, 10, 15.5, 10),
      Block.box(7, 15.5, 7, 9, 16, 9))
      .optimize();

    private static final VoxelShape ENIGMA_BERRY = VoxelShapes.or(
      Block.box(7, 14, 7, 9, 16, 9),
      Block.box(6.5, 13, 6.5, 9.5, 14, 9.5),
      Block.box(6, 7, 6, 10, 13, 10))
      .optimize();

    private static final VoxelShape ROWAP_BERRY = VoxelShapes.or(
      Block.box(3, 0, 3, 13, 7, 13))
      .optimize();

    public final Integer index;
    private final int    ind;

    public BerryFruit(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
        this.ind = index;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return VoxelShapes.empty();
    }

    @Override
    public ItemStack getCloneItemStack(final IBlockReader worldIn, final BlockPos pos, final BlockState state)
    {
        return new ItemStack(BerryManager.berryItems.get(this.ind));
    }

    @Override
    public VoxelShape getOcclusionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return state.getShape(worldIn, pos);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
    	Vector3d vec3d = state.getOffset(worldIn, pos);
        if (this.index == 3) { return BerryFruit.PECHA_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 5) { return BerryFruit.ASPEAR_BERRY; }
        else if (this.index == 6) { return BerryFruit.LEPPA_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 7) { return BerryFruit.ORAN_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 9) { return BerryFruit.LUM_BERRY; }
        else if (this.index == 10) { return BerryFruit.SITRUS_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 18) { return BerryFruit.NANAB_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 20) { return BerryFruit.PINAP_BERRY; }
        else if (this.index == 21) { return BerryFruit.POMEG_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 22) { return BerryFruit.KELPSY_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 23) { return BerryFruit.QUALOT_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 24) { return BerryFruit.HONDEW_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 25) { return BerryFruit.GREPA_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 26) { return BerryFruit.TAMATO_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 60) { return BerryFruit.ENIGMA_BERRY.move(vec3d.x, vec3d.y, vec3d.z); }
        else if (this.index == 64) { return BerryFruit.ROWAP_BERRY; }
        else return BerryGenManager.trees.containsKey(this.index) ? BerryFruit.BERRY_UP : BerryFruit.BERRY_DOWN;
    }
    
    @Override
    public AbstractBlock.OffsetType getOffsetType() {
        if (this.index == 3 || this.index == 6 || this.index == 7 || this.index == 10 || this.index == 18 || this.index == 21 || 
        		this.index == 22 || this.index == 23 || this.index == 24 || this.index == 25 || this.index == 26 || this.index == 60)
        	{
        		return AbstractBlock.OffsetType.XZ;
        	}
        return AbstractBlock.OffsetType.NONE;
    }

    @Override
    protected boolean mayPlaceOn(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return state.getBlock() instanceof BerryCrop || worldIn.getBlockState(pos.above(2))
                .getBlock() instanceof BerryLeaf;
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (!world.isClientSide) world.destroyBlock(pos, true);
        return ActionResultType.CONSUME;
    }
}
