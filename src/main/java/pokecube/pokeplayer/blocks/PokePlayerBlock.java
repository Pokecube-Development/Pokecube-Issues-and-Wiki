package pokecube.pokeplayer.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PokePlayerBlock extends PressurePlateBlock{
	
	public PokePlayerBlock(Sensitivity sensitivityIn, Properties propertiesIn) {
		super(sensitivityIn, propertiesIn);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader) {
		return new PokePlayerTileEntity();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
		super.eventReceived(state, world, pos, eventID, eventParam);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if(!worldIn.isRemote) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if(tile instanceof PokePlayerTileEntity) {
				//NetworkHooks.openGui((ServerPlayerEntity)player, (PokePlayerTileEntity)tile, pos);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.FAIL;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof PokePlayerTileEntity) {
				InventoryHelper.dropInventoryItems(world, pos, (PokePlayerTileEntity) tileentity);
				world.updateComparatorOutputLevel(pos, this);
			}
			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}
	
	@Override
	public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof PokePlayerTileEntity)
			return Container.calcRedstoneFromInventory((PokePlayerTileEntity) tileentity);
		else
			return 0;
	}
}