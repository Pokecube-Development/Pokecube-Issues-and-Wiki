package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.PlantsInit;

public class TallTaintedSeagrassBlock extends TallSeagrassBlock implements LiquidBlockContainer, BonemealableBlock
{
   public TallTaintedSeagrassBlock(BlockBehaviour.Properties config)
   {
      super(config);
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
      return new ItemStack(PlantsInit.TALL_TAINTED_SEAGRASS.get());
   }

   @Override
   protected boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos)
   {
      return state.isFaceSturdy(block, pos, Direction.UP) && !state.is(Blocks.MAGMA_BLOCK) || !state.is(BlockInit.CRYSTALLIZED_SAND.get());
   }

   @Override
   public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
   {
      return true;
   }

   @Override
   public boolean isBonemealSuccess(Level world, Random random, BlockPos pos, BlockState state)
   {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
      popResource(world, pos, new ItemStack(this));
   }

   @Override
   public PlantType getPlantType(BlockGetter world, BlockPos pos)
   {
       return PlantType.PLAINS;
   }
}