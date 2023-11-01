package thut.core.common.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.minecraftforge.fml.config.ModConfig;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configure
{
    /**
     * This should be set for non-static fields, static fields only check
     * comment anyway, and ignore this.
     */
    String category() default "unk";

    String comment() default "";

    ModConfig.Type type() default ModConfig.Type.COMMON;
}
