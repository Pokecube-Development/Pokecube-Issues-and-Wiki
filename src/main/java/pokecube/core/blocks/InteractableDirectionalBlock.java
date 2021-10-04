package pokecube.core.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class InteractableDirectionalBlock extends DirectionalBlock
{

    public InteractableDirectionalBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DirectionalBlock.FACING,
                Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DirectionalBlock.FACING);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof InteractableTile) return ((InteractableTile) tile).onInteract(pos, player, hand, hit);
        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(final Level worldIn, final BlockPos pos, final Entity entityIn)
    {
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof InteractableTile) ((InteractableTile) tile).onWalkedOn(entityIn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(final BlockState state, final Level worldIn, final BlockPos pos, final BlockState newState,
            final boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            final BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof InteractableTile) ((InteractableTile) tileentity).onBroken();
            if (tileentity == null)
            {

            }
            else if (tileentity instanceof Container)
            {
                Containers.dropContents(worldIn, pos, (Container) tileentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            else
            {
                final IItemHandler items = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .orElse(null);
                if (items != null)
                {

                    final Container inventory = new Container()
                    {

                        @Override
                        public void clearContent()
                        {
                        }

                        @Override
                        public void setItem(final int index, final ItemStack stack)
                        {
                        }

                        @Override
                        public ItemStack removeItemNoUpdate(final int index)
                        {
                            return ItemStack.EMPTY;
                        }

                        @Override
                        public void setChanged()
                        {
                        }

                        @Override
                        public boolean stillValid(final Player player)
                        {
                            return false;
                        }

                        @Override
                        public boolean isEmpty()
                        {
                            return false;
                        }

                        @Override
                        public ItemStack getItem(final int index)
                        {
                            return items.getStackInSlot(index);
                        }

                        @Override
                        public int getContainerSize()
                        {
                            return items.getSlots();
                        }

                        @Override
                        public ItemStack removeItem(final int index, final int count)
                        {
                            return ItemStack.EMPTY;
                        }
                    };
                    Containers.dropContents(worldIn, pos, inventory);
                    worldIn.updateNeighbourForOutputSignal(pos, this);
                }
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}
