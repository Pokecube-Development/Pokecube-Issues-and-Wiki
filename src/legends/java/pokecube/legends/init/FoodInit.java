package pokecube.legends.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

@SuppressWarnings("deprecation")
public class FoodInit
{
    //Foods
    public static final FoodProperties ABSORPTION_POKEPUFF;
    public static final FoodProperties DAMAGE_BOOST_POKEPUFF;
    public static final FoodProperties FIRE_RESISTANCE_POKEPUFF;
    public static final FoodProperties FOOD_POKEPUFF;
    public static final FoodProperties GLOWING_POKEPUFF;
    public static final FoodProperties GOLDEN_SHROOM;
    public static final FoodProperties GOLDEN_SWEET_BERRIES;
    public static final FoodProperties HEAL_POKEPUFF;
    public static final FoodProperties HERO_POISON_POKEPUFF;
    public static final FoodProperties HERO_WEAKNESS_POKEPUFF;
    public static final FoodProperties ICE_CARROT;
    public static final FoodProperties JUMP_POKEPUFF;
    public static final FoodProperties LUCK_DAMAGE_RESIST_POKEPUFF;
    public static final FoodProperties LUCK_POKEPUFF;
    public static final FoodProperties NIGHT_VISION_POKEPUFF;
    public static final FoodProperties NULL_POKEPUFF;
    public static final FoodProperties REGEN_POKEPUFF;
    public static final FoodProperties SATURATION_POKEPUFF;
    public static final FoodProperties WEAKNESS_LUCK_POKEPUFF;
    public static final FoodProperties SHADOW_CARROT;

    static
    {
        ABSORPTION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1), 1.0F).alwaysEdible().build();
        DAMAGE_BOOST_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1), 1.0F).alwaysEdible().build();
        FIRE_RESISTANCE_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 1), 1.0F).alwaysEdible().build();
        FOOD_POKEPUFF = (new FoodProperties.Builder()).nutrition(12).saturationModifier(0.3F).alwaysEdible().build();
        GLOWING_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.GLOWING, 400, 0), 1.0F).alwaysEdible().build();
        HEAL_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.HEAL, 1, 1), 1.0F).alwaysEdible().build();
        HERO_POISON_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 0), 1.0F)
                .effect(new MobEffectInstance(MobEffects.POISON, 2400, 4), 1.0F).build();
        HERO_WEAKNESS_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 0), 1.0F)
                .effect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 3), 1.0F).build();
        JUMP_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.JUMP, 400, 1), 1.0F).alwaysEdible().build();
        LUCK_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.LUCK, 200, 0), 1.0F).alwaysEdible().build();
        LUCK_DAMAGE_RESIST_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.LUCK, 200, 1), 1.0F)
                .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1), 1.0F).alwaysEdible().build();
        NIGHT_VISION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 1), 1.0F).alwaysEdible().build();
        NULL_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F).alwaysEdible().build();
        REGEN_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).alwaysEdible().build();
        SATURATION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.SATURATION, 400, 1), 1.0F).alwaysEdible().build();
        WEAKNESS_LUCK_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F)
                .effect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1), 1.0F)
                .effect(new MobEffectInstance(MobEffects.LUCK, 400, 1), 1.0F).alwaysEdible().build();

        GOLDEN_SHROOM = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.5F)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0), 1.0F)
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 4), 1.0F).alwaysEdible().build();
        GOLDEN_SWEET_BERRIES = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.5F)
                .effect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1), 1.0F).alwaysEdible().build();
        ICE_CARROT = (new FoodProperties.Builder()).nutrition(4).saturationModifier(1.2F)
                .effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0), 1.0F)
                .effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F).alwaysEdible().build();
        SHADOW_CARROT = (new FoodProperties.Builder()).nutrition(4).saturationModifier(1.2F)
                .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0), 1.0F)
                .effect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0), 1.0F).alwaysEdible().build();
    }
}
