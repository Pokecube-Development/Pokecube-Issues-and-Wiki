package pokecube.api.entity.pokemob.commandhandlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.lib.TComponent;

public class ChangeFormHandler extends DefaultHandler
{
    public static interface IChangeHandler extends Comparable<IChangeHandler>
    {
        boolean handleChange(IPokemob pokemob);

        default boolean needsMegaRing(IPokemob pokemob)
        {
            return true;
        }

        String changeKey();

        int getPriority();

        @Override
        default int compareTo(IChangeHandler o)
        {
            return Integer.compare(getPriority(), o.getPriority());
        }
    }

    public static List<IChangeHandler> processors = new ArrayList<>();

    public static void addChangeHandler(IChangeHandler toAdd)
    {
        processors.add(toAdd);
        processors.sort(null);
    }

    public ChangeFormHandler()
    {}

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        final LivingEntity owner = pokemob.getOwner();

        final Entity mob = pokemob.getEntity();
        Player player = owner instanceof Player p ? p : null;
        final Level world = mob.getLevel();
        final MinecraftServer server = mob.getServer();

        if (pokemob.getGeneralState(GeneralStates.EVOLVING) || server == null || owner == null) return;
        if (!(world instanceof ServerLevel level)) return;

        final boolean hasRing = player == null || MegaCapability.canMegaEvolve(owner, pokemob);

        boolean didAnything = false;
        for (var handler : processors)
        {
            if (!hasRing && handler.needsMegaRing(pokemob)) continue;
            if (handler.handleChange(pokemob))
            {
                didAnything = true;
                break;
            }
        }
        if (!didAnything && !hasRing)
        {
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
            return;
        }
    }
}
