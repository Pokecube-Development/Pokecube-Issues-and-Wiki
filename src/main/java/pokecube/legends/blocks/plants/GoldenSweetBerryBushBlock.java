package pokecube.legends.blocks.plants;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.init.ItemInit;

public class GoldenSweetBerryBushBlock extends SweetBerryBushBlock implements BonemealableBlock
{
   public GoldenSweetBerryBushBlock(BlockBehaviour.Properties properties)
   {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   @Override
   public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
   {
      return new ItemStack(ItemInit.GOLDEN_SWEET_BERRIES.get());
   }

   @Override
   public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitBlock)
   {
      int i = state.getValue(AGE);
      boolean flag = i == 3;
      if (!flag && player.getItemInHand(hand).is(Items.BONE_MEAL))
      {
         return InteractionResult.PASS;
      } else if (i > 1)
      {
         int j = 1 + world.random.nextInt(2);
         popResource(world, pos, new ItemStack(ItemInit.GOLDEN_SWEET_BERRIES.get(), j + (flag ? 1 : 0)));
         world.playSound((Player)null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
         world.setBlock(pos, state.setValue(AGE, Integer.valueOf(1)), 2);
         return InteractionResult.sidedSuccess(world.isClientSide);
      } else
      {
         return super.use(state, world, pos, player, hand, hitBlock);
      }
   }

    @Override
    public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
    {
        return false;
    }

    @Nullable
    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity)
    {
        return BlockPathTypes.DAMAGE_OTHER;
    }
}
