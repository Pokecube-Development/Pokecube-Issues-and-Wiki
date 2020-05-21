package pokecube.core.interfaces.capabilities.impl;

import java.util.UUID;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.combat.AIFindTarget;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.EggEvent;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.PokemobTracker;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokemobSexed extends PokemobStats
{
    private final UUID loveCause = null;

    @Override
    public boolean canMate(final AgeableEntity otherAnimal)
    {
        if (otherAnimal == null || !otherAnimal.isAlive()) return false;
        if (otherAnimal == this.getEntity()) return false;
        // Not allowed to mate!
        if (!this.isRoutineEnabled(AIRoutine.MATE)) return false;
        // Too injured, no mate!
        if (otherAnimal.getHealth() < otherAnimal.getMaxHealth() / 2) return false;

        final IPokemob otherMob = CapabilityPokemob.getPokemobFor(otherAnimal);
        if (otherMob != null)
        {
            // Not allowed to mate!
            if (!otherMob.isRoutineEnabled(AIRoutine.MATE)) return false;

            // Don't let tame and wild breed, prevents exploits with dittos
            if (otherMob.getOwnerId() != null && this.getOwnerId() == null) return false;
            if (this.getOwnerId() != null && otherMob.getOwnerId() == null) return false;

            if (PokecubeCore.POKEMOB_BUS.post(new EggEvent.CanBreed(this.getEntity(), otherAnimal))) return false;

            if (!otherMob.getPokedexEntry().breeds || !this.getPokedexEntry().breeds) return false;

            PokedexEntry thisEntry = this.getPokedexEntry();
            PokedexEntry thatEntry = otherMob.getPokedexEntry();

            if (thisEntry.isMega) thisEntry = this.getMegaBase();
            if (thatEntry.isMega) thatEntry = otherMob.getMegaBase();

            // Check if pokedex entries state they can breed, and then if so,
            // ensure sexe is different.
            final boolean neutral = this.getSexe() == IPokemob.NOSEXE || otherMob.getSexe() == IPokemob.NOSEXE;
            if ((thisEntry.areRelated(thatEntry) || thatEntry.areRelated(thisEntry)) && (neutral || otherMob
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

    private int getBreedingDelay(final IPokemob mate)
    {
        return PokecubeCore.getConfig().breedingDelay;
    }

    @Override
    public Object getChild(final IBreedingMob male)
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

    public void lay(final IPokemob male)
    {
        this.here.set(this.getEntity());
        if (PokecubeMod.debug) PokecubeCore.LOGGER.info(this + " lay()");
        if (this.getEntity().getEntityWorld().isRemote) return;
        final int num = PokemobTracker.countPokemobs(this.getEntity().getEntityWorld(), this.here, PokecubeCore
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
                    final ServerPlayerEntity player = (ServerPlayerEntity) (this
                            .getOwner() instanceof ServerPlayerEntity ? this.getOwner()
                                    : male.getOwner() instanceof ServerPlayerEntity ? male.getOwner() : null);
                    if (player != null) Triggers.BREEDPOKEMOB.trigger(player, this, male);
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

    protected void mate(final IBreedingMob male)
    {
        final IPokemob mate = (IPokemob) male;
        if (male == null || !mate.getEntity().isAlive()) return;
        if (this.getSexe() == IPokemob.MALE || male.getSexe() == IPokemob.FEMALE && male != this) return;
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 2;
        mate.setHungerTime(mate.getHungerTime() + hungerValue);
        this.setHungerTime(this.getHungerTime() + hungerValue);
        mate.resetLoveStatus();
        AIFindTarget.deagro(this.getEntity());
        AIFindTarget.deagro(mate.getEntity());
        this.lay(mate);
        this.resetLoveStatus();
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
        this.loveTimer = -this.rand.nextInt(600 + this.getBreedingDelay(null));
        this.setGeneralState(GeneralStates.MATING, false);
    }

    @Override
    public void tickBreedDelay(final int amount)
    {
        this.loveTimer += amount;
        if (this.loveTimer > 6000) this.resetLoveStatus();
    }

    @Override
    public void setReadyToMate(final PlayerEntity cause)
    {
        this.loveTimer = 0;
    }

    @Override
    public ServerPlayerEntity getCause()
    {
        if (this.loveCause == null) return null;
        else
        {
            final PlayerEntity playerentity = this.getEntity().getEntityWorld().getPlayerByUuid(this.loveCause);
            return playerentity instanceof ServerPlayerEntity ? (ServerPlayerEntity) playerentity : null;
        }
    }

    @Override
    public boolean canBreed()
    {
        return this.loveTimer >= 0;
    }

    @Override
    public boolean isBreeding()
    {
        return this.getGeneralState(GeneralStates.MATING);
    }
}
