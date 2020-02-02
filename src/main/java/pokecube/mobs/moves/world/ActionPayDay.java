package pokecube.mobs.moves.world;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Items;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class ActionPayDay implements IMoveAction
{
    public ActionPayDay()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        final int amountNugget = (int) (Math.random() * 2);
        if (amountNugget > 0 && user.getEntity().getAttackTarget() != null)
        {
            final ItemEntity item = user.getEntity().getAttackTarget().entityDropItem(Items.GOLD_NUGGET, amountNugget);
            if (item != null)
            {
                location.moveEntity(item);
                item.setPickupDelay(0);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "payday";
    }
}
