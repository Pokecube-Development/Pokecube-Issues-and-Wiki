package pokecube.adventures.blocks.genetics.splicer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;
import thut.api.entity.genetics.IMobGenetics;

public class SplicerTile extends BaseGeneticsTile
{
    public ItemStack override_selector = ItemStack.EMPTY;

    public SplicerTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.SPLICER_TYPE.get(), pos, state);
    }

    public SplicerTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state, 4, 3);
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
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
            final IMobGenetics genes = ClonerHelper.getGenes(stack);
            return genes != null;
        }
        return false;
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeSplice.class;
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        final TranslatableComponent name = new TranslatableComponent("block.pokecube_adventures.splicer");
        player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new SplicerContainer(
                id, playerInventory, ContainerLevelAccess.create(this.getLevel(), pos)), name));
        return InteractionResult.SUCCESS;
    }
}
