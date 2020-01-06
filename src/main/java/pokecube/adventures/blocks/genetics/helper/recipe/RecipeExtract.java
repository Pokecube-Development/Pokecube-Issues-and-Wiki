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
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper.DNAPack;
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

    ItemStack selector = ItemStack.EMPTY;

    public boolean fixed = false;

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
        final List<ItemStack> remaining = Lists.newArrayList(this.getRemainingItems(tile.getCraftMatrix()));
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
        IMobGenetics genes;
        final ItemStack destination = inv.getStackInSlot(0);
        ItemStack source = inv.getStackInSlot(2);
        if (!this.fixed) this.selector = inv.getStackInSlot(1);
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
        if (source.isEmpty() || genes == null) return ItemStack.EMPTY;
        if (output.getTag() == null) output.setTag(new CompoundNBT());
        ClonerHelper.mergeGenes(genes, output, new ItemBasedSelector(this.selector));
        output.setCount(1);
        return output;
    }

    @Override
    public int getEnergyCost()
    {
        return RecipeExtract.ENERGYCOST;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeExtract.SERIALIZER;
    }

    public void setSelector(final ItemStack selector)
    {
        this.selector = selector;
    }

    public ItemStack toKeep(final int slot, final ItemStack stackIn, final CraftingInventory inv)
    {
        boolean keepDNA = false;
        boolean keepSelector = false;
        final SelectorValue value = ClonerHelper.getSelectorValue(this.selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;
        if (slot == 2 && keepDNA) return stackIn;
        if (slot == 1 && keepSelector) return stackIn;
        return ItemStack.EMPTY;
    }
}
