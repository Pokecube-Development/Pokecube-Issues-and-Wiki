package pokecube.legends.blocks.plants;

import java.util.Random;
import java.util.function.ToIntFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.PlantsInit;

public class HangingTendrilsBlock extends GrowingPlantHeadBlock implements BonemealableBlock
{
   public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   public static final BooleanProperty EYES = BooleanProperty.create("eyes");

   public HangingTendrilsBlock(BlockBehaviour.Properties properties)
   {
       super(properties, Direction.DOWN, SHAPE, false, 0.1D);
       this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(EYES, Boolean.valueOf(false)));
   }

   @Override
   public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
   {
       super.createBlockStateDefinition(builder);
       builder.add(EYES);
   }

   @Override
   public BlockState updateBodyAfterConvertedFromHead(BlockState state, BlockState state1)
   {
      return state1.setValue(EYES, state.getValue(EYES));
   }

   @Override
   public BlockState getGrowIntoState(BlockState state, Random random)
   {
      return super.getGrowIntoState(state, random).setValue(EYES, Boolean.valueOf(random.nextFloat() < 0.11F));
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
   {
       if (state.getValue(EYES) == true)
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
   public GrowingPlantHeadBlock getHeadBlock()
   {
       return this;
   }

   @Override
   public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
   {
       return !state.getValue(EYES);
   }

   @Override
   public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
   {
       world.setBlock(pos, state.setValue(EYES, Boolean.valueOf(true)), 2);
   }

   @Override
   public int getBlocksToGrowWhenBonemealed(Random random)
   {
       return 1;
   }
   
   @Override
   public boolean canGrowInto(BlockState state)
   {
      return state.isAir();
   }
   
   @Override
   public Block getBodyBlock()
   {
      return PlantsInit.HANGING_TENDRILS_PLANT.get();
   }
   
   public static ToIntFunction<BlockState> emission(int lightLevel)
   {
       return (state) ->
       {
          return state.getValue(EYES) ? lightLevel : 0;
       };
   }
}
