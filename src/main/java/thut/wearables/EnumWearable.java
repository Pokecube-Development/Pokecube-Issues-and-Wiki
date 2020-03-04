package thut.wearables;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;

public enum EnumWearable
{

    FINGER(2, 0), WRIST(2, 2), ANKLE(2, 4), NECK(6), BACK(7), WAIST(8), EAR(2, 9), EYE(11), HAT(12);

    static EnumWearable[]        BYINDEX  = new EnumWearable[13];
    static Set<IWearableChecker> checkers = Sets.newHashSet();
    static
    {
        EnumWearable.BYINDEX[0] = FINGER;
        EnumWearable.BYINDEX[1] = FINGER;
        EnumWearable.BYINDEX[2] = WRIST;
        EnumWearable.BYINDEX[3] = WRIST;
        EnumWearable.BYINDEX[4] = ANKLE;
        EnumWearable.BYINDEX[5] = ANKLE;
        EnumWearable.BYINDEX[6] = NECK;
        EnumWearable.BYINDEX[7] = BACK;
        EnumWearable.BYINDEX[8] = WAIST;
        EnumWearable.BYINDEX[9] = EAR;
        EnumWearable.BYINDEX[10] = EAR;
        EnumWearable.BYINDEX[11] = EYE;
        EnumWearable.BYINDEX[12] = HAT;

        EnumWearable.checkers.add(new IWearableChecker()
        {
            @Override
            public boolean canRemove(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                    final int subIndex)
            {
                if (itemstack.isEmpty()) return true;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
                    return wearable.canRemove(player, itemstack, slot, subIndex);
                if (itemstack.getItem() instanceof IActiveWearable) return ((IActiveWearable) itemstack.getItem())
                        .canRemove(player, itemstack, slot, subIndex);
                return true;
            }

            @Override
            public EnumWearable getSlot(final ItemStack stack)
            {
                if (stack.isEmpty()) return null;
                IActiveWearable wearable;
                if ((wearable = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
                    return wearable.getSlot(stack);
                if (stack.getItem() instanceof IWearable) return ((IWearable) stack.getItem()).getSlot(stack);
                return null;
            }

            @Override
            public void onInteract(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                    final int subIndex, final ItemUseContext context)
            {
                if (!itemstack.isEmpty()) itemstack.getItem().onItemUse(context);
            }

            @Override
            public void onPutOn(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                    final int subIndex)
            {
                if (itemstack.isEmpty()) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
                {
                    wearable.onPutOn(player, itemstack, slot, subIndex);
                    return;
                }
                if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onPutOn(
                        player, itemstack, slot, subIndex);
            }

            @Override
            public void onTakeOff(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                    final int subIndex)
            {
                if (itemstack.isEmpty()) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
                {
                    wearable.onTakeOff(player, itemstack, slot, subIndex);
                    return;
                }
                if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onTakeOff(
                        player, itemstack, slot, subIndex);
            }

            @Override
            public void onUpdate(final LivingEntity player, final ItemStack itemstack, final EnumWearable slot,
                    final int subIndex)
            {
                if (itemstack == null) return;
                IActiveWearable wearable;
                if ((wearable = itemstack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null)) != null)
                    wearable.onUpdate(player, itemstack, slot, subIndex);
                if (itemstack.getItem() instanceof IActiveWearable) ((IActiveWearable) itemstack.getItem()).onUpdate(
                        player, itemstack, slot, subIndex);
                else if (player instanceof PlayerEntity) itemstack.getItem().onArmorTick(itemstack, player
                        .getEntityWorld(), (PlayerEntity) player);
                else itemstack.getItem().inventoryTick(itemstack, player.getEntityWorld(), player, slot.index
                        + subIndex, false);
            }
        });
    }

    public static boolean canTakeOff(final LivingEntity wearer, final ItemStack stack, final int index)
    {
        if (stack.isEmpty()) return true;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        for (final IWearableChecker checker : EnumWearable.checkers)
            if (!checker.canRemove(wearer, stack, slot, subIndex)) return false;
        return true;
    }

    public static String getIcon(final int index)
    {
        String tex = null;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        switch (slot)
        {
        case ANKLE:
            tex = ThutWearables.MODID + ":items/empty_ankle_" + (subIndex == 0 ? "left" : "right");
            break;
        case BACK:
            tex = ThutWearables.MODID + ":items/empty_back";
            break;
        case EAR:
            tex = ThutWearables.MODID + ":items/empty_ear_" + (subIndex == 0 ? "left" : "right");
            break;
        case EYE:
            tex = ThutWearables.MODID + ":items/empty_eye";
            break;
        case FINGER:
            tex = ThutWearables.MODID + ":items/empty_finger_" + (subIndex == 0 ? "left" : "right");
            break;
        case HAT:
            tex = ThutWearables.MODID + ":items/empty_hat";
            break;
        case NECK:
            tex = ThutWearables.MODID + ":items/empty_neck";
            break;
        case WAIST:
            tex = ThutWearables.MODID + ":items/empty_waist";
            break;
        case WRIST:
            tex = ThutWearables.MODID + ":items/empty_wrist_" + (subIndex == 0 ? "left" : "right");
            break;
        default:
            break;
        }
        return tex;
    }

    public static EnumWearable getSlot(final ItemStack item)
    {
        if (item.isEmpty()) return null;
        for (final IWearableChecker checker : EnumWearable.checkers)
        {
            final EnumWearable ret = checker.getSlot(item);
            if (ret != null) return ret;
        }
        return null;
    }

    public static int getSubIndex(final int index)
    {
        return index - EnumWearable.BYINDEX[index].index;
    }

    public static EnumWearable getWearable(final int index)
    {
        return EnumWearable.BYINDEX[index];
    }

    public static void interact(final PlayerEntity player, final ItemStack item, final int index,
            final ItemUseContext context)
    {
        if (item.isEmpty()) return;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        for (final IWearableChecker checker : EnumWearable.checkers)
            checker.onInteract(player, item, slot, subIndex, context);
    }

    public static void putOn(final LivingEntity wearer, final ItemStack stack, final int index)
    {
        if (stack.isEmpty()) return;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        for (final IWearableChecker checker : EnumWearable.checkers)
            checker.onPutOn(wearer, stack, slot, subIndex);
    }

    public static void registerWearableChecker(final IWearableChecker checker)
    {
        EnumWearable.checkers.add(checker);
    }

    public static void takeOff(final LivingEntity wearer, final ItemStack stack, final int index)
    {
        if (stack.isEmpty()) return;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        for (final IWearableChecker checker : EnumWearable.checkers)
            checker.onTakeOff(wearer, stack, slot, subIndex);
    }

    public static void tick(final LivingEntity wearer, final ItemStack stack, final int index)
    {
        if (stack.isEmpty()) return;
        final EnumWearable slot = EnumWearable.getWearable(index);
        final int subIndex = EnumWearable.getSubIndex(index);
        for (final IWearableChecker checker : EnumWearable.checkers)
            checker.onUpdate(wearer, stack, slot, subIndex);
    }

    public final int slots;

    public final int index;

    private EnumWearable(final int index)
    {
        this.index = index;
        this.slots = 1;
    }

    private EnumWearable(final int slots, final int index)
    {
        this.index = index;
        this.slots = slots;
    }
}
