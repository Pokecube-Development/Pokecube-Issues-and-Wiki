package pokecube.legends.blocks.normalblocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import pokecube.legends.Reference;

public class InfectedFireBlock extends BaseFireBlock
{
   // Tags
   public static TagKey<Block> INFECTED_FIRE_BASE_BLOCKS = BlockTags.create(new ResourceLocation(Reference.ID, "infected_fire_base_blocks"));
   private final float fireDamage;

   public InfectedFireBlock(float damage, BlockBehaviour.Properties properties)
   {
      super(properties, damage);
      this.fireDamage = damage;
   }

   @Override
   public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
   {
      return this.canSurvive(state, world, pos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
   {
      return canSurviveOnBlock(world.getBlockState(pos.below()));
   }

   public static boolean canSurviveOnBlock(BlockState state)
   {
      return state.is(INFECTED_FIRE_BASE_BLOCKS);
   }

   @Override
   public boolean canBurn(BlockState state)
   {
      return true;
   }

    @Override
    public void entityInside(final BlockState state, final Level world, final BlockPos pos, final Entity entity)
    {
        if (!entity.fireImmune())
        {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0)
            {
               entity.setSecondsOnFire(8);
            }
            if (entity instanceof LivingEntity)
            {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            }
            entity.hurt(entity.damageSources().inFire(), this.fireDamage);
        }
        super.entityInside(state, world, pos, entity);
    }

    @Override
    public boolean isPathfindable(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final PathComputationType path)
    {
        return false;
    }

    @Nullable
    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity)
    {
        return BlockPathTypes.DAMAGE_FIRE;
    }
}