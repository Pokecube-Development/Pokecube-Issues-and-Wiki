package pokecube.core.interfaces;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects;

public interface IPokemobUseable
{

    public static class Default implements IPokemobUseable
    {

    }

    public static IPokemobUseable getUsableFor(final ICapabilityProvider objectIn)
    {
        if (objectIn == null) return null;
        final IPokemobUseable pokemobHolder = objectIn.getCapability(UsableItemEffects.USABLEITEM_CAP, null).orElse(
                null);
        if (pokemobHolder != null) return pokemobHolder;
        else if (IPokemobUseable.class.isInstance(objectIn)) return IPokemobUseable.class.cast(objectIn);
        else if (objectIn instanceof ItemStack && IPokemobUseable.class.isInstance(((ItemStack) objectIn).getItem()))
            return IPokemobUseable.class.cast(((ItemStack) objectIn).getItem());
        return pokemobHolder;
    }

    /**
     * @param attacker
     * @param stack
     * @return
     */
    public default InteractionResultHolder<ItemStack> onMoveTick(final IPokemob attacker, final ItemStack stack, final MovePacket moveuse)
    {
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    /**
     * Called every tick while this item is the active held item for the
     * pokemob.
     *
     * @param pokemob
     * @param stack
     * @return something happened
     */
    public default InteractionResultHolder<ItemStack> onTick(final IPokemob pokemob, final ItemStack stack)
    {
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    /**
     * Called when this item is "used". Normally this means via right clicking
     * the pokemob with the itemstack. It can also be called via onTick or
     * onMoveTick, in which case user will be pokemob.getEntity()
     *
     * @param user
     * @param pokemob
     * @param stack
     * @return something happened
     */
    public default InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
    {
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }
}
