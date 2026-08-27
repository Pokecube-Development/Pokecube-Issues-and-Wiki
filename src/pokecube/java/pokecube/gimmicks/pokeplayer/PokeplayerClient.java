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

@EventBusSubscriber(modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class PokeplayerClient
{
    public static class PokePlayerComponent extends OutMobInfo
    {
        @Override
        protected IPokemob getMob()
        {
            var copy = ThutCaps.getCopyMob(Minecraft.getInstance().player);
            if (copy == null) return null;
            return PokemobCaps.getPokemobFor(copy.getCopiedMob());
        }
    }

    public static class PokePlayerGuiOverride extends GuiDisplayPokecubeInfo
    {
        public PokePlayerGuiOverride()
        {
            super();
            // We will only make this adjustment if some other addon hasn't adjusted it
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
        public IPokemob getCurrentPokemob()
        {
            var copy = ThutCaps.getCopyMob(Minecraft.getInstance().player);
            if (copy == null) return super.getCurrentPokemob();
            var mob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
            return mob == null ? super.getCurrentPokemob() : mob;
        }

        @Override
        public IPokemob[] getPokemobsToDisplay()
        {
            // TODO maybe adjust this?
            return super.getPokemobsToDisplay();
        }
    }

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        new PokePlayerGuiOverride();
    }
}
