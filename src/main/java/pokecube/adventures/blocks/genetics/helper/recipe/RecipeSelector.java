package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.IGeneSelector;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;

public class RecipeSelector extends SpecialRecipe
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
        public Alleles merge(final Alleles source, final Alleles destination)
        {
            final Set<Class<? extends Gene>> selected = ClonerHelper.getGeneSelectors(this.selector);
            if (selected.contains(source.getExpressed().getClass()))
            {
                if (destination == null) return source;
                return IGeneSelector.super.merge(source, destination);
            }
            return null;
        }
    }

    public static class SelectorValue
    {
        public static SelectorValue load(final CompoundNBT tag)
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
        public void addToTooltip(final List<ITextComponent> toolTip)
        {
            toolTip.add(new TranslationTextComponent("container.geneselector.tooltip.a", this.selectorDestructChance));
            toolTip.add(new TranslationTextComponent("container.geneselector.tooltip.b", this.dnaDestructChance));
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof SelectorValue)
                return ((SelectorValue) obj).selectorDestructChance == this.selectorDestructChance
                        && ((SelectorValue) obj).dnaDestructChance == this.dnaDestructChance;
            return false;
        }

        public CompoundNBT save()
        {
            final CompoundNBT tag = new CompoundNBT();
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

    private static Map<ItemStack, SelectorValue> selectorValues = Maps.newHashMap();

    public static final IRecipeSerializer<RecipeSelector> SERIALIZER = IRecipeSerializer.register(
            "pokecube_adventures:selectors", new SpecialRecipeSerializer<>(RecipeSelector::new));

    public static void addSelector(final ItemStack stack, final SelectorValue value)
    {
        RecipeSelector.selectorValues.put(stack, value);
    }

    public static SelectorValue getSelectorValue(final ItemStack stack)
    {
        SelectorValue value = RecipeSelector.defaultSelector;
        if (!stack.isEmpty()) for (final ItemStack stack1 : RecipeSelector.selectorValues.keySet())
            if (Tools.isSameStack(stack1, stack))
            {
                value = RecipeSelector.selectorValues.get(stack1);
                break;
            }
        return value;
    }

    public static boolean isSelector(final ItemStack stack)
    {
        if (!ClonerHelper.getGeneSelectors(stack).isEmpty()) return true;
        if (!stack.isEmpty()) for (final ItemStack stack1 : RecipeSelector.selectorValues.keySet())
            if (Tools.isSameStack(stack1, stack)) return true;
        return false;
    }

    public RecipeSelector(final ResourceLocation location)
    {
        super(location);
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        final ItemStack book = inv.getStackInSlot(0);
        final ItemStack modifier = inv.getStackInSlot(1);
        final SelectorValue value = RecipeSelector.getSelectorValue(modifier);
        ClonerHelper.getSelectorValue(book);
        final ItemStack ret = book.copy();
        ret.setCount(1);
        ret.getTag().put(ClonerHelper.SELECTORTAG, value.save());
        return ret;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeSelector.SERIALIZER;
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        if (inv.getSizeInventory() < 2) return false;
        final ItemStack book = inv.getStackInSlot(0);
        final ItemStack modifier = inv.getStackInSlot(1);
        if (ClonerHelper.getGeneSelectors(book).isEmpty() || modifier.isEmpty()) return false;
        final SelectorValue value = RecipeSelector.getSelectorValue(modifier);
        final SelectorValue oldValue = ClonerHelper.getSelectorValue(book);
        if (value.equals(oldValue)) return false;
        return true;
    }

}
