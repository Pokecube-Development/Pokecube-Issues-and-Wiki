package pokecube.adventures.blocks.genetics.splicer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import pokecube.adventures.blocks.genetics.helper.SelectorImpl;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSplice;
import pokecube.core.PokecubeCore;

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
        var access = this.getLevel() != null ? this.getLevel().registryAccess() : PokecubeCore.proxy.getRegistries();
        switch (index)
        {
        case 0:// DNA Container
            var sourceGenes = ClonerHelper.getGenes(access, stack);
            return sourceGenes != null && !sourceGenes.getAlleles().isEmpty();
        case 1:// DNA Selector
            final boolean hasGenes = !ClonerHelper.getGeneSelectors(access, stack).isEmpty();
            return hasGenes || !RecipeSelector.getSelectorValue(stack).equals(SelectorImpl.defaultSelector);
        case 2:// DNA Destination
            var destinationGenes = ClonerHelper.getGenes(access, stack);
            return destinationGenes != null && !destinationGenes.getAlleles().isEmpty();
        }
        return false;
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeSplice.class;
    }

    @Override
    public InteractionResult useWithoutItem(BlockPos pos, Player player, BlockHitResult hit)
    {
        final MutableComponent name = Component.translatable("block.pokecube_adventures.splicer");
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerIn) -> new SplicerContainer(id, playerInventory,
                        ContainerLevelAccess.create(this.getLevel(), pos)), name));
        return InteractionResult.SUCCESS;
    }
}
