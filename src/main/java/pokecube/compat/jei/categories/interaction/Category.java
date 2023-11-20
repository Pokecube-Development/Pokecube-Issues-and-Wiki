package pokecube.compat.jei.categories.interaction;

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
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.PokecubeItems;
import thut.lib.TComponent;

public class Category implements IRecipeCategory<InteractRecipe>
{
    public static final ResourceLocation GUI = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_interaction");

    public static final int width = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        this.background = guiHelper.createDrawable(Category.GUI, 29, 16, 116, 54);
        this.localizedName = I18n.get("gui.jei.pokemobs.interact");
        this.icon = guiHelper.createDrawable(Category.TABS, 48, 0, 16, 16);
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
    public Class<? extends InteractRecipe> getRecipeClass()
    {
        return InteractRecipe.class;
    }

    @Override
    public List<Component> getTooltipStrings(final InteractRecipe recipe, final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = Lists.newArrayList();
        final Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
        if (!recipe.interaction.male) tooltips.add(TComponent.translatable("gui.jei.pokemob.nogender",
                TComponent.translatable("gui.jei.pokemob.gender.male")));
        if (!recipe.interaction.female) tooltips.add(TComponent.translatable("gui.jei.pokemob.nogender",
                TComponent.translatable("gui.jei.pokemob.gender.female")));
        return tooltips;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InteractRecipe recipe, IFocusGroup focuses)
    {
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 19);
        if (recipe.to != null) outputSlot.addIngredient(Pokemob.TYPE, recipe.to);
        else if (!recipe.interaction.stacks.isEmpty())
        {
            outputSlot.addIngredients(VanillaTypes.ITEM, recipe.interaction.stacks);
        }

        IRecipeSlotBuilder inputMob = builder.addSlot(RecipeIngredientRole.INPUT, 18, 19);
        inputMob.addIngredient(Pokemob.TYPE, recipe.from);

        ItemStack needed = recipe.key;
        if (needed.isEmpty() && recipe.tag != null) needed = PokecubeItems.getStack(recipe.tag);
        if (!needed.isEmpty())
        {
            IRecipeSlotBuilder inputStack = builder.addSlot(RecipeIngredientRole.INPUT, 51, 1);
            inputStack.addItemStack(needed);
        }
    }
}
