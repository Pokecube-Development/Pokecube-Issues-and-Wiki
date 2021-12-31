package thut.concrete.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.block.ITickTile;
import thut.api.block.flowing.MoltenBlock;
import thut.concrete.Concrete;

public class VolcanoEntity extends BlockEntity implements ITickTile
{

    public VolcanoEntity(BlockPos p_155229_, BlockState p_155230_)
    {
        super(Concrete.VOLCANO_TYPE.get(), p_155229_, p_155230_);
    }

    @Override
    public void tick()
    {
        if (this.level.isClientSide) return;

        for (int i = 1; i < 10; i++)
        {
            BlockPos pos = this.getBlockPos().above(i);
            for (Direction d : Direction.values())
            {
                if (d == Direction.UP || d == Direction.DOWN) continue;
                pos = pos.relative(d);
                BlockState state = this.level.getBlockState(pos);
                if (!state.hasProperty(MoltenBlock.HEATED) || !state.getValue(MoltenBlock.HEATED))
                {
                    level.setBlock(pos,
                            Concrete.MOLTEN_BLOCK.get().defaultBlockState().setValue(MoltenBlock.HEATED, true), 3);
                    break;
                }
            }
        }

        if (level.random.nextDouble() < 0.0)
        {
            int r = 3;

            for (int x = -r; x <= r; x++) for (int y = -2; y <= 2; y++) for (int z = -r; z <= r; z++)
            {
                BlockPos pos = this.getBlockPos().above(38 + y).north(x).east(z);
                level.setBlock(pos, Concrete.MOLTEN_BLOCK.get().defaultBlockState().setValue(MoltenBlock.HEATED, true),
                        3);
            }
        }
        return;
    }

}
