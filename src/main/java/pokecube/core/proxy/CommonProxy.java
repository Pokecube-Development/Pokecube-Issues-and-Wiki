package pokecube.core.proxy;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onStart(final NewRegistryEvent event)
        {
            if (PokecubeCore.proxy == null)
            {
                PokecubeCore.proxy = new CommonProxy();
                NBTEdit.proxy = new pokecube.nbtedit.forge.CommonProxy();

                IPokecube.PokecubeBehavior.BEHAVIORS = event.create(new RegistryBuilder<PokecubeBehavior>()
                        .setIDRange(0, Short.MAX_VALUE).setType(PokecubeBehavior.class)
                        .setName(new ResourceLocation(PokecubeMod.ID, "pokecubes")));

            }
        }
    }

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
