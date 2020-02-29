package pokecube.compat.jei.categories.cloner;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeFossilRevive;

public class Category implements IRecipeCategory<RecipeFossilRevive>
{

    public Category()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IDrawable getBackground()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDrawable getIcon()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends RecipeFossilRevive> getRecipeClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTitle()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceLocation getUid()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setIngredients(final RecipeFossilRevive arg0, final IIngredients arg1)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void setRecipe(final IRecipeLayout arg0, final RecipeFossilRevive arg1, final IIngredients arg2)
    {
        // TODO Auto-generated method stub

    }

}
