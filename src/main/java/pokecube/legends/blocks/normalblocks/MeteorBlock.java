package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MeteorBlock extends FallingBlock
{
    private final int dustColor;

    public MeteorBlock(int num, Properties properties)
    {
        super(properties);
        this.dustColor = num;
    }

    @OnlyIn(Dist.CLIENT)
    public int getDustColor(BlockState state, BlockGetter block, BlockPos pos)
    {
        return this.dustColor;
    }
}
