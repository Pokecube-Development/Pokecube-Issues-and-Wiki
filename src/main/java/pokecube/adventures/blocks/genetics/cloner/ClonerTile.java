package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.GeneticsTileParentable;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import thut.api.item.ItemList;

public class ClonerTile extends GeneticsTileParentable
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public static final ResourceLocation EGGS = new ResourceLocation("forge", "eggs");

    ClonerTile parent        = null;
    boolean    checkedParent = false;

    public ClonerTile()
    {
        this(ClonerTile.TYPE);
    }

    public ClonerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn, 10, 9);
    }

    @Override
    public BaseGeneticsTile getParent()
    {
        if (!this.checkedParent && this.getWorld() != null)
        {
            this.checkedParent = true;
            final BlockState state = this.getWorld().getBlockState(this.getPos());
            if (state.get(ClonerBlock.HALF) == ClonerBlockPart.TOP)
            {
                final BlockPos new_pos = this.getPos().down();
                final TileEntity down = this.getWorld().getTileEntity(new_pos);
                if (down instanceof ClonerTile) this.parent = (ClonerTile) down;
            }
        }
        return this.parent;
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
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
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand, BlockRayTraceResult hit)
    {
        final BlockState state = this.getWorld().getBlockState(this.getPos());
        if (state.get(ClonerBlock.HALF) == ClonerBlockPart.TOP)
        {
            final BlockPos new_pos = this.getPos().down();
            final BlockState down = this.getWorld().getBlockState(new_pos);
            hit = new BlockRayTraceResult(hit.getHitVec(), hit.getFace(), new_pos, hit.isInside());
            return down.onBlockActivated(this.getWorld(), player, hand, hit);
        }
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.cloner");
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new ClonerContainer(id,
                playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), name));
        return true;
    }

}
