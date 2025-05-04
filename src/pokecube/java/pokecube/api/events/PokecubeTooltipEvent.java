package pokecube.api.events;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.Event;
import pokecube.api.items.PokecubeContents;

import java.util.List;

public class PokecubeTooltipEvent extends Event
{
    public final ItemStack pokecube;
    public final PokecubeContents contents;
    public final Item.TooltipContext context;
    public final List<Component> list;
    public final TooltipFlag advanced;

    public PokecubeTooltipEvent(ItemStack pokecube, PokecubeContents contents, Item.TooltipContext context,
            List<Component> list, TooltipFlag advanced)
    {
        this.pokecube = pokecube;
        this.contents = contents;
        this.context = context;
        this.list = list;
        this.advanced = advanced;
    }
}
