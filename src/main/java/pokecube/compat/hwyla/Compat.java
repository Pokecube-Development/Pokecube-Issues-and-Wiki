package pokecube.compat.hwyla;

import java.util.List;
import java.util.Random;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
        public void appendHead(final List<ITextComponent> tooltip, final IEntityAccessor accessor,
                final IPluginConfig config)
        {
            final Entity mob = accessor.getEntity();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);

            if (pokemob != null && Health.obfuscateName(pokemob))
            {
                final ITextComponent comp = mob.getName();
                String val = comp.getString();
                final Random rand = new Random();
                final char[] chars = val.toCharArray();
                for (int i = 0; i < val.length(); i++)
                    for (int j = 0; j < 10; j++)
                    {
                        final int rng = rand.nextInt(256);
                        if (Character.isAlphabetic(rng))
                        {
                            chars[i] = (char) rng;
                            break;
                        }
                    }
                val = new String(chars);
                final StringTextComponent name = new StringTextComponent(val);
                // TODO maybe instead look for the ones with the
                // waila.object.name or whatever and just replace those.
                tooltip.remove(0);
                tooltip.add(0, name);
            }
        }
    }

}
