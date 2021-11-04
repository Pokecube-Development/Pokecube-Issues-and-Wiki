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
    public static final FoodProperties GLOWING_POKEPUFF;
    public static final FoodProperties HEAL_POKEPUFF;
    public static final FoodProperties HERO_POISON_POKEPUFF;
    public static final FoodProperties HERO_WEAKNESS_POKEPUFF;
    public static final FoodProperties JUMP_POKEPUFF;
    public static final FoodProperties LUCK_POKEPUFF;
    public static final FoodProperties LUCK_DAMAGE_RESIST_POKEPUFF;
    public static final FoodProperties NIGHT_VISION_POKEPUFF;
    public static final FoodProperties REGEN_POKEPUFF;
    public static final FoodProperties SATURATION_POKEPUFF;
    public static final FoodProperties ICE_CARROT;
    public static final FoodProperties SHADOW_CARROT;

    static
	{
    	ABSORPTION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1), 1.0F).fast().build();
    	DAMAGE_BOOST_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1), 1.0F).fast().build();
    	FIRE_RESISTANCE_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 1), 1.0F).fast().build();
    	GLOWING_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.GLOWING, 100, 1), 1.0F).fast().build();
    	HEAL_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.HEAL, 100, 1), 1.0F).fast().build();
    	HERO_POISON_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
    			.effect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 1), 1.0F)
    			.effect(new MobEffectInstance(MobEffects.POISON, 2400, 4), 1.0F).fast().build();
    	HERO_WEAKNESS_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
    			.effect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 100, 1), 1.0F)
    			.effect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 3), 1.0F).fast().build();
    	JUMP_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.JUMP, 400, 1), 1.0F).fast().build();
    	LUCK_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.LUCK, 400, 1), 1.0F).fast().build();
    	LUCK_DAMAGE_RESIST_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
    			.effect(new MobEffectInstance(MobEffects.LUCK, 200, 1), 1.0F)
    			.effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1), 1.0F).fast().build();
    	NIGHT_VISION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 1), 1.0F).fast().build();
    	REGEN_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), 1.0F).fast().build();
    	SATURATION_POKEPUFF = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.SATURATION, 400, 1), 1.0F).fast().build();

    	ICE_CARROT = (new FoodProperties.Builder()).nutrition(4).saturationMod(1.2F)
				.effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 1), 1.0F).fast().build();
    	SHADOW_CARROT = (new FoodProperties.Builder()).nutrition(4).saturationMod(1.2F)
				.effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 1), 1.0F).fast().build();
	}
}
