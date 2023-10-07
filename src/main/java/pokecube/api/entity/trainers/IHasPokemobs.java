package pokecube.api.entity.trainers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.trainers.actions.ActionContext;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.PCEventsHandler;
import thut.api.world.mobs.data.DataSync;

/**
 * This is a general capability interface for a mob which is a "trainer", ie a
 * mob that has and can control pokemobs.
 *
 */
public interface IHasPokemobs extends ICapabilitySerializable<CompoundTag>, Container
{
    /**
     * LevelModes as follows:<br>
     * <br>
     * CONFIG - follow the config value in Config.instance.trainerslevel<br>
     * YES - we can have our mobs lvl up during battle<br>
     * NO - our mobs won't level up during battle.
     *
     */
    public static enum LevelMode
    {
        CONFIG, YES, NO;
    }

    /**
     * AllowedBattle enum, for determining cause of the battle request.<br>
     * <br>
     * YES - We can battle now<br>
     * NOWNOW - We cannot battle, due to temporary conditions<br>
     * NO - We presently cannot battle.
     *
     */
    public static enum AllowedBattle
    {
        YES, NOTNOW, NO;

        /**
         * @return if we are YES
         */
        public boolean test()
        {
            return this == YES;
        }
    }

    /**
     * Used to keep track of battle targets, including whether we can battle.
     *
     */
    public static interface ITargetWatcher
    {
        /**
         * Called when we are attached to the IHasPokemobs
         */
        default void onAdded(final IHasPokemobs pokemobs)
        {}

        /**
         * Called when we are de-attached from the IHasPokemobs
         */
        default void onRemoved(final IHasPokemobs pokemobs)
        {}

        /**
         * This checks if the target is valid for battle.
         * 
         * @param target - mob to check
         * @return Whether would want to battle this.
         */
        boolean isValidTarget(LivingEntity target);

        /**
         * @param target - mob to check
         * @return Whether we should ignore whether we have already battled the
         *         target.
         */
        default boolean ignoreHasBattled(final LivingEntity target)
        {
            return false;
        }

        /**
         * Called when we have a new combat target.
         * 
         * @param target - mob we are trying to battle.
         */
        default void onSet(final LivingEntity target)
        {}
    }

    /**
     * @return The living entity we are attached to.
     */
    LivingEntity getTrainer();

    /** Adds the pokemob back into the inventory, healing it as needed. */
    default boolean addPokemob(ItemStack mob)
    {
        UUID mobID = UUID.randomUUID();
        if (mob.hasTag()) if (mob.getTag().contains("Pokemob"))
        {
            final CompoundTag nbt = mob.getTag().getCompound("Pokemob");
            mobID = nbt.getUUID("UUID");
        }
        UUID testID = UUID.randomUUID();
        boolean found = false;
        int foundID = -1;
        for (int i = 0; i < this.getMaxPokemobCount(); i++)
        {
            final ItemStack ours = this.getPokemob(i);
            if (ours.isEmpty() || !ours.hasTag()) continue;
            if (ours.getTag().contains("Pokemob"))
            {
                final CompoundTag nbt = ours.getTag().getCompound("Pokemob");
                testID = nbt.getUUID("UUID");
                if (testID.equals(mobID))
                {
                    found = true;
                    foundID = i;
                    if (this.canLevel()) this.setPokemob(i, mob.copy());
                    else mob = this.getPokemob(i);
                    break;
                }
            }
        }
        if (found)
        {
            if (PokecubeCore.getConfig().debug_ai)
                PokecubeAPI.logInfo("Adding {} to slot {}", mob.getHoverName().getString(), foundID);
            this.setPokemob(foundID, mob.copy());
        }
        else for (int i = 0; i < this.getMaxPokemobCount(); i++)
        {
            final ItemStack ours = this.getPokemob(i);
            if (!found && ours.isEmpty())
            {
                this.setPokemob(i, mob.copy());
                if (PokecubeCore.getConfig().debug_ai)
                    PokecubeAPI.logInfo("Adding {} to slot {}", mob.getHoverName().getString(), i);
                break;
            }
        }
        for (int i = 0; i < this.getMaxPokemobCount(); i++)
        {
            final ItemStack stack = this.getPokemob(i);
            if (stack.isEmpty())
            {
                found = true;
                for (int j = i; j < this.getMaxPokemobCount() - 1; j++)
                {
                    this.setPokemob(j, this.getPokemob(j + 1));
                    this.setPokemob(j + 1, ItemStack.EMPTY);
                }
            }
        }
        this.onAddMob();
        return found;
    }

    /**
     * Adds a target watcher for combat validity checks.
     * 
     * @param watcher
     */
    default void addTargetWatcher(final ITargetWatcher watcher)
    {
        watcher.onAdded(this);
    }

    /** If we are agressive, is this a valid target? */
    default AllowedBattle canBattle(final LivingEntity target)
    {
        return this.canBattle(target, false);
    }

    /** If we are agressive, is this a valid target? */
    AllowedBattle canBattle(final LivingEntity target, final boolean checkWatchers);

    /**
     * 
     * @return whether our pokemobs can level up.
     */
    default boolean canLevel()
    {
        final LevelMode type = this.getLevelMode();
        if (type == LevelMode.CONFIG) return Config.instance.trainerslevel;
        return type == LevelMode.YES ? true : false;
    }

    /**
     * 
     * @return whether we can mega evolve our pokemobs.
     */
    boolean canMegaEvolve();

    @Override
    default void clearContent()
    {
        for (int i = 0; i < this.getMaxPokemobCount(); i++) this.setPokemob(i, ItemStack.EMPTY);
    }

    default boolean clearOnLoad()
    {
        return true;
    }

    /**
     * 
     * @return our current number of pokemobs
     */
    int countPokemon();

    /**
     * Initialises the count of pokemobs.
     */
    void initCount();

    /** The distance to see for attacking players */
    default int getAgressDistance()
    {
        return Config.instance.trainerSightRange;
    }

    /**
     * This is the cooldown for whether a pokemob can be sent out, it ticks
     * downwards, when less than 0, a mob may be thrown out as needed.
     */
    int getAttackCooldown();

    /**
     * This is the time when the next battle can start. it is in world ticks.
     */
    long getCooldown();

    /** 1 = male 2= female */
    byte getGender();

    /**
     * @return our present {@link LevelMode}.
     */
    LevelMode getLevelMode();

    /**
     * @return maximum pokemobs we can have on us at a time.
     */
    default int getMaxPokemobCount()
    {
        return 6;
    }

    /** The next pokemob to be sent out */
    default ItemStack getNextPokemob()
    {
        if (this.getNextSlot() < 0) return ItemStack.EMPTY;
        for (int i = 0; i < this.getMaxPokemobCount(); i++)
        {
            final ItemStack stack = this.getPokemob(i);
            if (stack.isEmpty()) for (int j = i; j < this.getMaxPokemobCount() - 1; j++)
            {
                this.setPokemob(j, this.getPokemob(j + 1));
                this.setPokemob(j + 1, ItemStack.EMPTY);
            }
        }
        return this.getPokemob(this.getNextSlot());
    }

    /** The next slot to be sent out. */
    int getNextSlot();

    /**
     * @return UUID of the pokemob we have presently out in the world.
     */
    UUID getOutID();

    /** If we have a mob out, this should be it. */
    IPokemob getOutMob();

    /**
     * 
     * @param slot - slot to check
     * @return Pokecube containing a pokemob at the slot, or empty.
     */
    ItemStack getPokemob(int slot);

    /**
     * 
     * @return our present combat target
     */
    LivingEntity getTarget();

    /**
     * This returns the target without any additional checks
     *
     * @return
     */
    LivingEntity getTargetRaw();

    /**
     * 
     * @return collection of ITargetWatchers we use for determining valid combat
     *         targets.
     */
    default Set<ITargetWatcher> getTargetWatchers()
    {
        return Collections.emptySet();
    }

    /**
     * 
     * @return the TypeTrainer we presently have, used for default trades,
     *         pokemob selection, etc.
     */
    TypeTrainer getType();

    /** Whether we should look for their target to attack. */
    default boolean isAgressive()
    {
        return true;
    }

    /**
     * Target sensitive version of isAgressive
     * 
     * @param target
     * @return if we should attack it.
     */
    default boolean isAgressive(final Entity target)
    {
        return this.isAgressive();
    }

    /**
     * Ticks the cooldowns. These are:<br>
     * <br>
     * attackCooldown - cooldown for sending mobs<br>
     * friendlyCooldown - cooldown for allowing trades<br>
     */
    void lowerCooldowns();

    /**
     * Called when we have added a pokemob, generally used for tracking combat
     * states, etc.
     */
    void onAddMob();

    /**
     * Called when we lose to a mob, this is used to give rewards, etc.
     * 
     * @param won - the mob that defeated us.
     */
    void onLose(Entity won);

    /**
     * Called when we defeat a mob, this is used for additional cooldowns, etc.
     * 
     * @param lost - the mob that we defeated.
     */
    void onWin(Entity lost);

    /**
     * Removes a ITargetWatcher from our collection of watchers.
     * 
     * @param watcher
     */
    default void removeTargetWatcher(final ITargetWatcher watcher)
    {
        watcher.onRemoved(this);
    }

    /**
     * Resets our status for who we defeated, and who defeated us.
     */
    void resetDefeatList();

    /** Resets the pokemobs; */
    void resetPokemob();

    /**
     * Sets the cooldown for sending our new pokemobs in battle.
     * 
     * @param value
     */
    void setAttackCooldown(int value);

    /**
     * Sets whether we can use mega evolution
     * 
     * @param flag
     */
    void setCanMegaEvolve(boolean flag);

    /**
     * Sets the cooldown for between battles, This should be set to a tick time,
     * so rather than a cooldown time, it is the next time we can battle.
     * 
     * @param value - {@link thut.api.Tracker}.instance().getTick() + cooldown
     *              time you want to set.
     */
    void setCooldown(long value);

    /** 1 = male 2= female */
    void setGender(byte value);

    /**
     * Sets our LevelMode
     * 
     * @param type - see {@link LevelMode} for details
     */
    void setLevelMode(LevelMode type);

    /**
     * Sets the next slot for the pokemob we should send in battle.
     * 
     * @param value
     */
    void setNextSlot(int value);

    /**
     * Sets the uuid of the primary pokemob we have out in the world.
     * 
     * @param mob - mob's uuid
     */
    void setOutID(UUID mob);

    /**
     * Sets the primary pokemob we have out in the world.
     * 
     * @param mob - our pokemob
     */
    void setOutMob(IPokemob mob);

    /**
     * Sets the pokecube for a slot.
     * 
     * @param slot - where to put the cube
     * @param cube - the pokemob's pokecube
     */
    void setPokemob(int slot, ItemStack cube);

    /**
     * Called when we have a new combat target.
     * 
     * @param target
     */
    default void onSetTarget(final LivingEntity target)
    {
        this.onSetTarget(target, false);
    }

    /**
     * 
     * @return whether we are currently in a battle.
     */
    boolean isInBattle();

    /**
     * Called when we have a new combat target.
     * 
     * @param target
     * @param ignoreCanBattle - if true, will ignore if we should be able to
     *                        battle.
     */
    void onSetTarget(LivingEntity target, boolean ignoreCanBattle);

    /**
     * Sets our trainer type, see {@link #getType()} for details
     * 
     * @param type
     */
    void setType(TypeTrainer type);

    /**
     * Throws our currently selected pokecube ({@link #getNextSlot()},
     * {@link #getNextPokemob()}) at a target.
     * 
     * @param target - who we toss a cube at
     */
    void throwCubeAt(Entity target);

    /**
     * Sets our {@link DataSync} object used to synchronize values between
     * client and server
     * 
     * @param sync
     */
    void setDataSync(DataSync sync);

    /**
     * Called each tick of our trainer.
     */
    default void onTick()
    {
        final boolean serverSide = this.getTrainer().level instanceof ServerLevel;
        if (!serverSide) return;

        // Every so often check if we have an out mob, and respond
        // accodingly
        mobcheck:
        if (this.getTrainer().tickCount % 600 == 10 && !this.isInBattle() && !(this.getTrainer() instanceof Player))
        {
            final List<Entity> mobs = PCEventsHandler.getOutMobs(this.getTrainer(), false);
            if (mobs.isEmpty()) break mobcheck;
            PCEventsHandler.recallAll(mobs, true);
        }

        // Check if we are still angry at something, or otherwise should be
        targetCheck:
        {
            final boolean hasTarget = this.getTargetRaw() != null;
            final boolean shouldHaveTarget = VillagerPanicTrigger.hasHostile(this.getTrainer());

            if (!(hasTarget || shouldHaveTarget)) break targetCheck;
            // This means we should have a target, but it isn't kept.
            if (!hasTarget)
            {
                final LivingEntity hostile = this.getTrainer().getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE)
                        .get();
                this.onSetTarget(hostile, true);
            }
        }
        this.lowerCooldowns();
    }

    /**
     * Called to end a battle between two {@link IHasPokemobs}
     * 
     * @param us
     * @param them
     */
    default void deAgro(final IHasPokemobs them)
    {
        this.getTrainer().setLastHurtByMob(null);
        this.getTrainer().setLastHurtMob(null);
        this.onSetTarget(null);
        if (them != null)
        {
            them.getTrainer().setLastHurtByMob(null);
            them.getTrainer().setLastHurtMob(null);
            them.onSetTarget(null);
        }
    }

    /**
     * The last {@link ActionContext} is cached for later checks for valid
     * combat targets later, or for trades, etc.
     * 
     * @return our last used {@link ActionContext}
     */
    ActionContext getLatestContext();

    /**
     * This returns the passed in argument to allow chain results after
     * processing the set.
     * 
     * @param our last used {@link ActionContext}
     * @return our last used {@link ActionContext}
     */
    ActionContext setLatestContext(ActionContext context);
}