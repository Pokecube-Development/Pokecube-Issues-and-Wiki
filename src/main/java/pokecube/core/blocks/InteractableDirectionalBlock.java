package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
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
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DirectionalBlock.FACING);
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        final TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof InteractableTile) return ((InteractableTile) tile).onInteract(pos, player, hand, hit);
        return ActionResultType.PASS;
    }

    @Override
    public void stepOn(final World worldIn, final BlockPos pos, final Entity entityIn)
    {
        final TileEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof InteractableTile) ((InteractableTile) tile).onWalkedOn(entityIn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(final BlockState state, final World worldIn, final BlockPos pos, final BlockState newState,
            final boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            final TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof InteractableTile) ((InteractableTile) tileentity).onBroken();
            if (tileentity == null)
            {

            }
            else if (tileentity instanceof IInventory)
            {
                InventoryHelper.dropContents(worldIn, pos, (IInventory) tileentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            else
            {
                final IItemHandler items = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .orElse(null);
                if (items != null)
                {

                    final IInventory inventory = new IInventory()
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
                        public boolean stillValid(final PlayerEntity player)
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
                    InventoryHelper.dropContents(worldIn, pos, inventory);
                    worldIn.updateNeighbourForOutputSignal(pos, this);
                }
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
}
