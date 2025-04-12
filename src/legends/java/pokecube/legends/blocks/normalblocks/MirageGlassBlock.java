package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MirageGlassBlock extends StainedGlassBlock implements BeaconBeamBlock
{
    public MirageGlassBlock(DyeColor color, final Properties properties)
    {
        super(color, properties);
    }

    @Override
    public DyeColor getColor()
    {
        return DyeColor.CYAN;
    }
}