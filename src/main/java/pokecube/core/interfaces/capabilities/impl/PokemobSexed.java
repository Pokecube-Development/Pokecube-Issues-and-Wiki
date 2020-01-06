package pokecube.core.interfaces.capabilities.impl;

import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.Tools;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokemobSexed extends PokemobStats
{

    @Override
    public boolean canMate(AnimalEntity AnimalEntity)
    {
        if (!this.isRoutineEnabled(AIRoutine.MATE)) return false;
        final IPokemob otherMob = CapabilityPokemob.getPokemobFor(AnimalEntity);
        if (otherMob != null)
        {
            PokedexEntry thisEntry = this.getPokedexEntry();
            PokedexEntry thatEntry = otherMob.getPokedexEntry();

            if (thisEntry.isMega) thisEntry = thisEntry.getBaseForme();
            if (thatEntry.isMega) thatEntry = thatEntry.getBaseForme();

            // Check if pokedex entries state they can breed, and then if so,
            // ensure sexe is different.
            final boolean neutral = this.getSexe() == IPokemob.NOSEXE || otherMob.getSexe() == IPokemob.NOSEXE;
            if (thisEntry.areRelated(thatEntry) || thatEntry.areRelated(thisEntry) && (neutral || otherMob
                    .getSexe() != this.getSexe())) return true;

            // Otherwise check for transform.
            boolean transforms = false;
            boolean otherTransforms = false;
            for (final String s : this.getMoves())
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
            for (final String s : otherMob.getMoves())
                if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;

            // can't breed two transformers
            if (transforms && otherTransforms) return false;
            else if (transforms || otherTransforms) // Anything else will mate
                                                    // with a transformer.
                return true;
        }

        return false;
    }

    private int getBreedingDelay(IPokemob mate)
    {
        return PokecubeCore.getConfig().breedingDelay;
    }

    @Override
    public Object getChild(IBreedingMob male)
    {
        if (!IPokemob.class.isInstance(male)) return null;
        boolean transforms = false;
        boolean otherTransforms = ((IPokemob) male).getTransformedTo() != null;
        final String[] moves = this.getMoves();
        for (final String s : moves)
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        if (!otherTransforms) for (final String s : ((IPokemob) male).getMoves())
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
        if (transforms && !otherTransforms && ((IPokemob) male).getTransformedTo() != this.getEntity()) return male
                .getChild(this);
        return this.getPokedexEntry().getChild(((IPokemob) male).getPokedexEntry());
    }

    @Override
    /**
     * Which entity is this pokemob trying to breed with
     *
     * @return
     */
    public Entity getLover()
    {
        return this.lover;
    }

    @Override
    public int getLoveTimer()
    {
        return this.loveTimer;
    }

    @Override
    public Vector<IBreedingMob> getMalesForBreeding()
    {
        return this.males;
    }

    public void lay(IPokemob male)
    {
        this.here.set(this.getEntity());
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info(this + " lay()");
        if (this.getEntity().getEntityWorld().isRemote) return;
        final int num = Tools.countPokemon(this.getEntity().getEntityWorld(), this.here, PokecubeCore
                .getConfig().maxSpawnRadius);
        if (!(this.getOwner() instanceof PlayerEntity) && num > PokecubeCore.getConfig().mobSpawnNumber * 1.25) return;
        final Vector3 pos = this.here.set(this.getEntity()).addTo(0, Math.max(this.getPokedexEntry().height * this
                .getSize() / 4, 0.5f), 0);
        if (pos.isClearOfBlocks(this.getEntity().getEntityWorld()))
        {
            Entity eggItem = null;
            try
            {
                eggItem = new EntityPokemobEgg(EntityPokemobEgg.TYPE, this.getEntity().getEntityWorld()).setPos(
                        this.here).setStackByParents(this.getEntity(), male);
            }
            catch (final Exception e1)
            {
                e1.printStackTrace();
            }
            EggEvent.Lay event;
            try
            {
                event = new EggEvent.Lay(eggItem);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled())
                {
                    this.egg = eggItem;
                    this.getEntity().getEntityWorld().addEntity(this.egg);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            return;
        }
    }

    protected void mate(IBreedingMob male)
    {
        final IPokemob mate = (IPokemob) male;
        if (male == null || !mate.getEntity().isAlive()) return;
        if (this.getSexe() == IPokemob.MALE || male.getSexe() == IPokemob.FEMALE && male != this) return;
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 2;
        mate.setHungerTime(mate.getHungerTime() + hungerValue);
        this.setHungerTime(this.getHungerTime() + hungerValue);
        mate.setLover(null);
        mate.resetLoveStatus();
        this.getEntity().setAttackTarget(null);
        mate.getEntity().setAttackTarget(null);
        this.lay(mate);
        this.resetLoveStatus();
        this.lover = null;
    }

    @Override
    public void mateWith(final IBreedingMob male)
    {
        if (ThutCore.proxy.isClientSide()) return;
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        server.enqueue(new TickDelayedTask(0, () -> this.mate(male)));
    }

    @Override
    public void resetLoveStatus()
    {
        this.setLoveTimer(this.rand.nextInt(600) - this.getBreedingDelay(null));
        this.setLover(null);
        this.setGeneralState(GeneralStates.MATING, false);
        if (this.males != null) this.males.clear();
    }

    @Override
    /**
     * Sets the entity to try to breed with
     *
     * @param lover
     */
    public void setLover(final Entity newLover)
    {
        this.lover = newLover;
    }

    @Override
    public void setLoveTimer(final int value)
    {
        this.loveTimer = value;
    }

    @Override
    public boolean tryToBreed()
    {
        return this.loveTimer > 0 || this.lover != null;
    }
}
