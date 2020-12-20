package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import pokecube.core.blocks.healer.HealerTile;

public class CommonProxy
{

    public PlayerEntity getPlayer(final UUID uuid)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getPlayerList().getPlayerByUUID(uuid);
    }

    public ResourceLocation getPlayerSkin(final String name)
    {
        return null;
    }

    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        return null;
    }

    public World getWorld()
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getWorld(World.OVERWORLD);
    }

    public PlayerEntity getPlayer()
    {
        return null;
    }

    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        // Do nothing here, the client side uses this to clear some things for
        // single player
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

}
