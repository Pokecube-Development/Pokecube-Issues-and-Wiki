package pokecube.legends.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class StoneLogBase
{
    public static RotatedPillarBlock concreteLog(final MaterialColor color1, final MaterialColor color2, final Material material,
                                                 final float hardness, final float resistance, final SoundType sound)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of(material, (state) ->
        {
            return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? color1 : color2;
        }).strength(hardness, resistance).sound(sound).requiresCorrectToolForDrops());
    }
}
