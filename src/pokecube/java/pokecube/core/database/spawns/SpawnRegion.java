package pokecube.core.database.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.common.util.INBTSerializable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.SpawnHandler;
import thut.api.level.structures.NamedVolumes;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnRegion implements NamedVolumes.INamedVolume, INBTSerializable<CompoundTag>
{
    String name;
    List<String> natives = new ArrayList<>();

    List<PokedexEntry> _natives = new ArrayList<>();
    List<PokedexEntry> _invasive = new ArrayList<>();
    List<PokedexEntry> _allFound = new ArrayList<>();
    BoundingBox _bounds = BoundingBox.infinite();

    public static SpawnRegion ALL_SPAWNS;

    public static void initAllSpawns()
    {
        ALL_SPAWNS = new SpawnRegion();
        ALL_SPAWNS.name = "global";
        ALL_SPAWNS._natives = Database.spawnables;
        ALL_SPAWNS._allFound = Database.spawnables;
    }

    public static SpawnRegion getFor(ServerLevel level, BlockPos pos)
    {
        return ALL_SPAWNS;
    }

    /**
     * Adds a pokedex entry listed as native to this region,
     * once added it shouldn't be removed
     */
    public void addNative(PokedexEntry entry)
    {
        if(this == ALL_SPAWNS) return;
        if(_natives.contains(entry)) return;
        _invasive.remove(entry);
        _natives.add(entry);
        natives.add(entry.getTrimmedName());
        _allFound.add(entry);
    }

    /**
     * Adds a pokedex entry listed as invasive,
     * this can be removed later via removeInvasive
     */
    public void addInvasive(PokedexEntry entry)
    {
        if(this == ALL_SPAWNS) return;
        if(_natives.contains(entry)) return;
        if(_invasive.contains(entry)) return;
        _invasive.add(entry);
        _allFound.add(entry);
    }

    /**
     * Removes invasive species for this region.
     */
    public void removeInvasive(PokedexEntry entry)
    {
        if(this == ALL_SPAWNS) return;
        if(_natives.contains(entry)) return;
        if(!_invasive.contains(entry)) return;
        _invasive.remove(entry);
        _allFound.remove(entry);
    }

    public PokedexEntry getSpawnFor(SpawnEvent.Pick.Pre event)
    {
        Vector3 v = event.getLocation();
        final ServerLevel world = event.level();
        SpawnEvent.SpawnContext context = event.context();
        BlockState state = v.getBlockState(world);
        List<PokedexEntry> entries = new ArrayList<>(this._allFound);

        SectionPos pos = SectionPos.of(v.getPos());
        // This gives us a fixed random value for the location, as well as time of day
        long seedA = SpawnHandler.getSeed(pos, world, context.time());
        Random rand = new Random(seedA);

        SpawnCheck filter = new SpawnCheck(v, world);
        filter.setRNGSeed(seedA);
        // Filter out entries which are not even valid options here.
        entries.removeIf(dbe -> {
            SpawnEvent.SpawnContext toUse = new SpawnEvent.SpawnContext(event.context(), dbe);
            float weight = dbe.getSpawnData().getWeight(toUse, filter, true);
            return weight <= 0;
        });

        if (entries.isEmpty()) return null;

        double spawnChance = rand.nextDouble();
        SpawnCheck checker = new SpawnCheck(v, world);
        Vector3 vbak = v.copy();
        while(!entries.isEmpty())
        {
            int index = rand.nextInt(entries.size());
            var dbe = entries.remove(index);
            context = new SpawnEvent.SpawnContext(context, v);
            context = new SpawnEvent.SpawnContext(context, dbe);
            float weight = dbe.getSpawnData().getWeight(context, checker, true);
            if (weight == 0) continue;
            if (!dbe.flys() && spawnChance >= weight) if (!(dbe.swims() && state.getFluidState().is(FluidTags.WATER)))
            {
                v = Vector3.getNextSurfacePoint(world, vbak, Vector3.secondAxisNeg, 20);
                if (v != null)
                {
                    v.offsetBy(Direction.UP);
                    context = new SpawnEvent.SpawnContext(context, v);
                    checker = new SpawnCheck(v, world);
                    weight = dbe.getSpawnData().getWeight(context, checker, true);
                }
                else weight = 0;
            }
            if (v == null) v = vbak.copy();
            if(weight > spawnChance)
            {
                if (dbe.isLegendary())
                {
                    final int level = SpawnHandler.getSpawnLevel(context);
                    if (level < PokecubeCore.getConfig().minLegendLevel) return null;
                }
                event.setLocation(v);
                return dbe;
            }
        }
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getKey()
    {
        return "pokecube:spawn_region";
    }

    @Override
    public List<NamedVolumes.INamedPart> getParts()
    {
        return List.of();
    }

    @Override
    public BoundingBox getTotalBounds()
    {
        return _bounds;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        CompoundTag tag = new CompoundTag();
        // ALL_SPAWNS is special and synced to database itself
        if (this == ALL_SPAWNS) return tag;
        ListTag nlist = new ListTag();
        _natives.forEach(entry->nlist.add(StringTag.valueOf(entry.getTrimmedName())));
        tag.put("natives", nlist);
        ListTag ilist = new ListTag();
        _invasive.forEach(entry->ilist.add(StringTag.valueOf(entry.getTrimmedName())));
        tag.put("invasives", ilist);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {
        // ALL_SPAWNS is special and synced to database itself
        if (this == ALL_SPAWNS) return;

        if(nbt.contains("natives"))
        {
            natives.clear();
            ListTag list = nbt.getList("natives", Tag.TAG_STRING);
            list.forEach(_tag->natives.add(_tag.getAsString()));
            _natives.clear();
            natives.forEach(name-> {
                var entry = Database.getEntry(name);
                if (name != null) _natives.add(entry);
            });
        }
        if(nbt.contains("invasives"))
        {
            List<String> invasives = new ArrayList<>();
            ListTag list = nbt.getList("invasives", Tag.TAG_STRING);
            list.forEach(_tag->invasives.add(_tag.getAsString()));
            _invasive.clear();
            invasives.forEach(name-> {
                var entry = Database.getEntry(name);
                if (name != null) _invasive.add(entry);
            });
        }
        _allFound.clear();
        _allFound.addAll(_natives);
        _allFound.addAll(_invasive);
    }
}
