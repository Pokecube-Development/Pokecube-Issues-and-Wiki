package pokecube.api.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler.IChangeHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import thut.api.Tracker;
import thut.lib.TComponent;

public class MegaEvolveHelper
{
    public static void init()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(MegaEvolveHelper::onFormRevert);
        PokecubeAPI.POKEMOB_BUS.addListener(MegaEvolveHelper::postFormChange);
        ChangeFormHandler.addChangeHandler(new MegaEvolver());
    }

    public static class MegaEvolver implements IChangeHandler
    {
        @Override
        public boolean handleChange(IPokemob pokemob)
        {
            final PokedexEntry entry = pokemob.getPokedexEntry();
            final Component oldName = pokemob.getDisplayName();
            boolean isMega = MegaEvolveHelper.isMega(pokemob);
            final LivingEntity owner = pokemob.getOwner();
            Player player = owner instanceof Player p ? p : null;
            PokedexEntry newEntry = entry;
            newEntry = pokemob.getPokedexEntry().getMegaEvo(pokemob);
            if (isMega)
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.revert", oldName);
                pokemob.displayMessageToOwner(mess);
                newEntry = pokemob.getBasePokedexEntry();
                mess = TComponent.translatable("pokemob.megaevolve.revert", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                MegaEvoTicker.scheduleRevert(newEntry, pokemob, mess);
            }
            else if (newEntry != null)
            {
                Component mess = TComponent.translatable("pokemob.megaevolve.command.evolve", oldName);
                pokemob.displayMessageToOwner(mess);
                mess = TComponent.translatable("pokemob.megaevolve.success", oldName,
                        TComponent.translatable(newEntry.getUnlocalizedName()));
                MegaEvolveHelper.megaEvolve(pokemob, newEntry, mess);
            }
            else thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokemob.megaevolve.failed", pokemob.getDisplayName()));
            return true;
        }

        @Override
        public String changeKey()
        {
            return "mega-evolve";
        }

        @Override
        public int getPriority()
        {
            // high number so we go last.
            return 100;
        }

        @Override
        public void onFail(IPokemob pokemob)
        {
            final LivingEntity owner = pokemob.getOwner();
            if (owner instanceof ServerPlayer player) thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.mega.noring", pokemob.getDisplayName()));
        }

    }

    public static boolean isMega(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:megatime");
    }

    public static void megaEvolve(IPokemob pokemob, PokedexEntry newEntry, Component mess)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().putLong("pokecube:megatime", Tracker.instance().getTick());
        MegaEvoTicker.scheduleEvolve(newEntry, pokemob, mess);
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        var entity = event.getPokemob().getEntity();
        entity.getPersistentData().remove("pokecube:megatime");
        entity.getPersistentData().putBoolean("pokecube:mega_reverted", true);
    }

    private static void postFormChange(ChangeForm.Post event)
    {
        var entity = event.getPokemob().getEntity();
        if (entity.getPersistentData().contains("pokecube:mega_reverted"))
        {
            entity.getPersistentData().remove("pokecube:mega_reverted");
            entity.getPersistentData().remove("pokecube:mega_ability");
            entity.getPersistentData().remove("pokecube:mega_base");
        }
    }
}
