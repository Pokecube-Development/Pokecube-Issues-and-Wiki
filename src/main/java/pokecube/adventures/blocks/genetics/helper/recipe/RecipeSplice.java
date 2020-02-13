package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.blocks.genetics.splicer.SplicerTile;
import pokecube.core.database.PokedexEntry;

public class RecipeSplice extends PoweredRecipe
{
    public static int                                   ENERGYCOST = 10000;
    public static final IRecipeSerializer<RecipeSplice> SERIALIZER = IRecipeSerializer.register(
            "pokecube_adventures:splicing", new SpecialRecipeSerializer<>(RecipeSplice::new));
    public static Function<ItemStack, Integer>          ENERGYNEED = (s) -> RecipeSplice.ENERGYCOST;

    public RecipeSplice(final ResourceLocation location)
    {
        super(location);
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height > 2;
    }

    @Override
    public boolean complete(final IPoweredProgress tile)
    {
        final List<ItemStack> remaining = Lists.newArrayList(this.getRemainingItems(tile.getCraftMatrix()));
        tile.setInventorySlotContents(tile.getOutputSlot(), this.getCraftingResult(tile.getCraftMatrix()));
        for (int i = 0; i < remaining.size(); i++)
        {
            final ItemStack stack = remaining.get(i);
            if (!stack.isEmpty()) tile.setInventorySlotContents(i, stack);
            else tile.decrStackSize(i, 1);
        }
        if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        return true;
    }

    @Override
    public Function<ItemStack, Integer> getCostFunction()
    {
        return RecipeSplice.ENERGYNEED;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        if (!(inv instanceof PoweredCraftingInventory)) return ItemStack.EMPTY;
        final PoweredCraftingInventory inv_p = (PoweredCraftingInventory) inv;
        if (!(inv_p.inventory instanceof SplicerTile)) return ItemStack.EMPTY;
        final SplicerTile tile = (SplicerTile) inv_p.inventory;

        ItemStack output = ItemStack.EMPTY;
        ItemStack dna = inv.getStackInSlot(0);
        ItemStack egg = inv.getStackInSlot(2);
        ItemStack selector = tile.override_selector.isEmpty() ? inv.getStackInSlot(1) : tile.override_selector;
        if (ClonerHelper.getGenes(dna) == null) dna = ItemStack.EMPTY;
        if (ClonerHelper.getGenes(egg) == null) egg = ItemStack.EMPTY;
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = ItemStack.EMPTY;
        if (!selector.isEmpty() && !dna.isEmpty() && !egg.isEmpty())
        {
            PokedexEntry entry = ClonerHelper.getFromGenes(dna);
            if (entry == null) entry = ClonerHelper.getFromGenes(egg);

            egg = egg.copy();
            if (egg.getTag() == null) egg.setTag(new CompoundNBT());
            ClonerHelper.spliceGenes(ClonerHelper.getGenes(dna), egg, new ItemBasedSelector(selector));
            egg.setCount(1);
            output = egg;
        }
        return output;
    }

    @Override
    public int getEnergyCost()
    {
        return RecipeSplice.ENERGYCOST;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        if (!(inv instanceof PoweredCraftingInventory)) return nonnulllist;
        final PoweredCraftingInventory inv_p = (PoweredCraftingInventory) inv;
        if (!(inv_p.inventory instanceof SplicerTile)) return nonnulllist;
        final SplicerTile tile = (SplicerTile) inv_p.inventory;
        final ItemStack selector = tile.override_selector.isEmpty() ? inv.getStackInSlot(1) : tile.override_selector;
        boolean keepDNA = false;
        boolean keepSelector = false;
        final SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;

        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getStackInSlot(i);

            if (i == 0 && keepDNA) nonnulllist.set(i, item);
            if (i == 1 && keepSelector) nonnulllist.set(i, item);
            if (i == 0 && item.getItem() == Items.POTION) nonnulllist.set(i, new ItemStack(Items.GLASS_BOTTLE));

            if (item.hasContainerItem()) nonnulllist.set(i, item.getContainerItem());
        }
        tile.override_selector = ItemStack.EMPTY;
        return nonnulllist;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeSplice.SERIALIZER;
    }
}
