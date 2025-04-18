package thut.bot.entity.ai;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BotAI
{
    String key();

    String mod() default "";
}
