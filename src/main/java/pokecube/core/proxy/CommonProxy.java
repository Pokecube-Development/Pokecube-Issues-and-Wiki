package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent.NewRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healer.HealerTile;

public class CommonProxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistry event)
        {
            if (PokecubeCore.proxy == null) PokecubeCore.proxy = new CommonProxy();
        }
    }

    public Player getPlayer(final UUID uuid)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getPlayerList().getPlayer(uuid);
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
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getLevel(Level.OVERWORLD);
    }

    public Player getPlayer()
    {
        return null;
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

}
