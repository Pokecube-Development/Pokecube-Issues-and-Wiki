package pokecube.core.handlers.playerdata;

import net.minecraft.nbt.CompoundTag;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/**
 * Generic data to store for each player, this gives another place besides in
 * the player's entity data to store information.
 */
public class PokecubePlayerCustomData extends PlayerData
{
    public CompoundTag tag = new CompoundTag();

    public PokecubePlayerCustomData()
    {
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
    public void readFromNBT(CompoundTag tag)
    {
        this.tag = tag.getCompound("data");
    }

    @Override
    public boolean shouldSync()
    {
        return false;
    }

    @Override
    public void writeToNBT(CompoundTag tag)
    {
        tag.put("data", this.tag);
    }

}
