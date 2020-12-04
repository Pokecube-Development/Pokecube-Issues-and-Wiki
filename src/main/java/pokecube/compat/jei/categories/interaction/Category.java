package pokecube.compat.jei.categories.interaction;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;

public class Category implements IRecipeCategory<InteractRecipe>
{
    public static final ResourceLocation GUI  = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_interaction");

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    public static final int width  = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String    localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        final ResourceLocation location = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
        this.background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        this.localizedName = Translator.translateToLocal("gui.jei.pokemobs.interact");
        this.icon = guiHelper.createDrawable(Category.TABS, 48, 0, 16, 16);
    }

    @Override
    public String getTitle()
    {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @Override
    public ResourceLocation getUid()
    {
        return Category.GUID;
    }

    @Override
    public Class<? extends InteractRecipe> getRecipeClass()
    {
        return InteractRecipe.class;
    }

    @Override
    public void setIngredients(final InteractRecipe arg0, final IIngredients arg1)
    {

    }

    @Override
    public void setRecipe(final IRecipeLayout arg0, final InteractRecipe arg1, final IIngredients arg2)
    {

    }

}
