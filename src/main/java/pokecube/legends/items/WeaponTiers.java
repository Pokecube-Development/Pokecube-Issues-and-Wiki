package pokecube.legends.items;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import pokecube.legends.init.ItemInit;

public class WeaponTiers
{
    public static final TagKey<Block> NEEDS_RAINBOW_TOOL = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("pokecube_legends:needs_rainbow_tool"));

    public static final Tier RAINBOW_WING = TierSortingRegistry.registerTier(
            new ForgeTier(5, 5000, 10.0F, 8, 20, NEEDS_RAINBOW_TOOL,
                    () -> Ingredient.of(ItemInit.RAINBOW_WING.get())),
            new ResourceLocation("pokecube_legends:rainbow_wing"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));

    public static final Tier COBALION = TierSortingRegistry.registerTier(
            new ForgeTier(3, 1700, 6.0F, 6.0F, 7, NEEDS_RAINBOW_TOOL,
                    () -> Ingredient.of(Items.NETHERITE_INGOT)),
            new ResourceLocation("pokecube_legends:cobalion"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
    public static final Tier KELDEO = TierSortingRegistry.registerTier(
            new ForgeTier(3, 1800, 7.0F, 4.5F, 10, NEEDS_RAINBOW_TOOL,
                    () -> Ingredient.of(Items.NETHERITE_INGOT)),
            new ResourceLocation("pokecube_legends:keldeo"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
    public static final Tier TERRAKION = TierSortingRegistry.registerTier(
            new ForgeTier(3, 2000, 7.5F, 5.0F, 8, NEEDS_RAINBOW_TOOL,
                    () -> Ingredient.of(Items.NETHERITE_INGOT)),
            new ResourceLocation("pokecube_legends:terrakion"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
    public static final Tier VIRIZION = TierSortingRegistry.registerTier(
            new ForgeTier(3, 1500, 8.0F, 4.0F, 9, NEEDS_RAINBOW_TOOL,
                    () -> Ingredient.of(Items.NETHERITE_INGOT)),
            new ResourceLocation("pokecube_legends:verizion"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
}
