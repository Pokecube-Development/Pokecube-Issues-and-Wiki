package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LilyPadBlock extends WaterlilyBlock
{
   // Precise selection box
   private static final VoxelShape LILY= Block.box(1, 0, 1, 15, 1.5, 15);

   public LilyPadBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
   }

   @Override
   public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
   {
      return LilyPadBlock.LILY;
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
   {
      FluidState fluidState = block.getFluidState(pos);
      FluidState fluidState1 = block.getFluidState(pos.above());
      return (fluidState.getType() == Fluids.WATER || state.getMaterial() == Material.ICE) && fluidState1.getType() == Fluids.EMPTY;
   }

   @Override
   public boolean canSurvive(final BlockState state, final LevelReader reader, final BlockPos pos)
   {
      FluidState fluidState = reader.getFluidState(pos.below());
      return (fluidState.getType() == Fluids.WATER || state.getMaterial() == Material.ICE);
   }
}