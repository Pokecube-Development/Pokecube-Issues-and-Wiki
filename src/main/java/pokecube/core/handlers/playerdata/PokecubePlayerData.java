package pokecube.core.handlers.playerdata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
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
    public void readFromNBT(final CompoundNBT tag)
    {
        this.hasStarter = tag.getBoolean("hasStarter");
        this.teleIndex = tag.getInt("teleIndex");
        final INBT temp2 = tag.get("telelocs");
        this.telelocs.clear();
        if (temp2 instanceof ListNBT)
        {
            final ListNBT tagListOptions = (ListNBT) temp2;
            CompoundNBT pokemobData2 = null;
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
    public void writeToNBT(final CompoundNBT tag)
    {
        tag.putBoolean("hasStarter", this.hasStarter);
        tag.putInt("teleIndex", this.teleIndex);
        final ListNBT list = new ListNBT();
        for (final TeleDest d : this.telelocs)
            if (d != null)
            {
                final CompoundNBT loc = new CompoundNBT();
                d.writeToNBT(loc);
                list.add(loc);
            }
        tag.put("telelocs", list);
    }
}
