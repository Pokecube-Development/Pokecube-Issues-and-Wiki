package pokecube.compat.jei.categories.cloner;

import mezz.jei.api.constants.VanillaTypes;
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
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.api.data.PokedexEntry;
import pokecube.compat.jei.Compat;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import thut.api.entity.genetics.Gene;
import thut.lib.TComponent;

import java.awt.*;
import java.util.List;

public class Category implements IRecipeCategory<RecipeClone>
{
    public static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "textures/gui/cloner.png");

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
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(PokecubeAdv.CLONER.get()));
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
    public RecipeType<RecipeClone> getRecipeType()
    {
        return Compat.clonerType;
    }

    @Override
    public void getTooltip(ITooltipBuilder builder, RecipeClone recipe, IRecipeSlotsView recipeSlotsView,
            final double mouseX, final double mouseY)
    {
        final Rectangle arrow = new Rectangle(51, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return;
        builder.add(TComponent.translatable("gui.jei.cloner.need_egg"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeClone recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 18);

        PokedexEntry entry = Database.missingno;
        List<Gene<?>> genes = recipe._genes.get(GeneticsManager.SPECIESGENE);

        final long time = System.currentTimeMillis() / 500;
        int index = genes.size() > 1 ? (int) (time % genes.size()) : 0;
        if (index < genes.size()) entry = ((SpeciesGene) genes.get(index)).getValue().getEntry();

        outputSlot.addIngredient(Pokemob.TYPE, Pokemob.ALLMAP.get(entry));

        List<Ingredient> ingredients = recipe.inputs;
        outer:
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 3; ++x)
            {
                index = x + y * 3;
                if (index >= ingredients.size()) break outer;
                int dy = x == 1 ? 1 : 10;
                IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 3, y * 18 + dy);
                inputSlot.addIngredients(ingredients.get(index));
            }
    }

}
