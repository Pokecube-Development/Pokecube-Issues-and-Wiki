package pokecube.core.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PCEvent extends Event
{
    public final ItemStack    toPC;
    public final LivingEntity owner;

    public PCEvent(ItemStack stack, LivingEntity owner)
    {
        this.toPC = stack;
        this.owner = owner;
    }
}
