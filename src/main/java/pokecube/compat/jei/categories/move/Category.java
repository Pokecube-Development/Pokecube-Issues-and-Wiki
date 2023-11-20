package pokecube.compat.jei.categories.move;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.moves.MovesUtils;
import pokecube.core.recipes.MoveRecipes.MoveRecipe;
import thut.lib.TComponent;

public class Category implements IRecipeCategory<MoveRecipe>
{
    public static final ResourceLocation GUI = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/move_recipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_move");

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    final IGuiHelper guiHelper;

    public Category(final IGuiHelper guiHelper)
    {
        this.guiHelper = guiHelper;
        this.background = guiHelper.createDrawable(Category.GUI, 29, 16, Category.width, Category.height);
        this.icon = guiHelper.createDrawable(Category.TABS, 0, 0, 16, 16);
        this.localizedName = I18n.get("gui.jei.pokemobs.moves");
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
    public Component getTitle()
    {
        return TComponent.literal(this.localizedName);
    }

    @Override
    public ResourceLocation getUid()
    {
        return Category.GUID;
    }

    @Override
    public Class<? extends MoveRecipe> getRecipeClass()
    {
        return MoveRecipe.class;
    }

    @Override
    public List<Component> getTooltipStrings(final MoveRecipe recipe, final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;

        if (recipe.matchedMoves.size() > 4)
        {
            final long time = System.currentTimeMillis() / 500;
            for (int i = 0; i < 4; i++)
            {
                final String name = recipe.matchedMoves.get((int) ((time - i) % recipe.matchedMoves.size()));
                tooltips.add(MovesUtils.getMoveName(name, null));
            }
        }
        else for (final String name : recipe.matchedMoves) tooltips.add(MovesUtils.getMoveName(name, null));
        return tooltips;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoveRecipe recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19);
        outputSlot.addItemStack(recipe.getResultItem());

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        outer:
        for (int y = 0; y < 3; ++y) for (int x = 0; x < 3; ++x)
        {
            final int index = x + y * 3;
            if (index >= ingredients.size()) break outer;
            IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 3, y * 18 + 1);
            inputSlot.addIngredients(ingredients.get(index));
        }
    }

}
