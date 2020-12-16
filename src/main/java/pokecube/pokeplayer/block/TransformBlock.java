package pokecube.pokeplayer.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

public class TransformBlock extends PressurePlateBlock
{
    public TransformBlock(Sensitivity sensitivityIn, Properties propertiesIn) {
		super(sensitivityIn, propertiesIn);
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand,
			BlockRayTraceResult hit)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTransformer)
        {
            ((TileEntityTransformer) tile).onInteract(playerIn);
        }
        return ActionResultType.SUCCESS;
    }
    
    @Override
    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity)
    {
    	super.onEntityWalk(world, pos, entity);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityTransformer && entity instanceof PlayerEntity)
        {
            ((TileEntityTransformer) tile).onStepped((PlayerEntity) entity, world);
        }
    }
    
//    /** Called When an Entity Collided with the Block */
//    @Override
//    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) 
//    {
//        TileEntity tile = worldIn.getTileEntity(pos);
//        if (tile instanceof TileEntityTransformer && entityIn instanceof PlayerEntity)
//        {
//            ((TileEntityTransformer) tile).onStepped((PlayerEntity) entityIn, worldIn);
//        }
//        super.onEntityCollision(state, worldIn, pos, entityIn);
//    }
    
    @Override
	public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityTransformer)
			return Container.calcRedstoneFromInventory((TileEntityTransformer) tileentity);
		else
			return 0;
	}
}
