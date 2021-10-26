package pokecube.legends.init.function;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;

public class UsableItemGigantShard
{
    public static class GigantShardUsable extends BaseUseable
    {
        /**
         * Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         *
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public ActionResult<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
        {
            System.out.println(user + " " + pokemob.getOwner());
            if (user != pokemob.getOwner()) return new ActionResult<>(ActionResultType.FAIL, stack);
            boolean gigant = pokemob.getCombatState(CombatStates.GIGANTAMAX);
            // Already able to gigantamax, no effect.
            if (gigant) return super.onUse(pokemob, stack, user);
            final PokedexEntry entry = pokemob.getPokedexEntry();
            gigant = Database.getEntry(entry.getTrimmedName() + "_gigantamax") != null;
            // No gigantamax form for this pokemob, no effect.
            if (!gigant) return super.onUse(pokemob, stack, user);
            pokemob.setCombatState(CombatStates.GIGANTAMAX, true);
            stack.split(1);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(Reference.ID, "usables");

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(UsableItemGigantShard.USABLE)) return;
        final Item item = event.getObject().getItem();
        if (item == ItemInit.GIGANTIC_SHARD.get()) event.addCapability(UsableItemGigantShard.USABLE,
                new GigantShardUsable());
    }
}