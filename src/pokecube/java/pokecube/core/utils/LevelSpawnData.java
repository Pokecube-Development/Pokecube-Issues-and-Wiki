package pokecube.core.utils;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LevelSpawnData extends SavedData
{
    private static final String MOBSPAWNS = "mob_spawns";
    private final Map<ChunkPos, Map<BlockPos, CompoundTag>> stale_npcs = new HashMap<>();

    // Create new instance of saved data
    public static LevelSpawnData create()
    {
        return new LevelSpawnData();
    }

    // Load existing instance of saved data
    public static LevelSpawnData load(CompoundTag tag, HolderLookup.Provider ignored)
    {
        return new LevelSpawnData(tag);
    }

    public static LevelSpawnData getForLevel(ServerLevel level)
    {
        var data = level.getDataStorage().get(new SavedData.Factory<>(LevelSpawnData::create, LevelSpawnData::load),
                "pokecube_structure_spawns");
        if (data == null) level.getDataStorage().set("pokecube_structure_spawns", data = new LevelSpawnData());
        return data;
    }

    public LevelSpawnData() {}

    public LevelSpawnData(CompoundTag tag)
    {
        try
        {
            var temp = tag.get(MOBSPAWNS);
            if (temp instanceof ListTag tagListChunks)
            {
                tagListChunks.forEach(t -> {
                    if (t instanceof CompoundTag _tag)
                    {
                        var pos = new ChunkPos(_tag.getInt("x"), _tag.getInt("z"));
                        var nbt = _tag.get("mobs");
                        var map = stale_npcs.put(pos, new HashMap<>());
                        if (nbt instanceof ListTag mobs)
                        {
                            mobs.forEach(tt -> {
                                if (tt instanceof CompoundTag __tag)
                                {
                                    var _pos = NbtUtils.readBlockPos(__tag, "pos").get();
                                    var _nbt = __tag.getCompound("nbt");
                                    map.put(_pos, _nbt);
                                }
                            });
                        }
                    }
                });
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)
    {
        final ListTag tagListChunks = new ListTag();
        for (var entry : this.stale_npcs.entrySet())
        {
            CompoundTag _tag = new CompoundTag();
            _tag.putInt("x", entry.getKey().x);
            _tag.putInt("z", entry.getKey().z);
            ListTag tagListSpots = new ListTag();
            for (var _entry : entry.getValue().entrySet())
            {
                CompoundTag __tag = new CompoundTag();
                __tag.put("pos", NbtUtils.writeBlockPos(_entry.getKey()));
                __tag.put("nbt", _entry.getValue());
                tagListSpots.add(__tag);
            }
            _tag.put("mobs", tagListSpots);
            tagListChunks.add(_tag);
        }
        tag.put(MOBSPAWNS, tagListChunks);
        return tag;
    }

    public Map<BlockPos, CompoundTag> getFor(ChunkPos chunk)
    {
        synchronized (stale_npcs)
        {
            return ImmutableMap.copyOf(stale_npcs.getOrDefault(chunk, Collections.emptyMap()));
        }
    }

    public void add(BlockPos pos, CompoundTag nbt)
    {
        ChunkPos chunk = new ChunkPos(pos);
        synchronized (stale_npcs)
        {
            stale_npcs.computeIfAbsent(chunk, c -> new HashMap<>()).put(pos, nbt);
        }
    }

    public void remove(BlockPos pos)
    {
        ChunkPos chunk = new ChunkPos(pos);
        synchronized (stale_npcs)
        {
            if (stale_npcs.containsKey(chunk)) stale_npcs.get(chunk).remove(pos);
        }
    }

    public void remove(ChunkPos chunk)
    {
        synchronized (stale_npcs)
        {
            stale_npcs.remove(chunk);
        }
    }
}
