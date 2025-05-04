package pokecube.gimmicks.terastal;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.events.PokecubeTooltipEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.client.render.mobs.overlays.Status.StatusOverlay;
import pokecube.core.client.render.mobs.overlays.Status.StatusTexturer;
import pokecube.core.utils.Resources;
import pokecube.gimmicks.terastal.TeraTypeGene.TeraType;
import thut.core.common.ThutCore;

@EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID, value = Dist.CLIENT)
public class TerastalClient
{
    private static final Int2ObjectArrayMap<StatusOverlay> TERA_TEX = new Int2ObjectArrayMap<>();

    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        Status.PROVIDERS.add(pokemob -> {
            TeraType type = TerastalMechanic.getTera(pokemob.getEntity());
            if (type != null && type.isTera)
            {
                return TERA_TEX.computeIfAbsent(type.teraType.ordinal(), index -> {
                    var _type = type.teraType;
                    var overlay = new StatusOverlay(new StatusTexturer(Resources.STATUS_TERA), 0.15f);
                    overlay.texturer().rate = 0;
                    overlay.texturer().animated = false;
                    overlay.texturer().red = FastColor.ARGB32.red(_type.colour);
                    overlay.texturer().green = FastColor.ARGB32.green(_type.colour);
                    overlay.texturer().blue = FastColor.ARGB32.blue(_type.colour);
                    return overlay;
                });
            }
            return null;
        });

        ThutCore.FORGE_BUS.addListener(TerastalClient::onPokecubeTooltip);
    }

    private static void onPokecubeTooltip(PokecubeTooltipEvent event)
    {
        if (event.advanced.hasShiftDown())
        {
            var tera = TerastalMechanic.getTera(event.contents.entity());
            if (tera != null) event.list.add(Component.translatable("pokecube_adventures.tooltip.gene.expressed.tera",
                    Component.translatable("type." + tera.teraType)));
        }
    }
}
