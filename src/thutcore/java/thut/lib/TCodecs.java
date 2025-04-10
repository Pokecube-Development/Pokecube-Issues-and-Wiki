package thut.lib;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import thut.api.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TCodecs
{
    public static final Codec<IntList> INT_LIST_CODEC = Codec.INT.listOf().xmap(IntArrayList::new, ArrayList::new);
    public static final StreamCodec<ByteBuf, IntList> INT_LIST_STREAM_CODEC = ByteBufCodecs.INT.apply(
            ByteBufCodecs.list()).map(IntArrayList::new, ArrayList::new);

    private static final Map<Class<?>, Codec<?>> CACHE = new HashMap<>();
    private static final Map<Class<?>, StreamCodec<ByteBuf, ?>> STREAMCACHE = new HashMap<>();

    private static <T> DataResult<T> readJson(JsonElement json, Class<T> classOfT)
    {
        try
        {
            return DataResult.success(JsonUtil.gson.fromJson(json, classOfT));
        }
        catch (Exception resourcelocationexception)
        {
            return DataResult.error(() -> "Not a valid json: " + json + " " + resourcelocationexception.getMessage());
        }
    }

    private static <T> JsonElement writeJson(T thing)
    {
        return JsonUtil.gson.toJsonTree(thing);
    }

    @SuppressWarnings("unchecked")
    public static <T> Codec<T> jsonCodec(Class<T> classOfT)
    {
        return (Codec<T>) CACHE.computeIfAbsent(classOfT,
                c -> ExtraCodecs.JSON.comapFlatMap(element -> readJson(element, c), TCodecs::writeJson));
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamCodec<ByteBuf, T> jsonStreamCodec(Class<T> classOfT)
    {
        return (StreamCodec<ByteBuf, T>) STREAMCACHE.computeIfAbsent(classOfT, c -> new StreamCodec<ByteBuf, T>()
        {
            @Override
            public T decode(ByteBuf buffer)
            {
                return JsonUtil.gson.fromJson(new FriendlyByteBuf(buffer).readUtf(), classOfT);
            }

            @Override
            public void encode(ByteBuf buffer, T value)
            {
                String s = writeJson(value).toString();
                new FriendlyByteBuf(buffer).writeUtf(s);
            }
        });
    }

    public static <T> JsonElement thingToJson(Codec<T> codec, T thing)
    {
        return JsonOps.INSTANCE.withEncoder(codec).apply(thing).getOrThrow();
    }

    public static <T> T thingFromJson(Codec<T> codec, JsonElement thing)
    {
        return JsonOps.INSTANCE.withDecoder(codec).apply(thing).getOrThrow().getFirst();
    }

    public static <T> JsonElement thingToJson(MapCodec<T> codec, T thing)
    {
        return JsonOps.INSTANCE.withEncoder(codec.encoder()).apply(thing).getOrThrow();
    }

    public static <T> T thingFromJson(MapCodec<T> codec, JsonElement thing)
    {
        return JsonOps.INSTANCE.withDecoder(codec.decoder()).apply(thing).getOrThrow().getFirst();
    }
}
