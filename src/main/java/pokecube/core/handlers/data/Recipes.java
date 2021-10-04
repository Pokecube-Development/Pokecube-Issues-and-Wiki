package pokecube.core.handlers.data;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import pokecube.core.handlers.ItemGenerator;

public class Recipes extends RecipeProvider implements IConditionBuilder
{

    public Recipes(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    protected void addConvertRecipe(final Consumer<FinishedRecipe> consumer, final Block from, final Block to,
            final int number)
    {
        final ResourceLocation id = new ResourceLocation(to.getRegistryName().getNamespace(), from.getRegistryName()
                .getPath() + "-" + to.getRegistryName().getPath());
        //@formatter:off
        ConditionalRecipe.builder()
        .addCondition(TrueCondition.INSTANCE)
        .addRecipe(
            ShapelessRecipeBuilder.shapeless(to, number)
            .requires(from)
            .group(to.getRegistryName().getNamespace())
            .unlockedBy("has_from", RecipeProvider.has(from))
            ::save
        )
        .build(consumer, id);
        //@formatter:on
    }

    @Override
    protected void buildShapelessRecipes(final Consumer<FinishedRecipe> consumer)
    {
        for (final String s : ItemGenerator.logs.keySet())
        {
            final Block from = ItemGenerator.logs.get(s);
            final Block to = ItemGenerator.planks.get(s);
            final int number = 4;
            this.addConvertRecipe(consumer, from, to, number);
        }
    }
}
