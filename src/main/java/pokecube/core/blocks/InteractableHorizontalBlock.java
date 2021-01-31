package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class InteractableHorizontalBlock extends HorizontalBlock
{

    public InteractableHorizontalBlock(final Properties properties, final MaterialColor color)
    {
        super(Properties.create(Material.IRON, color).hardnessAndResistance(3.0f, 5.0f).harvestTool(ToolType.PICKAXE));
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING,
                Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalBlock.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING,
                context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof InteractableTile) return ((InteractableTile) tile).onInteract(pos, player, hand, hit);
        return ActionResultType.PASS;
    }

    @Override
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof InteractableTile) ((InteractableTile) tile).onWalkedOn(entityIn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(final BlockState state, final World worldIn, final BlockPos pos, final BlockState newState,
            final boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            final TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof InteractableTile) ((InteractableTile) tileentity).onBroken();
            if (tileentity == null)
            {

            }
            else if (tileentity instanceof IInventory)
            {
                InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
                worldIn.updateComparatorOutputLevel(pos, this);
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
                        public void clear()
                        {
                        }

                        @Override
                        public void setInventorySlotContents(final int index, final ItemStack stack)
                        {
                        }

                        @Override
                        public ItemStack removeStackFromSlot(final int index)
                        {
                            return ItemStack.EMPTY;
                        }

                        @Override
                        public void markDirty()
                        {
                        }

                        @Override
                        public boolean isUsableByPlayer(final PlayerEntity player)
                        {
                            return false;
                        }

                        @Override
                        public boolean isEmpty()
                        {
                            return false;
                        }

                        @Override
                        public ItemStack getStackInSlot(final int index)
                        {
                            return items.getStackInSlot(index);
                        }

                        @Override
                        public int getSizeInventory()
                        {
                            return items.getSlots();
                        }

                        @Override
                        public ItemStack decrStackSize(final int index, final int count)
                        {
                            return ItemStack.EMPTY;
                        }
                    };
                    InventoryHelper.dropInventoryItems(worldIn, pos, inventory);
                    worldIn.updateComparatorOutputLevel(pos, this);
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
}
