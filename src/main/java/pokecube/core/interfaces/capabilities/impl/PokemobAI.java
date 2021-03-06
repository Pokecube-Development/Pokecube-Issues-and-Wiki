package pokecube.core.interfaces.capabilities.impl;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraftforge.common.util.INBTSerializable;
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
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.pokemob.InitAIEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.moves.Battle;
import pokecube.core.utils.AITools;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokemobAI extends PokemobEvolves
{
    private final boolean[] routineStates = new boolean[AIRoutine.values().length];

    private int cachedGeneralState;
    private int cachedCombatState;
    private int cachedLogicState;

    private List<IAIRunnable> tasks = Lists.newArrayList();

    private Battle battle;

    @Override
    public boolean getCombatState(final CombatStates state)
    {
        if (this.getEntity().getCommandSenderWorld().isClientSide) this.cachedCombatState = this.dataSync().get(
                this.params.COMBATSTATESDW);
        if (state == CombatStates.GIGANTAMAX && this.getGenesDynamax() != null) return this.getGenesDynamax()
                .getExpressed().getValue().gigantamax;
        return (this.cachedCombatState & state.getMask()) != 0;
    }

    @Override
    public float getDirectionPitch()
    {
        return this.dataSync().get(this.params.DIRECTIONPITCHDW);
    }

    @Override
    public boolean getGeneralState(final GeneralStates state)
    {
        // Read tamed status based on if we have an owner, rather than flag in
        // the bitmask.
        if (state == GeneralStates.TAMED) return this.getOwnerId() != null;
        if (this.getEntity().getCommandSenderWorld().isClientSide) this.cachedGeneralState = this.dataSync().get(
                this.params.GENERALSTATESDW);
        return (this.cachedGeneralState & state.getMask()) != 0;
    }

    @Override
    public boolean getLogicState(final LogicStates state)
    {
        if (this.getEntity().getCommandSenderWorld().isClientSide) this.cachedLogicState = this.dataSync().get(
                this.params.LOGICSTATESDW);
        return (this.cachedLogicState & state.getMask()) != 0;
    }

    @Override
    public ItemStack getPokecube()
    {
        return this.pokecube;
    }

    @Override
    public int getPokemonUID()
    {
        if (this.uid == -1 && this.getOwnerId() != null) this.uid = PokecubeSerializer.getInstance(this.getEntity()
                .isEffectiveAi()).getNextID();
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
        return this.dataSync().get(this.params.COMBATSTATESDW);
    }

    @Override
    public int getTotalGeneralState()
    {
        return this.dataSync().get(this.params.GENERALSTATESDW);
    }

    @Override
    public int getTotalLogicState()
    {
        return this.dataSync().get(this.params.LOGICSTATESDW);
    }

    @Override
    public boolean isGrounded()
    {
        return this.getLogicState(LogicStates.GROUNDED) || !this.isRoutineEnabled(AIRoutine.AIRBORNE) || this
                .getLogicState(LogicStates.SITTING) || this.getLogicState(LogicStates.SLEEPING);
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
        if (this.getEntity().getCommandSenderWorld().isClientSide) return;
        // Flag as not evolving
        this.setGeneralState(GeneralStates.EVOLVING, false);

        // Play the sound for the mob.
        this.getEntity().playSound(this.getSound(), 0.25f, 1);

        // Do the shiny particle effect.
        if (this.isShiny())
        {
            final Vector3 particleLoc = Vector3.getNewVector();
            for (int i = 0; i < 20; ++i)
            {
                particleLoc.set(this.getEntity().getX() + this.rand.nextFloat() * this.getEntity().getBbWidth() * 2.0F
                        - this.getEntity().getBbWidth(), this.getEntity().getY() + 0.5D + this.rand.nextFloat() * this
                                .getEntity().getBbHeight(), this.getEntity().getZ() + this.rand.nextFloat() * this
                                        .getEntity().getBbWidth() * 2.0F - this.getEntity().getBbWidth());
                this.getEntity().getCommandSenderWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, particleLoc.x,
                        particleLoc.y, particleLoc.z, 0, 0, 0);
            }
        }
        // Update genes settings.
        this.onGenesChanged();

        // Update/add cache.
        if (this.isPlayerOwned() && this.getOwnerId() != null) PlayerPokemobCache.UpdateCache(this);
    }

    @Override
    public void setCombatState(final CombatStates state, final boolean flag)
    {
        final int byte0 = this.dataSync().get(this.params.COMBATSTATESDW);
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        if (state == CombatStates.GIGANTAMAX && this.getGenesDynamax() != null) this.getGenesDynamax().getExpressed()
                .getValue().gigantamax = flag;
        this.setTotalCombatState(newState);
    }

    @Override
    public void setDirectionPitch(final float pitch)
    {
        this.dataSync().set(this.params.DIRECTIONPITCHDW, pitch);
    }

    @Override
    public void setGeneralState(final GeneralStates state, final boolean flag)
    {
        final int byte0 = this.dataSync().get(this.params.GENERALSTATESDW);
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalGeneralState(newState);
    }

    @Override
    public void setLogicState(final LogicStates state, final boolean flag)
    {
        final int byte0 = this.dataSync().get(this.params.LOGICSTATESDW);
        if (flag == ((byte0 & state.getMask()) != 0)) return;
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalLogicState(newState);
    }

    @Override
    public void setPokecube(ItemStack pokeballId)
    {
        if (!pokeballId.isEmpty())
        {
            pokeballId = pokeballId.copy();
            pokeballId.setCount(1);
            // Remove the extra tag containing data about this pokemob
            if (pokeballId.hasTag() && pokeballId.getTag().contains("Pokemob")) pokeballId.getTag().remove("Pokemob");
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
        if (this.getEntity() instanceof TameableEntity) ((TameableEntity) this.getEntity()).setOrderedToSit(
                (this.cachedLogicState & LogicStates.SITTING.getMask()) != 0);
    }

    @Override
    public List<IAIRunnable> getTasks()
    {
        return this.tasks;
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
    public void initAI()
    {
        final Brain<LivingEntity> brain = (Brain<LivingEntity>) this.getEntity().getBrain();
        // If brain was cleared at some point, this memory is removed.
        if (brain.checkMemory(MemoryModules.ATTACKTARGET, MemoryModuleStatus.REGISTERED)) return;

        final MobEntity entity = this.getEntity();
        final PokedexEntry entry = this.getPokedexEntry();

        this.guardCap = entity.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        this.genes = entity.getCapability(GeneRegistry.GENETICS_CAP).orElse(null);
        if (this.getOwnerHolder() == null) PokecubeCore.LOGGER.warn("Pokemob without ownable cap, this is a bug! "
                + this.getPokedexEntry());
        if (this.guardCap == null) PokecubeCore.LOGGER.warn("Pokemob without guard cap, this is a bug! " + this
                .getPokedexEntry());
        if (this.genes == null) PokecubeCore.LOGGER.warn("Pokemob without genetics cap, this is a bug! " + this
                .getPokedexEntry());

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
        if (entity.getCommandSenderWorld() == null || ThutCore.proxy.isClientSide()) return;

        // Set the pathing priorities for various blocks
        if (entity.fireImmune())
        {
            entity.setPathfindingMalus(PathNodeType.LAVA, 0);
            entity.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, 0);
            entity.setPathfindingMalus(PathNodeType.DANGER_FIRE, 0);
        }
        else
        {
            entity.setPathfindingMalus(PathNodeType.LAVA, -1);
            entity.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1);
            entity.setPathfindingMalus(PathNodeType.DANGER_FIRE, 16);
        }
        if (this.swims()) entity.setPathfindingMalus(PathNodeType.WATER, 0);
        if (this.getPokedexEntry().hatedMaterial != null) for (final String material : this
                .getPokedexEntry().hatedMaterial)
            if (material.equalsIgnoreCase("water")) entity.setPathfindingMalus(PathNodeType.WATER, -1);
            else if (material.equalsIgnoreCase("fire"))
            {
                entity.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1);
                entity.setPathfindingMalus(PathNodeType.DANGER_FIRE, -1);
            }

        // DOLATER decide on speed scaling here?
        if (entry.stock) entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2F);

        this.tasks = Lists.newArrayList();
        Tasks.initBrain(brain);

        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> idleMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> workMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> coreMems = Sets.newHashSet();

        idleMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_ABSENT));
        workMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_ABSENT));
        coreMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_PRESENT));

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
        PokecubeCore.POKEMOB_BUS.post(new InitAIEvent.Post(this));
    }

    @Override
    public void onSetTarget(final LivingEntity entity, final boolean forced)
    {
        final boolean remote = this.getEntity().getCommandSenderWorld().isClientSide;
        if (remote) return;
        if (entity == null)
        {
            if (forced && this.targetFinder != null) this.targetFinder.clear();
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Null Target Set for " + this.getEntity());
            this.setTargetID(-1);
            this.getEntity().getPersistentData().putString("lastMoveHitBy", "");
        }
        else if (entity != null)
        {
            final IOwnable target = OwnableCaps.getOwnable(entity);
            final boolean mateFight = this.getCombatState(CombatStates.MATEFIGHT);
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Target Set: {} -> {} ", this.getEntity(),
                    entity);
            /**
             * Ensure that the target being set is actually a valid target.
             */
            if (entity == this.getEntity())
            {
                if (BrainUtils.getAttackTarget(this.getEntity()) == this.getEntity()) BrainUtils.clearAttackTarget(this
                        .getEntity());
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
            else if (!forced && !AITools.validTargets.test(entity))
            {
                BrainUtils.clearAttackTarget(this.getEntity());
                return;
            }
            if (entity == null || remote) return;
            this.setLogicState(LogicStates.SITTING, false);
            this.setTargetID(entity.getId());
            this.setCombatState(CombatStates.ANGRY, true);
            if (this.getAbility() != null) this.getAbility().onAgress(this, entity);
        }
    }

    @Override
    public int getTargetID()
    {
        return this.dataSync.get(this.params.ATTACKTARGETIDDW);
    }

    @Override
    public void setTargetID(final int id)
    {
        this.dataSync.set(this.params.ATTACKTARGETIDDW, Integer.valueOf(id));
    }

}
