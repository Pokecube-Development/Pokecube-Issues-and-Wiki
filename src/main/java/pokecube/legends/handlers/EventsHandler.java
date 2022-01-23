package pokecube.legends.handlers;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
            final Biome biome = event.player.getLevel().getBiome(player.getOnPos());
            MobEffectInstance effect = new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 480, 2);

            if (biome.getRegistryName().toString().equals("pokecube_legends:aquamarine_caves")
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.DIG_SLOWDOWN))
            {
                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:azure_badlands")
                    || biome.getRegistryName().toString().equals("pokecube_legends:eroded_azure_badlands")
                    || biome.getRegistryName().toString().equals("pokecube_legends:wooded_azure_badlands"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.WEAKNESS))
            {
                effect = new MobEffectInstance(MobEffects.WEAKNESS, 480, 1);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:dried_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:magmatic_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:shattered_blinding_deltas")
                    || biome.getRegistryName().toString().equals("pokecube_legends:volcanic_blinding_deltas"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.BLINDNESS))
            {
                effect = new MobEffectInstance(MobEffects.BLINDNESS, 480, 0);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:burnt_beach")
                    || biome.getRegistryName().toString().equals("pokecube_legends:meteorite_spikes"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.UNLUCK))
            {
                effect = new MobEffectInstance(MobEffects.UNLUCK, 480, 1);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                    player.setSecondsOnFire(10);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:corrupted_caves")
                    || biome.getRegistryName().toString().equals("pokecube_legends:deep_frozen_polluted_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:deep_polluted_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:frozen_polluted_river")
                    || biome.getRegistryName().toString().equals("pokecube_legends:frozen_polluted_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:polluted_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:polluted_river")
                    || biome.getRegistryName().toString().equals("pokecube_legends:shattered_tainted_barrens")
                    || biome.getRegistryName().toString().equals("pokecube_legends:tainted_barrens"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.CONFUSION))
            {
                effect = new MobEffectInstance(MobEffects.CONFUSION, 480, 1);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:crystallized_beach")
                    || biome.getRegistryName().toString().equals("pokecube_legends:mirage_desert")
                    || biome.getRegistryName().toString().equals("pokecube_legends:rocky_mirage_desert")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_crystallized_beach"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.LEVITATION))
            {
                effect = new MobEffectInstance(MobEffects.LEVITATION, 120, 0);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:dead_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:dead_river")
                    || biome.getRegistryName().toString().equals("pokecube_legends:deep_dead_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:deep_frozen_dead_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:frozen_dead_ocean")
                    || biome.getRegistryName().toString().equals("pokecube_legends:frozen_dead_river"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.HUNGER))
            {
                effect = new MobEffectInstance(MobEffects.HUNGER, 480, 2);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:distorted_lands")
                    || biome.getRegistryName().toString().equals("pokecube_legends:small_distorted_islands"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SPEED))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 480, 2);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if (biome.getRegistryName().toString().equals("pokecube_legends:dripstone_caves")
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.HARM))
            {
                effect = new MobEffectInstance(MobEffects.HARM, 480, 0);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:forbidden_grove")
                    || biome.getRegistryName().toString().equals("pokecube_legends:forbidden_meadow")
                    || biome.getRegistryName().toString().equals("pokecube_legends:forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:old_growth_forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_forbidden_taiga")
                    || biome.getRegistryName().toString().equals("pokecube_legends:windswept_forbidden_taiga"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.WITHER))
            {
                effect = new MobEffectInstance(MobEffects.WITHER, 480, 1);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:frozen_peaks")
                    || biome.getRegistryName().toString().equals("pokecube_legends:jagged_peaks")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_slopes")
                    || biome.getRegistryName().toString().equals("pokecube_legends:ultra_stony_peaks"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SLOWDOWN))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 480, 2);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }
            
            if ((biome.getRegistryName().toString().equals("pokecube_legends:fungal_flower_forest")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_forest")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_plains")
                    || biome.getRegistryName().toString().equals("pokecube_legends:fungal_sunflower_plains")
                    || biome.getRegistryName().toString().equals("pokecube_legends:snowy_fungal_plains"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.MOVEMENT_SLOWDOWN))
            {
                effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 480, 0);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if ((biome.getRegistryName().toString().equals("pokecube_legends:sparse_temporal_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:temporal_bamboo_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:temporal_jungle")
                    || biome.getRegistryName().toString().equals("pokecube_legends:windswept_temporal_jungle"))
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.POISON))
            {
                effect = new MobEffectInstance(MobEffects.POISON, 480, 1);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            if (biome.getRegistryName().toString().equals("pokecube_legends:ultra_stony_shore")
                    && !player.isCreative() && !player.isSpectator()
                    && !player.getActiveEffects().stream().anyMatch(e -> e.getEffect() == MobEffects.HARM))
            {
                effect = new MobEffectInstance(MobEffects.UNLUCK, 480, 0);

                if (player.getInventory().armor.get(3).getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                        || player.getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                        || player.getInventory().armor.get(1).getItem() != new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                        || player.getInventory().armor.get(0).getItem() != new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
                {
                    player.addEffect(effect);
                }
            }

            effect.setCurativeItems(Lists.newArrayList(new ItemStack(ItemInit.ULTRA_HELMET.get()),
                    new ItemStack(ItemInit.ULTRA_CHESTPLATE.get()), new ItemStack(ItemInit.ULTRA_LEGGINGS.get()),
                    new ItemStack(ItemInit.ULTRA_BOOTS.get())));

            if (player.getInventory().armor.get(3).getItem() == new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem()
                    && player.getInventory().armor.get(2).getItem() == new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1).getItem()
                    && player.getInventory().armor.get(1).getItem() == new ItemStack(ItemInit.ULTRA_LEGGINGS.get(), 1).getItem()
                    && player.getInventory().armor.get(0).getItem() == new ItemStack(ItemInit.ULTRA_BOOTS.get(), 1).getItem())
            {
                player.clearFire();
                player.curePotionEffects(new ItemStack(ItemInit.ULTRA_HELMET.get()));
                player.setIsInPowderSnow(false);
            }
        }
    }
}
