package pokecube.legends.items;

import java.util.List;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
import pokecube.legends.init.ParticleInit;
import pokecube.legends.worldgen.DimensionTranserHelper;
import thut.lib.TComponent;

public class UltraKey extends ItemBase
{

    public UltraKey(final String name, final CreativeModeTab tab, final int maxStackSize)
    {
        super(name, tab, maxStackSize);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (Screen.hasShiftDown())
        {
            tooltip.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
            tooltip.add(TComponent.translatable(I18n.get("legends." + this.tooltip_id + ".tooltip.line1",
                    PokecubeLegends.config.ultraKeyRequiredFuelAmount, ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
        }
        else tooltip.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player entity, final InteractionHand hand)
    {
        ItemStack stack = new ItemStack(this);

        if (PokecubeLegends.config.ultraKeyRequireFuel)
        {
            UltraKey.dimensionTPRequireFuel(entity, world);
            return InteractionResultHolder.success(stack);
        }
        else {
            UltraKey.dimensionTP(entity, world);
            return InteractionResultHolder.success(stack);
        }
    }

    public static void dimensionTP(final Entity entity, final Level world)
    {
        final ResourceKey<Level> dim = world.dimension();

        if (dim == Level.OVERWORLD)
        {
            teleportEffects(entity, world, ParticleTypes.SCRAPE, SoundEvents.BEACON_POWER_SELECT);

            if (entity instanceof ServerPlayer) DimensionTranserHelper.sentToUltraspace((ServerPlayer) entity);

            if (entity instanceof Player)
                ((Player) entity).getCooldowns().addCooldown(ItemInit.ULTRAKEY.get(), 200);

        } else if (dim == FeaturesInit.ULTRASPACE_KEY)
        {
            teleportEffects(entity, world, ParticleTypes.WAX_OFF, SoundEvents.BEACON_POWER_SELECT);

            if (entity instanceof ServerPlayer) DimensionTranserHelper.sendToOverworld((ServerPlayer) entity);

            if (entity instanceof Player)
                ((Player) entity).getCooldowns().addCooldown(ItemInit.ULTRAKEY.get(), 200);
        }
    }

    public static void dimensionTPRequireFuel(final Entity entity, final Level world)
    {
        final ResourceKey<Level> dimension = world.dimension();

        if (entity instanceof Player player
                && player.getInventory()
                .countItem(ItemInit.COSMIC_DUST.get()) >= PokecubeLegends.config.ultraKeyRequiredFuelAmount
                || (entity instanceof Player playerC && playerC.isCreative()))
        {
            if (dimension == Level.OVERWORLD)
            {
                teleportEffects(entity, world, ParticleTypes.SCRAPE, SoundEvents.BEACON_POWER_SELECT);

                ((Player) entity).getInventory().clearOrCountMatchingItems(
                        p -> new ItemStack(ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(),
                        PokecubeLegends.config.ultraKeyRequiredFuelAmount, ((Player) entity).inventoryMenu.getCraftSlots());

                if (entity instanceof ServerPlayer) DimensionTranserHelper.sentToUltraspace((ServerPlayer) entity);

                ((Player) entity).getCooldowns().addCooldown(ItemInit.ULTRAKEY.get(), PokecubeLegends.config.ultraKeyCooldown);

            } else if (dimension == FeaturesInit.ULTRASPACE_KEY)
            {
                teleportEffects(entity, world, ParticleTypes.WAX_OFF, SoundEvents.BEACON_POWER_SELECT);

                ((Player) entity).getInventory().clearOrCountMatchingItems(
                        p -> new ItemStack(ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(),
                        PokecubeLegends.config.ultraKeyRequiredFuelAmount, ((Player) entity).inventoryMenu.getCraftSlots());

                if (entity instanceof ServerPlayer) DimensionTranserHelper.sendToOverworld((ServerPlayer) entity);

                ((Player) entity).getCooldowns().addCooldown(ItemInit.ULTRAKEY.get(), PokecubeLegends.config.ultraKeyCooldown);

            } else
            {
                teleportFailEffects(entity, world, ParticleInit.ERROR.get(), SoundEvents.AXE_SCRAPE, SoundEvents.BEACON_DEACTIVATE);
            }

        } else if (entity instanceof Player player && player.getInventory()
                .countItem(ItemInit.COSMIC_DUST.get()) < PokecubeLegends.config.ultraKeyRequiredFuelAmount
                && !player.isCreative())
        {
            final String message = I18n.get("msg.pokecube_legends.ultrakey.no_dust",
                    PokecubeLegends.config.ultraKeyRequiredFuelAmount, ChatFormatting.RED, ChatFormatting.BOLD);
            player.displayClientMessage(TComponent.translatable(message), true);

            teleportFailEffects(entity, world, ParticleInit.ERROR.get(), SoundEvents.AXE_SCRAPE, SoundEvents.BEACON_DEACTIVATE);
        } else
        {
            teleportFailEffects(entity, world, ParticleInit.ERROR.get(), SoundEvents.AXE_SCRAPE, SoundEvents.BEACON_DEACTIVATE);
        }
    }

    public static void teleportEffects(final Entity entity, final Level world, ParticleOptions particle, SoundEvent sound)
    {
        Random random = world.getRandom();
        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1, 1, false);

        for (int i = 0; i < 25; ++i)
        {
            world.addParticle(particle,
                    entity.getRandomX(1.5D), entity.getRandomY(), entity.getRandomZ(1.5D),
                    (random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(),
                    (random.nextDouble() - 0.5D) * 2.0D);
        }
    }

    public static void teleportFailEffects(final Entity entity, final Level world, ParticleOptions particle, SoundEvent sound, SoundEvent sound2)
    {
        Random random = world.getRandom();

        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), sound,
                SoundSource.PLAYERS, 1, 1, false);

        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), sound2,
                SoundSource.PLAYERS, 1, 1, false);

        for (int i = 0; i < 25; ++i)
        {
            world.addParticle(particle,
                    entity.getRandomX(1.5D), entity.getRandomY(), entity.getRandomZ(1.5D),
                    (random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(),
                    (random.nextDouble() - 0.5D) * 2.0D);
        }
    }
}