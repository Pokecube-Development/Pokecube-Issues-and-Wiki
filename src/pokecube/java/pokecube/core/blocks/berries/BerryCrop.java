package pokecube.core.blocks.berries;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.BlockUtils;
import pokecube.core.items.berries.BerryManager;

public class BerryCrop extends CropBlock
{
    final int index;

    protected int index()
    {
        return this.index;
    }

    public static final MapCodec<BerryCrop> CODEC = BlockUtils.singleArgumentCodec(BerryCrop::new,
            Codec.INT.fieldOf("index").forGetter(BerryCrop::index));

    @Override
    public MapCodec<? extends CropBlock> codec()
    {
        return CODEC;
    }

    public BerryCrop(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
    }

    @Override
    protected ItemLike getBaseSeedId()
    {
        return BerryManager.berryItems.get(this.index).get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos)
    {
        return super.mayPlaceOn(state, level, pos) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK);
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state)
    {
        // Unlike vanilla crops, we still do something when max age
        return true;
    }

    @Override
    public void randomTick(BlockState state, final ServerLevel worldIn, final BlockPos pos, final RandomSource random)
    {
        super.randomTick(state, worldIn, pos, random);
        if (!worldIn.isPositionEntityTicking(pos)) return;
        state = worldIn.getBlockState(pos);
        // This can happen if the super randomTick call invalidates this block,
        // like growing, event, etc.
        if (state.getBlock() != this) return;
        final int age = this.getAge(state);
        if (age == this.getMaxAge())
        {
            final Supplier<TreeGrower> grower = BerryGenManager.getTree(worldIn, random, this.index);
            final BlockPos up = pos.above();
            if (grower != null)
                grower.get().growTree(worldIn, worldIn.getChunkSource().getGenerator(), pos, state, random);
            else if (worldIn.isEmptyBlock(up))
                worldIn.setBlockAndUpdate(up, BerryManager.berryFruits.get(this.index).get().defaultBlockState());
        }
    }

    @Override
    public void performBonemeal(final ServerLevel worldIn, final RandomSource rand, final BlockPos pos,
            final BlockState state)
    {
        super.performBonemeal(worldIn, rand, pos, state);
        final int age = this.getAge(worldIn.getBlockState(pos));
        if (age == this.getMaxAge())
        {
            final Supplier<TreeGrower> grower = BerryGenManager.getTree(worldIn, rand, this.index);
            final BlockPos up = pos.above();
            if (grower != null)
                grower.get().growTree(worldIn, worldIn.getChunkSource().getGenerator(), pos, state, rand);
            else if (worldIn.isEmptyBlock(up))
                worldIn.setBlockAndUpdate(up, BerryManager.berryFruits.get(this.index).get().defaultBlockState());
        }
    }
}
