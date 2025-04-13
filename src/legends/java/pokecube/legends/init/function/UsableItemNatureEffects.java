package pokecube.legends.init.function;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.items.natureedit.ItemNature;

public class UsableItemNatureEffects
{
    public static class NatureUsable extends BaseUseable
    {
        public NatureUsable()
        {}

        /**
         * Called when this item is "used". Normally this means via right clicking the pokemob with the itemstack. It
         * can also be called via onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @return something happened
         */

        @Override
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack,
                final LivingEntity user)
        {
            if (!(stack.getItem() instanceof ItemNature itemNature))
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            var nature = itemNature.type;
            if (user != pokemob.getOwner()) return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            final boolean used = pokemob.getNature() != nature;
            if (used)
            {
                pokemob.setNature(nature);
                stack.split(1);
            }
            return new InteractionResultHolder<>(used ? InteractionResult.SUCCESS : InteractionResult.FAIL, stack);
        }
    }

    public static void init()
    {
        UsableItemEffects.REGISTRY.put(i -> i instanceof ItemNature, NatureUsable::new);
    }
}