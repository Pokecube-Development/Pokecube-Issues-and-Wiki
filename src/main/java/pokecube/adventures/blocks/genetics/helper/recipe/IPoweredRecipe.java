package pokecube.adventures.blocks.genetics.helper.recipe;

public interface IPoweredRecipe
{
    boolean complete(IPoweredProgress tile);

    int getEnergyCost();
}
