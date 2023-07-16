package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.blocks.FlowerBase;

public class AzureColeusBlock extends FlowerBase
{
   protected static final float AABB_OFFSET = 6.0F;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    
   public AzureColeusBlock(final MobEffect effects, int seconds, final BlockBehaviour.Properties properties)
   {
      super(effects, seconds, properties);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext collision)
   {
       Vec3 vec3 = state.getOffset(block, pos);
       return SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   @Override
   public BlockBehaviour.OffsetType getOffsetType()
   {
      return BlockBehaviour.OffsetType.XZ;
   }

   @Override
   public boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
   {
      return state.is(BlockTags.SAND) || state.is(Blocks.RED_SAND) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.DIRT);
   }
}
