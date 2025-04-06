package pokecube.api.items;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import thut.api.maths.Vector3;

public record PokecubeContents(IPokemob pokemob, LivingEntity entity, CompoundTag tag)
{
    public PokecubeContents(IPokemob contents)
    {
        this(contents, contents.getEntity(), serializePokemob(contents));
    }

    public PokecubeContents(CompoundTag tag)
    {
        this(null, null, tag);
    }

    public PokecubeContents(LivingEntity entity)
    {
        this(PokemobCaps.getPokemobFor(entity), entity, serializePokemob(PokemobCaps.getPokemobFor(entity)));
    }

    public PokecubeContents withPokemob(IPokemob pokemob)
    {
        CompoundTag copy = tag.copy();
        copy.remove("M");
        copy.remove("K");
        copy.remove("P");
        copy.remove("CHP");
        copy.remove("MHP");
        if (pokemob == null) return new PokecubeContents(copy);
        var saved = serializePokemob(pokemob);
        copy.merge(saved);
        return new PokecubeContents(pokemob, pokemob.getEntity(), copy);
    }

    public PokecubeContents withEntity(LivingEntity entity)
    {
        CompoundTag copy = tag.copy();
        copy.remove("M");
        copy.remove("K");
        copy.remove("P");
        copy.remove("CHP");
        copy.remove("MHP");
        if (pokemob == null) return new PokecubeContents(copy);
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null)
        {
            var saved = serializePokemob(pokemob);
            copy.merge(saved);
        }
        else
        {
            CompoundTag saved = new CompoundTag();
            CompoundTag mob = new CompoundTag();
            if (entity.save(mob)) saved.put("M", mob);
            copy.merge(saved);
        }
        return new PokecubeContents(pokemob, entity, copy);
    }

    public PokecubeContents withTilt(int tilt)
    {
        this.tag().putInt("tilt", tilt);
        return new PokecubeContents(pokemob, entity, tag());
    }

    public PokecubeContents withCapturePos(Vector3 pos)
    {
        pos.writeToNBT(this.tag(), "_cap_pos_");
        return new PokecubeContents(pokemob, entity, tag());
    }

    public Optional<Vector3> getCapturePos()
    {
        return Optional.ofNullable(Vector3.readFromNBT(this.tag(), "_cap_pos_"));
    }

    public int getTilt()
    {
        return this.tag().getInt("tilt");
    }

    public float getMaxHealth()
    {
        return tag.getFloat("MHP");
    }

    public float getCurrentHealth()
    {
        return tag.getFloat("CHP");
    }

    public static final Codec<PokecubeContents> CODEC = CompoundTag.CODEC
            .<PokecubeContents>comapFlatMap(PokecubeContents::read, PokecubeContents::tag).stable();

    public static final StreamCodec<ByteBuf, PokecubeContents> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG
            .map(PokecubeContents::parse, PokecubeContents::tag);

    public static DataResult<PokecubeContents> read(CompoundTag tag)
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

    public static PokecubeContents parse(CompoundTag location)
    {
        return new PokecubeContents(location);
    }

    public static CompoundTag serializePokemob(IPokemob pokemob)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mob = new CompoundTag();
        var entity = pokemob.getEntity();
        if (entity.save(mob)) tag.put("M", mob);
        tag.put("P", pokemob.serializeNBT(pokemob.getEntity().registryAccess()));
        tag.putString("K", pokemob.serKey().toString());
        tag.putFloat("CHP", entity.getHealth());
        tag.putFloat("MHP", entity.getMaxHealth());
        return tag;
    }
}
