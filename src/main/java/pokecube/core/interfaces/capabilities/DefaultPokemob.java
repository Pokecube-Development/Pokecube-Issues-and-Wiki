package pokecube.core.interfaces.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.logic.LogicFloatFlySwim;
import pokecube.core.ai.logic.LogicInLiquid;
import pokecube.core.ai.logic.LogicInMaterials;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.logic.LogicMovesUpdates;
import pokecube.core.ai.tasks.Tasks;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;
import pokecube.core.events.pokemob.InitAIEvent;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.impl.PokemobSaves;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.AITools;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TagNames;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.ThutCaps;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

public class DefaultPokemob extends PokemobSaves implements ICapabilitySerializable<CompoundNBT>, IPokemob
{
    private final LazyOptional<IPokemob> holder = LazyOptional.of(() -> this);

    private List<IAIRunnable> tasks = Lists.newArrayList();
    private ITargetFinder     targetFinder;

    private boolean initedAI = false;

    public DefaultPokemob()
    {
        for (final AIRoutine routine : AIRoutine.values())
            this.setRoutineState(routine, routine.getDefault());
    }

    public DefaultPokemob(final MobEntity mob)
    {
        this();
        this.setEntity(mob);
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        try
        {
            this.read(nbt);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Loading Pokemob", e);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        if (capability == ThutCaps.COLOURABLE) return this.holder.cast();
        if (capability == ThutCaps.BREEDS) return this.holder.cast();
        return CapabilityPokemob.POKEMOB_CAP.orEmpty(capability, this.holder);
    }

    @Override
    public float getHeading()
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED)) return this.dataSync.get(this.params.HEADINGDW);
        return this.getEntity().rotationYaw;
    }

    @Override
    public int getTargetID()
    {
        return this.dataSync.get(this.params.ATTACKTARGETIDDW);
    }

    @Override
    public List<IAIRunnable> getTasks()
    {
        return this.tasks;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initAI()
    {
        if (this.initedAI) return;
        this.initedAI = true;

        final MobEntity entity = this.getEntity();

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
        this.getTickLogic().add(new LogicFloatFlySwim(this));
        this.getTickLogic().add(new LogicMiscUpdate(this));

        // If the mob was constructed without a world somehow (during init for
        // JEI, etc), do not bother with AI stuff.
        if (entity.getEntityWorld() == null || ThutCore.proxy.isClientSide()) return;

        // Set the pathing priorities for various blocks
        if (entity.isImmuneToFire())
        {
            entity.setPathPriority(PathNodeType.LAVA, 0);
            entity.setPathPriority(PathNodeType.DAMAGE_FIRE, 0);
            entity.setPathPriority(PathNodeType.DANGER_FIRE, 0);
        }
        else
        {
            entity.setPathPriority(PathNodeType.LAVA, 20);
            entity.setPathPriority(PathNodeType.DAMAGE_FIRE, 8);
            entity.setPathPriority(PathNodeType.DANGER_FIRE, 8);
        }
        if (this.swims()) entity.setPathPriority(PathNodeType.WATER, 0);
        if (this.getPokedexEntry().hatedMaterial != null) for (final String material : this
                .getPokedexEntry().hatedMaterial)
            if (material.equalsIgnoreCase("water")) entity.setPathPriority(PathNodeType.WATER, -1);
            else if (material.equalsIgnoreCase("fire"))
            {
                entity.setPathPriority(PathNodeType.DAMAGE_FIRE, -1);
                entity.setPathPriority(PathNodeType.DANGER_FIRE, -1);
            }

        // DOLATER decide on speed scaling here?
        entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);

        // Generic goals
        if (!this.swims()) entity.goalSelector.addGoal(0, new SwimGoal(entity));
        entity.goalSelector.addGoal(6, new LookAtGoal(entity, PlayerEntity.class, 6.0F));
        entity.goalSelector.addGoal(7, new LookRandomlyGoal(entity));

        this.tasks = Lists.newArrayList();
        final Brain<LivingEntity> brain = (Brain<LivingEntity>) this.getEntity().getBrain();
        Tasks.initBrain(brain);

        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> idleMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> workMems = Sets.newHashSet();
        final Set<Pair<MemoryModuleType<?>, MemoryModuleStatus>> coreMems = Sets.newHashSet();

        idleMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_ABSENT));
        workMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_ABSENT));
        coreMems.add(Pair.of(MemoryModuleType.HURT_BY, MemoryModuleStatus.VALUE_PRESENT));

        brain.registerActivity(Activity.IDLE, Tasks.idle(this, 1), idleMems);
        brain.registerActivity(Activity.WORK, Tasks.utility(this, 1), workMems);
        brain.registerActivity(Activity.CORE, Tasks.combat(this, 1), coreMems);
        brain.setDefaultActivities(Sets.newHashSet(Activity.IDLE, Activity.WORK, Activity.CORE));
        brain.setFallbackActivity(Activity.IDLE);
        brain.switchTo(Activity.IDLE);
        brain.updateActivity(entity.world.getDayTime(), entity.world.getGameTime());

        if (this.loadedTasks != null) for (final IAIRunnable task : this.tasks)
            if (this.loadedTasks.contains(task.getIdentifier()) && task instanceof INBTSerializable)
                INBTSerializable.class.cast(task).deserializeNBT(this.loadedTasks.get(task.getIdentifier()));

        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        PokecubeCore.POKEMOB_BUS.post(new InitAIEvent(this));
    }

    @Override
    public void onSetTarget(final LivingEntity entity, final boolean forced)
    {
        final boolean remote = this.getEntity().getEntityWorld().isRemote;
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
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Target Set: {} -> {} ", this.getEntity(),
                    entity);
            /**
             * Ensure that the target being set is actually a valid target.
             */
            if (entity == this.getEntity())
            {
                if (BrainUtils.getAttackTarget(this.getEntity()) == this.getEntity()) BrainUtils.setAttackTarget(this
                        .getEntity(), null);
                return;
            }
            else if (target != null && this.getOwnerId() != null && this.getOwnerId().equals(target.getOwnerId()))
            {
                BrainUtils.setAttackTarget(this.getEntity(), null);
                return;
            }
            else if (TeamManager.sameTeam(entity, this.getEntity()) && !this.getCombatState(CombatStates.MATEFIGHT))
            {
                BrainUtils.setAttackTarget(this.getEntity(), null);
                return;
            }
            else if (!forced && !AITools.validTargets.test(entity))
            {
                BrainUtils.setAttackTarget(this.getEntity(), null);
                return;
            }
            if (entity == null || remote) return;
            this.setLogicState(LogicStates.SITTING, false);
            this.setTargetID(entity.getEntityId());
            this.setCombatState(CombatStates.ANGRY, true);
            if (entity != BrainUtils.getAttackTarget(this.getEntity()) && this.getAbility() != null && !entity
                    .getEntityWorld().isRemote) this.getAbility().onAgress(this, entity);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT tag;
        try
        {
            tag = this.write();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error Saving Pokemob", e);
            tag = new CompoundNBT();
        }
        return tag;
    }

    @Override
    public void setHeading(final float heading)
    {
        if (this.getGeneralState(GeneralStates.CONTROLLED))
        {
            this.getEntity().rotationYaw = heading;
            this.dataSync.set(this.params.HEADINGDW, heading);
        }
    }

    @Override
    public void setTargetID(final int id)
    {
        this.dataSync.set(this.params.ATTACKTARGETIDDW, Integer.valueOf(id));
    }

    @Override
    public boolean isSheared()
    {
        boolean sheared = this.getGeneralState(GeneralStates.SHEARED);
        if (sheared && this.getEntity().isServerWorld())
        {
            final MinecraftServer server = this.getEntity().getServer();
            final long lastShear = this.getEntity().getPersistentData().getLong(TagNames.SHEARTIME);
            final ItemStack key = new ItemStack(Items.SHEARS);
            if (this.getPokedexEntry().interact(key))
            {
                final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
                final int timer = action.cooldown + this.rand.nextInt(1 + action.variance);
                if (lastShear < server.getTickCounter() - timer) sheared = false;
            }
            // Cannot shear this!
            else sheared = false;
            this.setGeneralState(GeneralStates.SHEARED, sheared);
        }
        return sheared;
    }

    @Override
    public void shear(final ItemStack shears)
    {
        if (this.isSheared() || !this.getEntity().isServerWorld()) return;
        final ResourceLocation WOOL = new ResourceLocation("wool");

        final ItemStack key = shears;
        if (this.getPokedexEntry().interact(key))
        {
            final MinecraftServer server = this.getEntity().getServer();
            final ArrayList<ItemStack> ret = new ArrayList<>();
            this.setGeneralState(GeneralStates.SHEARED, true);
            this.getEntity().getPersistentData().putLong(TagNames.SHEARTIME, server.getTickCounter());
            final Interaction action = this.getPokedexEntry().interactionLogic.getFor(key);
            final List<ItemStack> list = action.stacks;
            final int time = this.getHungerTime();
            this.setHungerTime(time + action.hunger);
            for (final ItemStack stack : list)
            {
                ItemStack toAdd = stack.copy();
                if (ItemList.is(WOOL, stack))
                {
                    final DyeColor colour = DyeColor.byId(this.getDyeColour());
                    final Item wool = SheepEntity.WOOL_BY_COLOR.get(colour).asItem();
                    toAdd = new ItemStack(wool, stack.getCount());
                    if (stack.hasTag()) toAdd.setTag(stack.getTag().copy());
                }
                ret.add(toAdd);
            }
            for (final ItemStack stack : ret)
                this.getEntity().entityDropItem(stack);
            this.getEntity().playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
        }

    }

    @Override
    public void setTargetFinder(final ITargetFinder tracker)
    {
        this.targetFinder = tracker;
    }

    @Override
    public ITargetFinder getTargetFinder()
    {
        return this.targetFinder;
    }
}