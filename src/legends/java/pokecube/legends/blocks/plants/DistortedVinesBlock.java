package pokecube.legends.blocks.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.blocks.normalblocks.DistorticGrassBlock;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

public class DistortedVinesBlock extends GrowingPlantBodyBlock
{
   public static final MapCodec<DistortedVinesBlock> CODEC = simpleCodec(DistortedVinesBlock::new);
   @Override
   protected MapCodec<? extends GrowingPlantBodyBlock> codec()
   {
      return CODEC;
   }

   public static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

   public DistortedVinesBlock(BlockBehaviour.Properties properties)
   {
       super(properties, Direction.UP, SHAPE, false);
   }

   protected GrowingPlantHeadBlock getHeadBlock()
   {
      return (GrowingPlantHeadBlock)PlantsInit.DISTORTIC_VINES.get();
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state)
   {
      return new ItemStack(ItemInit.DISTORTIC_VINES.get());
   }
}
