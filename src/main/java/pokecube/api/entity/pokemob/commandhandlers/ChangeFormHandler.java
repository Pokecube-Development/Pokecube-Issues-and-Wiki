package pokecube.api.entity.pokemob.commandhandlers;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
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

        void onFail(IPokemob pokemob);

        @Override
        default int compareTo(IChangeHandler o)
        {
            return Integer.compare(getPriority(), o.getPriority());
        }
    }

    public static interface RingChecker
    {
        boolean hasFormChangeRing(LivingEntity player, IPokemob toEvolve);
    }

    public static RingChecker checker = (player, toEvolve) -> {
        return true;
    };

    public static List<IChangeHandler> processors = new ArrayList<>();

    public static void addChangeHandler(IChangeHandler toAdd)
    {
        processors.add(toAdd);
        processors.sort(null);
    }

    private String preference = "";

    public ChangeFormHandler()
    {}

    public ChangeFormHandler(String preference)
    {
        this.preference = preference;
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        if (buf.readableBytes() > 0)
        {
            FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);
            preference = fbuf.readUtf();
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        if (!preference.isBlank())
        {
            FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);
            fbuf.writeUtf(preference);
        }
    }

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

        final boolean hasRing = player == null || checker.hasFormChangeRing(owner, pokemob);

        boolean didAnything = false;
        IChangeHandler last = null;
        if (this.preference.isEmpty())
        {
            this.preference = mob.getPersistentData().getString("pokecube:mega_mode");
        }
        for (var handler : processors)
        {
            if (!preference.isBlank() && !preference.equals(handler.changeKey())) continue;
            last = handler;
            if (!hasRing && handler.needsMegaRing(pokemob)) continue;
            if (handler.handleChange(pokemob))
            {
                didAnything = true;
                break;
            }
        }
        if (!didAnything)
        {
            if (last != null) last.onFail(pokemob);
            else if (!hasRing) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
        }
    }
}
