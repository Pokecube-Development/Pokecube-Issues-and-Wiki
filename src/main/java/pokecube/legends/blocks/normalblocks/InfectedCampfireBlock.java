package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.TileEntityInit;
import pokecube.legends.tileentity.InfectedCampfireBlockEntity;

public class InfectedCampfireBlock extends CampfireBlock
{
    private final int fireDamage;

    public InfectedCampfireBlock(boolean smoke, int damage, BlockBehaviour.Properties properties)
    {
        super(smoke, damage, properties);
        this.fireDamage = damage;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
       if (!entity.fireImmune() && state.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity))
       {
          entity.hurt(DamageSource.IN_FIRE, (float)this.fireDamage);
          if (entity instanceof LivingEntity)
          {
              ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
          }
       }
       super.entityInside(state, world, pos, entity);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new InfectedCampfireBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        if (world.isClientSide)
        {
            return state.getValue(LIT) ? createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::particleTick) : null;
        } else
        {
            return state.getValue(LIT) ? createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::cookTick) :
                createTickerHelper(type, TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireBlockEntity::cooldownTick);
        }
    }
}