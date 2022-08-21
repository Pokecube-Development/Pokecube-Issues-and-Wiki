package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.api.data.PokedexEntry;

public class RecipeSplice extends PoweredRecipe
{
    public static int ENERGYCOST = 10000;
    public static Function<ItemStack, Integer> ENERGYNEED = (s) -> RecipeSplice.ENERGYCOST;

    public RecipeSplice(final ResourceLocation location)
    {
        super(location);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 2;
    }

    @Override
    public boolean complete(final IPoweredProgress tile)
    {
        final List<ItemStack> remaining = Lists.newArrayList(this.getRemainingItems(tile.getCraftMatrix()));
        tile.setItem(tile.getOutputSlot(), this.assemble(tile.getCraftMatrix()));
        for (int i = 0; i < remaining.size(); i++)
        {
            final ItemStack stack = remaining.get(i);
            if (!stack.isEmpty()) tile.setItem(i, stack);
            else tile.removeItem(i, 1);
        }
        if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.broadcastChanges();
        return true;
    }

    @Override
    public Function<ItemStack, Integer> getCostFunction()
    {
        return RecipeSplice.ENERGYNEED;
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv)
    {
        if (!(inv instanceof PoweredCraftingInventory inv_p)) return ItemStack.EMPTY;
        if (!(inv_p.inventory instanceof SplicerTile tile)) return ItemStack.EMPTY;

        ItemStack output = ItemStack.EMPTY;
        ItemStack dna = inv.getItem(0);
        ItemStack egg = inv.getItem(2);
        ItemStack selector = tile.override_selector.isEmpty() ? inv.getItem(1) : tile.override_selector;
        if (ClonerHelper.getGenes(dna) == null) dna = ItemStack.EMPTY;
        if (ClonerHelper.getGenes(egg) == null) egg = ItemStack.EMPTY;
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = ItemStack.EMPTY;
        if (!selector.isEmpty() && !dna.isEmpty() && !egg.isEmpty())
        {
            PokedexEntry entry = ClonerHelper.getFromGenes(dna);
            if (entry == null) entry = ClonerHelper.getFromGenes(egg);
            egg = egg.copy();
            if (egg.getTag() == null) egg.setTag(new CompoundTag());
            ClonerHelper.spliceGenes(ClonerHelper.getGenes(dna), egg, new ItemBasedSelector(selector));
            egg.setCount(1);
            output = egg;
        }
        return output;
    }

    @Override
    public int getEnergyCost(final IPoweredProgress tile)
    {
        return RecipeSplice.ENERGYCOST;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingContainer inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        if (!(inv instanceof PoweredCraftingInventory inv_p)) return nonnulllist;
        if (!(inv_p.inventory instanceof SplicerTile tile)) return nonnulllist;
        final ItemStack selector = tile.override_selector.isEmpty() ? inv.getItem(1) : tile.override_selector;
        boolean keepDNA = false;
        boolean keepSelector = false;
        final SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;

        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getItem(i).copy();
            if (i == 1 && keepSelector) nonnulllist.set(i, item);
            if (i == 0)
            {
                final boolean multiple = item.getCount() > 1;
                if (keepDNA) nonnulllist.set(i, item);
                else if (item.getItem() == Items.POTION) nonnulllist.set(i, new ItemStack(Items.GLASS_BOTTLE));
                else if (!multiple)
                {
                    item.setTag(null);
                    nonnulllist.set(i, item.copy());
                }
            }
            if (item.hasContainerItem()) nonnulllist.set(i, item.getContainerItem());
        }
        tile.override_selector = ItemStack.EMPTY;
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.SPLICE.get();
    }
}
