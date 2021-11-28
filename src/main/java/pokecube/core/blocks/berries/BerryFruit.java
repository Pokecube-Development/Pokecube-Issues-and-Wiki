package pokecube.core.blocks.berries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.items.berries.BerryManager;

public class BerryFruit extends BushBlock
{
    public static final VoxelShape BERRY_UP   = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    public static final VoxelShape BERRY_DOWN = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 6.0D, 13.5D);

    // Precise selection box @formatter:off
    private static final VoxelShape PECHA_BERRY = Shapes.or(
      Block.box(5.5, 12, 6.5, 10.5, 16, 9.5),
      Block.box(6, 10.5, 6.5, 10, 12, 9.5))
      .optimize();

    private static final VoxelShape ASPEAR_BERRY = Shapes.or(
      Block.box(5, 0, 5, 11, 6, 11))
      .optimize();

    private static final VoxelShape LEPPA_BERRY = Shapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(6, 10.5, 6, 10, 15.5, 10))
      .optimize();

    private static final VoxelShape ORAN_BERRY = Shapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(5.5, 10.5, 5.5, 10.5, 15.5, 10.5))
      .optimize();

    private static final VoxelShape LUM_BERRY = Shapes.or(
      Block.box(2, 0, 2, 14, 6, 14))
      .optimize();

    private static final VoxelShape SITRUS_BERRY = Shapes.or(
      Block.box(7, 15.5, 7, 9, 16, 9),
      Block.box(6, 13.5, 6, 10, 15.5, 10),
      Block.box(5.5, 9.5, 5.5, 10.5, 13.5, 10.5))
      .optimize();

    private static final VoxelShape NANAB_BERRY = Shapes.or(
      Block.box(4, 7.6, 4, 12, 16, 12))
      .optimize();

    private static final VoxelShape PINAP_BERRY = Shapes.or(
      Block.box(4, 0, 4, 12, 15, 12))
      .optimize();

    private static final VoxelShape POMEG_BERRY = Shapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(6, 14, 6, 10, 15, 10),
      Block.box(5.5, 10, 5.5, 10.5, 14, 10.5),
      Block.box(6, 9, 6, 10, 10, 10))
      .optimize();

    private static final VoxelShape KELPSY_BERRY = Shapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(6.5, 7, 6.5, 9.5, 15, 9.5),
      Block.box(7.5, 6, 7.5, 8.5, 7, 8.5))
      .optimize();

    private static final VoxelShape QUALOT_BERRY = Shapes.or(
      Block.box(7.5, 14.5, 7.5, 8.5, 16, 8.5),
      Block.box(6, 13.5, 6, 10, 14.5, 10),
      Block.box(5.5, 8.5, 5.5, 10.5, 13.5, 10.5),
      Block.box(6, 7.5, 6, 10, 8.5, 10))
      .optimize();

    private static final VoxelShape HONDEW_BERRY = Shapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(5.5, 9, 5.5, 10.5, 15, 10.5))
      .optimize();

    private static final VoxelShape GREPA_BERRY = Shapes.or(
      Block.box(7.5, 15, 7.5, 8.5, 16, 8.5),
      Block.box(5.5, 10, 5.5, 10.5, 15, 10.5))
      .optimize();

    private static final VoxelShape TAMATO_BERRY = Shapes.or(
      Block.box(6, 11.5, 6, 10, 15.5, 10),
      Block.box(7, 15.5, 7, 9, 16, 9))
      .optimize();

    private static final VoxelShape ENIGMA_BERRY = Shapes.or(
      Block.box(7, 14, 7, 9, 16, 9),
      Block.box(6.5, 13, 6.5, 9.5, 14, 9.5),
      Block.box(6, 7, 6, 10, 13, 10))
      .optimize();

    private static final VoxelShape ROWAP_BERRY = Shapes.or(
      Block.box(0, 0, 0, 16, 7, 16))
      .optimize();
    //@formatter:on

    private final int ind;

    public BerryFruit(final Properties builder, final int ind)
    {
        super(builder);
        this.ind = ind;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    public ItemStack getCloneItemStack(final BlockGetter worldIn, final BlockPos pos, final BlockState state)
    {
        return new ItemStack(BerryManager.berryItems.get(this.ind));
    }

    @Override
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos)
    {
        return state.getShape(worldIn, pos);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final Vec3 vec3d = state.getOffset(worldIn, pos);
        if (this.ind == 3) return BerryFruit.PECHA_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 5) return BerryFruit.ASPEAR_BERRY;
        else if (this.ind == 6) return BerryFruit.LEPPA_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 7) return BerryFruit.ORAN_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 9) return BerryFruit.LUM_BERRY;
        else if (this.ind == 10) return BerryFruit.SITRUS_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 18) return BerryFruit.NANAB_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 20) return BerryFruit.PINAP_BERRY;
        else if (this.ind == 21) return BerryFruit.POMEG_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 22) return BerryFruit.KELPSY_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 23) return BerryFruit.QUALOT_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 24) return BerryFruit.HONDEW_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 25) return BerryFruit.GREPA_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 26) return BerryFruit.TAMATO_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 60) return BerryFruit.ENIGMA_BERRY.move(vec3d.x, vec3d.y, vec3d.z);
        else if (this.ind == 64) return BerryFruit.ROWAP_BERRY;
        else return BerryGenManager.trees.containsKey(this.ind) ? BerryFruit.BERRY_UP : BerryFruit.BERRY_DOWN;
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType()
    {
        if (this.ind == 3 || this.ind == 6 || this.ind == 7 || this.ind == 10 || this.ind == 18 || this.ind == 21
                || this.ind == 22 || this.ind == 23 || this.ind == 24 || this.ind == 25 || this.ind == 26
                || this.ind == 60) return BlockBehaviour.OffsetType.XZ;
        return BlockBehaviour.OffsetType.NONE;
    }

    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter worldIn, final BlockPos pos)
    {
        return state.getBlock() instanceof BerryCrop || worldIn.getBlockState(pos.above(2))
                .getBlock() instanceof BerryLeaf;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockState state2 = BerryManager.berryCrops.get(this.ind).defaultBlockState();
        if (!world.isClientSide)
        {
            if (world.getBlockState(pos.below()).is(BerryManager.berryCrops.get(this.ind))) world.setBlockAndUpdate(pos
                    .below(), state2.setValue(CropBlock.AGE, Integer.valueOf(5)));
            world.destroyBlock(pos, true);
        }
        return InteractionResult.SUCCESS;
    }
}
