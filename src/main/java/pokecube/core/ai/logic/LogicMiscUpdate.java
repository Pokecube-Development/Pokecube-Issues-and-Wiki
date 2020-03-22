package pokecube.core.ai.logic;

import java.util.Calendar;
import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;

/**
 * Mostly does visuals updates, such as particle effects, checking that
 * shearing status is reset properly. It also resets stat modifiers when the mob
 * is out of combat.
 */
public class LogicMiscUpdate extends LogicBase
{
    public static final int[] FLAVCOLOURS      = new int[] { 0xFFFF4932, 0xFF4475ED, 0xFFF95B86, 0xFF2EBC63,
            0xFFEBCE36 };
    public static int         EXITCUBEDURATION = 40;

    public static final boolean holiday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 25 && Calendar
            .getInstance().get(Calendar.MONTH) == 11;

    private int          lastHadTargetTime = 0;
    private final int[]  flavourAmounts    = new int[5];
    private PokedexEntry entry;
    private String       particle          = null;
    private boolean      reset             = false;
    private boolean      initHome          = false;
    private boolean      checkedEvol       = false;
    private int          pathTimer         = 0;
    private long         dynatime          = -1;
    private boolean      de_dyna           = false;
    Vector3              v                 = Vector3.getNewVector();

    public LogicMiscUpdate(final IPokemob entity)
    {
        super(entity);
        // Initialize this at 20 ticks to prevent resetting any states set by
        // say exiting pokecubes.
        this.lastHadTargetTime = 20;
    }

    private void checkAIStates()
    {
        final boolean angry = this.pokemob.getCombatState(CombatStates.ANGRY);

        // check dynamax timer for cooldown.
        if (this.pokemob.getCombatState(CombatStates.DYNAMAX))
        {
            final Long time = this.pokemob.getEntity().getServer().getWorld(DimensionType.OVERWORLD).getGameTime();
            if (this.dynatime == -1) this.dynatime = this.pokemob.getEntity().getPersistentData().getLong(
                    "pokecube:dynatime");
            if (!this.de_dyna && time - PokecubeCore.getConfig().dynamax_duration > this.dynatime)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.timeout.revert", "green",
                        this.pokemob.getDisplayName());
                this.pokemob.displayMessageToOwner(mess);
                this.pokemob.setCombatState(CombatStates.MEGAFORME, false);
                mess = CommandTools.makeTranslatedMessage("pokemob.dynamax.revert", "green", this.pokemob
                        .getDisplayName());
                final PokedexEntry newEntry = this.entry.getBaseForme() != null ? this.entry.getBaseForme()
                        : this.entry;
                ICanEvolve.setDelayedMegaEvolve(this.pokemob, newEntry, mess, true);
                this.de_dyna = true;
            }
        }
        else
        {
            this.dynatime = -1;
            this.de_dyna = false;
        }

        // If angry and has no target, make it not angry.
        if (angry && this.lastHadTargetTime-- <= 0) this.pokemob.setCombatState(CombatStates.ANGRY, false);
        else if (angry && this.entity.getAttackTarget() != null)
        {
            this.lastHadTargetTime = 100;
            this.reset = false;
        }
        else if (!angry && this.reset)
        {
            this.lastHadTargetTime = 100;
            this.reset = false;
        }

        if (this.pokemob.getSexe() != IPokemob.MALE)
        {
            int diff = 1 * PokecubeCore.getConfig().mateMultiplier;
            if (this.pokemob.getLoveTimer() > 0) diff = 1;
            this.pokemob.setLoveTimer(this.pokemob.getLoveTimer() + diff);
        }

        // If not angry, and not been so for a while, reset stat modifiers.
        if (!angry)
        {
            if (this.lastHadTargetTime <= 0 && !this.reset)
            {
                this.reset = true;
                this.pokemob.getModifiers().outOfCombatReset();
                this.pokemob.getMoveStats().reset();
            }
        }
        else /** Angry pokemobs shouldn't decide to walk around. */
            this.pokemob.setRoutineState(AIRoutine.AIRBORNE, true);

        // Reset tamed state for things with no owner.
        if (this.pokemob.getGeneralState(GeneralStates.TAMED) && this.pokemob.getOwnerId() == null) this.pokemob
                .setGeneralState(GeneralStates.TAMED, false);

        // Ensure cap on love timer.
        if (this.pokemob.getLoveTimer() > 600) this.pokemob.resetLoveStatus();

        // Check exit cube state.
        if (this.entity.ticksExisted > LogicMiscUpdate.EXITCUBEDURATION && this.pokemob.getGeneralState(
                GeneralStates.EXITINGCUBE)) this.pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);

        // Ensure sitting things don't have a path.
        if (this.pokemob.getLogicState(LogicStates.SITTING) && !this.entity.getNavigator().noPath()) this.entity
                .getNavigator().clearPath();

        // Check pathing states.
        if (this.pokemob.getLogicState(LogicStates.PATHING) && this.entity.getNavigator().noPath()
                && this.pathTimer++ > 10)
        {
            this.pokemob.setLogicState(LogicStates.PATHING, false);
            this.pathTimer = 0;
        }

        // Check if we shouldn't just randomly go to sleep.
        final boolean ownedSleepCheck = this.pokemob.getGeneralState(GeneralStates.TAMED) && !this.pokemob
                .getGeneralState(GeneralStates.STAYING);
        if (ownedSleepCheck) this.pokemob.setLogicState(LogicStates.SLEEPING, false);
    }

    private void checkEvolution()
    {
        boolean evolving = this.pokemob.getGeneralState(GeneralStates.EVOLVING);
        if (PokecubeItems.is(ICanEvolve.EVERSTONE, this.pokemob.getHeldItem()))
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

    private void checkInventory(final World world)
    {
        for (int i = 0; i < this.pokemob.getInventory().getSizeInventory(); i++)
        {
            ItemStack stack;
            if (!(stack = this.pokemob.getInventory().getStackInSlot(i)).isEmpty()) stack.getItem().inventoryTick(stack,
                    world, this.entity, i, false);
        }
    }

    @Override
    public void tick(final World world)
    {
        super.tick(world);
        this.entry = this.pokemob.getPokedexEntry();
        Random rand = new Random(this.pokemob.getRNGValue());

        if (!world.isRemote)
        {
            // Check that AI states are correct
            this.checkAIStates();
            // Check evolution
            this.checkEvolution();
            // Check and tick inventory
            this.checkInventory(world);

            // // Ensure the cache position is kept updated
            if (this.entity.ticksExisted % 100 == 0) PlayerPokemobCache.UpdateCache(this.pokemob);

            // Randomly increase happiness for being outside of pokecube.
            if (Math.random() > 0.999 && this.pokemob.getGeneralState(GeneralStates.TAMED)) HappinessType
                    .applyHappiness(this.pokemob, HappinessType.TIME);

            final ItemStack pokecube = this.pokemob.getPokecube();
            final ResourceLocation id = PokecubeItems.getCubeId(pokecube);
            final PokecubeBehavior behaviour = IPokecube.BEHAVIORS.getValue(id);
            if (behaviour != null) behaviour.onUpdate(this.pokemob);
        }

        for (int i = 0; i < 5; i++)
            this.flavourAmounts[i] = this.pokemob.getFlavourAmount(i);
        for (int i = 0; i < this.flavourAmounts.length; i++)
            if (this.flavourAmounts[i] > 0) this.pokemob.setFlavourAmount(i, this.flavourAmounts[i] - 1);

        if (!this.initHome)
        {
            this.initHome = true;
            if (this.pokemob.getHome() != null)
            {
                final TileEntity te = world.getTileEntity(this.pokemob.getHome());
                if (te != null && te instanceof NestTile)
                {
                    final NestTile nest = (NestTile) te;
                    nest.addResident(this.pokemob);
                }
            }
        }
        final int id = this.pokemob.getTargetID();

        if (this.entity.getEntityWorld() instanceof ServerWorld)
        {
            final LivingEntity targ = this.entity.getAttackTarget();
            if (targ != null && targ.isAlive())
            {
                this.pokemob.setTargetID(targ.getEntityId());
                return;
            }
            this.pokemob.setTargetID(-1);
            return;
        }

        // Everything below here is client side only!

        if (id >= 0 && this.entity.getAttackTarget() == null) this.entity.setAttackTarget((LivingEntity) PokecubeCore
                .getEntityProvider().getEntity(world, id, false));
        if (id < 0 && this.entity.getAttackTarget() != null) this.entity.setAttackTarget(null);
        if (this.entity.getAttackTarget() != null && !this.entity.getAttackTarget().isAlive()) this.entity
                .setAttackTarget(null);

        // Particle stuff below here, WARNING, RESETTING RNG HERE
        rand = new Random();
        final Vector3 particleLoc = Vector3.getNewVector().set(this.entity);
        boolean randomV = false;
        final Vector3 particleVelo = Vector3.getNewVector();
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
                if (args.length == 1) dy = Double.parseDouble(args[0]) * this.entity.getHeight();
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
        if (this.pokemob.getGeneralState(GeneralStates.MATING) && this.entity.ticksExisted % 10 == 0)
        {
            final Vector3 heart = Vector3.getNewVector();
            for (int i = 0; i < 3; ++i)
            {
                heart.set(this.entity.posX + rand.nextFloat() * this.entity.getWidth() * 2.0F - this.entity.getWidth(),
                        this.entity.posY + 0.5D + rand.nextFloat() * this.entity.getHeight(), this.entity.posZ + rand
                                .nextFloat() * this.entity.getWidth() * 2.0F - this.entity.getWidth());
                this.entity.getEntityWorld().addParticle(ParticleTypes.HEART, heart.x, heart.y, heart.z, 0, 0, 0);
            }
        }
        int[] args = {};
        if (this.particle != null && rand.nextInt(100) < particleIntensity)
        {
            if (!pokedex)
            {
                final float scale = this.entity.getWidth() * 2;
                final Vector3 offset = Vector3.getNewVector().set(rand.nextDouble() - 0.5, rand.nextDouble(), rand
                        .nextDouble() - 0.5);
                offset.scalarMultBy(scale);
                particleLoc.addTo(offset);
            }
            if (randomV)
            {
                particleVelo.set(rand.nextDouble() - 0.5, rand.nextDouble() + this.entity.getHeight() / 2, rand
                        .nextDouble() - 0.5);
                particleVelo.scalarMultBy(0.25);
            }
            PokecubeCore.spawnParticle(this.entity.getEntityWorld(), this.particle, particleLoc, particleVelo, args);
        }
        for (int i = 0; i < this.flavourAmounts.length; i++)
        {
            final int var = this.flavourAmounts[i];
            particleIntensity = var;
            if (var > 0 && rand.nextInt(100) < particleIntensity)
            {
                if (!pokedex)
                {
                    final float scale = this.entity.getWidth() * 2;
                    final Vector3 offset = Vector3.getNewVector().set(rand.nextDouble() - 0.5, rand.nextDouble(), rand
                            .nextDouble() - 0.5);
                    offset.scalarMultBy(scale);
                    particleLoc.addTo(offset);
                }
                if (randomV)
                {
                    particleVelo.set(rand.nextDouble() - 0.5, rand.nextDouble() + this.entity.getHeight() / 2, rand
                            .nextDouble() - 0.5);
                    particleVelo.scalarMultBy(0.25);
                }
                args = new int[] { LogicMiscUpdate.FLAVCOLOURS[i] };
                this.particle = "powder";
                PokecubeCore.spawnParticle(this.entity.getEntityWorld(), this.particle, particleLoc, particleVelo,
                        args);
            }
        }
        this.particle = null;
    }
}
