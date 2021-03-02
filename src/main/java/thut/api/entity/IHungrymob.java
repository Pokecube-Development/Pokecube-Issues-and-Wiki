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
    <T> T eat(@Nonnull T e);

    /** Mob eats berries */
    boolean eatsBerries();

    /** Mob eats from being in water */
    boolean filterFeeder();

    /**
     * @return Cooldown time between looking for meal, will only look if this
     *         is less than or equal to 0
     */
    int getHungerCooldown();

    /** @return Time since last meal */
    int getHungerTime();

    /** Mob eats other mobs */
    boolean isCarnivore();

    /** Mob eats electricity */
    boolean isElectrotroph();

    /** Mob eats plants (grass, flowers, etc) */
    boolean isHerbivore();

    /** Mob eats rock */
    boolean isLithotroph();

    /** Mob eats light */
    boolean isPhototroph();

    /**
     * returns true if the mob is not actually a hungry mob, but uses the
     * interface for something else.
     *
     * @return
     */
    boolean neverHungry();

    /**
     * Called when the mob fails to eat the entity, this is often because it
     * was already eaten by someone else.
     *
     * @param e
     */
    void noEat(Object e);

    /**
     * Sets the hungerCooldown
     *
     * @param hungerCooldown
     */
    void setHungerCooldown(int hungerCooldown);

    /**
     * sets time since last meal.
     *
     * @param hungerTime
     */
    void setHungerTime(int hungerTime);

    /**
     * Applies the amount to the hunger, this should be positive for things that
     * make the mob more hungry, and negative for things that make it less
     * hungry.
     *
     * @param amount
     */
    default void applyHunger(final int amount)
    {
        this.setHungerTime(this.getHungerTime() + amount);
    }
}
