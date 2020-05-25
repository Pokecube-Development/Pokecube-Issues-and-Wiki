package pokecube.core.interfaces.pokemob.commandhandlers;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector4;
import thut.core.common.commands.CommandTools;
import thut.core.common.handlers.PlayerDataHandler;

public class TeleportHandler extends DefaultHandler
{
    public static float MINDIST = 5;

    public static final Set<ResourceLocation> invalidDests = Sets.newHashSet();

    public static Predicate<ItemStack> VALIDTELEITEMS = t -> t.getItem() == Items.ENDER_PEARL;

    public static int getTeleIndex(final String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).getTeleIndex();
    }

    public static TeleDest getTeleport(final String uuid)
    {
        return TeleportHandler.getTeleport(uuid, TeleportHandler.getTeleIndex(uuid));
    }

    public static TeleDest getTeleport(final String uuid, final int teleIndex)
    {
        final List<TeleDest> list = TeleportHandler.getTeleports(uuid);
        for (final TeleDest dest : list)
            if (dest.index == teleIndex) return dest;
        return null;
    }

    public static List<TeleDest> getTeleports(final String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).getTeleDests();
    }

    public static void initTeleportRestrictions()
    {
        TeleportHandler.invalidDests.clear();
        for (final String s : PokecubeCore.getConfig().blackListedTeleDims)
            TeleportHandler.invalidDests.add(new ResourceLocation(s));
    }

    public static void renameTeleport(final String uuid, final int index, final String customName)
    {
        TeleportHandler.getTeleport(uuid, index).setName(customName);
    }

    public static void setTeleIndex(final String uuid, int index)
    {
        final List<?> list = TeleportHandler.getTeleports(uuid);
        if (index < 0) index = list.size() - 1;
        if (index < 0 || index > list.size() - 1) index = 0;
        PlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerData.class).setTeleIndex(index);
    }

    public static void setTeleport(final String uuid, final TeleDest teleport)
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

    public static void setTeleport(final Vector4 v, final String uuid)
    {
        final TeleDest d = new TeleDest().setLoc(v);
        TeleportHandler.setTeleport(uuid, d);
    }

    public static void swapTeleports(final String uuid, final int index1, final int index2)
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

    public static void unsetTeleport(final int index, final String uuid)
    {
        final TeleDest dest = TeleportHandler.getTeleport(uuid, index);
        final List<TeleDest> list = TeleportHandler.getTeleports(uuid);
        if (dest != null) list.remove(dest);
        for (int i = 0; i < list.size(); i++)
            list.get(i).index = i;
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        final ServerPlayerEntity player = (ServerPlayerEntity) pokemob.getOwner();
        final TeleDest d = TeleportHandler.getTeleport(player.getCachedUniqueIdString());
        if (d == null) return;
        final DimensionType dim = d.getPos().getDimension();
        final DimensionType oldDim = player.dimension;
        int needed = PokecubeCore.getConfig().telePearlsCostSameDim;
        if (dim != oldDim)
        {
            needed = PokecubeCore.getConfig().telePearlsCostOtherDim;
            if (TeleportHandler.invalidDests.contains(dim.getRegistryName()) || TeleportHandler.invalidDests.contains(
                    oldDim.getRegistryName()))
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
        ThutTeleporter.transferTo(player, d.loc, true);
    }
}
