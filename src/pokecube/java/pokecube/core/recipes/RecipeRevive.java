package pokecube.core.recipes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.items.PokesealContents;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.item.ItemList;

public class RecipeRevive extends CustomRecipe
{
    public static final ResourceLocation REVIVETAG = ResourceLocation.parse("pokecube:revive");

    public RecipeRevive(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, Provider access)
    {
        ItemStack healed = ItemStack.EMPTY;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty())
            {
                if (PokecubeManager.isFilled(stack)) other = stack;
                if (ItemList.is(RecipeRevive.REVIVETAG, stack)) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehaviour.POKESEAL)) seal = stack;
            }
        }
        revive = revive && !other.isEmpty();
        pokeseal = !seal.isEmpty() && !other.isEmpty();

        if (pokeseal)
        {
            var sealData = PokemobCaps.getPokeseal(seal);
            if (sealData != null)
            {
                PokemobCaps.updatePokeseal(other, new PokesealContents(sealData.tag()));
            }
            else
            {
                PokemobCaps.removePokeseal(other);
            }
            healed = other;
        }
        else if (revive)
        {
            final ItemStack stack = other;
            if (PokecubeManager.isFilled(stack))
            {
                healed = stack.copy();
                PokecubeManager.heal(healed, PokecubeCore.proxy.getWorld(), false);
            }
        }
        return healed;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeHandler.REVIVE.get();
    }

    @Override
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        int n = 0;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty())
            {
                n++;
                if (PokecubeManager.isFilled(stack)) other = stack;
                if (ItemList.is(RecipeRevive.REVIVETAG, stack)) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehaviour.POKESEAL)) seal = stack;
            }
        }
        revive = revive && !other.isEmpty();
        pokeseal = !seal.isEmpty() && !other.isEmpty();
        if (n != 2) return false;
        return pokeseal || revive && other.getDamageValue() == 255;
    }
}
