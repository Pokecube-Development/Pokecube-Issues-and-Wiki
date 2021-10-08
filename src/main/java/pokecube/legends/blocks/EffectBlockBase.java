package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import pokecube.legends.init.ItemInit;

public class EffectBlockBase extends BlockBase
{
    private final MobEffect effect;

    public EffectBlockBase(final String name, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean hadDrop, final MobEffect effects)
    {
        super(name, material, color, hardness, resistance, sound, hadDrop, effects);
        this.effect = effects;
    }

    public EffectBlockBase(final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean hadDrop, final MobEffect effects)
    {
        super(material, color, hardness, resistance, sound, hadDrop, effects);
        this.effect = effects;
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        EffectBlockBase.applyEffects(entity, this.effect);
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
