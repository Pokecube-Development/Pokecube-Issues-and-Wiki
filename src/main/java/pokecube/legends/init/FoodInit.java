package pokecube.legends.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

@SuppressWarnings("deprecation")
public class FoodInit 
{
	//Foods
    public static final FoodProperties POKEPUFF_GREEN;
    public static final FoodProperties POKEPUFF_ORANGE;
    public static final FoodProperties POKEPUFF_PINK;
    public static final FoodProperties POKEPUFF_BROWN;
    public static final FoodProperties POKEPUFF_BLUE;
    
    static
	{
    	POKEPUFF_GREEN = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_ORANGE = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.JUMP, 200, 1), 1.0F).fast().build();
    	POKEPUFF_PINK = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_BROWN = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_BLUE = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1), 1.0F).fast().build();
	}
}
