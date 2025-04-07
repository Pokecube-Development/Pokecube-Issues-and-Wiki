package pokecube.api.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import thut.api.maths.Vector3;

import java.util.Optional;

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
        this(PokemobCaps.getPokemobFor(entity), entity, serializeEntity(entity));
    }

    public PokecubeContents withPokemob(IPokemob pokemob)
    {
        CompoundTag copy = tag.copy();
        if (pokemob == null)
        {
            copy.remove("M");
            copy.remove("K");
            copy.remove("I");
            copy.remove("CHP");
            copy.remove("MHP");
        }
        if (pokemob == null) return new PokecubeContents(copy);
        copy.merge(serializePokemob(pokemob));
        return new PokecubeContents(pokemob, pokemob.getEntity(), copy);
    }

    public PokecubeContents withEntity(LivingEntity entity)
    {
        CompoundTag copy = tag.copy(), saved;
        if (entity == null)
        {
            copy.remove("M");
            copy.remove("K");
            copy.remove("I");
            copy.remove("CHP");
            copy.remove("MHP");
        }
        if (entity == null) return new PokecubeContents(copy);
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        if (pokemob != null) saved = serializePokemob(pokemob);
        else saved = serializeEntity(entity);
        copy.merge(saved);
        return new PokecubeContents(pokemob, entity, copy);
    }

    public PokecubeContents withTilt(int tilt)
    {
        tag().putInt("tilt", tilt);
        return new PokecubeContents(pokemob(), entity(), tag());
    }

    public PokecubeContents withCapturePos(Vector3 pos)
    {
        pos.writeToNBT(tag(), "_cap_pos_");
        return new PokecubeContents(pokemob(), entity(), tag());
    }

    public Optional<Vector3> getCapturePos()
    {
        return Optional.ofNullable(Vector3.readFromNBT(tag(), "_cap_pos_"));
    }

    public int getTilt()
    {
        return tag().getInt("tilt");
    }

    public float getMaxHealth()
    {
        return tag().getFloat("MHP");
    }

    public float getCurrentHealth()
    {
        return tag().getFloat("CHP");
    }

    public static final Codec<PokecubeContents> CODEC = CompoundTag.CODEC.<PokecubeContents>comapFlatMap(
            PokecubeContents::read, PokecubeContents::save).stable();

    public static final StreamCodec<ByteBuf, PokecubeContents> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
            PokecubeContents::parse, PokecubeContents::save);

    public static DataResult<PokecubeContents> read(CompoundTag tag)
    {
        try
        {
            return DataResult.success(parse(tag));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult.error(
                    () -> "Not a valid pokemob tag: " + tag + " " + resourcelocationexception.getMessage());
        }
    }

    public static CompoundTag save(PokecubeContents contents)
    {
        return contents.tag();
    }

    public static PokecubeContents parse(CompoundTag location)
    {
        return new PokecubeContents(location);
    }

    public static CompoundTag serializePokemob(IPokemob pokemob)
    {
        CompoundTag tag = serializeEntity(pokemob.getEntity());
        tag.putString("K", pokemob.serKey().toString());
        return tag;
    }

    public static CompoundTag serializeEntity(Entity entity)
    {
        CompoundTag tag = new CompoundTag();
        CompoundTag mob = new CompoundTag();
        entity.saveWithoutId(mob);
        var key = entity.getEncodeId();
        mob.putString("id", key);
        tag.putString("I", key);
        tag.put("M", mob);
        if (entity instanceof LivingEntity living)
        {
            tag.putFloat("CHP", living.getHealth());
            tag.putFloat("MHP", living.getMaxHealth());
        }
        return tag;
    }
}
