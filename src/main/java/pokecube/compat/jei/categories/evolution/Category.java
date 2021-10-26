package pokecube.compat.jei.categories.evolution;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.constants.VanillaTypes;
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
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry.EvolutionData;

public class Category implements IRecipeCategory<Evolution>
{
    public static final ResourceLocation GUI  = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_evolution");

    public static final int width  = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String    localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        final ResourceLocation location = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
        this.background = guiHelper.createDrawable(location, 29, 16, Category.width, Category.height);
        this.localizedName = I18n.get("gui.jei.pokemobs");
        this.icon = guiHelper.createDrawable(Category.TABS, 32, 0, 16, 16);
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
    public Class<? extends Evolution> getRecipeClass()
    {
        return Evolution.class;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(final Evolution recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        final EvolutionData data = recipe.data;
        tooltips.addAll(data.getEvoClauses());
        return tooltips;
    }

    @Override
    public void setIngredients(final Evolution evolution, final IIngredients ingredients)
    {
        ingredients.setInput(Pokemob.TYPE, evolution.from);
        ItemStack needed = evolution.data.item;
        if (needed.isEmpty() && evolution.data.preset != null) needed = PokecubeItems.getStack(evolution.data.preset);
        if (!needed.isEmpty()) ingredients.setInput(VanillaTypes.ITEM, needed);
        ingredients.setOutput(Pokemob.TYPE, evolution.to);
    }

    @Override
    public void setRecipe(final IRecipeLayout recipeLayout, final Evolution evolution, final IIngredients ingredients)
    {
        final int out = 24;
        final IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).init(0, false, Pokemob.RENDER, 81, 15, out, out, 4, 4);
        int x = 50;
        int y = 0;
        guiItemStacks.init(1, true, x, y);
        x = 14;
        y = 15;
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).init(1, true, Pokemob.RENDER, x, y, out, out, 4, 4);
        guiItemStacks.set(ingredients);
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).set(ingredients);
    }

}
