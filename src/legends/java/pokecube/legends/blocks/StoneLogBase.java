package pokecube.legends.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class StoneLogBase
{
    public static RotatedPillarBlock concreteLog(final MapColor mapColorSide, final MapColor mapColorTop, final SoundType sound,
                                                 final NoteBlockInstrument instrument, final float destroyTime, final float explosionResistance)
    {
        return new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(destroyTime, explosionResistance)
                .sound(sound).instrument(instrument).requiresCorrectToolForDrops()
                .mapColor((state) -> {
                    return state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? mapColorSide : mapColorTop;
                }));
    }
}
