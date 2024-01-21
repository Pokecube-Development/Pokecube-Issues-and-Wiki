package pokecube.legends.init;

import java.util.function.Supplier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.Reference;

public enum LegendsArmorMaterial implements ArmorMaterial
{
    ULTRA_ARMOR("ultraspace_suit", 33, new int[]{3, 6, 8, 3}, 10,
            SoundEvents.ARMOR_EQUIP_LEATHER, 2.0F, 0.0F, () -> Ingredient.of(ItemInit.SPECTRUM_SHARD.get())),
    IMPRISONMENT_ARMOR("imprisonment", 37, new int[]{3, 6, 8, 3}, 15,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    LegendsArmorMaterial(String name, int durability, int[] damageReduction, int enchantability, SoundEvent sound,
                         float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.slotProtections = damageReduction;
        this.enchantability = enchantability;
        this.equipSound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getName()
    {
        return Reference.ID + ":" + name;
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slotIn)
    {
        return HEALTH_PER_SLOT[slotIn.getIndex()] * durabilityMultiplier;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slotIn)
    {
        return slotProtections[slotIn.getIndex()];
    }

    @Override
    public int getEnchantmentValue()
    {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound()
    {
        return equipSound;
    }

    @Override
    public float getToughness()
    {
        return toughness;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return repairIngredient.get();
    }

    @Override
    public float getKnockbackResistance()
    {
        return this.knockbackResistance;
    }
}
