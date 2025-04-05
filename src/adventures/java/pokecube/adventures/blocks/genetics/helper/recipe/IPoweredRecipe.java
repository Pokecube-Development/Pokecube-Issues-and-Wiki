package pokecube.adventures.blocks.genetics.helper.recipe;

import net.minecraft.world.level.Level;

public interface IPoweredRecipe
{
    boolean complete(IPoweredProgress tile, Level world);

    int getEnergyCost(IPoweredProgress tile);
}
