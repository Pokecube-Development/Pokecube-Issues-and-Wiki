package pokecube.legends.handlers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.legends.init.ItemInit;

public class EventsHandler
{
    public static void register()
    {
        MinecraftForge.EVENT_BUS.addListener(EventsHandler::onPlayerTick);
    }

    public static void onPlayerTick(final PlayerTickEvent event)
    {
        if (event.side == LogicalSide.SERVER && event.player instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) event.player;
            final Biome biome = player.getLevel().getBiome(player.getOnPos());

            if ((biome.getRegistryName().toString().equals("pokecube_legends:blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:dried_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:magmatic_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:shattered_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:volcanic_blinding_deltas"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.BLINDNESS)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0));
                }
            }

            if (biome.getRegistryName().toString().equals("pokecube_legends:burnt_beach")
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.UNLUCK)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.UNLUCK, 240, 1));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:corrupted_caves")
                    || biome.getRegistryName().toString().equals("pokecube_legends:polluted_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:polluted_river")
                    || biome.getRegistryName().toString().equals("pokecube_legends:shattered_tainted_barrens")
                    || biome.getRegistryName().toString().equals("pokecube_legends:tainted_barrens"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.CONFUSION)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 1));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:crystallized_beach")
                    || biome.getRegistryName().toString().equals("pokecube_legends:mirage_desert")
                    || biome.getRegistryName().toString().equals("pokecube_legends:rocky_mirage_desert")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_sunflower_plains")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_fungal_plains"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.LEVITATION)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 240, 0));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:dead_ocean"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.HUNGER)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.HUNGER, 240, 2));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:distorted_lands")
                    || biome.getRegistryName().toString().equals("pokecube_legends:small_distorted_islands"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SPEED)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 2));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:old_growth_forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:windswept_forbidden_taiga"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.WITHER)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.WITHER, 240, 0));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:fungal_flower_forest")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_forest")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_plains")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_sunflower_plains")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_fungal_plains"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SLOWDOWN)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 1));
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:sparse_temporal_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:temporal_bamboo_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:temporal_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:windswept_temporal_jungle"))
                    && !player.isCreative() && !player.isSpectator()
                    && !(player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.POISON)))
            {
                if (((Player) player).getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || ((Player) player).getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    ((LivingEntity) player).addEffect(new MobEffectInstance(MobEffects.POISON, 240, 1));
                }
            }
        }
    }
}
