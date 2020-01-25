package pokecube.legends.init.function;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import pokecube.legends.items.zmove.ItemZCrystal;

public class UsableItemZMoveEffects
{
    public static class ZMoveUsable extends BaseUseable
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

        @SuppressWarnings("null")
        @Override
        public ActionResult<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner()) return new ActionResult<>(
                    ActionResultType.FAIL, stack);
            final MovePacket moveUse = null;
            final boolean used = true;
            if (used)
            {
                moveUse.didCrit = true;
                PokecubeItems.deValidate(stack);
            }
            stack.setTag(null);
            return new ActionResult<>(used ? ActionResultType.SUCCESS : ActionResultType.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(Reference.ID, "usables");

    /** 1.12 this needs to be ItemStack instead of item. */
    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(UsableItemZMoveEffects.USABLE)) return;
        final Item item = event.getObject().getItem();
        if (item instanceof ItemZCrystal) event.addCapability(UsableItemZMoveEffects.USABLE, new ZMoveUsable());
    }
}