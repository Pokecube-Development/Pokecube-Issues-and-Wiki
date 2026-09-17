package pokecube.api.effects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.effects.AnimationMultiAnimations;
import pokecube.core.effects.presets.AnimationPowder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PokemobTickParticles
{
    public static final int[] FLAVCOLOURS = new int[] { 0xFFFF4932, 0xFF4475ED, 0xFFF95B86, 0xFF2EBC63, 0xFFEBCE36 };
    public static Function<EffectContext<?>, IAnimatedEffects.EffectRecord> HOLIDAY_EFFECT = context->{
        if (!(context.getContext() instanceof IPokemob pokemob)) return null;
        var powder = new AnimationPowder();
        var json = new JsonObject();
        json.add("v_y", new JsonPrimitive("0"));
        json.add("f_y", new JsonPrimitive("4*rand()*" + pokemob.getEntity().getBbHeight()));
        powder.init(json);
        powder.values.density = 1.5f;
        powder.values.width = 0.15f;
        powder.values.particle = "aurora"; // Merry Xmas
        powder.setDuration(20);
        return new IAnimatedEffects.EffectRecord("pokecube.pokemob.holiday", powder);
    };

    public static Function<EffectContext<?>, IAnimatedEffects.EffectRecord> SHADOW_EFFECT = context->{
        if (!(context.getContext() instanceof IPokemob pokemob)) return null;
        var powder = new AnimationPowder();
        var json = new JsonObject();
        json.add("v_y", new JsonPrimitive("0"));
        json.add("f_y", new JsonPrimitive("4*rand()*" + pokemob.getEntity().getBbHeight()));
        powder.init(json);
        powder.values.density = 1.5f;
        powder.values.width = 0.15f;
        powder.values.particle = "portal";
        powder.setDuration(20);
        return new IAnimatedEffects.EffectRecord("pokecube.pokemob.shadow", powder);
    };

    public static Function<EffectContext<?>, IAnimatedEffects.EffectRecord> MATE_EFFECT = context->{
        if (!(context.getContext() instanceof IPokemob pokemob)) return null;
        var powder = new AnimationPowder();
        var json = new JsonObject();
        json.add("v_y", new JsonPrimitive("0"));
        json.add("f_y", new JsonPrimitive("4*rand()*" + pokemob.getEntity().getBbHeight()));
        powder.init(json);
        powder.values.density = 1.75f;
        powder.values.width = 0.15f;
        powder.values.particle = "heart";
        powder.setDuration(10);
        return new IAnimatedEffects.EffectRecord("pokecube.pokemob.mating", powder);
    };

    public static Function<EffectContext<?>, IAnimatedEffects.EffectRecord> FLAVOUR_EFFECT = (context) -> {
        if (!(context.getContext() instanceof IPokemob pokemob)) return null;
        List<IAnimatedEffects> list = new ArrayList<>();
        for (int index = 0; index < 5; index++)
        {
            var amt = pokemob.getFlavourAmount(index);
            var powder = new AnimationPowder();
            var json = new JsonObject();
            json.add("v_y", new JsonPrimitive("0"));
            json.add("f_y", new JsonPrimitive("4*rand()*" + pokemob.getEntity().getBbHeight()));
            powder.init(json);
            powder.values.density = 1.0f / amt;
            powder.values.width = 0.15f * amt;
            powder.values.rgba = FLAVCOLOURS[index];
            powder.values.particle = "powder";
            powder.setDuration(20);
            list.add(powder);
        }
        return new IAnimatedEffects.EffectRecord("pokecube.pokemob.flavour", new AnimationMultiAnimations(list));
    };

    public static void init()
    {
        ParticleEffects.registerRecord("pokecube.pokemob.holiday", HOLIDAY_EFFECT);
        ParticleEffects.registerRecord("pokecube.pokemob.shadow", SHADOW_EFFECT);
        ParticleEffects.registerRecord("pokecube.pokemob.mating", MATE_EFFECT);
        ParticleEffects.registerRecord("pokecube.pokemob.flavour", FLAVOUR_EFFECT);
    }
}
