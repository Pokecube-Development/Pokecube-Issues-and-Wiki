package pokecube.core.ai.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.context.NBTContext;
import pokecube.api.effects.context.PokemobContext;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.ai.AnimationSelectionEvent;
import pokecube.api.items.IPokecube;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.moves.utils.IMoveConstants.ContactCategory;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.tasks.TaskBase;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.moves.damage.effects.Sleep;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.recipes.RecipePokeseals;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.PokemobTracker.MobEntry;
import thut.api.ThutCaps;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.multipart.IBBPartMultipart;
import thut.api.item.ItemList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Mostly does visuals updates, such as particle effects, checking that shearing status is reset properly. It also
 * resets stat modifiers when the mob is out of combat.
 */
public class LogicMiscUpdate extends LogicBase
{
    public static int EXITCUBEDURATION = 40;

    public static final boolean holiday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 25
            && Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER;

    private final int[] flavourAmounts = new int[5];

    private boolean checkedEvol = false;
    private boolean usedMoveSinceResetAttr = false;
    private boolean usingMoveThisTick = false;
    private boolean complexTick = false;
    private boolean exitingCube = false;
    private EffectPacketInfo evo_effect = null;
    private EffectPacketInfo cube_effect = null;

    private int floatTimer = 0;

    boolean inCombat = false;

    int combatTimer = 0;

    private int cacheTimer = 0;

    BlockPos lastCache;

    UUID prevOwner = null;

    UUID prevID = null;

    final IAnimated animated;
    final IAnimationHolder holder;
    IAnimationHolder liveHolder;

    public LogicMiscUpdate(final IPokemob pokemob)
    {
        super(pokemob);
        this.lastCache = this.entity.blockPosition();

        animated = ThutCaps.getAnimated(this.entity);
        holder = ThutCaps.getAnimationHolder(this.entity);
    }

    private void checkAIStates(UUID ownerID)
    {
        boolean angry = this.pokemob.inCombat();

        boolean sleepingAI = this.pokemob.getLogicState(LogicStates.SLEEPING);
        if (entity.hasEffect(StatusEffects.SLEEP))
        {
            var inst = entity.getEffect(StatusEffects.SLEEP);
            var _sleepingAI = inst.getAmplifier() == Sleep.NATURAL_SLEEP;
            if (sleepingAI != _sleepingAI) this.pokemob.setLogicState(LogicStates.SLEEPING, _sleepingAI);
            sleepingAI = _sleepingAI;
        }
        else if (sleepingAI) this.pokemob.setLogicState(LogicStates.SLEEPING, sleepingAI = false);

        if (this.pokemob.getGeneralState(GeneralStates.MATING) && !BrainUtils.hasMateTarget((AgeableMob) this.entity))
            this.pokemob.setGeneralState(GeneralStates.MATING, false);

        // Check if we are sheared every second or so
        if (complexTick) this.pokemob.isSheared();

        // If angry and has no target, make it not angry.

        // If not angry, and not been so for a while, reset stat modifiers.
        if (!angry)
        {
            final boolean resetCombat = this.combatTimer == 0;
            if (resetCombat)
            {
                this.pokemob.getMoveStats().reset();
                this.pokemob.setCombatState(CombatStates.NOITEMUSE, false);
                // Then reset stat multipliers here.
                PokecubeAttributes.resetToEntry(this.pokemob);
            }
            this.combatTimer++;

            if (this.combatTimer > 50 && this.combatTimer % 100 == 0)
            {
                // If we are using a move, reset the combat healing timer
                if (usingMoveThisTick) combatTimer = 10;
                    // Otherwise, if we had attacked before, reset attributes next cycle
                else if (usedMoveSinceResetAttr) PokecubeAttributes.resetToEntry(this.pokemob);
                    // Finally otherwise apply the healing
                else if (PokecubeCore.getConfig().outOfCombatHealing)
                {
                    float health = this.pokemob.getHealth();
                    final float max = this.pokemob.getMaxHealth();
                    if (health < max && health > 0)
                    {
                        health = Math.min(max, health + max / 16);
                        this.pokemob.setHealth(health);
                    }
                }
            }
        }
        /*
         * Angry pokemobs shouldn't decide that walking is better than flying.
         */
        else
        {
            this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
            // Much longer cooldown if actually, really in combat
            this.combatTimer = -50;

            // Ensure we are not sleeping state while in combat
            if (sleepingAI) this.pokemob.setLogicState(LogicStates.SLEEPING, sleepingAI = false);
        }

        this.inCombat = angry;
        this.pokemob.tickBreedDelay(PokecubeCore.getConfig().mateMultiplier);

        // Reset tamed state for things with no owner.
        if (ownerID == null && this.pokemob.getGeneralState(GeneralStates.TAMED))
            this.pokemob.setGeneralState(GeneralStates.TAMED, false);

        // Check exit cube state.
        if (this.entity.tickCount > LogicMiscUpdate.EXITCUBEDURATION && exitingCube)
            this.pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
        boolean noMotion = !TaskBase.canMove(this.pokemob);
        boolean sitting = this.pokemob.getLogicState(LogicStates.SITTING);
        noMotion |= sitting;

        // Ensure sitting things don't have a path.
        if (sitting && !this.entity.getNavigation().isDone())
        {
            this.entity.getNavigation().stop();
        }

        if (noMotion)
        {
            Vec3 v = entity.getDeltaMovement();
            entity.setZza(0);
            entity.setXxa(0);
            entity.setYya(0);
            entity.setDeltaMovement(v.x * 0.5f, v.y, v.z * 0.5f);
        }

        // Check if we shouldn't just randomly go to sleep.
        final boolean ownedSleepCheck =
                sleepingAI && this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob.getGeneralState(
                        GeneralStates.STAYING);
        if (ownedSleepCheck) this.pokemob.setLogicState(LogicStates.SLEEPING, false);

        // Ensure sitting status is synced for TameableEntities
        if (this.entity instanceof TamableAnimal animal)
        {
            final boolean tameSitting = animal.isOrderedToSit();
            if (tameSitting != sitting) this.pokemob.setLogicState(LogicStates.SITTING, tameSitting);
        }

        // Check egg guarding only once per second
        if (complexTick && entity.getBrain().hasMemoryValue(MemoryModules.EGG.get()))
        {
            boolean guardingEgg = pokemob.getGeneralState(GeneralStates.GUARDEGG);
            Optional<EntityPokemobEgg> eggOpt = entity.getBrain().getMemory(MemoryModules.EGG.get());
            boolean shouldGuard = eggOpt.isPresent() && eggOpt.get().isAlive();
            if (guardingEgg != shouldGuard) pokemob.setGeneralState(GeneralStates.GUARDEGG, shouldGuard);
        }

        if (!pokemob.getMoveStats().isExecutingMoves()) pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
    }

    private void checkEvolution(boolean evolving)
    {
        if (ItemList.is(ICanEvolve.EVERSTONE, this.pokemob.getHeldItem()))
        {
            if (evolving)
            {
                this.pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                this.pokemob.setEvolutionTicks(-1);
                evolving = false;
            }
            this.pokemob.setTraded(false);
        }
        final int evo_ticks = this.pokemob.getEvolutionTicks();
        if (evo_ticks > 0) this.pokemob.setEvolutionTicks(evo_ticks - 1);
        if (!this.checkedEvol && this.pokemob.traded())
        {
            this.pokemob.evolve(true, this.pokemob.getHeldItem());
            this.checkedEvol = true;
            return;
        }
        if (evolving)
        {
            if (ItemList.is(ICanEvolve.EVERSTONE, this.pokemob.getEvolutionStack()))
            {
                return;
            }
            if (evo_ticks <= 0)
            {
                this.pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                this.pokemob.setEvolutionTicks(-1);
            }
            if (evo_ticks <= 50)
            {
                this.pokemob.evolve(false, this.pokemob.getEvolutionStack());
                this.pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                this.pokemob.setEvolutionTicks(-1);
            }
        }
    }

    private void checkInventory(final Level world)
    {
        for (int i = 0; i < this.pokemob.getInventory().getContainerSize(); i++)
        {
            ItemStack stack;
            if (!(stack = this.pokemob.getInventory().getItem(i)).isEmpty())
                stack.getItem().inventoryTick(stack, world, this.entity, i, false);
        }
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);

        PokedexEntry entry = this.pokemob.getPokedexEntry();
        Random rand = new Random(this.pokemob.getRNGValue());
        final int timer = 100;

        // Validate status if the mob trackers first, this applies server and
        // client side
        final UUID uuid = this.pokemob.getEntity().getUUID();
        final UUID ownerID = this.pokemob.getOwnerId();
        final MobEntry mobEntry = PokemobTracker.getMobEntry(uuid, world);

        boolean shouldUpdate = mobEntry == null;
        shouldUpdate = shouldUpdate || this.prevOwner == null && ownerID != null;
        shouldUpdate = shouldUpdate || this.prevOwner != null && !this.prevOwner.equals(ownerID);
        shouldUpdate = shouldUpdate || mobEntry.pokemob != this.pokemob;
        shouldUpdate = shouldUpdate || !uuid.equals(this.prevID);

        shouldUpdate = shouldUpdate && this.pokemob.getEntity().isAddedToLevel();

        if (shouldUpdate)
        {
            if (mobEntry != null) PokemobTracker.removeMobEntry(mobEntry.getUUID(), world);
            if (!uuid.equals(this.prevID) && this.prevID != null) PokemobTracker.removeMobEntry(this.prevID, world);
            PokemobTracker.addPokemob(this.pokemob);
        }
        this.prevOwner = ownerID;
        this.prevID = uuid;

        if (this.entity.onGround()) this.floatTimer = 0;
        else this.floatTimer++;

        usingMoveThisTick = this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE);
        this.usedMoveSinceResetAttr |= usingMoveThisTick;

        boolean evolving = this.pokemob.getGeneralState(GeneralStates.EVOLVING);
        this.exitingCube = this.pokemob.getGeneralState(GeneralStates.EXITINGCUBE);

        // Now some server only processing
        if (!world.isClientSide)
        {
            complexTick = entity.tickCount % 20 == Math.abs(entity.getId()) / 20;

            // Check that AI states are correct
            this.checkAIStates(ownerID);
            // Check evolution
            this.checkEvolution(evolving);
            // Check and tick inventory
            this.checkInventory(world);

            // // Ensure the cache position is kept updated
            if (this.cacheTimer++ % timer == rand.nextInt(timer) && this.pokemob.isPlayerOwned()
                    && this.pokemob.getOwnerId() != null)
            {
                final BlockPos here = this.entity.blockPosition();
                if (here.distSqr(this.lastCache) > 64 * 64)
                {
                    this.lastCache = here;
                    PlayerPokemobCache.UpdateCache(this.pokemob);
                }
            }

            // Randomly increase happiness for being outside of pokecube.
            if (Math.random() > 0.999 && this.pokemob.getGeneralState(GeneralStates.TAMED))
                HappinessType.applyHappiness(this.pokemob, HappinessType.TIME);

            final ItemStack pokecube = this.pokemob.getPokecube();
            final ResourceLocation id = PokecubeItems.getCubeId(pokecube);
            final PokecubeBehaviour behaviour = IPokecube.PokecubeBehaviour.BEHAVIORS.get(id);
            if (behaviour != null) behaviour.onUpdate(this.pokemob);
        }

        if (this.entity.tickCount % 20 == 0)
        {
            for (int i = 0; i < 5; i++) this.flavourAmounts[i] = this.pokemob.getFlavourAmount(i);
            for (int i = 0; i < this.flavourAmounts.length; i++)
                if (this.flavourAmounts[i] > 0) this.pokemob.setFlavourAmount(i, this.flavourAmounts[i] - 1);
        }

        // Ensure our pose matches what we are doing
        this.checkPose();
        // This is used server side as well, for hitbox positions.
        this.checkAnimationStates(entry);

        if (holder != null)
        {
            // Update molang things for stuff that is slow to read.
            holder.updateTickVariables(this.entity);
        }

        // end of server side logic here.
        if (this.entity.level() instanceof ServerLevel)
        {
            return;
        }

        var effects = new ArrayList<>(entity.getActiveEffects());
        for (var e : effects)
        {
            if (e.getDuration() == 0)
            {
                entity.removeEffect(e.getEffect());
            }
        }
        var pokemobContext = new PokemobContext(pokemob);
        // Particle stuff below here
        if (this.entity.tickCount % 20 == 0)
        {
            // Shadow mob effect
            if (this.pokemob.isShadow())
            {
                var effect_key = "pokemob.shadow";
                var effectFunction = ParticleEffects.getEffect(effect_key);
                var applied = effectFunction.get();
                var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(
                        pokemobContext);
                effect.onClientTick = effect.onClientTick.andThen(
                        info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
                ParticleEffects.ADD_FOR_RENDER.accept(effect);
            }
            // Holiday effect
            if (LogicMiscUpdate.holiday)
            {
                var effect_key = "pokemob.holiday";
                var effectFunction = ParticleEffects.getEffect(effect_key);
                var applied = effectFunction.get();
                var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(
                        pokemobContext);
                effect.onClientTick = effect.onClientTick.andThen(
                        info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
                ParticleEffects.ADD_FOR_RENDER.accept(effect);
            }
            // flavour effects
            boolean anyFlav = false;
            for (final int var : this.flavourAmounts)
            {
                if (var > 0)
                {
                    anyFlav = true;
                    break;
                }
            }
            if (anyFlav)
            {
                var effect_key = "pokemob.flavour";
                var effectFunction = ParticleEffects.getEffect(effect_key);
                var applied = effectFunction.get();
                var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(
                        pokemobContext);
                effect.onClientTick = effect.onClientTick.andThen(
                        info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
                ParticleEffects.ADD_FOR_RENDER.accept(effect);
            }
        }
        if (this.entity.tickCount % 10 == 0 && this.pokemob.getGeneralState(GeneralStates.MATING))
        {
            var effect_key = "pokemob.mating";
            var effectFunction = ParticleEffects.getEffect(effect_key);
            var applied = effectFunction.get();
            var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(pokemobContext);
            effect.onClientTick = effect.onClientTick.andThen(
                    info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
            ParticleEffects.ADD_FOR_RENDER.accept(effect);
        }

        // Evolution effects
        if (evolving && evo_effect == null)
        {
            var effect_key = "pokemob.evolution";
            var effectFunction = ParticleEffects.getEffect(effect_key);
            var applied = effectFunction.get();
            evo_effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(pokemobContext);
            evo_effect.onClientTick = evo_effect.onClientTick.andThen(
                    info -> info.animation.effect().setDuration(PokecubeCore.getConfig().evolutionTicks));
            evo_effect.onClientEnd = evo_effect.onClientEnd.andThen(info -> {
                if (info.isFinished()) this.evo_effect = null;
            });
        }
        // Exiting cube and pokeseal effects
        if (exitingCube && cube_effect == null)
        {
            // First the regular exit cube effect
            var effect_key = "pokecube.exit_cube";
            var effectFunction = ParticleEffects.getEffect(effect_key);
            var applied = effectFunction.get();
            cube_effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(pokemobContext);
            cube_effect.onClientTick = cube_effect.onClientTick.andThen(
                    info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
            ParticleEffects.ADD_FOR_RENDER.accept(cube_effect);
            // Now additional effects, starting with "Shiny"
            if (pokemob.isShiny())
            {
                effect_key = "pokecube.shiny";
                effectFunction = ParticleEffects.getEffect(effect_key);
                applied = effectFunction.get();
                var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(
                        pokemobContext);
                effect.onClientTick = effect.onClientTick.andThen(
                        info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
                ParticleEffects.ADD_FOR_RENDER.accept(effect);
            }
            var seal = pokemob.getPokecube().get(PokemobCaps.POKESEAL_DATA);
            var tag = seal != null ? seal.tag() : new CompoundTag();
            // Then process the pokeseal data
            if (seal != null && !seal.tag().isEmpty())
            {
                var context = new NBTContext(tag);
                for (String key : seal.tag().getAllKeys())
                {
                    effect_key = RecipePokeseals.POKESEAL_EFFECT_NAMES.get(key);
                    if (effect_key == null) continue;
                    effectFunction = ParticleEffects.getEffect(effect_key);
                    applied = effectFunction.get();
                    var effect = new EffectPacketInfo(applied, entity, ParticleEffects.EVO_ANCHORS).addContext(context)
                            .addContext(pokemobContext);
                    effect.onClientTick = effect.onClientTick.andThen(
                            info -> info.animation.effect().setDuration(PokecubeCore.getConfig().exitCubeDuration));
                    ParticleEffects.ADD_FOR_RENDER.accept(effect);
                }
            }
        }
    }

    private void checkPose()
    {
        final Pose old = this.entity.getPose();
        final boolean sleeping = this.pokemob.getLogicState(LogicStates.SLEEPING);
        Pose next;
        if (this.entity.deathTime > 0 || this.entity.isDeadOrDying()) next = Pose.DYING;
        else if (sleeping) next = Pose.SLEEPING;
        else if (this.entity.isInWater() || this.entity.isInLava()) next = Pose.SWIMMING;
        else if (this.floatTimer < 20) next = Pose.STANDING;
        else next = Pose.FALL_FLYING;
        if (next != old) entity.setPose(next);
    }

    private void addAnimation(List<String> anims, String key, boolean isRidden)
    {
        if (isRidden)
        {
            String ridden = "ridden_" + key;
            if (!anims.contains(ridden)) anims.add(ridden);
        }
        if (!anims.contains(key)) anims.add(key);
    }

    private void checkAnimationStates(PokedexEntry entry)
    {
        if (animated == null) return;
        animated.clearChoices();
        List<String> anims = animated.getChoices();
        List<String> transients = animated.transientAnimations();
        var trackedEntity = pokemob.getTrackedEntity();
        var tracker = ThutCaps.getPositionTracker(trackedEntity);
        boolean isRidden = !entity.getPassengers().isEmpty();
        var velocity = tracker.getVelocity();
        float walkspeed = (float) (velocity.x * velocity.x + velocity.z * velocity.z);
        boolean onGround = entity.onGround();

        // Server side less often computation of molangs for body animation and positioning
        if (entry.bodyModel != null)
        {
            if (liveHolder == null)
                liveHolder = this.entity instanceof IBBPartMultipart<?, ?> poke ? poke.getAnimationHolder() : holder;
            liveHolder.initFromEntity(this.entity);
        }

        if (onGround)
        {
            // This includes bouncing up/down for flying mobs, so we only want to account for it when walking.
            walkspeed = entity.walkAnimation.speed();
            walkspeed *= walkspeed;
        }
        float stationary = 5e-4f;
        boolean moving = walkspeed > stationary;
        Pose pose = this.entity.getPose();
        if (pose == Pose.STANDING && !onGround)
        {
            pose = Pose.FALL_FLYING;
        }
        boolean walking = this.floatTimer < 20 && moving;
        boolean noBlink = false;
        boolean guarding = pokemob.getCombatState(CombatStates.GUARDING);
        if (pose == Pose.DYING || entity.deathTime > 0)
        {
            addAnimation(anims, "dead", isRidden);
            noBlink = true;
        }
        if (trackedEntity.getVehicle() != null) addAnimation(anims, "sitting", isRidden);
        for (final LogicStates state : LogicStates.values())
        {
            if (this.pokemob.getLogicState(state)) addAnimation(anims, state.getName(), isRidden);
        }
        switch (pose)
        {
        case FALL_FLYING:
            if (!moving) addAnimation(anims, "floating", isRidden);
            addAnimation(anims, "flying", isRidden);
            if (moving) addAnimation(anims, "floating", isRidden);
            break;
        case SLEEPING:
            noBlink = true;
            addAnimation(anims, "sleeping", isRidden);
            break;
        case SWIMMING:
            if (!moving) addAnimation(anims, "in_water", isRidden);
            addAnimation(anims, "swimming", isRidden);
            if (moving) addAnimation(anims, "in_water", isRidden);
            break;
        default:
            break;
        }
        if (this.entity.isSprinting())
        {
            if (guarding) addAnimation(anims, "guarding_sprinting", isRidden);
            addAnimation(anims, "sprinting", isRidden);
        }
        if (walking)
        {
            if (guarding) addAnimation(anims, "guarding_walking", isRidden);
            addAnimation(anims, "walking", isRidden);
        }
        for (final CombatStates state : CombatStates.values())
        {
            if (this.pokemob.getCombatState(state)) addAnimation(anims, state.getName(), isRidden);
        }
        for (final GeneralStates state : GeneralStates.values())
        {
            if (this.pokemob.getGeneralState(state)) addAnimation(anims, state.getName(), isRidden);
        }

        // Add in some transients which might occur
        float blink_rate = 0.5f;
        if (!noBlink && entity.tickCount % 40 == 0 && entity.getRandom().nextFloat() < blink_rate)
        {
            addAnimation(transients, "blink", false);
        }
        if (usingMoveThisTick)
        {
            MoveEntry move = this.pokemob.getSelectedMove();
            {
                if (move != null) addAnimation(transients, "attack_" + move.name, isRidden);
                if (move.getAttackCategory(pokemob) == ContactCategory.CONTACT)
                    addAnimation(transients, "attack_contact", isRidden);
                if (move.getAttackCategory(pokemob) == ContactCategory.RANGED)
                    addAnimation(transients, "attack_ranged", isRidden);
                if (move.getCategory(pokemob) == AttackCategory.STATUS)
                    addAnimation(transients, "attack_status", isRidden);
                if (move.getCategory(pokemob) == AttackCategory.OTHER)
                    addAnimation(transients, "attack_other", isRidden);
            }
        }

        if (this.pokemob.inCombat())
        {
            addAnimation(anims, "battling", isRidden);
        }
        if (isRidden) addAnimation(anims, "idle", isRidden);
//        anims.addFirst("flying"); // Debug comment out to test specific animations server/client side

        PokecubeAPI.POKEMOB_BUS.post(new AnimationSelectionEvent(pokemob, animated));
    }

    @Override
    public boolean shouldRun()
    {
        // The base class only runs if the mob is not dead, we need to run while
        // dead to also handle animation setting.
        return true;
    }
}
