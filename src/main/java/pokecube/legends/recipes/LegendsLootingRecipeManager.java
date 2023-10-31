package pokecube.legends.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsLootingRecipeSerializer.SerializerLooting;
import thut.core.common.ThutCore;

public class LegendsLootingRecipeManager
{
    public static final RegistryObject<RecipeType<?>> LEGENDS_LOOTING_RECIPE_TYPE = PokecubeLegends.RECIPE_TYPE
            .register("legends_looting", () -> new RecipeType<>()
            {
                public String toString()
                {
                    return "pokecube_legends:legends_looting";
                }
            });
    public static final RegistryObject<SerializerLooting> LEGENDS_LOOTING_RECIPE = PokecubeLegends.RECIPE_SERIALIZER
            .register("legends_looting", () -> new SerializerLooting());

    public static void onPlayerClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (!event.getLevel().isClientSide && event.getEntity() != null)
        {

            final ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());

            for (final Recipe<?> recipe : LegendsLootingRecipeManager.getRecipes(
                    LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE_TYPE.get(), event.getLevel().getRecipeManager()).values())
                if (recipe instanceof LegendsLootingRecipeSerializer)
            {
                final LegendsLootingRecipeSerializer blockRecipe = (LegendsLootingRecipeSerializer) recipe;

                if (blockRecipe.isValid(heldItem, event.getLevel().getBlockState(event.getPos()).getBlock()))
                {
                    final LootTable loottable = event.getEntity().getServer().getLootData().getLootTable(blockRecipe.output);
                    final LootParams.Builder lootcontext$builder =
                            new LootParams.Builder((ServerLevel) event.getEntity().level());

                    final List<ItemStack> list = loottable
                            .getRandomItems(lootcontext$builder.create(loottable.getParamSet()));

                    if (!list.isEmpty()) Collections.shuffle(list);

                    for (final ItemStack itemstack : list)
                    {
                        ItemHandlerHelper.giveItemToPlayer(event.getEntity(), itemstack);
                        break;
                    }

                    heldItem.shrink(1);
                    ItemHandlerHelper.giveItemToPlayer(event.getEntity(), blockRecipe.getResultItem(event.getLevel().registryAccess()));
                    break;
                }
            }
        }
    }

    private static Map<ResourceLocation, Recipe<?>> getRecipes(final RecipeType<?> recipeType,
            final RecipeManager manager)
    {

        final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipesMap = ObfuscationReflectionHelper
                .getPrivateValue(RecipeManager.class, manager, "f_44007_");
        return recipesMap.getOrDefault(recipeType, Collections.emptyMap());
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(LegendsLootingRecipeManager::onPlayerClickBlock);
    }
}
