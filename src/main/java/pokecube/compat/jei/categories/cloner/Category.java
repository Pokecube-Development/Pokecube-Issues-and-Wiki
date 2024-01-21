package pokecube.compat.jei.categories.cloner;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.ingredients.Pokemob;
import thut.lib.RegHelper;
import thut.lib.TComponent;

public class Category implements IRecipeCategory<Wrapper>
{
    public static final ResourceLocation GUI = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/cloner.png");

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
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(PokecubeAdv.CLONER.get()));
        this.localizedName = I18n.get("block.pokecube_adventures.cloner");
    }

    @Override
    public Component getTitle()
    {
        return TComponent.literal(this.localizedName);
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
        return RegHelper.getKey(PokecubeAdv.CLONER.get());
    }

    @Override
    public Class<? extends Wrapper> getRecipeClass()
    {
        return Wrapper.class;
    }

    @Override
    public List<Component> getTooltipStrings(final Wrapper recipe, final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(51, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        tooltips.add(TComponent.translatable("gui.jei.cloner.need_egg"));
        return tooltips;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Wrapper recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 18);
        outputSlot.addIngredient(Pokemob.TYPE, Pokemob.ALLMAP.get(recipe.wrapped.getDefault()));

        List<Ingredient> ingredients = recipe.wrapped.getInputs();
        outer:
        for (int y = 0; y < 3; ++y) for (int x = 0; x < 3; ++x)
        {
            final int index = x + y * 3;
            if (index >= ingredients.size()) break outer;
            final int dy = x == 1 ? 1 : 10;
            IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 3, y * 18 + dy);
            inputSlot.addIngredients(ingredients.get(index));
        }
    }

}
