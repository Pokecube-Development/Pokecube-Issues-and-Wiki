package pokecube.core.interfaces.pokemob.commandhandlers;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.commands.CommandTools;
import thut.core.common.entity.Transporter;
import thut.core.common.handlers.PlayerDataHandler;

public class TeleportHandler extends DefaultHandler
{
    public static float              MINDIST      = 5;
    public static final Set<Integer> invalidDests = Sets.newHashSet();

    public static Predicate<ItemStack> VALIDTELEITEMS = t -> t.getItem() == Items.ENDER_PEARL;

    public static int getTeleIndex(String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).getTeleIndex();
    }

    public static TeleDest getTeleport(String uuid)
    {
        return TeleportHandler.getTeleport(uuid, TeleportHandler.getTeleIndex(uuid));
    }

    public static TeleDest getTeleport(String uuid, int teleIndex)
    {
        final List<TeleDest> list = TeleportHandler.getTeleports(uuid);
        for (final TeleDest dest : list)
            if (dest.index == teleIndex) return dest;
        return null;
    }

    public static List<TeleDest> getTeleports(String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).getTeleDests();
    }

    public static void initTeleportRestrictions()
    {
        TeleportHandler.invalidDests.clear();
        for (final int i : PokecubeCore.getConfig().teleDimBlackList)
            TeleportHandler.invalidDests.add(new Integer(i));
    }

    public static void renameTeleport(String uuid, int index, String customName)
    {
        TeleportHandler.getTeleport(uuid, index).setName(customName);
    }

    public static void setTeleIndex(String uuid, int index)
    {
        final List<?> list = TeleportHandler.getTeleports(uuid);
        if (index < 0) index = list.size() - 1;
        if (index < 0 || index > list.size() - 1) index = 0;
        PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).setTeleIndex(index);
    }

    public static void setTeleport(String uuid, TeleDest teleport)
    {
        boolean set = false;
        final List<TeleDest> list = TeleportHandler.getTeleports(uuid);
        final ListIterator<TeleDest> dests = list.listIterator();
        while (dests.hasNext())
        {
            final TeleDest dest = dests.next();
            if (dest.loc.withinDistance(TeleportHandler.MINDIST, teleport.loc)) if (set) dests.remove();
            else
            {
                set = true;
                teleport.index = dest.index;
                dests.set(teleport);
            }
        }
        if (!set)
        {
            list.add(teleport);
            teleport.index = list.size() - 1;
        }
        for (int i = 0; i < list.size(); i++)
            list.get(i).index = i;
    }

    public static void setTeleport(Vector4 v, String uuid)
    {
        final TeleDest d = new TeleDest(v);
        TeleportHandler.setTeleport(uuid, d);
    }

    public static void swapTeleports(String uuid, int index1, int index2)
    {
        final List<TeleDest> teleports = TeleportHandler.getTeleports(uuid);
        if (index1 < 0 || index1 >= teleports.size() || index2 < 0 || index2 >= teleports.size()) return;
        final TeleDest dest1 = teleports.get(index1);
        final TeleDest dest2 = teleports.get(index2);
        dest1.index = index2;
        dest2.index = index1;
        teleports.set(index1, dest2);
        teleports.set(index2, dest1);
        for (int i = 0; i < teleports.size(); i++)
            teleports.get(i).index = i;
    }

    public static void unsetTeleport(int index, String uuid)
    {
        final TeleDest dest = TeleportHandler.getTeleport(uuid, index);
        final List<TeleDest> list = TeleportHandler.getTeleports(uuid);
        if (dest != null) list.remove(dest);
        for (int i = 0; i < list.size(); i++)
            list.get(i).index = i;
    }

    @Override
    public void handleCommand(IPokemob pokemob) throws Exception
    {
        final ServerPlayerEntity player = (ServerPlayerEntity) pokemob.getOwner();
        final TeleDest d = TeleportHandler.getTeleport(player.getCachedUniqueIdString());
        if (d == null) return;
        final Vector3 loc = d.getLoc();
        final Integer dim = d.getDim();
        final Integer oldDim = player.dimension.getId();
        int needed = PokecubeCore.getConfig().telePearlsCostSameDim;
        if (dim != oldDim)
        {
            needed = PokecubeCore.getConfig().telePearlsCostOtherDim;
            if (TeleportHandler.invalidDests.contains(dim) || TeleportHandler.invalidDests.contains(oldDim))
            {
                final ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.invalid", "red");
                if (this.fromOwner()) pokemob.displayMessageToOwner(text);
                return;
            }
        }
        int count = 0;
        for (int i = 2; i < pokemob.getInventory().getSizeInventory(); i++)
        {
            final ItemStack stack = pokemob.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) if (TeleportHandler.VALIDTELEITEMS.test(stack)) count += stack.getCount();
        }
        if (needed > count)
        {
            final ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.noitems", "red", needed);
            if (this.fromOwner()) pokemob.displayMessageToOwner(text);
            return;
        }
        if (needed > 0) for (int i = 2; i < pokemob.getInventory().getSizeInventory(); i++)
        {
            final ItemStack stack = pokemob.getInventory().getStackInSlot(i);
            if (!stack.isEmpty())
            {
                if (TeleportHandler.VALIDTELEITEMS.test(stack))
                {
                    final int toRemove = Math.min(needed, stack.getCount());
                    stack.split(toRemove);
                    needed -= toRemove;
                    if (!stack.isEmpty()) pokemob.getInventory().setInventorySlotContents(i, stack);
                    else pokemob.getInventory().setInventorySlotContents(i, ItemStack.EMPTY);
                }
                if (needed <= 0) break;
            }
        }
        if (needed > 0)
        {
            final ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.teleport.noitems", "red", needed);
            if (this.fromOwner()) pokemob.displayMessageToOwner(text);
            return;
        }
        final ITextComponent attackName = new TranslationTextComponent(MovesUtils.getUnlocalizedMove(
                IMoveNames.MOVE_TELEPORT));
        final ITextComponent text = CommandTools.makeTranslatedMessage("pokemob.move.used", "green", pokemob
                .getDisplayName(), attackName);
        if (this.fromOwner()) pokemob.displayMessageToOwner(text);
        EventsHandler.recallAllPokemobsExcluding(player, null, false);
        Transporter.teleportEntity(player, loc, dim, false);
    }
}
