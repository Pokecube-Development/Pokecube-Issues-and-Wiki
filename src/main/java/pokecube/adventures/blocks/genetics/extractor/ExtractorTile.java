package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;

public class ExtractorTile extends BaseGeneticsTile
{
    public ItemStack override_selector = ItemStack.EMPTY;

    public ExtractorTile()
    {
        this(PokecubeAdv.EXTRACTOR_TYPE.get());
    }

    public ExtractorTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn, 4, 3);
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.isDNAContainer(stack) && ClonerHelper.getGenes(stack) == null;
        case 1:// DNA Selector
            final boolean hasGenes = !ClonerHelper.getGeneSelectors(stack).isEmpty();
            final boolean selector = hasGenes || RecipeSelector.getSelectorValue(
                    stack) != RecipeSelector.defaultSelector;
            return hasGenes || selector;
        case 2:// DNA Source
            final IMobGenetics genes = ClonerHelper.getGenes(stack);
            if (genes == null && !stack.isEmpty()) for (final ItemStack stack1 : ClonerHelper.DNAITEMS.keySet())
                if (Tools.isSameStack(stack1, stack)) return true;
            return genes != null;
        }
        return false;
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeExtract.class;
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.extractor");
        player.openMenu(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new ExtractorContainer(
                id, playerInventory, IWorldPosCallable.create(this.getLevel(), pos)), name));
        return ActionResultType.SUCCESS;
    }

}
