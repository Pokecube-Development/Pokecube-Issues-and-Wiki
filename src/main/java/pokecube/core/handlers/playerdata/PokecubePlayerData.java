package pokecube.core.handlers.playerdata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import thut.api.entity.ThutTeleporter.TeleDest;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/**
 * Data which needs to be synced to clients about the player, this is teleport
 * information and starter status.
 */
public class PokecubePlayerData extends PlayerData
{
    // TODO a way to share teleports.
    // TODO a way to sort teleports into groups.
    ArrayList<TeleDest> telelocs = Lists.newArrayList();
    int                 teleIndex;
    boolean             hasStarter;

    public PokecubePlayerData()
    {
        super();
    }

    @Override
    public String dataFileName()
    {
        return "pokecubeData";
    }

    @Override
    public String getIdentifier()
    {
        return "pokecube-data";
    }

    public List<TeleDest> getTeleDests()
    {
        return this.telelocs;
    }

    public int getTeleIndex()
    {
        return this.teleIndex;
    }

    public boolean hasStarter()
    {
        return this.hasStarter;
    }

    @Override
    public void readFromNBT(final CompoundTag tag)
    {
        this.hasStarter = tag.getBoolean("hasStarter");
        this.teleIndex = tag.getInt("teleIndex");
        final Tag temp2 = tag.get("telelocs");
        this.telelocs.clear();
        if (temp2 instanceof ListTag)
        {
            final ListTag tagListOptions = (ListTag) temp2;
            CompoundTag pokemobData2 = null;
            for (int j = 0; j < tagListOptions.size(); j++)
            {
                pokemobData2 = tagListOptions.getCompound(j);
                final TeleDest d = TeleDest.readFromNBT(pokemobData2);
                if (d != null) this.telelocs.add(d.setIndex(j));
            }
        }
    }

    public void setHasStarter(final boolean has)
    {
        this.hasStarter = has;
    }

    public void setTeleIndex(final int index)
    {
        this.teleIndex = index;
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(final CompoundTag tag)
    {
        tag.putBoolean("hasStarter", this.hasStarter);
        tag.putInt("teleIndex", this.teleIndex);
        final ListTag list = new ListTag();
        for (final TeleDest d : this.telelocs)
            if (d != null && d.loc != null)
            {
                final CompoundTag loc = new CompoundTag();
                d.writeToNBT(loc);
                list.add(loc);
            }
        tag.put("telelocs", list);
    }
}
