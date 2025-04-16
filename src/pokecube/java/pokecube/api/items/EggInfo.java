package pokecube.api.items;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EggInfo(CompoundTag tag)
{
    public EggInfo()
    {
        this(new CompoundTag());
    }

    public EggInfo withMother(String motherId)
    {
        tag.putString("motherId", motherId);
        return this;
    }

    public EggInfo withNest(BlockPos pos)
    {
        tag.put("nestLoc", NbtUtils.writeBlockPos(pos));
        return this;
    }

    public EggInfo withTime(int time)
    {
        tag.putInt("time", time);
        return this;
    }

    public int getTime()
    {
        var tag = this.tag;
        if (!tag.contains("time")) tag.putInt("time", -24000);
        return tag.getInt("time");
    }

    public String getMotherId()
    {
        return tag.getString("motherId");
    }

    public Optional<BlockPos> getNest()
    {
        return NbtUtils.readBlockPos(tag, "nestLoc");
    }

    public static final Codec<EggInfo> CODEC = CompoundTag.CODEC.<EggInfo>comapFlatMap(EggInfo::read, EggInfo::tag)
            .stable();
    public static final StreamCodec<ByteBuf, EggInfo> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(EggInfo::parse,
            EggInfo::tag);

    public static DataResult<EggInfo> read(CompoundTag tag)
    {
        try
        {
            return DataResult.success(parse(tag));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult
                    .error(() -> "Not a valid pokemob tag: " + tag + " " + resourcelocationexception.getMessage());
        }
    }

    public static EggInfo parse(CompoundTag tag)
    {
        return new EggInfo(tag);
    }
}
