package pokecube.core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.items.UsableItemEffects;

public interface IPokemobUseable
{

    public static class Default implements IPokemobUseable
    {

    }

    public static class Storage implements Capability.IStorage<IPokemobUseable>
    {

        @Override
        public void readNBT(Capability<IPokemobUseable> capability, IPokemobUseable instance, Direction side, INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(Capability<IPokemobUseable> capability, IPokemobUseable instance, Direction side)
        {
            return null;
        }

    }

    public static IPokemobUseable getUsableFor(ICapabilityProvider objectIn)
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
    public default ActionResult<ItemStack> onMoveTick(IPokemob attacker, ItemStack stack, MovePacket moveuse)
    {
        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    /**
     * Called every tick while this item is the active held item for the
     * pokemob.
     *
     * @param pokemob
     * @param stack
     * @return something happened
     */
    public default ActionResult<ItemStack> onTick(IPokemob pokemob, ItemStack stack)
    {
        return new ActionResult<>(ActionResultType.FAIL, stack);
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
    public default ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, LivingEntity user)
    {
        return new ActionResult<>(ActionResultType.FAIL, stack);
    }
}
