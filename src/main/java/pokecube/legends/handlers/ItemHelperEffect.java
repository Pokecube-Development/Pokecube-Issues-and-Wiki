package pokecube.legends.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import thut.api.item.ItemList;

public class ItemHelperEffect 
{
	public static class PoffinEffects extends BaseUseable
    {
        /**
         * @param pokemob
         * @param stack
         * @return
         */
        @Override
        public ActionResult<ItemStack> onMoveTick(final IPokemob pokemob, final ItemStack stack,
                final MovePacket moveuse)
        {
            if (pokemob == moveuse.attacker && moveuse.pre) 
            	if (ItemList.is(new ResourceLocation(Reference.ID, "poffin_"+moveuse.getMove().getType(pokemob)), stack))
            {
                moveuse.PWR *= 1.2;
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE_EFFECTS = new ResourceLocation(Reference.ID, "usables_effects");
    
    public static void init() {
    	MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, ItemHelperEffect::registerCapabilities);
    }
    
    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(ItemHelperEffect.USABLE_EFFECTS)) return;
        if (ItemList.is(USABLE_EFFECTS, event.getObject().getItem()))
        	event.addCapability(ItemHelperEffect.USABLE_EFFECTS, new PoffinEffects());
    }
}
