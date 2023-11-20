package pokecube.compat.jei.categories.evolution;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.compat.jei.ingredients.Pokemob;
import thut.lib.TComponent;

public class Category implements IRecipeCategory<Evolution>
{
    public static final ResourceLocation GUI = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_evolution");

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        final ResourceLocation location = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
        this.background = guiHelper.createDrawable(location, 29, 16, Category.width, Category.height);
        this.localizedName = I18n.get("gui.jei.pokemobs");
        this.icon = guiHelper.createDrawable(Category.TABS, 32, 0, 16, 16);
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
        return Category.GUID;
    }

    @Override
    public Class<? extends Evolution> getRecipeClass()
    {
        return Evolution.class;
    }

    @Override
    public List<Component> getTooltipStrings(final Evolution recipe, final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        final EvolutionData data = recipe.data;
        tooltips.addAll(data.getEvoClauses());
        return tooltips;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Evolution recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 19);
        outputSlot.addIngredient(Pokemob.TYPE, recipe.to);

        IRecipeSlotBuilder inputMob = builder.addSlot(RecipeIngredientRole.INPUT, 18, 19);
        inputMob.addIngredient(Pokemob.TYPE, recipe.from);

//        ItemStack needed = recipe.data.item;
//        if (needed.isEmpty() && recipe.data.preset != null) needed = PokecubeItems.getStack(recipe.data.preset);
//        if (!needed.isEmpty())
//        {
//            IRecipeSlotBuilder inputStack = builder.addSlot(RecipeIngredientRole.INPUT, 51, 1);
//            inputStack.addItemStack(needed);
//        }
    }

}
