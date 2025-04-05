package pokecube.adventures.blocks.afa;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.block.ITickTile;

public class AfaBlock extends InteractableHorizontalBlock implements EntityBlock
{
    public static final MapCodec<AfaBlock> CODEC = simpleCodec(AfaBlock::new);

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return CODEC;
    }

    public AfaBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new AfaTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }

}
