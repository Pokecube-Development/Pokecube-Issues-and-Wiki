package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface EffectContext<T>
{
    T getContext(Level level);
    T getContext();
    void write(ByteBuf buffer);
    ResourceLocation getKey();
}
