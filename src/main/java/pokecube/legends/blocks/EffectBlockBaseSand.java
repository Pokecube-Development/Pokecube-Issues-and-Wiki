package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import pokecube.legends.init.ItemInit;

public class EffectBlockBaseSand extends SandBlock
{
    private final MobEffect effect;
    private final int dustColor;

    public EffectBlockBaseSand(int color, BlockBehaviour.Properties properties, final MobEffect effects)
    {
        super(color, properties);
        this.dustColor = color;
        this.effect = effects;
    }

    @Override
    public int getDustColor(BlockState p_55970_, BlockGetter p_55971_, BlockPos p_55972_) {
       return this.dustColor;
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        EffectBlockBaseSand.applyEffects(entity, this.effect);
    }

    public static void applyEffects(final Entity entity, final MobEffect effects)
    {
        if (entity instanceof ServerPlayer) if (((Player) entity).getInventory().armor.get(3)
                .getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() || ((Player) entity)
                        .getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1)
                                .getItem() || ((Player) entity).getInventory().armor.get(1).getItem() != new ItemStack(
                                        ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() || ((Player) entity)
                                                .getInventory().armor.get(0).getItem() != new ItemStack(
                                                        ItemInit.ULTRA_BOOTS.get(), 1).getItem())
            ((LivingEntity) entity).addEffect(new MobEffectInstance(effects, 120, 1));
    }
}
