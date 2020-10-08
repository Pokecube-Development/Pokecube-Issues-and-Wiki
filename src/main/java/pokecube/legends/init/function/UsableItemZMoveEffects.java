package pokecube.legends.init.function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import pokecube.legends.items.zmove.ItemZCrystal;

public class UsableItemZMoveEffects
{
    public static class ZMoveUsable extends BaseUseable
    {
        @Override
        public ActionResult<ItemStack> onMoveTick(final IPokemob attacker, final ItemStack stack,
                final MovePacket moveuse)
        {
            if (stack == attacker.getHeldItem()) moveuse.criticalLevel = 0;
            return super.onMoveTick(attacker, stack, moveuse);
        }
    }

    public static final ResourceLocation USABLE = new ResourceLocation(Reference.ID, "usables");

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(UsableItemZMoveEffects.USABLE)) return;
        final Item item = event.getObject().getItem();
        if (item instanceof ItemZCrystal) event.addCapability(UsableItemZMoveEffects.USABLE, new ZMoveUsable());
    }
}