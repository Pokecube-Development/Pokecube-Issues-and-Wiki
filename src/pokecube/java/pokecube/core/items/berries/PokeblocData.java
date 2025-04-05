package pokecube.core.items.berries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.codec.StreamCodec;
import thut.lib.TCodecs;

public record PokeblocData(IntList flavours)
{
    public static final Codec<PokeblocData> CODEC = TCodecs.INT_LIST_CODEC
            .<PokeblocData>comapFlatMap(PokeblocData::read, PokeblocData::flavours).stable();
    public static final StreamCodec<ByteBuf, PokeblocData> STREAM_CODEC = TCodecs.INT_LIST_STREAM_CODEC
            .map(PokeblocData::new, PokeblocData::flavours);

    public static DataResult<PokeblocData> read(IntList list)
    {
        try
        {
            return DataResult.success(new PokeblocData(list));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult
                    .error(() -> "Not a valid pokemob tag: " + list + " " + resourcelocationexception.getMessage());
        }
    }

    public PokeblocData(int[] flavour)
    {
        this(IntList.of(flavour));
    }
}
