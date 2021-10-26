package pokecube.core.events;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.database.PokedexEntry;

public class StarterEvent extends Event
{
    @Cancelable
    @HasResult
    public static class Pick extends StarterEvent
    {
        public Pick(Player player, Collection<ItemStack> starterPack, PokedexEntry entry)
        {
            super(player, starterPack, entry);
        }
    }

    @Cancelable
    @HasResult
    public static class Pre extends StarterEvent
    {
        public Pre(Player player)
        {
            super(player, null, null);
        }
    }

    public final Player player;
    public List<ItemStack>    starterPack = Lists.newArrayList();

    public final PokedexEntry pick;

    public StarterEvent(Player player, Collection<ItemStack> pack, PokedexEntry numberPicked)
    {
        this.player = player;
        if (pack != null) this.starterPack.addAll(pack);
        this.pick = numberPicked;
    }

}
