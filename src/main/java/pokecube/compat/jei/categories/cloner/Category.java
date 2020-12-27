package pokecube.compat.jei.categories.cloner;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.ingredients.Pokemob;

public class Category implements IRecipeCategory<Wrapper>
{
    public static final ResourceLocation GUI = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png");

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    public static final int width  = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String    localizedName;
    final IGuiHelper        guiHelper;

    public Category(final IGuiHelper guiHelper)
    {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(Category.GUI, 29, 16, Category.width, Category.height);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(PokecubeAdv.CLONER.get()));
        this.localizedName = I18n.format("block.pokecube_adventures.cloner");
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
        return PokecubeAdv.CLONER.get().getRegistryName();
    }

    @Override
    public Class<? extends Wrapper> getRecipeClass()
    {
        return Wrapper.class;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(final Wrapper recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        tooltips.add(new TranslationTextComponent("gui.jei.cloner.need_egg"));
        return tooltips;
    }

    @Override
    public void setIngredients(final Wrapper recipe, final IIngredients ingredients)
    {
        ingredients.setOutput(Pokemob.TYPE, Pokemob.ALLMAP.get(recipe.wrapped.getDefault()));
        ingredients.setInputIngredients(recipe.wrapped.getInputs());
    }

    @Override
    public void setRecipe(final IRecipeLayout recipeLayout, final Wrapper recipeWrapper, final IIngredients ingredients)
    {
        final IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).init(Category.craftOutputSlot, false, Pokemob.RENDER, 94, 18, 16,
                16, 0, 0);
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 3; ++x)
            {
                final int index = Category.craftInputSlot1 + x + y * 3;
                final int dy = x == 1 ? 0 : 9;
                guiItemStacks.init(index, true, x * 18 + 2, y * 18 + dy);
            }
        guiItemStacks.set(ingredients);
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).set(ingredients);
    }

}
