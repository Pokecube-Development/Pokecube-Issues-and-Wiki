package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.core.blocks.healer.HealerTile;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    public Player getPlayer(final UUID uuid)
    {
        return this.getServer().getPlayerList().getPlayer(uuid);
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
        return this.getServer().getLevel(Level.OVERWORLD);
    }

    public Player getPlayer()
    {
        return null;
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

}
