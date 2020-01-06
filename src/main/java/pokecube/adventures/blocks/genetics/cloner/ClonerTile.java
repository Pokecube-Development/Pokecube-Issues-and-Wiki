package pokecube.adventures.blocks.genetics.cloner;

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
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeFossilRevive;
import pokecube.core.PokecubeItems;

public class ClonerTile extends BaseGeneticsTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(ClonerTile::new,
            PokecubeAdv.CLONER).build(null);

    public static final ResourceLocation EGGS = new ResourceLocation("forge", "eggs");

    public ClonerTile()
    {
        this(ClonerTile.TYPE);
    }

    public ClonerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn, 10, 9);
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getFromGenes(stack) != null;
        case 1:// Egg
            PokecubeItems.is(ClonerTile.EGGS, stack);
        }
        return index != this.getOutputSlot();
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        return recipe == RecipeFossilRevive.class;
    }

    @Override
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final TranslationTextComponent name = new TranslationTextComponent("block.pokecube_adventures.cloner");
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new ClonerContainer(id,
                playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), name));
        return true;
    }
}
