package pokecube.core.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import thut.core.common.handlers.PlayerDataHandler;

public class PokecubePlayerDataHandler extends PlayerDataHandler
{
    public static CompoundNBT getCustomDataTag(PlayerEntity player)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static CompoundNBT getCustomDataTag(String player)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static void saveCustomData(PlayerEntity player)
    {
        PokecubePlayerDataHandler.saveCustomData(player.getCachedUniqueIdString());
    }

    public static void saveCustomData(String cachedUniqueIdString)
    {
        PlayerDataHandler.getInstance().save(cachedUniqueIdString, "pokecube-custom");
    }
}
