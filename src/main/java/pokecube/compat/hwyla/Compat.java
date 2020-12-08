package pokecube.compat.hwyla;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.IFormattableTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

@WailaPlugin(value = PokecubeCore.MODID)
public class Compat implements IWailaPlugin
{

    @Override
    public void register(final IRegistrar registrar)
    {
        PokecubeCore.LOGGER.debug("Attempting WAILA support?");
        registrar.registerComponentProvider(HUDHandlerMobs.INSTANCE, TooltipPosition.BODY, EntityPokemob.class);
    }

    public static class HUDHandlerMobs implements IEntityComponentProvider
    {
        public static final HUDHandlerMobs INSTANCE = new HUDHandlerMobs();

        @Override
        public Entity getOverride(final IEntityAccessor accessor, final IPluginConfig config)
        {
            final Entity mob = accessor.getEntity();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob != null && Health.obfuscateName(pokemob))
            {
                final IFormattableTextComponent comp = (IFormattableTextComponent) mob.getName();
                comp.setStyle(comp.getStyle().setObfuscated(true));
                return mob;
            }
            return IEntityComponentProvider.super.getOverride(accessor, config);
        }
    }

}
