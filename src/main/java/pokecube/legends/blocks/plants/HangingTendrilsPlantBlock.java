package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.PlantsInit;

public class HangingTendrilsPlantBlock extends GrowingPlantBodyBlock implements BonemealableBlock
{
   public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   public static final BooleanProperty EYES = BooleanProperty.create("eyes");

   public HangingTendrilsPlantBlock(BlockBehaviour.Properties properties)
   {
       super(properties, Direction.DOWN, SHAPE, false);
       this.registerDefaultState(this.stateDefinition.any().setValue(EYES, Boolean.valueOf(false)));
   }

   @Override
   public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
   {
       builder.add(EYES);
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
   {
       if (state.getValue(EYES) && player.isShiftKeyDown())
       {
           float f = Mth.randomBetween(world.random, 0.8F, 1.2F);
           world.playSound((Player)null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
           world.setBlock(pos, state.setValue(EYES, Boolean.valueOf(false)), 2);
           return InteractionResult.sidedSuccess(world.isClientSide);
       } else
       {
           return InteractionResult.PASS;
       }
   }

   @Override
   public BlockState updateHeadAfterConvertedFromBody(BlockState state, BlockState state1)
   {
      return state1.setValue(EYES, state.getValue(EYES));
   }

   @Override
   public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
   {
       return true;
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {       
       if (!state.getValue(EYES))
       {
           world.setBlock(pos, state.setValue(EYES, Boolean.valueOf(true)), 2);
       }
   }

   @Override
   protected GrowingPlantHeadBlock getHeadBlock()
   {
      return (GrowingPlantHeadBlock)PlantsInit.HANGING_TENDRILS.get();
   }
   
   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
       return new ItemStack(PlantsInit.HANGING_TENDRILS.get());
   }
}
