package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class InteractableBlock extends Block
{
    public static final VoxelShape PARTIAL_BASE  = Block.box(0.05D, 0.0D, 0.05D, 15.95D, 2.0D, 15.95D);
    public static final VoxelShape CENTRALCOLUMN = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
    public static final VoxelShape RENDERSHAPE   = VoxelShapes.or(InteractableBlock.PARTIAL_BASE,
            InteractableBlock.CENTRALCOLUMN);

    public InteractableBlock(final Properties properties)
    {
        super(properties);
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
