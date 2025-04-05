package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlossomLilyPadBlock extends LilyPadBlock
{
   // Precise selection box
   private static final VoxelShape LILY_COLLISION = Block.box(1, 0, 1, 15, 1.5, 15);
   private static final VoxelShape LILY = Shapes.or(
      Block.box(1, 0, 1, 15, 1.5, 15),
      Block.box(3, 0.25, 3, 13, 6.25, 13)).optimize();

   public BlossomLilyPadBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
   {
      return BlossomLilyPadBlock.LILY;
   }

   @Override
   public VoxelShape getCollisionShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
   {
      return BlossomLilyPadBlock.LILY_COLLISION;
   }
}