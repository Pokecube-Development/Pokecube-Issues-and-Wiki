package pokecube.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

    public InteractionResult useWithoutItem(final BlockPos pos, final Player player, final BlockHitResult hit)
    {
        return InteractionResult.PASS;
    }

    public ItemInteractionResult useItemOn(ItemStack stack, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult)
    {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
