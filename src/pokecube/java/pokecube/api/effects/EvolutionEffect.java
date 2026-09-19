package pokecube.api.effects;

import pokecube.api.data.PokedexEntry;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.entity.pokemobs.helper.PokemobHasParts;

import java.awt.Color;
import java.util.function.Supplier;

public class EvolutionEffect
{
    public static record EvoContext(Color col1, Color col2, PokedexEntry entry, Supplier<Float> scale)
    {
        public static EvoContext fromMoveInfo(EffectPacketInfo info)
        {
            if (info.processedContext instanceof EvoContext context) return context;
            IPokemob pokemob = info.getContext(EffectContext.POKEMOB);
            if (pokemob == null) return null;
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
            info.processedContext = context;
            return context;
        }
    }
}
