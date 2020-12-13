package thut.api.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * These mobs will attempt to eat items, blocks, or other mobs.
 *
 * @author Thutmose
 */
public interface IHungrymob
{
    /**
     * Called when the mob eats the Object e. e can be any entity, will often
     * be an ItemEntity.
     * The return is whatever is "left" after eating, in the case of an
     * itemEntity or ItemStack, this is the remaining items. Note, this is
     * nullable!
     *
     * @param e
     */
    @Nullable
    public <T> T eat(@Nonnull T e);

    /** Mob eats berries */
    public boolean eatsBerries();

    /** Mob eats from being in water */
    public boolean filterFeeder();

    /**
     * @return Cooldown time between looking for meal, will only look if this
     *         is less than or equal to 0
     */
    public int getHungerCooldown();

    /** @return Time since last meal */
    public int getHungerTime();

    /** Mob eats other mobs */
    public boolean isCarnivore();

    /** Mob eats electricity */
    public boolean isElectrotroph();

    /** Mob eats plants (grass, flowers, etc) */
    public boolean isHerbivore();

    /** Mob eats rock */
    public boolean isLithotroph();

    /** Mob eats light */
    public boolean isPhototroph();

    /**
     * returns true if the mob is not actually a hungry mob, but uses the
     * interface for something else.
     *
     * @return
     */
    public boolean neverHungry();

    /**
     * Called when the mob fails to eat the entity, this is often because it
     * was already eaten by someone else.
     *
     * @param e
     */
    public void noEat(Object e);

    /**
     * Sets the hungerCooldown
     *
     * @param hungerCooldown
     */
    public void setHungerCooldown(int hungerCooldown);

    /**
     * sets time since last meal.
     *
     * @param hungerTime
     */
    public void setHungerTime(int hungerTime);
}
