package thut.wearables.events;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class WearableDroppedEvent extends EntityEvent
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
