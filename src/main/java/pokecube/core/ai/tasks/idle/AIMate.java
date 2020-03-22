package pokecube.core.ai.tasks.idle;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.AIBase;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasMobAIStates;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.PokeType;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

/**
 * This IAIRunnable is responsible for most of the breeding AI for the
 * pokemobs. It finds the mates, initiates the fighting over a mate (if
 * applicable), then tells the mobs to breed if they should.
 */
public class AIMate extends AIBase
{
    public static int PATHCOOLDOWN = 5;

    int cooldown       = 0;
    int spawnBabyDelay = 0;
    int pathDelay      = 0;

    public AIMate(final IPokemob mob)
    {
        super(mob);
    }

    public boolean findLover()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (this.pokemob.getLover() != null) return true;
        boolean transforms = false;
        for (final String s : this.pokemob.getMoves())
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        if (!this.pokemob.getPokedexEntry().breeds && !transforms) return false;
        if (this.pokemob.isType(PokeType.getType("ghost")) && !this.pokemob.getGeneralState(GeneralStates.TAMED))
            return false;
        if (this.pokemob.getSexe() == IPokemob.MALE && !transforms || this.pokemob.getMalesForBreeding().size() > 0)
            return false;

        final float searchingLoveDist = 5F;
        AxisAlignedBB bb = this.makeBox(searchingLoveDist, searchingLoveDist, searchingLoveDist, this.entity
                .getBoundingBox());
        final List<Entity> targetMates = this.entity.getEntityWorld().getEntitiesInAABBexcluding(this.entity, bb,
                input ->
                {
                    final World world = input.getEntityWorld();
                    input = PokecubeCore.getEntityProvider().getEntity(world, input.getEntityId(), true);
                    return input instanceof AnimalEntity && AIMate.this.pokemob.canMate((AnimalEntity) input);
                });
        bb = this.makeBox(PokecubeCore.getConfig().maxSpawnRadius, searchingLoveDist, PokecubeCore
                .getConfig().maxSpawnRadius, this.entity.getBoundingBox());
        final List<Entity> otherMobs = this.entity.getEntityWorld().getEntitiesInAABBexcluding(this.entity, bb,
                input -> input instanceof AnimalEntity && CapabilityPokemob.getPokemobFor(input) != null);
        final float multiplier = (float) (this.pokemob.isPlayerOwned() ? PokecubeCore.getConfig().mateDensityPlayer
                : PokecubeCore.getConfig().mateDensityWild);
        if (otherMobs.size() >= PokecubeCore.getConfig().mobSpawnNumber * multiplier)
        {
            this.pokemob.resetLoveStatus();
            return false;
        }
        final boolean gendered = this.pokemob.getSexe() == IPokemob.MALE || this.pokemob.getSexe() == IPokemob.FEMALE;
        for (int i = 0; i < targetMates.size(); i++)
        {
            Entity mob = targetMates.get(i);
            if (!(mob instanceof AnimalEntity)) mob = PokecubeCore.getEntityProvider().getEntity(mob.getEntityWorld(),
                    mob.getEntityId(), true);
            final IPokemob otherPokemob = CapabilityPokemob.getPokemobFor(mob);
            final AnimalEntity animal = (AnimalEntity) mob;
            if (gendered && !transforms && otherPokemob.getSexe() == this.pokemob.getSexe()) continue;
            if (!otherPokemob.isRoutineEnabled(AIRoutine.MATE)) continue;
            if (otherPokemob == this.pokemob || otherPokemob.getGeneralState(GeneralStates.TAMED) != this.pokemob
                    .getGeneralState(GeneralStates.TAMED) || !otherPokemob.getPokedexEntry().breeds) continue;
            boolean otherTransforms = false;
            for (final String s : otherPokemob.getMoves())
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;

            if (transforms && otherTransforms || !(otherPokemob.getEntity() instanceof AnimalEntity)) continue;

            final boolean validMate = this.pokemob.canMate((AnimalEntity) otherPokemob.getEntity());
            if (!validMate || this.entity.getDistanceSq(otherPokemob.getEntity()) > searchingLoveDist
                    * searchingLoveDist) continue;
            if (!Vector3.isVisibleEntityFromEntity(this.entity, otherPokemob.getEntity()) || otherPokemob
                    .getCombatState(CombatStates.ANGRY)) continue;

            if (otherPokemob != this && animal.getHealth() > animal.getMaxHealth() / 1.5f) if (!this.pokemob
                    .getMalesForBreeding().contains(otherPokemob))
            {
                otherPokemob.setLover(this.entity);
                if (transforms) this.pokemob.setLover(animal);
                this.pokemob.getMalesForBreeding().add(otherPokemob);
                otherPokemob.setLoveTimer(200);
            }
        }
        return !this.pokemob.getMalesForBreeding().isEmpty();
    }

    public void initiateMateFight()
    {
        if (this.pokemob.getSexe() == IPokemob.MALE && this.pokemob.getLover() != null)
        {
            Entity emob = this.pokemob.getLover();
            if (emob != null) emob = PokecubeCore.getEntityProvider().getEntity(emob.getEntityWorld(), emob
                    .getEntityId(), true);
            final IPokemob loverMob = CapabilityPokemob.getPokemobFor(emob);
            this.entity.getLookController().setLookPositionWithEntity(emob, 10.0F, this.entity.getVerticalFaceSpeed());
            if (loverMob.getMalesForBreeding().size() > 1)
            {
                final IPokemob[] males = loverMob.getMalesForBreeding().toArray(new IPokemob[0]);
                Arrays.sort(males, (o1, o2) ->
                {
                    if (o2.getLevel() == o1.getLevel()) return o1.getDisplayName().getFormattedText().compareTo(o2
                            .getDisplayName().getFormattedText());
                    return o2.getLevel() - o1.getLevel();
                });
                final int level = males[0].getLevel();
                int n = 0;
                for (final IPokemob mob : males)
                    if (mob.getLevel() < level || mob.getHealth() < mob.getMaxHealth() / 1.5f)
                    {
                        loverMob.getMalesForBreeding().remove(mob);
                        mob.resetLoveStatus();
                        n++;
                    }
                if (n == 0 && loverMob.getMalesForBreeding().size() > 1)
                {

                    final IPokemob mob0 = (IPokemob) loverMob.getMalesForBreeding().get(0);
                    final IPokemob mob1 = (IPokemob) loverMob.getMalesForBreeding().get(1);
                    mob0.resetLoveStatus();
                    mob1.resetLoveStatus();
                    mob0.setCombatState(CombatStates.MATEFIGHT, true);
                    mob1.setCombatState(CombatStates.MATEFIGHT, true);
                    mob0.getEntity().setAttackTarget(mob1.getEntity());
                }
            }

            if (loverMob.getMalesForBreeding().size() > 1) return;
            else if (loverMob.getMalesForBreeding().size() == 0)
            {
                loverMob.resetLoveStatus();
                this.pokemob.resetLoveStatus();
            }
        }
        else if (this.pokemob.getMalesForBreeding().size() == 1)
        {
            final IBreedingMob loverMob = this.pokemob.getMalesForBreeding().get(0);
            this.pokemob.setLover(loverMob.getLover());
            loverMob.setLover(this.entity);
        }
    }

    private AxisAlignedBB makeBox(final double dx, final double dy, final double dz, final AxisAlignedBB centre)
    {
        return centre.grow(dx, dy, dz);
    }

    @Override
    public void reset()
    {
        if (this.cooldown > 0) return;
        this.cooldown = 20;
    }

    @Override
    public void run()
    {
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.MATE)) return false;
        if (--this.cooldown > 0) return false;
        if (this.pokemob.getLover() != null) if (this.pokemob.tryToBreed() && this.pokemob.getLover().isAlive())
            return true;
        if (this.pokemob.getGeneralState(GeneralStates.MATING)) return true;
        if (this.pokemob.getLover() != null) return true;
        if (this.pokemob.getSexe() == IPokemob.MALE || !this.pokemob.tryToBreed()) return false;
        if (this.pokemob.getCombatState(CombatStates.ANGRY) || this.entity.getAttackTarget() != null) return false;
        return true;
    }

    @Override
    public void tick()
    {
        if (this.pokemob.getSexe() != IPokemob.MALE)
        {
            int diff = 1 * PokecubeCore.getConfig().mateMultiplier;
            if (this.pokemob.getLoveTimer() > 0) diff = 1;
            this.pokemob.setLoveTimer(this.pokemob.getLoveTimer() + diff);
        }
        Entity mob = this.pokemob.getLover();
        if (mob != null) mob = PokecubeCore.getEntityProvider().getEntity(mob.getEntityWorld(), mob.getEntityId(),
                true);
        final IPokemob loverMob = CapabilityPokemob.getPokemobFor(mob);
        if (this.pokemob.getGeneralState(GeneralStates.MATING) && (mob == null || !mob.isAlive()
                || loverMob != this.pokemob)) this.pokemob.setGeneralState(GeneralStates.MATING, false);
        if (this.cooldown-- > 0) return;

        if (this.pokemob.getLoveTimer() > 0 && mob == null) this.findLover();
        if (this.pokemob.getLover() == null && this.pokemob.getMalesForBreeding().isEmpty())
        {
            this.cooldown = PokecubeCore.getConfig().mateAIRate;
            return;
        }
        boolean transforms = false;
        for (final String s : this.pokemob.getMoves())
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        mob = this.pokemob.getLover();
        if (mob != null) mob = PokecubeCore.getEntityProvider().getEntity(mob.getEntityWorld(), mob.getEntityId(),
                true);
        if (transforms && mob != null) this.pokemob.setTransformedTo(mob);
        if ((mob != null || !this.pokemob.getMalesForBreeding().isEmpty()) && (transforms || this.pokemob
                .getSexe() != IPokemob.MALE))
        {
            if (this.pokemob.getMalesForBreeding().size() == 1 && mob == null && this.pokemob.getMalesForBreeding().get(
                    0) instanceof IPokemob) this.pokemob.setLover(((IPokemob) this.pokemob.getMalesForBreeding().get(0))
                            .getEntity());
            if (this.pokemob.getMalesForBreeding().size() <= 1) this.tryFindMate();
            else this.initiateMateFight();
        }
        if (!this.pokemob.tryToBreed()) this.cooldown = PokecubeCore.getConfig().mateAIRate;
    }

    public void tryFindMate()
    {
        Entity emob = this.pokemob.getLover();
        if (emob != null) emob = PokecubeCore.getEntityProvider().getEntity(emob.getEntityWorld(), emob.getEntityId(),
                true);
        if (emob == null) return;
        if (!this.pokemob.isRoutineEnabled(AIRoutine.MATE))
        {
            this.pokemob.resetLoveStatus();
            return;
        }
        if (this.pokemob.getLogicState(LogicStates.SITTING)) this.pokemob.setLogicState(LogicStates.SITTING, false);

        double dist = this.entity.getWidth() * this.entity.getWidth() + emob.getWidth() * emob.getWidth();
        dist = Math.max(dist, 1);
        if (this.pathDelay-- < 0)
        {
            this.pathDelay = AIMate.PATHCOOLDOWN;
            this.entity.getNavigator().tryMoveToEntityLiving(emob, this.pokemob.getMovementSpeed());
        }
        this.spawnBabyDelay++;
        this.pokemob.setGeneralState(GeneralStates.MATING, true);
        final IPokemob loverMob = CapabilityPokemob.getPokemobFor(emob);
        if (loverMob != null)
        {
            loverMob.setGeneralState(GeneralStates.MATING, true);
            loverMob.setLover(this.entity);
            if (this.spawnBabyDelay >= 50)
            {
                if (emob instanceof IHasMobAIStates) ((IHasMobAIStates) emob).setGeneralState(GeneralStates.MATING,
                        false);
                this.pokemob.mateWith(loverMob);
                this.pokemob.setGeneralState(GeneralStates.MATING, false);
                this.spawnBabyDelay = 0;
                this.pokemob.resetLoveStatus();
                loverMob.resetLoveStatus();
            }
        }
    }
}
