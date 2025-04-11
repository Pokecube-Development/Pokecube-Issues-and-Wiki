package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeExtract;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.core.PokecubeCore;
import thut.api.item.ItemList;
import thut.lib.TComponent;

public class ExtractorTile extends BaseGeneticsTile
{
    private static final ResourceLocation EXTRACT_DEST = ResourceLocation.fromNamespaceAndPath("pokecube_adventures",
            "dna_extractor_destination");

    public static boolean isDNAContainer(final ItemStack stack)
    {
        return ItemList.is(EXTRACT_DEST, stack);
    }

    public ItemStack override_selector = ItemStack.EMPTY;

    public ExtractorTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.EXTRACTOR_TYPE.get(), pos, state);
    }

    public ExtractorTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
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
            return isDNAContainer(stack);
        case 1:// DNA Selector
            final boolean hasGenes = !ClonerHelper.getGeneSelectors(access, stack).isEmpty();
            final boolean selector = hasGenes || RecipeSelector.getSelectorValue(stack) != SelectorImpl.defaultSelector;
            return hasGenes || selector;
        case 2:// DNA Source
            // TODO decide on whether to search recipes to see if it is valid.
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeExtract.class;
    }

    @Override
    public InteractionResult useWithoutItem(final BlockPos pos, final Player player, final BlockHitResult hit)
    {
        final MutableComponent name = TComponent.translatable("block.pokecube_adventures.extractor");
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerIn) -> new ExtractorContainer(id, playerInventory,
                        ContainerLevelAccess.create(this.getLevel(), pos)), name));
        return InteractionResult.SUCCESS;
    }

}
