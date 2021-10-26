package pokecube.adventures.blocks.genetics.helper.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.utils.RecipePokeAdv;
import thut.core.common.network.TileUpdate;

public class PoweredProcess
{
    public static PoweredRecipe findRecipe(final IPoweredProgress tile, final Level world)
    {
        if (!tile.getItem(tile.getOutputSlot()).isEmpty()) return null;
        PoweredRecipe output = null;
        final RecipeClone cloneRecipe = new RecipeClone(RecipePokeAdv.REVIVE.getId());
        final RecipeSplice spliceRecipe = new RecipeSplice(RecipePokeAdv.SPLICE.getId());
        final RecipeExtract extractRecipe = new RecipeExtract(RecipePokeAdv.EXTRACT.getId());
        // This one checks if it matches, as has no item output.
        if (tile.isValid(RecipeClone.class) && cloneRecipe.matches(tile.getCraftMatrix(), world)) output = cloneRecipe;
        // This checks for item output
        else if (tile.isValid(RecipeSplice.class) && !spliceRecipe.assemble(tile.getCraftMatrix()).isEmpty())
            output = spliceRecipe;
        // This checks for item output also
        else if (tile.isValid(RecipeExtract.class) && !extractRecipe.assemble(tile.getCraftMatrix()).isEmpty())
            output = extractRecipe;
        return output;
    }

    public static PoweredProcess load(final CompoundTag tag, final BaseGeneticsTile tile)
    {
        // TODO load what is saved in save()
        return null;
    }

    public PoweredRecipe recipe;
    IPoweredProgress     tile;
    Level                world;
    BlockPos             pos;

    public int needed = 0;

    public PoweredProcess()
    {
    }

    public boolean complete()
    {
        if (this.recipe == null || this.tile == null) return false;
        final boolean ret = this.recipe.complete(this.tile);
        if (this.tile.getItem(this.tile.getOutputSlot()).isEmpty())
        {
            this.tile.setItem(this.tile.getOutputSlot(), this.recipe.assemble(this.tile
                    .getCraftMatrix()));
            if (this.tile.getCraftMatrix().eventHandler != null) this.tile.getCraftMatrix().eventHandler
                    .slotsChanged(this.tile);
            TileUpdate.sendUpdate((BlockEntity) this.tile);
        }
        if (ret)
        {
            this.setTile(this.tile);
            TileUpdate.sendUpdate((BlockEntity) this.tile);
        }
        return ret;
    }

    /** @return the amount of energy already consumed. */
    public int getProgress()
    {
        if (this.recipe == null) return 0;
        return this.recipe.getEnergyCost(this.tile) - this.needed;
    }

    public void reset()
    {
        if (this.recipe != null) this.needed = this.recipe.getEnergyCost(this.tile);
        else this.needed = 0;
        if (this.tile != null) this.tile.setProgress(this.getProgress());
    }

    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        // TODO save things here
        return tag;
    }

    public PoweredProcess setTile(final IPoweredProgress tile)
    {
        this.tile = tile;
        this.world = ((BlockEntity) tile).getLevel();
        this.pos = ((BlockEntity) tile).getBlockPos();
        this.recipe = PoweredProcess.findRecipe(tile, this.world);
        if (this.recipe != null) this.needed = this.recipe.getEnergyCost(this.tile);
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
        return valid || !this.recipe.assemble(this.tile.getCraftMatrix()).isEmpty();
    }
}
