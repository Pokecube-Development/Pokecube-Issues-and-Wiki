package pokecube.core.handlers;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import thut.core.common.handlers.PlayerDataHandler;

public class PokecubePlayerDataHandler extends PlayerDataHandler
{
    public static CompoundTag getCustomDataTag(final Player player)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static CompoundTag getCustomDataTag(final UUID player)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static CompoundTag getCustomDataTag(final String player)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static <T extends INBTSerializable<CompoundTag>> T getCustomDataValue(final String player, String key)
    {
        final PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        final PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        try
        {
            @SuppressWarnings("unchecked")
            var value = (T) data.customValues.get(key);
            return value;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveCustomData(final Player player)
    {
        PokecubePlayerDataHandler.saveCustomData(player.getStringUUID());
    }

    public static void saveCustomData(final String cachedUniqueIdString)
    {
        PlayerDataHandler.getInstance().save(cachedUniqueIdString, "pokecube-custom");
    }
}
