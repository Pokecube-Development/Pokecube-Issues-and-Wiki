package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.IGeneSelector;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl.SelectorValue;
import pokecube.adventures.utils.RecipePokeAdv;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeSelector extends CustomRecipe
{
    public static class ItemBasedSelector implements IGeneSelector
    {
        final ItemStack selector;
        final int arrIndex;

        public ItemBasedSelector(final ItemStack selector)
        {
            this(selector, ClonerHelper.getIndex(selector));
        }

        public ItemBasedSelector(final ItemStack selector, final int arrIndex)
        {
            this.selector = selector;
            this.arrIndex = arrIndex;
        }

        @Override
        public int arrIndex()
        {
            return this.arrIndex;
        }

        @Override
        public <T, GENE extends Gene<T>> Alleles<T, GENE> merge(Provider provider, final IMobGenetics sourceG,
                final IMobGenetics destinationG, final Alleles<T, GENE> source, final Alleles<T, GENE> destination)
        {
            final Set<Class<? extends Gene<?>>> selected = ClonerHelper.getGeneSelectors(provider, this.selector);
            if (selected.contains(source.getExpressed().getClass()))
            {
                if (destination == null) return source;
                return IGeneSelector.super.merge(provider, sourceG, destinationG, source, destination);
            }
            return null;
        }
    }

    private static Map<Ingredient, SelectorValue> selectorValues = Maps.newHashMap();

    public static void clear()
    {
        RecipeSelector.selectorValues.clear();
    }

    public static void addSelector(final Ingredient stack, final SelectorValue value)
    {
        RecipeSelector.selectorValues.put(stack, value);
    }

    public static SelectorValue getSelectorValue(final ItemStack stack)
    {
        SelectorValue value = SelectorImpl.defaultSelector;
        if (!stack.isEmpty())
            for (final Ingredient stack1 : RecipeSelector.selectorValues.keySet()) if (stack1.test(stack))
        {
            value = RecipeSelector.selectorValues.get(stack1);
            break;
        }
        return value;
    }

    public static boolean isSelector(Provider access, final ItemStack stack)
    {
        if (!ClonerHelper.getGeneSelectors(access, stack).isEmpty()) return true;
        if (!stack.isEmpty())
            for (final Ingredient stack1 : RecipeSelector.selectorValues.keySet()) if (stack1.test(stack)) return true;
        return false;
    }

    public RecipeSelector(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(final CraftingInput inv, Provider access)
    {

        ItemStack book = ItemStack.EMPTY;
        ItemStack modifier = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack test = inv.getItem(i);
            final boolean isBook = !ClonerHelper.getGeneSelectors(access, test).isEmpty();
            if (isBook)
            {
                if (!book.isEmpty()) return ItemStack.EMPTY;
                book = test;
                continue;
            }
            final boolean isModifier = !RecipeSelector.getSelectorValue(test).equals(SelectorImpl.defaultSelector);
            if (isModifier)
            {
                if (!modifier.isEmpty()) return ItemStack.EMPTY;
                modifier = test;
                continue;
            }
            if (!test.isEmpty()) return ItemStack.EMPTY;
        }
        if (book.isEmpty() || modifier.isEmpty()) return ItemStack.EMPTY;
        final SelectorValue value = RecipeSelector.getSelectorValue(modifier);
        final ItemStack ret = book.copy();
        ret.setCount(1);
        ret.set(SelectorImpl.VALUE_STORE, value);
        return ret;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.SELECTOR.get();
    }

    @Override
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        ItemStack book = ItemStack.EMPTY;
        ItemStack modifier = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++)
        {
            final ItemStack test = inv.getItem(i);
            final boolean isBook = !ClonerHelper.getGeneSelectors(worldIn.registryAccess(), test).isEmpty();
            if (isBook)
            {
                if (!book.isEmpty()) return false;
                book = test;
                continue;
            }
            final boolean isModifier = !RecipeSelector.getSelectorValue(test).equals(SelectorImpl.defaultSelector);
            if (isModifier)
            {
                if (!modifier.isEmpty()) return false;
                modifier = test;
                continue;
            }
            if (!test.isEmpty()) return false;
        }
        if (book.isEmpty() || modifier.isEmpty()) return false;
        final SelectorValue value = RecipeSelector.getSelectorValue(modifier);
        final SelectorValue oldValue = ClonerHelper.getSelectorValue(book);
        return !value.equals(oldValue);
    }

}
