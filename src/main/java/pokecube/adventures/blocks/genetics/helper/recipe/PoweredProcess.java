package pokecube.adventures.blocks.genetics.helper.recipe;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import thut.core.common.network.TileUpdate;

public class PoweredProcess
{
    public static final RecipeExtract      EXTRACT = new RecipeExtract(new ResourceLocation(
            "pokecube_adventures:extracting"));
    public static final RecipeSplice       SPLICE  = new RecipeSplice(new ResourceLocation(
            "pokecube_adventures:splicing"));
    public static final RecipeFossilRevive REVIVE  = new RecipeFossilRevive(new ResourceLocation(
            "pokecube_adventures:reviving"));

    public static PoweredRecipe findRecipe(final IPoweredProgress tile, final World world)
    {
        if (!tile.getStackInSlot(tile.getOutputSlot()).isEmpty()) return null;
        if (tile.isValid(RecipeFossilRevive.class) && PoweredProcess.REVIVE.matches(tile.getCraftMatrix(), world))
            return PoweredProcess.REVIVE;
        if (tile.isValid(RecipeExtract.class) && !PoweredProcess.EXTRACT.getCraftingResult(tile.getCraftMatrix())
                .isEmpty()) return PoweredProcess.EXTRACT;
        if (tile.isValid(RecipeSplice.class) && !PoweredProcess.SPLICE.getCraftingResult(tile.getCraftMatrix())
                .isEmpty()) return PoweredProcess.SPLICE;
        return null;
    }

    public static void init()
    {
        RecipeSelector.SERIALIZER.toString();
        RecipeExtract.SERIALIZER.toString();
        RecipeSplice.SERIALIZER.toString();
        RecipeFossilRevive.SERIALIZER.toString();
    }

    public static PoweredProcess load(final CompoundNBT tag, final BaseGeneticsTile tile)
    {
        // TODO load what is saved in save()
        return null;
    }

    public PoweredRecipe recipe;
    IPoweredProgress     tile;
    World                world;
    BlockPos             pos;

    public int needed = 0;

    public PoweredProcess()
    {
    }

    public boolean complete()
    {
        if (this.recipe == null || this.tile == null) return false;
        final boolean ret = this.recipe.complete(this.tile);
        if (this.tile.getStackInSlot(this.tile.getOutputSlot()).isEmpty())
        {
            this.tile.setInventorySlotContents(this.tile.getOutputSlot(), this.recipe.getCraftingResult(this.tile
                    .getCraftMatrix()));
            if (this.tile.getCraftMatrix().eventHandler != null) this.tile.getCraftMatrix().eventHandler
                    .onCraftMatrixChanged(this.tile);
            TileUpdate.sendUpdate((TileEntity) this.tile);
        }
        if (ret)
        {
            this.setTile(this.tile);
            TileUpdate.sendUpdate((TileEntity) this.tile);
        }
        return ret;
    }

    /** @return the amount of energy already consumed. */
    public int getProgress()
    {
        if (this.recipe == null) return 0;
        return this.recipe.getEnergyCost() - this.needed;
    }

    public void reset()
    {
        if (this.recipe != null) this.needed = this.recipe.getEnergyCost();
        else this.needed = 0;
        if (this.tile != null) this.tile.setProgress(this.getProgress());
    }

    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        // TODO save things here
        return tag;
    }

    public PoweredProcess setTile(final IPoweredProgress tile)
    {
        this.tile = tile;
        this.world = ((TileEntity) tile).getWorld();
        this.pos = ((TileEntity) tile).getPos();
        this.recipe = PoweredProcess.findRecipe(tile, this.world);
        if (this.recipe != null) this.needed = this.recipe.getEnergyCost();
        return this;
    }

    public boolean tick()
    {
        if (this.needed > 0)
        {
            this.tile.setProgress(this.getProgress());
            return true;
        }
        return !this.complete();
    }

    public boolean valid()
    {
        if (this.world == null || this.recipe == null) return false;
        final boolean valid = this.recipe.matches(this.tile.getCraftMatrix(), this.world);
        // check this, as the "matches" sometimes checks the energy value.
        return valid || !this.recipe.getCraftingResult(this.tile.getCraftMatrix()).isEmpty();
    }
}
