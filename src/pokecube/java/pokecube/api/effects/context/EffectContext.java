package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.effects.EffectPacketInfo;

public interface EffectContext<T>
{
    public static ResourceLocation POKEMOB = ResourceLocation.fromNamespaceAndPath("pokecube", "pokemob");
    public static ResourceLocation ENTITY = ResourceLocation.fromNamespaceAndPath("pokecube", "entity");
    public static ResourceLocation MOVE = ResourceLocation.fromNamespaceAndPath("pokecube", "move_entry");
    public static ResourceLocation NBT = ResourceLocation.fromNamespaceAndPath("pokecube", "nbt");

    T getContext(Level level);

    T getContext();

    void write(ByteBuf buffer);

    ResourceLocation getKey();

    default void onAttach(EffectPacketInfo effectPacketInfo)
    {
    }
}
