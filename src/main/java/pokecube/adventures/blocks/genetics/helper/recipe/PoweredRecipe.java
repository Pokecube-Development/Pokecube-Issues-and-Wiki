package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.function.Function;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public abstract class PoweredRecipe extends SpecialRecipe implements IPoweredRecipe
{

    public PoweredRecipe(final ResourceLocation location)
    {
        super(location);
    }

    public abstract Function<ItemStack, Integer> getCostFunction();

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        if (!(inv instanceof PoweredCraftingInventory)) return false;
        final int energy = ((PoweredCraftingInventory) inv).getEnergy();
        final ItemStack result = this.getCraftingResult(inv);
        if (result.isEmpty()) return false;
        final int needed = this.getCostFunction().apply(result);
        if (energy < needed) return false;
        return true;
    }
}
