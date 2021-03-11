package pokecube.mobs;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.berries.ItemBerry.BerryType;

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
        public ActionResult<ItemStack> onTick(final IPokemob pokemob, final ItemStack stack)
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
        public ActionResult<ItemStack> onUse(final IPokemob pokemob, final ItemStack stack, final LivingEntity user)
        {
            return BerryHelper.applyEffect(pokemob, user, stack);
        }
    }

    public static ActionResult<ItemStack> applyEffect(final IPokemob pokemob, final LivingEntity user,
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
        if (useStack && user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild)
            useStack = false;
        if (useStack) stack.split(1);
        return new ActionResult<>(applied ? ActionResultType.SUCCESS : ActionResultType.FAIL, stack);
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

        final boolean apply = (berryId == 7 || berryId == 10 || berryId == 60) && user instanceof PlayerEntity
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
        new BerryType("cheri", effect, 1, 10, 0, 0, 0, 0);// Cures
                                                          // Paralysis
        new BerryType("chesto", effect, 2, 0, 10, 0, 0, 0);// Cures
                                                           // sleep
        new BerryType("pecha", effect, 3, 0, 0, 10, 0, 0);// Cures
                                                          // poison
        new BerryType("rawst", effect, 4, 0, 0, 0, 10, 0);// Cures burn
        new BerryType("aspear", effect, 5, 0, 0, 0, 0, 10);// Cures
                                                           // freeze
        new BerryType("leppa", effect, 6, 10, 0, 10, 10, 10);// Restores
                                                             // 10PP
        new BerryType("oran", effect, 7, 10, 10, 10, 10, 10);// Restores
                                                             // 10HP
        new BerryType("persim", effect, 8, 10, 10, 10, 0, 10);// Cures
                                                              // confusion
        new BerryType("lum", effect, 9, 10, 10, 10, 10, 0);// Cures any
                                                           // status
                                                           // ailment
        new BerryType("sitrus", effect, 10, 0, 10, 10, 10, 10);// Restores
                                                               // 1/4 HP
        new BerryType("nanab", effect, 18, 0, 0, 10, 10, 0);// Pokeblock
                                                            // ingredient
        new BerryType("pinap", effect, 20, 10, 0, 0, 0, 10);// Pokeblock
                                                            // ingredient
        new BerryType("pomeg", effect, 21, 10, 0, 10, 10, 0);// EV Berry
        new BerryType("kelpsy", effect, 22, 0, 10, 0, 10, 10);// EV
                                                              // Berry
        new BerryType("qualot", effect, 23, 10, 0, 10, 0, 10);// EV
                                                              // Berry
        new BerryType("hondew", effect, 24, 10, 10, 0, 10, 0);// EV
                                                              // Berry
        new BerryType("grepa", effect, 25, 0, 10, 10, 0, 10);// EV Berry
        new BerryType("tamato", effect, 26, 20, 10, 0, 0, 0);// EV Berry
        new BerryType("cornn", effect, 27, 0, 20, 10, 0, 0);// Pokeblock
                                                            // ingredient
        new BerryType("enigma", effect, 60, 40, 10, 0, 0, 0);// Restores
                                                             // 1/4 of
                                                             // HP
        new BerryType("jaboca", effect, 63, 0, 0, 0, 40, 10);// 4th gen.
                                                             // Causes
                                                             // recoil
                                                             // damage
                                                             // on foe
                                                             // if
                                                             // holder
                                                             // is hit
                                                             // by a
                                                             // physical
                                                             // move
        new BerryType("rowap", effect, 64, 10, 0, 0, 0, 40);// 4th gen.
                                                            // Causes
                                                            // recoil
                                                            // damage on
                                                            // foe if
                                                            // holder is
                                                            // hit by a
                                                            // special
                                                            // move
    }
}
