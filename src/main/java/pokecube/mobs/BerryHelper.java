package pokecube.mobs;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;
import pokecube.core.items.berries.ItemBerry;

public class BerryHelper implements IMoveConstants
{
    public static class DefaultBerryEffect implements BerryEffect
    {
        /**
         * Called every tick while this item is the active held item for the
         * pokemob.
         *
         * @param pokemob
         * @param stack
         * @return something happened
         */
        @Override
        public InteractionResultHolder<ItemStack> onTick(final IPokemob pokemob, final ItemStack stack)
        {
            return this.onUse(pokemob, stack, pokemob.getEntity());
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
        public InteractionResultHolder<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
        {
            return BerryHelper.applyEffect(pokemob, user, stack);
        }
    }

    public static InteractionResultHolder<ItemStack> applyEffect(final IPokemob pokemob, final LivingEntity user,
            final ItemStack stack)
    {
        final boolean applied = BerryHelper.berryEffect(pokemob, user, stack);
        if (stack.getItem() instanceof ItemBerry)
        {
            final int[] flavours = ((ItemBerry) stack.getItem()).type.flavours;
            if (applied && flavours != null) for (int i = 0; i < 5; i++)
                pokemob.setFlavourAmount(i, pokemob.getFlavourAmount(i) + flavours[i]);
        }
        boolean useStack = applied;
        if (useStack && user instanceof Player && ((Player) user).getAbilities().instabuild)
            useStack = false;
        if (useStack) stack.split(1);
        return new InteractionResultHolder<>(applied ? InteractionResult.SUCCESS : InteractionResult.FAIL, stack);
    }

    private static boolean handleEVBerry(final IPokemob pokemob, final int index)
    {
        final byte[] evs = pokemob.getEVs();
        if (evs[index] == 0) return false;
        HappinessType.applyHappiness(pokemob, HappinessType.EVBERRY);
        evs[index] = (byte) Math.max(Byte.MIN_VALUE, evs[index] - 10);
        pokemob.setEVs(evs);
        return true;
    }

    public static boolean berryEffect(final IPokemob pokemob, final LivingEntity user, final ItemStack berry)
    {
        if (!(berry.getItem() instanceof ItemBerry)) return false;
        final byte status = pokemob.getStatus();
        final int berryId = ((ItemBerry) berry.getItem()).type.index;
        if (berryId >= 21 && berryId <= 26) return BerryHelper.handleEVBerry(pokemob, berryId - 21);
        if (status == IMoveConstants.STATUS_PAR && berryId == 1)
        {
            pokemob.healStatus();
            return true;
        }
        if (status == IMoveConstants.STATUS_SLP && berryId == 2)
        {
            pokemob.healStatus();
            return true;
        }
        if ((status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2) && berryId == 3)
        {
            pokemob.healStatus();
            return true;
        }
        if (status == IMoveConstants.STATUS_BRN && berryId == 4)
        {
            pokemob.healStatus();
            return true;
        }
        if (status == IMoveConstants.STATUS_FRZ && berryId == 5)
        {
            pokemob.healStatus();
            return true;
        }
        if (status != IMoveConstants.STATUS_NON && berryId == 9)
        {
            pokemob.healStatus();
            return true;
        }
        final LivingEntity entity = pokemob.getEntity();
        final float HP = entity.getHealth();
        final float HPmax = entity.getMaxHealth();

        final boolean apply = (berryId == 7 || berryId == 10 || berryId == 60) && user instanceof Player
                && HP < HPmax || HP < HPmax / 3;
        if (apply) if (berryId == 7)
        {
            entity.heal(10);
            return true;
        }
        else if (berryId == 10 || berryId == 60)
        {
            entity.heal(HPmax / 4f);
            return true;
        }
        return false;
    }

    public static void initBerries()
    {
        final DefaultBerryEffect effect = new DefaultBerryEffect();
        ItemBerry.registerBerryType("cheri", effect, 1, 10, 0, 0, 0, 0);// Cures Paralysis
        ItemBerry.registerBerryType("chesto", effect, 2, 0, 10, 0, 0, 0);// Cures sleep
        ItemBerry.registerBerryType("pecha", effect, 3, 0, 0, 10, 0, 0);// Cures poison
        ItemBerry.registerBerryType("rawst", effect, 4, 0, 0, 0, 10, 0);// Cures burn
        ItemBerry.registerBerryType("aspear", effect, 5, 0, 0, 0, 0, 10);// Cures freeze
        ItemBerry.registerBerryType("leppa", effect, 6, 10, 0, 10, 10, 10);// Restores 10PP
        ItemBerry.registerBerryType("oran", effect, 7, 10, 10, 10, 10, 10);// Restores 10HP
        ItemBerry.registerBerryType("persim", effect, 8, 10, 10, 10, 0, 10);// Cures confusion
        ItemBerry.registerBerryType("lum", effect, 9, 10, 10, 10, 10, 0);// Cures any status ailment
        ItemBerry.registerBerryType("sitrus", effect, 10, 0, 10, 10, 10, 10);// Restores 1/4 HP
        ItemBerry.registerBerryType("nanab", effect, 18, 0, 0, 10, 10, 0);// Pokeblock ingredient
        ItemBerry.registerBerryType("pinap", effect, 20, 10, 0, 0, 0, 10);// Pokeblock ingredient
        ItemBerry.registerBerryType("pomeg", effect, 21, 10, 0, 10, 10, 0);// EV Berry
        ItemBerry.registerBerryType("kelpsy", effect, 22, 0, 10, 0, 10, 10);// EV Berry
        ItemBerry.registerBerryType("qualot", effect, 23, 10, 0, 10, 0, 10);// EV Berry
        ItemBerry.registerBerryType("hondew", effect, 24, 10, 10, 0, 10, 0);// EV Berry
        ItemBerry.registerBerryType("grepa", effect, 25, 0, 10, 10, 0, 10);// EV Berry
        ItemBerry.registerBerryType("tamato", effect, 26, 20, 10, 0, 0, 0);// EV Berry
        ItemBerry.registerBerryType("cornn", effect, 27, 0, 20, 10, 0, 0);// Pokeblock ingredient
        ItemBerry.registerBerryType("enigma", effect, 60, 40, 10, 0, 0, 0);// Restores 1/4 of HP
        ItemBerry.registerBerryType("jaboca", effect, 63, 0, 0, 0, 40, 10);// 4th gen. Causes recoil damage on foe if holder is hit by a physical move
        ItemBerry.registerBerryType("rowap", effect, 64, 10, 0, 0, 0, 40);// 4th gen. Causes recoil damage on foe if holder is hit by a special move
    }
}
