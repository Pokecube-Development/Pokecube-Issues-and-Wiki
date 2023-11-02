package pokecube.legends.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.items.UsableItemEffects.BaseUseable;
import pokecube.legends.Reference;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

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
        public InteractionResultHolder<ItemStack> onMoveTick(final IPokemob pokemob, final ItemStack stack,
                final MoveApplication moveuse, boolean pre)
        {
            if (pokemob == moveuse.getUser() && pre) if (ItemList
                    .is(new ResourceLocation(Reference.ID, "poffin_" + moveuse.getMove().getType(pokemob)), stack))
            {
                moveuse.pwr *= 1.2;
                stack.shrink(1);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
    }

    public static final ResourceLocation USABLE_EFFECTS = new ResourceLocation(Reference.ID, "usable_effects");

    public static void init()
    {
        ThutCore.FORGE_BUS.addGenericListener(ItemStack.class, ItemHelperEffect::registerCapabilities);
    }

    public static void registerCapabilities(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(ItemHelperEffect.USABLE_EFFECTS)) return;
        if (ItemList.is(USABLE_EFFECTS, event.getObject().getItem()))
            event.addCapability(ItemHelperEffect.USABLE_EFFECTS, new PoffinEffects());
    }
}
