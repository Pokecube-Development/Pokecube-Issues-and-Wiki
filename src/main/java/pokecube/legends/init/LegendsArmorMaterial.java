package pokecube.legends.init;

import java.util.EnumMap;
import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.Reference;

public enum LegendsArmorMaterial implements StringRepresentable, ArmorMaterial
{
    ULTRA_ARMOR("ultraspace_suit", 33, Util.make(new EnumMap<>(ArmorItem.Type.class),
        (map) -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 8);
            map.put(ArmorItem.Type.HELMET, 3);
        }), 10, SoundEvents.ARMOR_EQUIP_LEATHER, 2.0F, 0.0F,
        () -> { return Ingredient.of(ItemInit.SPECTRUM_SHARD.get()); }),
    IMPRISONMENT_ARMOR("imprisonment", 37, Util.make(new EnumMap<>(ArmorItem.Type.class),
            (map) -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
            }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> {
        return Ingredient.of(Items.NETHERITE_INGOT);
    });

    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FOR_TYPE =
            Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 13);
        map.put(ArmorItem.Type.LEGGINGS, 15);
        map.put(ArmorItem.Type.CHESTPLATE, 16);
        map.put(ArmorItem.Type.HELMET, 11);
    });
    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionForType;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    LegendsArmorMaterial(String name, int durability, EnumMap<ArmorItem.Type, Integer> damageReduction, int enchantability, SoundEvent sound,
                         float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durability;
        this.protectionForType = damageReduction;
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
    public int getDurabilityForType(ArmorItem.Type slotIn)
    {
        return HEALTH_FOR_TYPE.get(slotIn) * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type slotIn)
    {
        return protectionForType.get(slotIn);
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

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
