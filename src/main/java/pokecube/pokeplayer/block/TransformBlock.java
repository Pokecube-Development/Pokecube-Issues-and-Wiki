package pokecube.pokeplayer.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

public class TransformBlock extends PressurePlateBlock
{
	public TransformBlock(Sensitivity sensitivity, Properties propertiesIn) {
		super(sensitivity, propertiesIn);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader reader)
    {
        return new TileEntityTransformer();
    }

	@Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
   
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
    		Hand hand, BlockRayTraceResult hit) 
    {
    	if(!world.isRemote) {
			final TileEntity tile = world.getTileEntity(pos);
		    if (tile instanceof TileEntityTransformer) {
		    	NetworkHooks.openGui((ServerPlayerEntity) player, (TileEntityTransformer) tile, pos);
		    	return ActionResultType.SUCCESS;
		    }
    	}
    	return ActionResultType.FAIL;
    }
    
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) 
    {
    	super.onEntityCollision(state, worldIn, pos, entityIn);
    	TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTransformer && entityIn instanceof PlayerEntity)
        {
            ((TileEntityTransformer) tile).onWalkedOn(entityIn);
        }
    }
//    
//    @Override
//    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) 
//    {
//      TileEntity tile = worldIn.getTileEntity(pos);
//      if (tile instanceof TileEntityTransformer && entityIn instanceof PlayerEntity)
//      {
//          ((TileEntityTransformer) tile).onWalkedOn(entityIn);
//          PokePlayer.LOGGER.debug("PIsou");
//      }
//      super.onEntityWalk(worldIn, pos, entityIn);
//    }
//	@Override
//    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand,
//			BlockRayTraceResult hit)
//    {
//        final TileEntity tile = worldIn.getTileEntity(pos);
//        if (tile instanceof TileEntityTransformer) 
//       return ((TileEntityTransformer) tile).onInteract(pos, playerIn, hand, hit);
//        return ActionResultType.PASS;
//    }
    
//    @Override
//    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity)
//    {
//    	super.onEntityWalk(world, pos, entity);
//        TileEntity tile = world.getTileEntity(pos);
//        if (tile instanceof TileEntityTransformer && entity instanceof PlayerEntity)
//        {
//            ((TileEntityTransformer) tile).onStepped((PlayerEntity) entity, world);
//        }
//    }
    
//    /** Called When an Entity Collided with the Block */
//    @Override
//    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) 
//    {
//        TileEntity tile = worldIn.getTileEntity(pos);
//        if (tile instanceof TileEntityTransformer && entityIn instanceof PlayerEntity)
//        {
//            ((TileEntityTransformer) tile).onWalkedOn(entityIn);
//        }
//        super.onEntityCollision(state, worldIn, pos, entityIn);
//    }
    
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    	if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileEntityTransformer) {
				InventoryHelper.dropItems(worldIn, pos, ((TileEntityTransformer) tileentity).getItems());
				worldIn.updateComparatorOutputLevel(pos, this);
			}
    	}
    }
}
