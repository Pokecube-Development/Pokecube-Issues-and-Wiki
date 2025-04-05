package thut.wearables.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import thut.wearables.IActiveWearable;

public record WearableData(ResourceLocation key, IActiveWearable wearable)
{
    public WearableData(ResourceLocation key)
    {
        this(key, null);
    }

    public WearableData withWearable(IActiveWearable wearable)
    {
        return new WearableData(this.key, wearable);
    }

    @Override
    public final String toString()
    {
        return key.toString();
    }

    public static final Codec<WearableData> CODEC = Codec.STRING
            .<WearableData>comapFlatMap(WearableData::read, WearableData::toString).stable();

    public static final StreamCodec<ByteBuf, WearableData> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(WearableData::parse, WearableData::toString);

    public static DataResult<WearableData> read(String location)
    {
        try
        {
            return DataResult.success(parse(location));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult.error(
                    () -> "Not a valid resource location: " + location + " " + resourcelocationexception.getMessage());
        }
    }

    public static WearableData parse(String location)
    {
        ResourceLocation loc = ResourceLocation.parse(location);
        return new WearableData(loc);
    }
}
