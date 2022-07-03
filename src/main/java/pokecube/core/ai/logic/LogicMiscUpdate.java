package pokecube.core.ai.logic;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.stats.IStatsModifiers;
import pokecube.core.interfaces.pokemob.stats.StatModifiers;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketSyncModifier;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.PokemobTracker.MobEntry;
import thut.api.AnimatedCaps;
import thut.api.Tracker;
import thut.api.entity.IAnimated;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

/**
 * Mostly does visuals updates, such as particle effects, checking that shearing
 * status is reset properly. It also resets stat modifiers when the mob is out
 * of combat.
 */
public class LogicMiscUpdate extends LogicBase
{
    public static final int[] FLAVCOLOURS = new int[]
    { 0xFFFF4932, 0xFF4475ED, 0xFFF95B86, 0xFF2EBC63, 0xFFEBCE36 };

    public static int EXITCUBEDURATION = 40;

    public static final boolean holiday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 25
            && Calendar.getInstance().get(Calendar.MONTH) == 11;

    public static void getStatModifiers(final EquipmentSlot slot, final ItemStack stack, final Map<Stats, Float> vals)
    {
        if (stack.isEmpty()) return;
        switch (slot)
        {
        case CHEST:
            break;
        case FEET:
            break;
        case HEAD:
            break;
        case LEGS:
            break;
        case MAINHAND:
            break;
        case OFFHAND:
            break;
        default:
            break;
        }
    }

    private final int[] flavourAmounts = new int[5];

    private PokedexEntry entry;

    private String particle = null;
    private boolean initHome = false;
    private boolean checkedEvol = false;
    private long dynatime = -1;
    private boolean de_dyna = false;

    private int floatTimer = 0;

    private IStatsModifiers mods;

    boolean inCombat = false;

    int combatTimer = 0;

    private int cacheTimer = 0;

    BlockPos lastCache = null;

    Vector3 v = new Vector3();

    UUID prevOwner = null;

    UUID prevID = null;

    public LogicMiscUpdate(final IPokemob pokemob)
    {
        super(pokemob);
        this.lastCache = this.entity.blockPosition();
    }

    private void checkAIStates()
    {
        final boolean angry = this.pokemob.inCombat();

        boolean gigant = pokemob.getCombatState(CombatStates.GIGANTAMAX);
        boolean isDyna = pokemob.getCombatState(CombatStates.DYNAMAX);

        // check dynamax timer for cooldown.
        if (gigant || isDyna)
        {
            final long time = Tracker.instance().getTick();
            if (this.dynatime == -1)
                this.dynatime = this.pokemob.getEntity().getPersistentData().getLong("pokecube:dynatime");
            if (!this.de_dyna && time - PokecubeCore.getConfig().dynamax_duration > this.dynatime)
            {
                Component mess = new TranslatableComponent("pokemob.dynamax.timeout.revert",
                        this.pokemob.getDisplayName());
                this.pokemob.displayMessageToOwner(mess);
                
                final PokedexEntry newEntry = this.pokemob.getMegaBase();
                if (newEntry != this.pokemob.getPokedexEntry())
                    ICanEvolve.setDelayedMegaEvolve(this.pokemob, newEntry, mess, true);
                
                pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = new TranslatableComponent("pokemob.dynamax.revert", this.pokemob.getDisplayName());
                ICanEvolve.setDelayedMegaEvolve(this.pokemob, newEntry, mess, true);
                
                this.de_dyna = true;
            }
        }
        else
        {
            this.dynatime = -1;
            this.de_dyna = false;
        }

        if (this.pokemob.getGeneralState(GeneralStates.MATING) && !BrainUtils.hasMateTarget((AgeableMob) this.entity))
            this.pokemob.setGeneralState(GeneralStates.MATING, false);

        // Check if we are sheared every second or so
        if (this.entity.tickCount % 20 == 0) this.pokemob.isSheared();

        // If angry and has no target, make it not angry.

        // If not angry, and not been so for a while, reset stat modifiers.
        if (!angry)
        {
            final boolean resetCombat = this.combatTimer == 0;
            if (resetCombat)
            {
                this.pokemob.getModifiers().outOfCombatReset();
                this.pokemob.getMoveStats().reset();
                this.pokemob.setCombatState(CombatStates.NOITEMUSE, false);
                if (this.pokemob.getOwner() instanceof ServerPlayer)
                    PacketSyncModifier.sendUpdate(StatModifiers.DEFAULT, this.pokemob);
            }
            this.combatTimer--;

            if (this.combatTimer < -50 && this.combatTimer % 100 == 0 && PokecubeCore.getConfig().outOfCombatHealing)
            {
                float health = this.pokemob.getHealth();
                final float max = this.pokemob.getMaxHealth();
                if (health < max)
                {
                    health = Math.min(max, health + max / 16);
                    this.pokemob.setHealth(health);
                }
            }
        }
        /**
         * Angry pokemobs shouldn't decide that walking is better than flying.
         */
        if (angry)
        {
            this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);
            // Much longer cooldown if actually, really in combat
            this.combatTimer = 50;
        }

        boolean noMotion = this.pokemob.getLogicState(LogicStates.SLEEPING);
        boolean sitting = this.pokemob.getLogicState(LogicStates.SITTING);
        noMotion |= sitting;

        this.inCombat = angry;
        this.pokemob.tickBreedDelay(PokecubeCore.getConfig().mateMultiplier);

        // Reset tamed state for things with no owner.
        if (this.pokemob.getGeneralState(GeneralStates.TAMED) && this.pokemob.getOwnerId() == null)
            this.pokemob.setGeneralState(GeneralStates.TAMED, false);

        // Check exit cube state.
        if (this.entity.tickCount > LogicMiscUpdate.EXITCUBEDURATION
                && this.pokemob.getGeneralState(GeneralStates.EXITINGCUBE))
            this.pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);

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
        final boolean ownedSleepCheck = this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
        if (ownedSleepCheck) this.pokemob.setLogicState(LogicStates.SLEEPING, false);

        // Ensure sitting status is synced for TameableEntities
        if (this.entity instanceof TamableAnimal)
        {
            final boolean tameSitting = ((TamableAnimal) this.entity).isOrderedToSit();
            this.pokemob.setLogicState(LogicStates.SITTING, tameSitting);
        }
    }

    private void checkEvolution()
    {
        boolean evolving = this.pokemob.getGeneralState(GeneralStates.EVOLVING);
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
        final int num = this.pokemob.getEvolutionTicks();
        if (num > 0) this.pokemob.setEvolutionTicks(this.pokemob.getEvolutionTicks() - 1);
        if (!this.checkedEvol && this.pokemob.traded())
        {
            this.pokemob.evolve(true, false, this.pokemob.getHeldItem());
            this.checkedEvol = true;
            return;
        }
        if (evolving)
        {
            if (num <= 0)
            {
                this.pokemob.setGeneralState(GeneralStates.EVOLVING, false);
                this.pokemob.setEvolutionTicks(-1);
            }
            if (num <= 50)
            {
                this.pokemob.evolve(false, false, this.pokemob.getEvolutionStack());
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

        this.entry = this.pokemob.getPokedexEntry();
        Random rand = new Random(this.pokemob.getRNGValue());
        final int timer = 100;

        // Validate status if the mob trackers first, this applies server and
        // client side
        final UUID uuid = this.pokemob.getEntity().getUUID();
        final UUID ownerID = this.pokemob.getOwnerId();
        final MobEntry entry = PokemobTracker.getMobEntry(uuid, world);

        boolean shouldUpdate = entry == null;
        shouldUpdate = shouldUpdate || this.prevOwner == null && ownerID != null;
        shouldUpdate = shouldUpdate || this.prevOwner != null && !this.prevOwner.equals(ownerID);
        shouldUpdate = shouldUpdate || entry.pokemob != this.pokemob;
        shouldUpdate = shouldUpdate || !uuid.equals(this.prevID);

        shouldUpdate = shouldUpdate && this.pokemob.getEntity().isAddedToWorld();

        if (shouldUpdate)
        {
            if (entry != null) PokemobTracker.removeMobEntry(entry.getUUID(), world);
            if (!uuid.equals(this.prevID) && this.prevID != null) PokemobTracker.removeMobEntry(this.prevID, world);
            PokemobTracker.addPokemob(this.pokemob);
        }
        this.prevOwner = ownerID;
        this.prevID = uuid;

        // Here we apply worn/held equipment modifiers
        final Map<Stats, Float> vals = Maps.newHashMap();
        for (final EquipmentSlot type : EquipmentSlot.values())
            LogicMiscUpdate.getStatModifiers(type, this.entity.getItemBySlot(type), vals);
        if (this.mods == null) this.mods = this.pokemob.getModifiers().getModifiers(StatModifiers.ARMOUR);
        for (final Stats stat : Stats.values())
        {
            final Float val = vals.getOrDefault(stat, (float) 0);
            this.mods.setModifier(stat, val);
        }

        if (this.entity.isOnGround()) this.floatTimer = 0;
        else this.floatTimer++;

        // Now some server only processing
        if (!world.isClientSide)
        {
            // Check that AI states are correct
            this.checkAIStates();
            // Check evolution
            this.checkEvolution();
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
            final PokecubeBehavior behaviour = IPokecube.BEHAVIORS.getValue(id);
            if (behaviour != null) behaviour.onUpdate(this.pokemob);
        }

        for (int i = 0; i < 5; i++) this.flavourAmounts[i] = this.pokemob.getFlavourAmount(i);
        for (int i = 0; i < this.flavourAmounts.length; i++)
            if (this.flavourAmounts[i] > 0) this.pokemob.setFlavourAmount(i, this.flavourAmounts[i] - 1);

        if (!this.initHome)
        {
            this.initHome = true;
            if (this.pokemob.getHome() != null)
            {
                final BlockEntity te = world.getBlockEntity(this.pokemob.getHome());
                if (te != null && te instanceof NestTile)
                {
                    final NestTile nest = (NestTile) te;
                    nest.addResident(this.pokemob);
                }
            }
        }
        final int id = this.pokemob.getTargetID();

        // Ensure our pose matches what we are doing
        this.checkPose();
        // This is used server side as well, for hitbox positions.
        this.checkAnimationStates();

        final LivingEntity targ = BrainUtils.getAttackTarget(this.entity);
        if (this.entity.getLevel() instanceof ServerLevel)
        {
            if (targ != null && targ.isAlive())
            {
                this.pokemob.setTargetID(targ.getId());
                return;
            }
            this.pokemob.setTargetID(-1);
            return;
        }

        // Everything below here is client side only!

        if (id >= 0 && targ == null)
            this.entity.setTarget((LivingEntity) PokecubeCore.getEntityProvider().getEntity(world, id, false));
        if (id < 0 && targ != null) this.entity.setTarget(null);
        if (targ != null && !targ.isAlive()) this.entity.setTarget(null);

        // Particle stuff below here, WARNING, RESETTING RNG HERE
        rand = ThutCore.newRandom();
        final Vector3 particleLoc = new Vector3().set(this.entity);
        boolean randomV = false;
        final Vector3 particleVelo = new Vector3();
        boolean pokedex = false;
        int particleIntensity = 100;
        if (this.pokemob.isShadow()) this.particle = "portal";
        particles:
        if (this.particle == null && this.entry.particleData != null)
        {
            pokedex = true;
            final double intensity = Double.parseDouble(this.entry.particleData[1]);
            int val = (int) intensity;
            if (intensity < 1) if (rand.nextDouble() <= intensity) val = 1;
            if (val == 0) break particles;
            this.particle = this.entry.particleData[0];
            particleIntensity = val;
            if (this.entry.particleData.length > 2)
            {
                final String[] args = this.entry.particleData[2].split(",");
                double dx = 0, dy = 0, dz = 0;
                if (args.length == 1) dy = Double.parseDouble(args[0]) * this.entity.getBbHeight();
                else
                {
                    dx = Double.parseDouble(args[0]);
                    dy = Double.parseDouble(args[1]);
                    dz = Double.parseDouble(args[2]);
                }
                particleLoc.addTo(dx, dy, dz);
            }
            if (this.entry.particleData.length > 3)
            {
                final String[] args = this.entry.particleData[3].split(",");
                double dx = 0, dy = 0, dz = 0;
                if (args.length == 1) switch (args[0])
                {
                case "r":
                    randomV = true;
                    break;
                case "v":
                    particleVelo.setToVelocity(this.entity);
                    break;
                default:
                    break;
                }
                else
                {
                    dx = Double.parseDouble(args[0]);
                    dy = Double.parseDouble(args[1]);
                    dz = Double.parseDouble(args[2]);
                    particleVelo.set(dx, dy, dz);
                }
            }
        }
        if (LogicMiscUpdate.holiday)
        {
            this.particle = "aurora";// Merry Xmas
            particleIntensity = 10;
        }
        if (this.pokemob.getGeneralState(GeneralStates.MATING) && this.entity.tickCount % 10 == 0)
        {
            final Vector3 heart = new Vector3();
            for (int i = 0; i < 3; ++i)
            {
                heart.set(
                        this.entity.getX() + rand.nextFloat() * this.entity.getBbWidth() * 2.0F
                                - this.entity.getBbWidth(),
                        this.entity.getY() + 0.5D + rand.nextFloat() * this.entity.getBbHeight(), this.entity.getZ()
                                + rand.nextFloat() * this.entity.getBbWidth() * 2.0F - this.entity.getBbWidth());
                this.entity.getLevel().addParticle(ParticleTypes.HEART, heart.x, heart.y, heart.z, 0, 0, 0);
            }
        }
        int[] args = {};
        if (this.particle != null && rand.nextInt(100) < particleIntensity)
        {
            if (!pokedex)
            {
                final float scale = this.entity.getBbWidth() * 2;
                final Vector3 offset = new Vector3().set(rand.nextDouble() - 0.5, rand.nextDouble(),
                        rand.nextDouble() - 0.5);
                offset.scalarMultBy(scale);
                particleLoc.addTo(offset);
            }
            if (randomV)
            {
                particleVelo.set(rand.nextDouble() - 0.5, rand.nextDouble() + this.entity.getBbHeight() / 2,
                        rand.nextDouble() - 0.5);
                particleVelo.scalarMultBy(0.25);
            }
            PokecubeCore.spawnParticle(this.entity.getLevel(), this.particle, particleLoc, particleVelo, args);
        }
        for (int i = 0; i < this.flavourAmounts.length; i++)
        {
            final int var = this.flavourAmounts[i];
            particleIntensity = var;
            if (var > 0 && rand.nextInt(100) < particleIntensity)
            {
                if (!pokedex)
                {
                    final float scale = this.entity.getBbWidth() * 2;
                    final Vector3 offset = new Vector3().set(rand.nextDouble() - 0.5, rand.nextDouble(),
                            rand.nextDouble() - 0.5);
                    offset.scalarMultBy(scale);
                    particleLoc.addTo(offset);
                }
                if (randomV)
                {
                    particleVelo.set(rand.nextDouble() - 0.5, rand.nextDouble() + this.entity.getBbHeight() / 2,
                            rand.nextDouble() - 0.5);
                    particleVelo.scalarMultBy(0.25);
                }
                args = new int[]
                { LogicMiscUpdate.FLAVCOLOURS[i] };
                this.particle = "powder";
                PokecubeCore.spawnParticle(this.entity.getLevel(), this.particle, particleLoc, particleVelo, args);
            }
        }
        this.particle = null;
    }

    private void checkPose()
    {
        final Pose old = this.entity.getPose();
        final boolean sleeping = this.pokemob.getStatus() == IMoveConstants.STATUS_SLP
                || this.pokemob.getLogicState(LogicStates.SLEEPING);
        Pose next = old;
        if (this.entity.deathTime > 0 || this.entity.isDeadOrDying()) next = Pose.DYING;
        else if (sleeping) next = Pose.SLEEPING;
        else if (this.entity.isInWater() || this.entity.isInLava()) next = Pose.SWIMMING;
        else if (this.floatTimer < 5) next = Pose.STANDING;
        else next = Pose.FALL_FLYING;
        if (next != old) this.entity.setPose(next);
    }

    private void checkAnimationStates()
    {
        final IAnimated holder = AnimatedCaps.getAnimated(this.entity);
        if (holder == null) return;
        final List<String> anims = holder.getChoices();
        anims.clear();
        final Vec3 velocity = this.entity.getDeltaMovement();
        final float dStep = this.entity.animationSpeed - this.entity.animationSpeedOld;
        final float walkspeed = (float) (velocity.x * velocity.x + velocity.z * velocity.z + dStep * dStep);
        final float stationary = 1e-5f;
        final boolean moving = walkspeed > stationary;
        final Pose pose = this.entity.getPose();
        final boolean walking = this.floatTimer < 5 && moving;
        if (pose == Pose.DYING) anims.add("dead");
        if (this.pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
        {
            final int index = this.pokemob.getMoveIndex();
            Move_Base move;
            if (index < 4 && (move = MovesUtils.getMoveFromName(this.pokemob.getMove(index))) != null)
            {
                if ((move.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0) anims.add("attack_contact");
                if ((move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0) anims.add("attack_ranged");
            }
        }
        for (final LogicStates state : LogicStates.values())
        {
            final String anim = ThutCore.trim(state.toString());
            if (this.pokemob.getLogicState(state)) anims.add(anim);
        }
        if (walking) anims.add("walking");
        switch (pose)
        {
        case DYING:
            break;
        case CROUCHING:
            break;
        case FALL_FLYING:
            if (!moving) anims.add("floating");
            anims.add("flying");
            break;
        case SLEEPING:
            anims.add("sleeping");
            break;
        case SPIN_ATTACK:
            break;
        case STANDING:
            break;
        case SWIMMING:
            anims.add("swimming");
            break;
        default:
            break;
        }
        for (final CombatStates state : CombatStates.values())
        {
            final String anim = ThutCore.trim(state.toString());
            if (this.pokemob.getCombatState(state)) anims.add(anim);
        }
    }
}
