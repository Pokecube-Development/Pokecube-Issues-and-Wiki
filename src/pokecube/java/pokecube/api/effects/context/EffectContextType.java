package pokecube.api.effects.context;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public interface EffectContextType<T extends EffectContext<?>>
{
    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
    ResourceLocation key();
}
