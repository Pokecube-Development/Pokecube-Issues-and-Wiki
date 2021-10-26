package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.IGeneSelector;
import pokecube.adventures.utils.RecipePokeAdv;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeSelector extends CustomRecipe
{

    public static class ItemBasedSelector implements IGeneSelector
    {
        final ItemStack selector;
        final int       arrIndex;

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
        public <T, GENE extends Gene<T>> Alleles<T, GENE> merge(final IMobGenetics sourceG,
                final IMobGenetics destinationG, final Alleles<T, GENE> source, final Alleles<T, GENE> destination)
        {
            final Set<Class<? extends Gene<?>>> selected = ClonerHelper.getGeneSelectors(this.selector);
            if (selected.contains(source.getExpressed().getClass()))
            {
                if (destination == null) return source;
                return IGeneSelector.super.merge(sourceG, destinationG, source, destination);
            }
            return null;
        }
    }

    public static class SelectorValue
    {
        public static SelectorValue load(final CompoundTag tag)
        {
            if (!tag.contains("S") || !tag.contains("D")) return RecipeSelector.defaultSelector;
            return new SelectorValue(tag.getFloat("S"), tag.getFloat("D"));
        }

        public final float selectorDestructChance;

        public final float dnaDestructChance;

        public SelectorValue(final float select, final float dna)
        {
            this.selectorDestructChance = select;
            this.dnaDestructChance = dna;
        }

        @OnlyIn(Dist.CLIENT)
        public void addToTooltip(final List<Component> toolTip)
        {
            toolTip.add(new TranslatableComponent("container.geneselector.tooltip.a", this.selectorDestructChance));
            toolTip.add(new TranslatableComponent("container.geneselector.tooltip.b", this.dnaDestructChance));
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof SelectorValue)
                return ((SelectorValue) obj).selectorDestructChance == this.selectorDestructChance
                        && ((SelectorValue) obj).dnaDestructChance == this.dnaDestructChance;
            return false;
        }

        public CompoundTag save()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putFloat("S", this.selectorDestructChance);
            tag.putFloat("D", this.dnaDestructChance);
            return tag;
        }

        @Override
        public String toString()
        {
            return this.selectorDestructChance + " " + this.dnaDestructChance;
        }
    }

    public static SelectorValue defaultSelector = new SelectorValue(0.0f, 0.9f);

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
        SelectorValue value = RecipeSelector.defaultSelector;
        if (!stack.isEmpty()) for (final Ingredient stack1 : RecipeSelector.selectorValues.keySet())
            if (stack1.test(stack))
            {
                value = RecipeSelector.selectorValues.get(stack1);
                break;
            }
        return value;
    }

    public static boolean isSelector(final ItemStack stack)
    {
        if (!ClonerHelper.getGeneSelectors(stack).isEmpty()) return true;
        if (!stack.isEmpty()) for (final Ingredient stack1 : RecipeSelector.selectorValues.keySet())
            if (stack1.test(stack)) return true;
        return false;
    }

    public RecipeSelector(final ResourceLocation location)
    {
        super(location);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv)
    {

        ItemStack book = ItemStack.EMPTY;
        ItemStack modifier = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack test = inv.getItem(i);
            final boolean isBook = !ClonerHelper.getGeneSelectors(test).isEmpty();
            if (isBook)
            {
                if (!book.isEmpty()) return ItemStack.EMPTY;
                book = test;
                continue;
            }
            final boolean isModifier = RecipeSelector.getSelectorValue(test) != RecipeSelector.defaultSelector;
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
        ClonerHelper.getSelectorValue(book);
        final ItemStack ret = book.copy();
        ret.setCount(1);
        ret.getTag().put(ClonerHelper.SELECTORTAG, value.save());
        return ret;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.SELECTOR.get();
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        ItemStack book = ItemStack.EMPTY;
        ItemStack modifier = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack test = inv.getItem(i);
            final boolean isBook = !ClonerHelper.getGeneSelectors(test).isEmpty();
            if (isBook)
            {
                if (!book.isEmpty()) return false;
                book = test;
                continue;
            }
            final boolean isModifier = RecipeSelector.getSelectorValue(test) != RecipeSelector.defaultSelector;
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
        if (value.equals(oldValue)) return false;
        return true;
    }

}
