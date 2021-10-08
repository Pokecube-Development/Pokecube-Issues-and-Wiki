package pokecube.compat.jei.categories.interaction;

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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class Category implements IRecipeCategory<InteractRecipe>
{
    public static final ResourceLocation GUI  = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/evorecipe.png");
    public static final ResourceLocation TABS = new ResourceLocation(PokecubeAdv.MODID, "textures/gui/jeitabs.png");
    public static final ResourceLocation GUID = new ResourceLocation(PokecubeAdv.MODID, "pokemob_interaction");

    public static final int width  = 116;
    public static final int height = 54;

    private final IDrawable background;
    private final IDrawable icon;
    private final String    localizedName;

    public Category(final IGuiHelper guiHelper)
    {
        this.background = guiHelper.createDrawable(Category.GUI, 29, 16, 116, 54);
        this.localizedName = I18n.get("gui.jei.pokemobs.interact");
        this.icon = guiHelper.createDrawable(Category.TABS, 48, 0, 16, 16);
    }

    @Override
    public Component getTitle()
    {
        return new TextComponent(this.localizedName);
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
        if (!recipe.interaction.male) tooltips.add(new TranslatableComponent("gui.jei.pokemob.nogender",
                new TranslatableComponent("gui.jei.pokemob.gender.male")));
        if (!recipe.interaction.female) tooltips.add(new TranslatableComponent("gui.jei.pokemob.nogender",
                new TranslatableComponent("gui.jei.pokemob.gender.female")));
        return tooltips;
    }

    @Override
    public void setIngredients(final InteractRecipe interaction, final IIngredients ingredients)
    {
        ingredients.setInput(Pokemob.TYPE, interaction.from);
        ItemStack needed = interaction.key;
        if (needed.isEmpty() && interaction.tag != null) needed = PokecubeItems.getStack(interaction.tag);
        if (!needed.isEmpty()) ingredients.setInput(VanillaTypes.ITEM, needed);
        if (!interaction.interaction.stacks.isEmpty()) ingredients.setOutputs(VanillaTypes.ITEM,
                interaction.interaction.stacks);
        else if (interaction.interaction.lootTable != null) PokecubeCore.LOGGER.debug("me no know what to do here...");
        if (interaction.to != null) ingredients.setOutput(Pokemob.TYPE, interaction.to);
    }

    @Override
    public void setRecipe(final IRecipeLayout recipeLayout, final InteractRecipe evolution,
            final IIngredients ingredients)
    {
        final int out = 24;
        final IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(Pokemob.TYPE).init(0, false, Pokemob.RENDER, 81, 15, out, out, 4, 4);
        recipeLayout.getIngredientsGroup(VanillaTypes.ITEM).init(0, false, 84, 18);
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
