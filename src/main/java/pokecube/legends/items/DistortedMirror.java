package pokecube.legends.items;

import java.util.List;
import java.util.Random;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.worldgen.DimensionTranserHelper;
import thut.lib.TComponent;

public class DistortedMirror extends ItemBase
{

    public DistortedMirror(final String name, final CreativeModeTab tab, final int maxStackSize)
    {
        super(name, tab, maxStackSize);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltip_id + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(TComponent.translatable(message));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player entity, final InteractionHand hand)
    {
        ItemStack stack = new ItemStack(this);
        final ResourceKey<Level> dim = world.dimension();
        Random random = world.getRandom();
        final double x = entity.getX();
        final double y = entity.getY();
        final double z = entity.getZ();

        if (dim == Level.OVERWORLD)
        {
            world.playLocalSound(x, y, z, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS, 1, 1, false);

            for (int i = 0; i < 100; ++i)
            {
                world.addParticle(ParticleTypes.ENCHANT,
                        entity.getRandomX(1.5D), entity.getRandomY(), entity.getRandomZ(1.5D),
                        (random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(),
                        (random.nextDouble() - 0.5D) * 2.0D);
            }

            if (entity instanceof ServerPlayer)
            {
                DimensionTranserHelper.sentToDistorted((ServerPlayer) entity);
                entity.addEffect((new MobEffectInstance(MobEffects.CONFUSION, 200, 1)));
            }

            world.playLocalSound(x, y, z, SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS, 1, 1, false);

            entity.getCooldowns().addCooldown(ItemInit.GIRATINA_MIRROR.get(),
                    PokecubeLegends.config.mirrorCooldown);

            return InteractionResultHolder.success(stack);
        }
        else if (dim == FeaturesInit.DISTORTEDWORLD_KEY)
        {
            world.playLocalSound(x, y, z, SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.PLAYERS, 1, 1, false);

            for (int i = 0; i < 100; ++i)
            {
                world.addParticle(ParticleTypes.ENCHANT,
                        entity.getRandomX(1.5D), entity.getRandomY(), entity.getRandomZ(1.5D),
                        (random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(),
                        (random.nextDouble() - 0.5D) * 2.0D);
            }

            if (entity instanceof ServerPlayer)
            {
                DimensionTranserHelper.sendToOverworld((ServerPlayer) entity);
                entity.addEffect((new MobEffectInstance(MobEffects.CONFUSION, 200, 1)));
            }

            world.playLocalSound(x, y, z, SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS, 1, 1, false);

            entity.getCooldowns().addCooldown(ItemInit.GIRATINA_MIRROR.get(),
                    PokecubeLegends.config.mirrorCooldown);

            return InteractionResultHolder.success(stack);
        }
        else
        {
            world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS, 1, 1, false);
            return InteractionResultHolder.success(stack);
        }
    }
}
