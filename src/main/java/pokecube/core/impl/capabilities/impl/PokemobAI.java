package pokecube.core.impl.capabilities.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.InitAIEvent;
import pokecube.api.moves.Battle;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.logic.Logic;
import pokecube.core.ai.logic.LogicFloatFlySwim;
import pokecube.core.ai.logic.LogicInLiquid;
import pokecube.core.ai.logic.LogicInMaterials;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.logic.LogicMovesUpdates;
import pokecube.core.ai.tasks.Tasks;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.utils.AITools;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokemobAI extends PokemobEvolves
{
    private final boolean[] routineStates = new boolean[AIRoutine.values().length];

    private int cachedGeneralState;
    private int cachedCombatState;
    private int cachedLogicState;

    private List<IAIRunnable> tasks = new ArrayList<>();
    private Map<String, IAIRunnable> namedTasks = new HashMap<>();

    private Battle battle;

    @Override
    public float getPitch()
    {
        return this.dataSync().get(this.params.DIRECTIONPITCHDW);
    }

    @Override
    public boolean getGeneralState(GeneralStates state)
    {
        // Read tamed status based on if we have an owner, rather than flag in
        // the bitmask.
        if (state == GeneralStates.TAMED) return this.getOwnerId() != null;
        return super.getGeneralState(state);
    }

    @Override
    public ItemStack getPokecube()
    {
        return this.pokecube;
    }

    @Override
    public int getPokemonUID()
    {
        if (this.uid == -1 && this.getOwnerId() != null)
            this.uid = PokecubeSerializer.getInstance(this.getEntity().isEffectiveAi()).getNextID();
        return this.uid;
    }

    @Override
    public List<Logic> getTickLogic()
    {
        return this.logic;
    }

    @Override
    public int getTotalCombatState()
    {
        if (this.getEntity().getLevel().isClientSide)
            this.cachedCombatState = this.dataSync().get(this.params.COMBATSTATESDW);
        return this.cachedCombatState;
    }

    @Override
    public int getTotalGeneralState()
    {
        if (this.getEntity().getLevel().isClientSide)
            this.cachedGeneralState = this.dataSync().get(this.params.GENERALSTATESDW);
        return this.cachedGeneralState;
    }

    @Override
    public int getTotalLogicState()
    {
        if (this.getEntity().getLevel().isClientSide)
            this.cachedLogicState = this.dataSync().get(this.params.LOGICSTATESDW);
        return this.cachedLogicState;
    }

    @Override
    public boolean isGrounded()
    {
        return this.getLogicState(LogicStates.GROUNDED) || !this.isRoutineEnabled(AIRoutine.AIRBORNE)
                || this.getLogicState(LogicStates.SITTING) || this.getLogicState(LogicStates.SLEEPING);
    }

    @Override
    public boolean isRoutineEnabled(final AIRoutine routine)
    {
        if (!routine.isAllowed(this)) return false;
        return this.routineStates[routine.ordinal()];
    }

    @Override
    public void onSendOut()
    {
        // Reset some values to prevent spontaneous damage.
        this.getEntity().fallDistance = 0;
        this.getEntity().clearFire();
        // After here is server side only.
        if (this.getEntity().getLevel().isClientSide) return;
        // Flag as not evolving
        this.setGeneralState(GeneralStates.EVOLVING, false);

        // Play the sound for the mob.
        this.getEntity().playAmbientSound();

        // Do the shiny particle effect.
        if (this.isShiny())
        {
            final Vector3 particleLoc = new Vector3();
            for (int i = 0; i < 20; ++i)
            {
                particleLoc.set(
                        this.getEntity().getX()
                                + this.getEntity().getRandom().nextFloat() * this.getEntity().getBbWidth() * 2.0F
                                - this.getEntity().getBbWidth(),
                        this.getEntity().getY() + 0.5D
                                + this.getEntity().getRandom().nextFloat() * this.getEntity().getBbHeight(),
                        this.getEntity().getZ()
                                + this.getEntity().getRandom().nextFloat() * this.getEntity().getBbWidth() * 2.0F
                                - this.getEntity().getBbWidth());
                this.getEntity().getLevel().addParticle(ParticleTypes.HAPPY_VILLAGER, particleLoc.x, particleLoc.y,
                        particleLoc.z, 0, 0, 0);
            }
        }
        // Update genes settings.
        this.onGenesChanged();

        // Update our owner entity if needed
        if (this.getOwnerId() != null && this.getOwner() != null) this.setOwner(this.getOwner());

        // Update/add cache.
        if (this.isPlayerOwned() && this.getOwnerId() != null) PlayerPokemobCache.UpdateCache(this);
    }

    @Override
    public void setDirectionPitch(final float pitch)
    {
        this.dataSync().set(this.params.DIRECTIONPITCHDW, pitch);
    }

    @Override
    public void setPokecube(ItemStack pokeballId)
    {
        if (!pokeballId.isEmpty())
        {
            pokeballId = pokeballId.copy();
            pokeballId.setCount(1);
            // Remove the extra tag containing data about this pokemob
            if (pokeballId.hasTag()) pokeballId.getTag().remove("Pokemob");
        }
        this.pokecube = pokeballId;
    }

    @Override
    public void setRoutineState(final AIRoutine routine, final boolean enabled)
    {
        this.routineStates[routine.ordinal()] = enabled;
    }

    @Override
    public void setTotalCombatState(final int state)
    {
        this.cachedCombatState = state;
        this.dataSync().set(this.params.COMBATSTATESDW, state);
    }

    @Override
    public void setTotalGeneralState(final int state)
    {
        this.cachedGeneralState = state;
        this.dataSync().set(this.params.GENERALSTATESDW, state);
    }

    @Override
    public void setTotalLogicState(final int state)
    {
        this.cachedLogicState = state;
        this.dataSync().set(this.params.LOGICSTATESDW, state);
        // Sync sitting status over to the TameableEntity
        if (this.getEntity() instanceof TamableAnimal animal)
            animal.setOrderedToSit((this.cachedLogicState & LogicStates.SITTING.getMask()) != 0);
    }

    @Override
    public List<IAIRunnable> getTasks()
    {
        return this.tasks;
    }

    @Override
    public Map<String, IAIRunnable> getNamedTaskes()
    {
        return namedTasks;
    }

    @Override
    public Battle getBattle()
    {
        return this.battle;
    }

    @Override
    public void setBattle(final Battle battle)
    {
        this.battle = battle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void preInitAI()
    {

        final Brain<?> brain = this.getEntity().getBrain();
        // If brain was cleared at some point, this memory is removed.
        if (brain.checkMemory(MemoryModules.ATTACKTARGET.get(), MemoryStatus.REGISTERED)) return;

        final Mob entity = this.getEntity();
        final PokedexEntry entry = this.getPokedexEntry();

        this.guardCap = CapHolders.getGuardAI(entity);

        if (this.getOwnerHolder() == null)
            PokecubeAPI.LOGGER.warn("Pokemob without ownable cap, this is a bug! " + this.getPokedexEntry());
        if (this.guardCap == null)
            PokecubeAPI.LOGGER.warn("Pokemob without guard cap, this is a bug! " + this.getPokedexEntry());
        if (this.getGenes() == null)
            PokecubeAPI.LOGGER.warn("Pokemob without genetics cap, this is a bug! " + this.getPokedexEntry());

        this.getTickLogic().clear();

        // // Controller is done separately for ease of locating it for
        // // controls.
        this.getTickLogic().add(this.controller = new LogicMountedControl(this));

        // // Add in the various logic AIs that are needed on both client and
        // // server, so it is done here instead of in initAI.
        this.getTickLogic().add(new LogicInLiquid(this));
        this.getTickLogic().add(new LogicMovesUpdates(this));
        this.getTickLogic().add(new LogicInMaterials(this));
        if (entry.stock) this.getTickLogic().add(new LogicFloatFlySwim(this));
        this.getTickLogic().add(new LogicMiscUpdate(this));

        // If the mob was constructed without a world somehow (during init for
        // JEI, etc), do not bother with AI stuff.
        if (entity.getLevel() == null || ThutCore.proxy.isClientSide())
        {
            if (entity.getLevel() != null) PokecubeAPI.POKEMOB_BUS.post(new InitAIEvent.Post(this));
            return;
        }

        // DOLATER decide on speed scaling here?
        if (entry.stock)
        {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2F);
            if (entity.getAttributes().hasAttribute(Attributes.KNOCKBACK_RESISTANCE))
            {
                entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.98F);
            }
        }

        this.tasks = Lists.newArrayList();
        Tasks.initBrain(brain);

        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> idleMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> workMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryStatus>> coreMems = Sets.newHashSet();

        idleMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT));
        workMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT));
        coreMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT));

        brain.addActivityWithConditions(Activity.IDLE, Tasks.idle(this, 1), idleMems);
        brain.addActivityWithConditions(Activity.WORK, Tasks.utility(this, 1), workMems);
        brain.addActivityWithConditions(Activity.CORE, Tasks.combat(this, 1), coreMems);
        brain.setCoreActivities(Sets.newHashSet(Activity.IDLE, Activity.WORK, Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(entity.level.getDayTime(), entity.level.getGameTime());

        if (this.loadedTasks != null) for (final IAIRunnable task : this.tasks)
            if (this.loadedTasks.contains(task.getIdentifier()) && task instanceof INBTSerializable)
                INBTSerializable.class.cast(task).deserializeNBT(this.loadedTasks.get(task.getIdentifier()));
        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        PokecubeAPI.POKEMOB_BUS.post(new InitAIEvent.Post(this));

        for (var task : this.tasks) if (!task.getIdentifier().isBlank()) namedTasks.put(task.getIdentifier(), task);
    }

    @Override
    public void postInitAI()
    {
        // If the mob was constructed without a world somehow (during init for
        // JEI, etc), do not bother with AI stuff.
        if (entity.getLevel() == null || ThutCore.proxy.isClientSide()) return;

        // Set the pathing priorities for various blocks
        if (entity.fireImmune())
        {
            entity.setPathfindingMalus(BlockPathTypes.LAVA, 0);
            entity.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0);
            entity.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0);
        }
        else
        {
            entity.setPathfindingMalus(BlockPathTypes.LAVA, -1);
            entity.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1);
            entity.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16);
        }
        if (this.swims()) entity.setPathfindingMalus(BlockPathTypes.WATER, 0);
        this.getPokedexEntry().materialActions.forEach(a -> a.init(entity));
    }

    @Override
    public void onSetTarget(final LivingEntity entity, final boolean forced)
    {
        final boolean remote = this.getEntity().getLevel().isClientSide;
        if (remote) return;
        if (entity == null)
        {
            if (forced && this.targetFinder != null) this.targetFinder.clear();
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Null Target Set for " + this.getEntity());
            this.setTargetID(-1);
            this.getEntity().getPersistentData().putString("lastMoveHitBy", "");
        }
        else if (entity != null)
        {
            final IOwnable target = OwnableCaps.getOwnable(entity);
            final boolean mateFight = this.getCombatState(CombatStates.MATEFIGHT);
            if (PokecubeCore.getConfig().debug_ai)
                PokecubeAPI.logInfo("Target Set: {} -> {} ", this.getEntity(), entity);
            /**
             * Ensure that the target being set is actually a valid target.
             */
            if (entity == this.getEntity())
            {
                if (BrainUtils.getAttackTarget(this.getEntity()) == this.getEntity())
                    BrainUtils.clearAttackTarget(this.getEntity());
                return;
            }
            else if (target != null && this.getOwnerId() != null && this.getOwnerId().equals(target.getOwnerId())
                    && !mateFight)
            {
                BrainUtils.clearAttackTarget(this.getEntity());
                return;
            }
            else if (!PokecubeCore.getConfig().teamsBattleEachOther && TeamManager.sameTeam(entity, this.getEntity())
                    && !mateFight)
            {
                BrainUtils.clearAttackTarget(this.getEntity());
                return;
            }
            else if (!forced && !AITools.validCombatTargets.test(entity))
            {
                BrainUtils.clearAttackTarget(this.getEntity());
                return;
            }
            if (entity == null || remote) return;
            this.setLogicState(LogicStates.SITTING, false);
            if (this.getAbility() != null) this.getAbility().onAgress(this, entity);
        }
    }

}
