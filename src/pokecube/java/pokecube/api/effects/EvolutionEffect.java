package pokecube.api.effects;

import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.entity.pokemobs.helper.PokemobHasParts;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class EvolutionEffect
{

    public static Function<IPokemob, IAnimatedEffects.EffectPacketInfo> EVO_EFFECT_FACTORY = pokemob -> null;

    public static IAnimatedEffects.EffectPacketInfo makeAndAddEffect(IPokemob pokemob, int duration)
    {
        var evo_effect = EVO_EFFECT_FACTORY.apply(pokemob);
        if (evo_effect == null) return evo_effect;
        evo_effect.animation.setDuration(duration);
        // Reset this to match new duration
        evo_effect.removalTick = evo_effect.animation.getDuration();
        evo_effect.endTick = evo_effect.removalTick;
        ParticleEffects.ADD_FOR_RENDER.accept(evo_effect);
        return evo_effect;
    }

    public static record EvoContext(Color col1, Color col2, PokedexEntry entry, Supplier<Float> scale)
    {
        public static EvoContext fromMoveInfo(IAnimatedEffects.EffectPacketInfo info)
        {
            if(info.context instanceof EvoContext context) return context;
            if(info.context instanceof IPokemob pokemob)
            {
                var entry = pokemob.getPokedexEntry();
                int color1 = pokemob.getType1().colour;
                int color2 = pokemob.getType2().colour;
                if (pokemob.getType2() == PokeType.unknown) color2 = color1;
                Color col1 = new Color(color1);
                Color col2 = new Color(color2);
                Supplier<Float> scale = () -> {
                    float mobScale;
                    if (pokemob.getEntity() instanceof PokemobHasParts parts) mobScale = parts.getScaleFast();
                    else mobScale = pokemob.getEntity().getScale();
                    var dims = entry.getModelSize();
                    return 0.1f * Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
                };
                var context = new EvoContext(col1, col2, entry, scale);
                info.context = context;
                return context;
            }
            return null;
        }
    }
}
