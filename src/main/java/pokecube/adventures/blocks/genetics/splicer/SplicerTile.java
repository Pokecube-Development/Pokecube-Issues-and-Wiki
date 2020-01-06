package pokecube.adventures.blocks.genetics.splicer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class SplicerTile extends BaseGeneticsTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(SplicerTile::new,
            PokecubeAdv.SPLICER).build(null);

    public SplicerTile()
    {
        this(SplicerTile.TYPE);
    }

    public SplicerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn, 4, 3);
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getGenes(stack) != null;
        case 1:// DNA Selector
            final boolean hasGenes = !ClonerHelper.getGeneSelectors(stack).isEmpty();
            final boolean selector = hasGenes || RecipeSelector.getSelectorValue(
                    stack) != RecipeSelector.defaultSelector;
            return hasGenes || selector;
        case 2:// DNA Destination
            return ItemPokemobEgg.getEntry(stack) != null;
        }
        return false;
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeSplice.class;
    }

    @Override
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.splicer");
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new SplicerContainer(
                id, playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), name));
        return true;
    }
}
