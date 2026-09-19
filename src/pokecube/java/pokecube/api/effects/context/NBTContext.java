package pokecube.api.effects.context;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import pokecube.api.effects.EffectPacketInfo;

import java.util.function.Consumer;
import java.util.function.Function;

public class NBTContext implements EffectContext<CompoundTag>
{
    public static Function<CompoundTag, Consumer<EffectPacketInfo>> ANIMATION_CLIENT_FACTORY = tag -> (m) -> {};
    public static Function<CompoundTag, Consumer<EffectPacketInfo>> ANIMATION_SERVER_FACTORY = tag -> (m) -> {};

    public static final StreamCodec<ByteBuf, NBTContext> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, NBTContext::getContext, NBTContext::new);

    public CompoundTag tag;

    public NBTContext(CompoundTag tag)
    {
        this.tag = tag;
    }

    @Override
    public CompoundTag getContext(Level level)
    {
        return this.tag;
    }

    @Override
    public CompoundTag getContext()
    {
        return tag;
    }

    @Override
    public void onAttach(EffectPacketInfo info)
    {
        this.getContext(info.level);
        if (info.level.isClientSide()) ANIMATION_CLIENT_FACTORY.apply(getContext()).accept(info);
        else ANIMATION_SERVER_FACTORY.apply(getContext()).accept(info);
    }

    @Override
    public void write(ByteBuf buffer)
    {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public ResourceLocation getKey()
    {
        return NBT;
    }
}
