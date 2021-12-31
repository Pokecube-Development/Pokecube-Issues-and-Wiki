package thut.concrete.block.entity;

import net.minecraft.core.BlockPos;
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
        for (int i = 1; i < 40; i++)
        {
            BlockPos pos = this.getBlockPos().above(i);
            BlockState state = this.level.getBlockState(pos);
            if (!state.hasProperty(MoltenBlock.HEATED) || !state.getValue(MoltenBlock.HEATED))
            {
                level.setBlock(pos, Concrete.MOLTEN_BLOCK.get().defaultBlockState().setValue(MoltenBlock.HEATED, true),
                        3);
                return;
            }
        }
    }

}
