package pokecube.legends.items;

import java.util.function.Supplier;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import pokecube.legends.init.ItemInit;

public enum ItemTiers implements Tier {

    RAINBOW_WING(3, 1561, 7.0F, 3.0F, 15, () -> {
        return Ingredient.of(new ItemLike[]{ItemInit.RAINBOW_WING.get()});
    });

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    ItemTiers(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability,
                     Supplier<Ingredient> repairMaterial) {
        this.level = harvestLevel;
        this.uses = maxUses;
        this.speed = efficiency;
        this.damage = attackDamage;
        this.enchantmentValue = enchantability;
        this.repairIngredient = new LazyLoadedValue<>(repairMaterial);
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public int getLevel() {
        return this.level;
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public Ingredient getRepairIngredient() {
        return (Ingredient)this.repairIngredient.get();
    }
}
