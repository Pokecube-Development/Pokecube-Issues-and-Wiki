package pokecube.legends.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.recipes.LegendsLootingRecipeImpl.SerializerLooting;
import thut.core.common.ThutCore;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LegendsLootingRecipeManager
{
    public static final Supplier<RecipeType<LegendsLootingRecipeImpl>> LEGENDS_LOOTING_RECIPE_TYPE = PokecubeLegends.RECIPE_TYPE.register(
            "legends_looting", () -> new RecipeType<>()
            {
                public String toString()
                {
                    return "pokecube_legends:legends_looting";
                }
            });
    public static final Supplier<SerializerLooting> LEGENDS_LOOTING_RECIPE = PokecubeLegends.RECIPE_SERIALIZER.register(
            "legends_looting", SerializerLooting::new);

    public static void onPlayerClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (!event.getLevel().isClientSide && event.getEntity() != null)
        {

            final ItemStack heldItem = event.getEntity().getItemInHand(event.getHand());

            for (final RecipeHolder<LegendsLootingRecipeImpl> recipe : event.getLevel().getRecipeManager()
                    .getAllRecipesFor(LegendsLootingRecipeManager.LEGENDS_LOOTING_RECIPE_TYPE.get()))
                if (recipe.value() instanceof LegendsLootingRecipeImpl blockRecipe)
                {
                    if (blockRecipe.isValid(heldItem, event.getLevel().getBlockState(event.getPos()).getBlock()))
                    {
                        final LootTable loottable = event.getEntity().getServer().reloadableRegistries()
                                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, blockRecipe.output));
                        final LootParams.Builder lootcontext$builder = new LootParams.Builder(
                                (ServerLevel) event.getEntity().level());

                        final List<ItemStack> list = loottable.getRandomItems(
                                lootcontext$builder.create(loottable.getParamSet()));

                        if (!list.isEmpty()) Collections.shuffle(list);

                        for (final ItemStack itemstack : list)
                        {
                            ItemHandlerHelper.giveItemToPlayer(event.getEntity(), itemstack);
                            break;
                        }

                        heldItem.shrink(1);
                        ItemHandlerHelper.giveItemToPlayer(event.getEntity(),
                                blockRecipe.getResultItem(event.getLevel().registryAccess()));
                        break;
                    }
                }
        }
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(LegendsLootingRecipeManager::onPlayerClickBlock);
    }
}
