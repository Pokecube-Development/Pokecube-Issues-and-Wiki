package pokecube.gimmicks.pokeplayer;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.components.OutMobInfo;
import thut.api.ThutCaps;

import java.util.List;

@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class PokeplayerClient
{
    public static class PokePlayerComponent extends OutMobInfo
    {
        @Override
        protected IPokemob getMob()
        {
            // This now no longer does anything special, but is kept here as an API reference
            return super.getMob();
        }
    }

    public static class PokePlayerGuiOverride extends GuiDisplayPokecubeInfo
    {
        public PokePlayerGuiOverride()
        {
            super();
            // We will only make this adjustment if some other addon hasn't adjusted it
            // This now no longer does anything special, but is kept here as an API reference
            if (outMobRenderer.getClass() == OutMobInfo.class)
            {
                var handler = new PokePlayerComponent();
                var selectedHandlers = GUI_HANDLERS.get(GuiEvent.RenderSelectedInfo.class);
                selectedHandlers.remove(outMobRenderer);
                selectedHandlers.add(handler);
                COMPONENTS.remove(outMobRenderer);
                COMPONENTS.add(handler);
                outMobRenderer = handler;
            }
        }

        @Override
        public List<IPokemob> getPokemobsToDisplay()
        {
            var list = super.getPokemobsToDisplay();
            var player = Minecraft.getInstance().player;
            var copy = ThutCaps.getCopyMob(player);
            if (copy == null) return list;
            // Remove any previous instances of us, this occurs because of the "markDirty" in Pokeplayer
            // resulting in the IPokemob instance being replaced with the one synced from the server,
            // or if we have transformed back from a pokemob recently
            list.removeIf(mob->mob.getTrackedEntity() == player);
            var mob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
            if (mob != null && !list.contains(mob)) list.addFirst(mob);
            return super.getPokemobsToDisplay();
        }
    }

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        new PokePlayerGuiOverride();
    }
}
