package thut.api.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public interface IDyedBlock
{
    DyeColor getColour();

    Block getFor(DyeColor c);
}
