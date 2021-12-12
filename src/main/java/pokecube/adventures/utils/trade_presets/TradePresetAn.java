package pokecube.adventures.utils.trade_presets;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TradePresetAn
{
    String key();

    String mod() default "";
}
