package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.GeneticsTileParentable;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.core.PokecubeCore;
import thut.api.item.ItemList;
import thut.lib.TComponent;

public class ClonerTile extends GeneticsTileParentable<ClonerTile>
{
    public static final ResourceLocation EGGS = ResourceLocation.fromNamespaceAndPath("c", "eggs");

    public ClonerTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.CLONER_TYPE.get(), pos, state);
    }

    public ClonerTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state, 10, 9);
    }

    @Override
    protected ClonerTile findParent()
    {
        final BlockState state = this.getBlockState();
        final boolean nullState = !state.hasProperty(ClonerBlock.HALF);
        if (nullState) return null;
        if (state.getValue(ClonerBlock.HALF) == ClonerBlockPart.TOP)
        {
            final BlockEntity down = this.getLevel().getBlockEntity(this.getBlockPos().below());
            if (down instanceof ClonerTile tile) return tile;
        }
        return null;
    }

    @Override
    protected boolean saveInv(final BlockState state)
    {
        if (!this.isDummy) return true;
        return state.hasProperty(ClonerBlock.HALF) && state.getValue(ClonerBlock.HALF) == ClonerBlockPart.BOTTOM;
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        var access = this.getLevel() != null ? this.getLevel().registryAccess() : PokecubeCore.proxy.getRegistries();
        return switch (index)
        {
            case 0 ->// DNA Container
                    ClonerHelper.getFromGenes(access, stack) != null;
            case 1 ->// Egg
                    ItemList.is(ClonerTile.EGGS, stack);
            default -> index != this.getOutputSlot();
        };
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeClone.class;
    }

    @Override
    public InteractionResult useWithoutItem(BlockPos pos, Player player, BlockHitResult hit)
    {
        final BlockState state = this.getLevel().getBlockState(this.getBlockPos());
        if (state.getValue(ClonerBlock.HALF) == ClonerBlockPart.TOP)
        {
            final BlockPos new_pos = this.getBlockPos().below();
            final BlockState down = this.getLevel().getBlockState(new_pos);
            hit = new BlockHitResult(hit.getLocation(), hit.getDirection(), new_pos, hit.isInside());
            return down.useWithoutItem(this.getLevel(), player, hit);
        }
        final MutableComponent name = TComponent.translatable("block.pokecube_adventures.cloner");
        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerIn) -> new ClonerContainer(id, playerInventory,
                        ContainerLevelAccess.create(this.getLevel(), pos)), name));
        return InteractionResult.SUCCESS;
    }

}
