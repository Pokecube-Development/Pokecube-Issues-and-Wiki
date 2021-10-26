package thut.wearables.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class WearableDroppedEvent extends EntityEvent
{
    private final ItemStack toDrop;

    private final int       index;

    public WearableDroppedEvent(final Entity entity, final ItemStack dropped, final int index)
    {
        super(entity);
        this.toDrop = dropped;
        this.index = index;
    }

    public ItemStack getToDrop()
    {
        return this.toDrop;
    }

    public int getIndex()
    {
        return this.index;
    }


}
