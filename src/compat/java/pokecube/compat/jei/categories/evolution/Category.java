package pokecube.compat.jei.categories.evolution;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.data.pokedex.conditions.HasHeldItem;
import pokecube.compat.jei.Compat;
import pokecube.compat.jei.ingredients.Pokemob;

import java.awt.*;

public class Category implements IRecipeCategory<Evolution>
{
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "pokemob_evolution");

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
                "textures/gui/evorecipe.png");
        this.background = guiHelper.createDrawable(location, 29, 16, Category.width, Category.height);
        this.localizedName = I18n.get("gui.jei.pokemobs");
        this.icon = guiHelper.createDrawable(Category.TABS, 32, 0, 16, 16);
    }

    @Override
    public Component getTitle()
    {
        return Component.literal(this.localizedName);
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
    public RecipeType<Evolution> getRecipeType()
    {
        return Compat.evoType;
    }

    @Override
    public void getTooltip(ITooltipBuilder builder, Evolution recipe, IRecipeSlotsView recipeSlotsView,
            final double mouseX, final double mouseY)
    {
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return;
        final EvolutionData data = recipe.data;
        builder.addAll(data.getEvoClauses());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Evolution recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 19);
        outputSlot.addIngredient(Pokemob.TYPE, recipe.to);

        IRecipeSlotBuilder inputMob = builder.addSlot(RecipeIngredientRole.INPUT, 18, 19);
        inputMob.addIngredient(Pokemob.TYPE, recipe.from);

        ItemStack stack = ItemStack.EMPTY;
        for (var e : recipe.data.data._bits)
        {
            if (e instanceof HasHeldItem item)
            {
                stack = item._value;
                if (item._tag != null)
                {
                    // TODO pick a random item in the tag?
                }
            }
        }
        IRecipeSlotBuilder inputItem = builder.addSlot(RecipeIngredientRole.INPUT, 51, 1);
        inputItem.addIngredients(Ingredient.of(stack));
    }

}
