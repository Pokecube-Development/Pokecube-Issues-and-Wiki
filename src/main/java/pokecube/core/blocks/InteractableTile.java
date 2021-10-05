package pokecube.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class InteractableTile extends BlockEntity
{

    public InteractableTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        return InteractionResult.PASS;
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
