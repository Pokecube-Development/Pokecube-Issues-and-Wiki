package pokecube.legends.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;
import thut.lib.RegHelper;

public class WeaponTiers
{
    public static final TagKey<Block> NEEDS_RAINBOW_TOOL = TagKey.create(RegHelper.BLOCK_REGISTRY,
            ResourceLocation.parse("pokecube_legends:needs_rainbow_tool"));

    public static final TagKey<Block> INCORRECT_FOR_RAINBOW = create("incorrect_for_rainbow_tool");
    public static final TagKey<Block> INCORRECT_FOR_DIAMOND_TOOL = create("incorrect_for_cobalion_tool");
    public static final TagKey<Block> INCORRECT_FOR_IRON_TOOL = create("incorrect_for_keldeo_tool");
    public static final TagKey<Block> INCORRECT_FOR_STONE_TOOL = create("incorrect_for_terrakion_tool");
    public static final TagKey<Block> INCORRECT_FOR_GOLD_TOOL = create("incorrect_for_verizion_tool");
    
    
    public static final Tier RAINBOW_WING = 
            new SimpleTier(INCORRECT_FOR_RAINBOW, 5000, 10.0F, 8, 20, 
                    () -> Ingredient.of(ItemInit.RAINBOW_WING.get()));

    public static final Tier COBALION = 
            new SimpleTier(INCORRECT_FOR_DIAMOND_TOOL, 1700, 6.0F, 6.0F, 7, 
                    () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Tier KELDEO = 
            new SimpleTier(INCORRECT_FOR_IRON_TOOL, 1800, 7.0F, 4.5F, 10,
                    () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Tier TERRAKION = 
            new SimpleTier(INCORRECT_FOR_STONE_TOOL, 2000, 7.5F, 5.0F, 8,
                    () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Tier VIRIZION = 
            new SimpleTier(INCORRECT_FOR_GOLD_TOOL, 1500, 8.0F, 4.0F, 9, 
                    () -> Ingredient.of(Items.NETHERITE_INGOT));
    
    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Reference.ID,name));
    }

}
