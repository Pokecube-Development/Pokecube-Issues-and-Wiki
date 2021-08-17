package pokecube.core.handlers;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.LazyValue;
import pokecube.legends.init.ItemInit;

import java.util.function.Supplier;

public enum ItemTiers implements IItemTier {

    RAINBOW_WING(3, 1561, 7.0F, 3.0F, 15, () -> {
        return Ingredient.of(new IItemProvider[]{ItemInit.RAINBOW_WING.get()});
    });

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyValue<Ingredient> repairIngredient;

    ItemTiers(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability,
                     Supplier<Ingredient> repairMaterial) {
        this.level = harvestLevel;
        this.uses = maxUses;
        this.speed = efficiency;
        this.damage = attackDamage;
        this.enchantmentValue = enchantability;
        this.repairIngredient = new LazyValue<>(repairMaterial);
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
