package pokecube.api.effects.context;

import net.minecraft.world.level.Level;

public interface EffectContext<T>
{
    T getContext(Level level);
    EffectContextType<? extends EffectContext<?>> getType();
}
