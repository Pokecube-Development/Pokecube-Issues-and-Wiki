package pokecube.api.items;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PokesealContents(CompoundTag tag)
{
    public static final Codec<PokesealContents> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.AS_CODEC)
            .xmap(PokesealContents::new, contents -> contents.tag);
    public static final StreamCodec<ByteBuf, PokesealContents> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(PokesealContents::new,
            contents -> contents.tag);
}
