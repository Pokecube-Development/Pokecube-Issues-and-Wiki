package pokecube.legends.init.function;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import pokecube.legends.items.natureedit.ItemNature;

public class UsableItemNatureEffects
{
    public static class NatureUsable extends BaseUseable
    {
        private final Nature nature;

        public NatureUsable(final Nature nature)
        {
            this.nature = nature;
        }

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
            if (user != pokemob.getOwner()) return new ActionResult<>(ActionResultType.FAIL, stack);
            final boolean used = pokemob.getNature() != this.nature;
            if (used)
            {
                pokemob.setNature(this.nature);
                stack.split(1);
            }
            return new ActionResult<>(used ? ActionResultType.SUCCESS : ActionResultType.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(Reference.ID, "usables");

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(UsableItemNatureEffects.USABLE)) return;
        final Item item = event.getObject().getItem();
        if (item instanceof ItemNature) event.addCapability(UsableItemNatureEffects.USABLE, new NatureUsable(
                ((ItemNature) item).type));
    }
}