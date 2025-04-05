package thut.wearables.events;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class WearableDroppedEvent extends EntityEvent implements ICancellableEvent
{
    private final ItemStack toDrop;
    private final LivingDropsEvent parent;
    private final int index;

    public WearableDroppedEvent(LivingDropsEvent parent, final ItemStack dropped, final int index)
    {
        super(parent.getEntity());
        this.parent = parent;
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

    public LivingDropsEvent getParent()
    {
        return parent;
    }

}
