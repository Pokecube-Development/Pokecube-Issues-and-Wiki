package pokecube.core.handlers.playerdata;

import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Generic data to store for each player, this gives another place besides in the player's entity data to store
 * information.
 */
public class PokecubePlayerCustomData extends PlayerData
{
    private static final Map<String, Supplier<INBTSerializable<CompoundTag>>> VALUES = Maps.newHashMap();

    public static void registerDataType(String id, Supplier<INBTSerializable<CompoundTag>> data)
    {
        synchronized (VALUES)
        {
            VALUES.put(id, data);
        }
    }

    public CompoundTag tag = new CompoundTag();

    public Map<String, INBTSerializable<CompoundTag>> customValues = Maps.newConcurrentMap();

    public PokecubePlayerCustomData()
    {
        VALUES.forEach((key, suppl) -> customValues.put(key, suppl.get()));
    }

    @Override
    public String dataFileName()
    {
        return "customData";
    }

    @Override
    public String getIdentifier()
    {
        return "pokecube-custom";
    }

    @Override
    public void readFromNBT(Provider provider, CompoundTag tag)
    {
        this.tag = tag.getCompound("data");
        if (tag.contains("values"))
        {
            CompoundTag values = tag.getCompound("values");
            values.getAllKeys().forEach(key -> {
                INBTSerializable<CompoundTag> value = this.customValues.get(key);
                if (value != null && values.get(key) instanceof CompoundTag tag2) value.deserializeNBT(provider, tag2);
            });
        }
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(Provider provider, CompoundTag tag)
    {
        tag.put("data", this.tag);
        if (!this.customValues.isEmpty())
        {
            CompoundTag values = new CompoundTag();
            this.customValues.forEach((key, value) -> values.put(key, value.serializeNBT(provider)));
            tag.put("values", values);
        }
    }

}
