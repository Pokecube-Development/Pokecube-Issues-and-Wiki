package pokecube.api.events;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;

public class StarterEvent extends Event
{
    @Cancelable
    /**
     * This event is fired on the ThutCore.FORGE_BUS after creating the
     * pokemob's itemstack for the player's chosen pokemob. Cancelling this
     * event will prevent giving the items to the player. The list of items for
     * the player can be edited in this event, so it can be used to add
     * additional items, or to edit the chosen pokemob.
     * 
     * @author Thutmose
     *
     */
    public static class Pick extends StarterEvent
    {
        public Pick(ServerPlayer player, Collection<ItemStack> starterPack, PokedexEntry entry)
        {
            super(player, starterPack, entry);
        }
    }

    @Cancelable
    /**
     * This event is fired on the ThutCore.FORGE_BUS before processing the
     * starter selection for the player. Cancelling this event will prevent the
     * starter pack of items from being processed entirely.
     * 
     * @author Thutmose
     *
     */
    public static class Pre extends StarterEvent
    {
        public Pre(ServerPlayer player)
        {
            super(player, Collections.emptyList(), Database.missingno);
        }
    }

    @Cancelable
    /**
     * This event is fired on the ThutCore.FORGE_BUS, on the client side.
     * If it is cancelled, then the default ChooseFirst gui will not be
     * displayed to the client. Use this event to either deny such gui, or as a
     * place to open a custom selection gui.
     * 
     * @author Thutmose
     *
     */
    public static class Gui extends Event
    {}

    /**
     * The player choosing their pokemob.
     */
    public final ServerPlayer player;
    /**
     * The list if items to give to the player (immutable and empty if in Pre)
     */
    public final List<ItemStack> starterPack = Lists.newArrayList();

    /**
     * The chosen pokemob (or missingo if in Pre)
     */
    public final PokedexEntry pick;

    public StarterEvent(ServerPlayer player, Collection<ItemStack> pack, PokedexEntry numberPicked)
    {
        this.player = player;
        if (pack != null) this.starterPack.addAll(pack);
        this.pick = numberPicked;
    }

}
