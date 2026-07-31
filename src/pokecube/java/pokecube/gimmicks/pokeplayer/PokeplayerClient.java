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

        @Override
        public void _drawGui(GuiEvent evt)
        {
            this.pos.y0 += this.bounds.h;
            this.pos.y1 += this.bounds.h;
            super._drawGui(evt);
        }
    }

    public static class PokePlayerGuiOverride extends GuiDisplayPokecubeInfo
    {
        public PokePlayerGuiOverride()
        {
            super();
        }

        @Override
        public IPokemob getCurrentPokemob()
        {
            var copy = ThutCaps.getCopyMob(Minecraft.getInstance().player);
            if (copy == null) return super.getCurrentPokemob();
            var mob = PokemobCaps.getPokemobFor(copy.getCopiedMob());
            return mob == null ? super.getCurrentPokemob() : mob;
        }
    }

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        new PokePlayerGuiOverride();
    }
}
