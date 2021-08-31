package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.GeneticsTileParentable;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import thut.api.item.ItemList;

public class ClonerTile extends GeneticsTileParentable<ClonerTile>
{
    public static final ResourceLocation EGGS = new ResourceLocation("forge", "eggs");

    public ClonerTile()
    {
        this(PokecubeAdv.CLONER_TYPE.get());
    }

    public ClonerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn, 10, 9);
    }

    @Override
    protected ClonerTile findParent()
    {
        final BlockState state = this.getBlockState();
        final boolean nullState = !state.hasProperty(ClonerBlock.HALF);
        if (nullState) return null;
        if (state.getValue(ClonerBlock.HALF) == ClonerBlockPart.TOP)
        {
            final TileEntity down = this.getLevel().getBlockEntity(this.getBlockPos().below());
            if (down instanceof ClonerTile) return (ClonerTile) down;
        }
        return null;
    }

    @Override
    protected boolean saveInv(final BlockState state)
    {
        if (!this.isDummy) return true;
        final boolean doSave = state.hasProperty(ClonerBlock.HALF) && state.getValue(
                ClonerBlock.HALF) == ClonerBlockPart.BOTTOM;
        return doSave;
    }

    @Override
    public boolean canPlaceItem(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getFromGenes(stack) != null;
        case 1:// Egg
            return ItemList.is(ClonerTile.EGGS, stack);
        }
        return index != this.getOutputSlot();
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeClone.class;
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            BlockRayTraceResult hit)
    {
        final BlockState state = this.getLevel().getBlockState(this.getBlockPos());
        if (state.getValue(ClonerBlock.HALF) == ClonerBlockPart.TOP)
        {
            final BlockPos new_pos = this.getBlockPos().below();
            final BlockState down = this.getLevel().getBlockState(new_pos);
            hit = new BlockRayTraceResult(hit.getLocation(), hit.getDirection(), new_pos, hit.isInside());
            return down.use(this.getLevel(), player, hand, hit);
        }
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.cloner");
        player.openMenu(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new ClonerContainer(id,
                playerInventory, IWorldPosCallable.create(this.getLevel(), pos)), name));
        return ActionResultType.SUCCESS;
    }

}
