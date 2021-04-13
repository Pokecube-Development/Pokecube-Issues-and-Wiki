package pokecube.legends.init;

import net.minecraft.item.Food;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

@SuppressWarnings("deprecation")
public class FoodInit 
{
	//Foods
    public static final Food POKEPUFF_GREEN;
    public static final Food POKEPUFF_ORANGE;
    public static final Food POKEPUFF_PINK;
    public static final Food POKEPUFF_BROWN;
    public static final Food POKEPUFF_BLUE;
    
    static
	{
    	POKEPUFF_GREEN = (new Food.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new EffectInstance(Effects.REGENERATION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_ORANGE = (new Food.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new EffectInstance(Effects.JUMP, 200, 1), 1.0F).fast().build();
    	POKEPUFF_PINK = (new Food.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new EffectInstance(Effects.NIGHT_VISION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_BROWN = (new Food.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new EffectInstance(Effects.ABSORPTION, 200, 1), 1.0F).fast().build();
    	POKEPUFF_BLUE = (new Food.Builder()).nutrition(4).saturationMod(0.3F)
				.effect(new EffectInstance(Effects.DAMAGE_BOOST, 200, 1), 1.0F).fast().build();
	}
}
