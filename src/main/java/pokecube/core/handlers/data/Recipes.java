package pokecube.core.handlers.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

public class Recipes extends RecipeProvider implements IConditionBuilder
{
    // FIXME recipe data gen
    public Recipes(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }
    //
    // protected void addConvertRecipe(final Consumer<FinishedRecipe> consumer,
    // final Block from, final Block to,
    // final int number)
    // {
    // final ResourceLocation id = new
    // ResourceLocation(to.getRegistryName().getNamespace(),
    // from.getRegistryName()
    // .getPath() + "-" + to.getRegistryName().getPath());
//        //@formatter:off
//        ConditionalRecipe.builder()
//        .addCondition(TrueCondition.INSTANCE)
//        .addRecipe(
//            ShapelessRecipeBuilder.shapeless(to, number)
//            .requires(from)
//            .group(to.getRegistryName().getNamespace())
//            .unlockedBy("has_from", RecipeProvider.has(from))
//            ::save
//        )
//        .build(consumer, id);
//        //@formatter:on
    // }
    //
    // @Override
    // protected void buildShapelessRecipes(final Consumer<FinishedRecipe>
    // consumer)
    // {
    // for (final String s : ItemGenerator.logs.keySet())
    // {
    // final Block from = ItemGenerator.logs.get(s);
    // final Block to = ItemGenerator.planks.get(s);
    // final int number = 4;
    // this.addConvertRecipe(consumer, from, to, number);
    // }
    // }
}
