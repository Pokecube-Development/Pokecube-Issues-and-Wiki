package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pokecube.core.blocks.healer.HealerTile;

public class CommonProxy
{
    public Player getPlayer(final UUID uuid)
    {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }

    public ResourceLocation getPlayerSkin(final String name)
    {
        return null;
    }

    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        return null;
    }

    public Level getWorld()
    {
        return ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
    }

    public Player getPlayer()
    {
        return null;
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

}
