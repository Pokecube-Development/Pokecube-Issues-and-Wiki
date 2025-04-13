package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class OneWayLaboratoryGlass extends OneWayStainedGlass
{
    protected static final DirectionProperty FACING = DirectionalBlock.FACING;

    public OneWayLaboratoryGlass(DyeColor color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public DyeColor getColor()
    {
        // TODO fix this
        return DyeColor.LIGHT_BLUE;
    }
}