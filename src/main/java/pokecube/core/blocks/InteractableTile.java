package pokecube.core.blocks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

public abstract class InteractableTile extends TileEntity
{

    public InteractableTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        return ActionResultType.PASS;
    }

    public void onWalkedOn(final Entity entityIn)
    {

    }

    /**
     * This is called when the block is broken, before attempting to drop the
     * inventory of the tile, if present
     */
    public void onBroken()
    {

    }

}
