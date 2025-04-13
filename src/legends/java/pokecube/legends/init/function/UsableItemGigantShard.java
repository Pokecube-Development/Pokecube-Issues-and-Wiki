package pokecube.legends.init.function;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.database.Database;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.gimmicks.dynamax.DynamaxGene;
import pokecube.gimmicks.dynamax.DynamaxGene.DynaObject;
import pokecube.legends.init.ItemInit;

public class UsableItemGigantShard
{
    public static class GigantShardUsable extends BaseUseable
    {
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
            if (user != pokemob.getOwner()) return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            DynaObject dyna = DynamaxGene.getDyna(pokemob.getEntity());
            boolean gigant = dyna.gigantamax;
            // Already able to gigantamax, no effect.
            if (gigant) return super.onUse(pokemob, stack, user);
            final PokedexEntry entry = pokemob.getPokedexEntry();
            gigant = Database.getEntry(entry.getTrimmedName() + "-gmax") != null;
            // No gigantamax form for this pokemob, no effect.
            if (!gigant) return super.onUse(pokemob, stack, user);
            dyna.gigantamax = true;
            stack.split(1);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
    }

    public static void init()
    {
        UsableItemEffects.REGISTRY.put(i -> i == ItemInit.GIGANTIC_SHARD.get(), GigantShardUsable::new);
    }
}