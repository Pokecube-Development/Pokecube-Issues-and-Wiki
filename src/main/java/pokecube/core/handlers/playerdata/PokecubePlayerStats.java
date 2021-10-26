package pokecube.core.handlers.playerdata;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IPokemob;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Player capture/hatch/kill stats */
public class PokecubePlayerStats extends PlayerData
{
    private Map<PokedexEntry, Integer> hatches;
    private Map<PokedexEntry, Integer> captures;
    private Map<PokedexEntry, Integer> kills;
    private Set<PokedexEntry>          inspected;
    protected boolean                  hasFirst = false;

    public PokecubePlayerStats()
    {
        super();
    }

    public void addCapture(final PokedexEntry entry)
    {
        final int num = this.getCaptures().get(entry) == null ? 0 : this.getCaptures().get(entry);
        this.getCaptures().put(entry, num + 1);
    }

    public void addHatch(final PokedexEntry entry)
    {
        final int num = this.getHatches().get(entry) == null ? 0 : this.getHatches().get(entry);
        this.getHatches().put(entry, num + 1);
    }

    public void addKill(final PokedexEntry entry)
    {
        final int num = this.getKills().get(entry) == null ? 0 : this.getKills().get(entry);
        this.getKills().put(entry, num + 1);
    }

    @Override
    public String dataFileName()
    {
        return "pokecubeStats";
    }

    public Map<PokedexEntry, Integer> getCaptures()
    {
        if (this.captures == null) this.initMaps();
        return this.captures;
    }

    public Map<PokedexEntry, Integer> getHatches()
    {
        if (this.hatches == null) this.initMaps();
        return this.hatches;
    }

    @Override
    public String getIdentifier()
    {
        return "pokecube-stats";
    }

    public Map<PokedexEntry, Integer> getKills()
    {
        if (this.kills == null) this.initMaps();
        return this.kills;
    }

    public boolean hasFirst()
    {
        return this.hasFirst;
    }

    public boolean hasInspected(final PokedexEntry entry)
    {
        if (this.inspected == null) this.initMaps();
        return this.inspected.contains(entry);
    }

    public void initMaps()
    {
        this.captures = Maps.newHashMap();
        this.hatches = Maps.newHashMap();
        this.kills = Maps.newHashMap();
        this.inspected = Sets.newHashSet();
    }

    public boolean inspect(final PlayerEntity player, final IPokemob pokemob)
    {
        if (this.inspected == null) this.initMaps();
        if (player instanceof ServerPlayerEntity) Triggers.INSPECTPOKEMOB.trigger((ServerPlayerEntity) player, pokemob);
        return this.inspected.add(pokemob.getPokedexEntry());
    }

    @Override
    public void readFromNBT(final CompoundNBT tag)
    {
        CompoundNBT temp = tag.getCompound("kills");
        PokedexEntry entry;
        this.initMaps();
        this.hasFirst = tag.getBoolean("F");
        for (final String s : temp.getAllKeys())
        {
            final int num = temp.getInt(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null) for (int i = 0; i < num; i++)
                this.addKill(entry);
        }
        temp = tag.getCompound("captures");
        for (final String s : temp.getAllKeys())
        {
            final int num = temp.getInt(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null) for (int i = 0; i < num; i++)
                this.addCapture(entry);
        }
        temp = tag.getCompound("hatches");
        for (final String s : temp.getAllKeys())
        {
            final int num = temp.getInt(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null) for (int i = 0; i < num; i++)
                this.addHatch(entry);
        }
        if (tag.contains("inspected"))
        {
            final ListNBT list = (ListNBT) tag.get("inspected");
            if (this.inspected == null) this.initMaps();
            for (int i = 0; i < list.size(); i++)
            {
                final String s = list.getString(i);
                entry = Database.getEntry(s);
                if (entry != null) this.inspected.add(entry);
            }
        }
    }

    public void setHasFirst(final PlayerEntity player)
    {
        this.hasFirst = true;
        Triggers.FIRSTPOKEMOB.trigger((ServerPlayerEntity) player);
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(final CompoundNBT tag_)
    {
        CompoundNBT tag = new CompoundNBT();
        for (final PokedexEntry e : this.getKills().keySet())
            tag.putInt(e.getName(), this.getKills().get(e));
        tag_.put("kills", tag);
        tag = new CompoundNBT();
        for (final PokedexEntry e : this.getCaptures().keySet())
            tag.putInt(e.getName(), this.getCaptures().get(e));
        tag_.put("captures", tag);
        tag = new CompoundNBT();
        for (final PokedexEntry e : this.getHatches().keySet())
            tag.putInt(e.getName(), this.getHatches().get(e));
        tag_.put("hatches", tag);
        final ListNBT list = new ListNBT();
        for (final PokedexEntry e : this.inspected)
            list.add(StringNBT.valueOf(e.getName()));
        tag_.put("inspected", list);
        tag_.putBoolean("F", this.hasFirst);
    }
}
