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

public interface IHasPokemobs extends ICapabilitySerializable<CompoundTag>, Container
{
    public static enum LevelMode
    {
        CONFIG, YES, NO;
    }

    public static enum AllowedBattle
    {
        YES, NOTNOW, NO;

        public boolean test()
        {
            return this == YES;
        }
    }

    public static interface ITargetWatcher
    {
        default void onAdded(final IHasPokemobs pokemobs)
        {}

        default void onRemoved(final IHasPokemobs pokemobs)
        {}

        boolean isValidTarget(LivingEntity target);

        default boolean ignoreHasBattled(final LivingEntity target)
        {
            return false;
        }

        default void onSet(final LivingEntity target)
        {

        }
    }

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

    default boolean canLevel()
    {
        final LevelMode type = this.getLevelMode();
        if (type == LevelMode.CONFIG) return Config.instance.trainerslevel;
        return type == LevelMode.YES ? true : false;
    }

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

    int countPokemon();

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

    LevelMode getLevelMode();

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

    UUID getOutID();

    /** If we have a mob out, this should be it. */
    IPokemob getOutMob();

    ItemStack getPokemob(int slot);

    LivingEntity getTarget();

    /**
     * This returns the target without any additional checks
     *
     * @return
     */
    LivingEntity getTargetRaw();

    default Set<ITargetWatcher> getTargetWatchers()
    {
        return Collections.emptySet();
    }

    TypeTrainer getType();

    /** Whether we should look for their target to attack. */
    default boolean isAgressive()
    {
        return true;
    }

    default boolean isAgressive(final Entity target)
    {
        return this.isAgressive();
    }

    void lowerCooldowns();

    void onAddMob();

    void onLose(Entity won);

    void onWin(Entity lost);

    default void removeTargetWatcher(final ITargetWatcher watcher)
    {
        watcher.onRemoved(this);
    }

    void resetDefeatList();

    /** Resets the pokemobs; */
    void resetPokemob();

    void setAttackCooldown(int value);

    void setCanMegaEvolve(boolean flag);

    void setCooldown(long value);

    /** 1 = male 2= female */
    void setGender(byte value);

    void setLevelMode(LevelMode type);

    void setNextSlot(int value);

    void setOutID(UUID mob);

    void setOutMob(IPokemob mob);

    void setPokemob(int slot, ItemStack cube);

    default void onSetTarget(final LivingEntity target)
    {
        this.onSetTarget(target, false);
    }

    boolean isInBattle();

    void onSetTarget(LivingEntity target, boolean ignoreCanBattle);

    void setType(TypeTrainer type);

    void throwCubeAt(Entity target);

    void setDataSync(DataSync sync);

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

    default void deAgro(final IHasPokemobs us, final IHasPokemobs them)
    {
        if (us != null)
        {
            us.getTrainer().setLastHurtByMob(null);
            us.getTrainer().setLastHurtMob(null);
            us.onSetTarget(null);
        }
        if (them != null)
        {
            them.getTrainer().setLastHurtByMob(null);
            them.getTrainer().setLastHurtMob(null);
            them.onSetTarget(null);
        }
    }

    ActionContext getLatestContext();

    ActionContext setLatestContext(ActionContext context);
}