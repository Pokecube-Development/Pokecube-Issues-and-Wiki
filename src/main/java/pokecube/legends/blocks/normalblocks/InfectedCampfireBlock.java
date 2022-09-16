package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import pokecube.legends.init.ParticleInit;
import pokecube.legends.init.TileEntityInit;
import pokecube.legends.tileentity.InfectedCampfireBlockEntity;

public class InfectedCampfireBlock extends CampfireBlock
{
    private final int fireDamage;
    private final boolean spawnParticles;

    public InfectedCampfireBlock(boolean particles, int damage, BlockBehaviour.Properties properties)
    {
        super(particles, damage, properties);
        this.fireDamage = damage;
        this.spawnParticles = particles;
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

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource source)
    {
        if (state.getValue(LIT))
        {
            if (source.nextInt(10) == 0)
            {
                world.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                        SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + source.nextFloat(), source.nextFloat() * 0.7F + 0.6F, false);
            }

            if (this.spawnParticles && source.nextInt(5) == 0)
            {
                for(int i = 0; i < source.nextInt(1) + 1; ++i)
                {
                    world.addParticle(ParticleInit.INFECTED_SPARK.get(), (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D,
                            (double)pos.getZ() + 0.5D, (double)(source.nextFloat() / 2.0F), 5.0E-5D, (double)(source.nextFloat() / 2.0F));
                }
            }
        }
    }
}