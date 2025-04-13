package pokecube.legends.init.function;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.items.zmove.ItemZCrystal;

public class UsableItemZMoveEffects
{
    public static class ZMoveUsable extends BaseUseable
    {
        @Override
        public InteractionResultHolder<ItemStack> onMoveTick(final IPokemob attacker, final ItemStack stack,
                final MoveApplication moveuse, boolean pre)
        {
            if (pre && stack == attacker.getHeldItem()) moveuse.crit = 0;
            return super.onMoveTick(attacker, stack, moveuse, pre);
        }
    }
    public static void init()
    {
        UsableItemEffects.REGISTRY.put(i -> i instanceof ItemZCrystal, ZMoveUsable::new);
    }
}