package thut.api.entity.genetics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import thut.core.common.genetics.DefaultGenetics;

public record GeneHolder(IMobGenetics genes, CompoundTag tag)
{
    public GeneHolder(IMobGenetics contents, HolderLookup.Provider context)
    {
        this(contents, serializeGenes(contents, context));
    }

    public GeneHolder(CompoundTag tag)
    {
        this(null, tag);
    }

    public GeneHolder(IAttachmentHolder mob, HolderLookup.Provider context)
    {
        this(DefaultGenetics.get(mob), context);
    }

    public GeneHolder withContext(HolderLookup.Provider context)
    {
        IMobGenetics contents = new DefaultGenetics();
        contents.deserializeNBT(context, this.tag().getList("P", Tag.TAG_COMPOUND));
        return new GeneHolder(contents, this.tag());
    }

    public static final Codec<GeneHolder> CODEC = CompoundTag.CODEC
            .<GeneHolder>comapFlatMap(GeneHolder::read, GeneHolder::tag).stable();
    public static final StreamCodec<ByteBuf, GeneHolder> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG
            .map(GeneHolder::parse, GeneHolder::tag);

    public static DataResult<GeneHolder> read(CompoundTag tag)
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

    public static GeneHolder parse(CompoundTag tag)
    {
        return new GeneHolder(tag);
    }

    public static CompoundTag serializeGenes(IMobGenetics pokemob, HolderLookup.Provider context)
    {
        CompoundTag tag = new CompoundTag();
        tag.put("P", pokemob.serializeNBT(context));
        return tag;
    }
}
