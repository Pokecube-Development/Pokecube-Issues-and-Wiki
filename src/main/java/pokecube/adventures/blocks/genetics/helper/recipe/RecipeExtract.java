package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper.DNAPack;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeExtract extends PoweredRecipe
{

    public static int                                    ENERGYCOST = 10000;
    public static final IRecipeSerializer<RecipeExtract> SERIALIZER = IRecipeSerializer.register(
            "pokecube_adventures:extracting", new SpecialRecipeSerializer<>(RecipeExtract::new));
    public static Function<ItemStack, Integer>           ENERGYNEED = (s) -> RecipeExtract.ENERGYCOST;

    public RecipeExtract(final ResourceLocation location)
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
        final List<ItemStack> remaining = this.getRemainingItems(tile.getCraftMatrix());
        tile.setInventorySlotContents(tile.getOutputSlot(), this.getCraftingResult(tile.getCraftMatrix()));
        for (int i = 0; i < remaining.size(); i++)
        {
            final ItemStack stack = remaining.get(i);
            if (!stack.isEmpty()) tile.setInventorySlotContents(i, stack);
            else
            {
                final ItemStack old = tile.getStackInSlot(i);
                if (PokecubeManager.isFilled(old)) PlayerPokemobCache.UpdateCache(old, false, true);
                tile.decrStackSize(i, 1);
            }
        }
        if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        return true;
    }

    @Override
    public Function<ItemStack, Integer> getCostFunction()
    {
        return RecipeExtract.ENERGYNEED;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        if (!(inv instanceof PoweredCraftingInventory)) return ItemStack.EMPTY;
        final PoweredCraftingInventory inv_p = (PoweredCraftingInventory) inv;
        if (!(inv_p.inventory instanceof ExtractorTile)) return ItemStack.EMPTY;
        final ExtractorTile tile = (ExtractorTile) inv_p.inventory;

        IMobGenetics genes;
        final ItemStack destination = inv.getStackInSlot(0);
        ItemStack source = inv.getStackInSlot(2);
        ItemStack selector = tile.override_selector.isEmpty() ? inv.getStackInSlot(1) : tile.override_selector;
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = ItemStack.EMPTY;
        source:
        if ((genes = ClonerHelper.getGenes(source)) == null)
        {
            final List<ItemStack> stacks = Lists.newArrayList(ClonerHelper.DNAITEMS.keySet());
            Collections.shuffle(stacks);
            if (!source.isEmpty()) for (final ItemStack stack : stacks)
                if (Tools.isSameStack(stack, source))
                {
                    final DNAPack pack = ClonerHelper.DNAITEMS.get(stack);
                    final Alleles alleles = pack.alleles;
                    genes = GeneRegistry.GENETICS_CAP.getDefaultInstance();
                    genes.getAlleles().put(alleles.getExpressed().getKey(), alleles);
                    break source;
                }
            source = ItemStack.EMPTY;
        }
        final ItemStack output = destination.copy();
        output.setCount(1);
        if (source.isEmpty() || genes == null || selector.isEmpty()) return ItemStack.EMPTY;
        if (output.getTag() == null) output.setTag(new CompoundNBT());
        ClonerHelper.mergeGenes(genes, output, new ItemBasedSelector(selector));
        output.setCount(1);
        return output;
    }

    @Override
    public int getEnergyCost(final IPoweredProgress tile)
    {
        return RecipeExtract.ENERGYCOST;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeExtract.SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        if (!(inv instanceof PoweredCraftingInventory)) return nonnulllist;
        final PoweredCraftingInventory inv_p = (PoweredCraftingInventory) inv;
        if (!(inv_p.inventory instanceof ExtractorTile)) return nonnulllist;
        final ExtractorTile tile = (ExtractorTile) inv_p.inventory;
        final ItemStack selector = tile.override_selector.isEmpty() ? inv.getStackInSlot(1) : tile.override_selector;
        boolean keepDNA = false;
        boolean keepSelector = false;
        final SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;
        System.out.println(value.selectorDestructChance + " " + value.dnaDestructChance + " " + keepSelector);

        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getStackInSlot(i);
            if (i == 0 && keepDNA) nonnulllist.set(i, item);
            if (i == 1 && keepSelector) nonnulllist.set(i, item);
            if (item.hasContainerItem()) nonnulllist.set(i, item.getContainerItem());
        }
        tile.override_selector = ItemStack.EMPTY;
        return nonnulllist;
    }
}
