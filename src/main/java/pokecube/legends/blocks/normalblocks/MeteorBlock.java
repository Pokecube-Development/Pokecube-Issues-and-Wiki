package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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
    public int getDustColor(BlockState state, IBlockReader block, BlockPos pos)
    {
        return this.dustColor;
    }
}
