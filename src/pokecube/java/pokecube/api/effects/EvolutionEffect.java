package pokecube.api.effects;

import pokecube.api.data.PokedexEntry;
import pokecube.api.effects.context.EffectContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.PokeType;
import pokecube.core.ai.logic.LogicMiscUpdate;
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
                float s = 1.0f;
                if (pokemob != null && pokemob.getGeneralState(GeneralStates.EXITINGCUBE))
                {
                    s = Math.min(1,
                            (pokemob.getEntity().tickCount + 1 + info.partialTick) / LogicMiscUpdate.EXITCUBEDURATION);
                    s = Math.max(0.01f, s);
                }
                mobScale *= 0.5f * dims.y;
                return s * mobScale - mobScale / 2;
            };
            var context = new EvoContext(col1, col2, entry, scale);
            info.processedContext = context;
            return context;
        }
    }
}
