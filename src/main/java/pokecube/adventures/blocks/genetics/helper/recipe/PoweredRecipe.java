package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public abstract class PoweredRecipe extends CustomRecipe implements IPoweredRecipe
{

    public PoweredRecipe(final ResourceLocation location)
    {
        super(location);
    }

    public abstract Function<ItemStack, Integer> getCostFunction();

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        if (!(inv instanceof PoweredCraftingInventory)) return false;
        final int energy = ((PoweredCraftingInventory) inv).getEnergy();
        final ItemStack result = this.assemble(inv);
        if (result.isEmpty()) return false;
        final int needed = this.getCostFunction().apply(result);
        if (energy < needed) return false;
        return true;
    }
}
