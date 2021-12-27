package pokecube.compat.hwyla;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
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
        registrar.registerComponentProvider(HUDHandlerMobs.INSTANCE, TooltipPosition.HEAD, EntityPokemob.class);
    }

    public static class HUDHandlerMobs implements IEntityComponentProvider
    {
        public static final HUDHandlerMobs INSTANCE = new HUDHandlerMobs();

        @Override
        public void appendTooltip(final ITooltip tooltip, final EntityAccessor accessor,
                final IPluginConfig config)
        {
            final Entity mob = accessor.getEntity();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);

            if (pokemob != null && Health.obfuscateName(pokemob))
            {
                final Component name = Health.obfuscate(mob.getName());
                tooltip.clear();
                tooltip.add(0, name);
            }
        }
    }

}
