package pokecube.core.impl.capabilities.impl;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.EggEvent;
import pokecube.api.moves.IMoveNames;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.init.EntityTypes;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.utils.PokemobTracker;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokemobSexed extends PokemobSaves implements IBreedingMob
{
    private final UUID loveCause = null;

    @Override
    public boolean canMate(final AgeableMob otherAnimal)
    {
        if (otherAnimal == this.getEntity()) return false;
        // Not allowed to mate!
        if (!this.canBreed()) return false;
        if (otherAnimal == null || !otherAnimal.isAlive()) return false;
        // Too injured, no mate!
        if (otherAnimal.getHealth() < otherAnimal.getMaxHealth() / 2) return false;

        final IPokemob otherMob = PokemobCaps.getPokemobFor(otherAnimal);
        if (otherMob != null)
        {
            // Don't let tame and wild breed, prevents exploits with dittos
            if (otherMob.getOwnerId() != null && this.getOwnerId() == null) return false;
            if (this.getOwnerId() != null && otherMob.getOwnerId() == null) return false;

            if (!otherMob.canBreed()) return false;

            if (PokecubeAPI.POKEMOB_BUS.post(new EggEvent.CanBreed(this.getEntity(), otherAnimal))) return false;

            PokedexEntry thisEntry = this.getPokedexEntry();
            PokedexEntry thatEntry = otherMob.getPokedexEntry();

            if (thisEntry.isMega()) thisEntry = this.getMegaBase();
            if (thatEntry.isMega()) thatEntry = otherMob.getMegaBase();

            // Check if pokedex entries state they can breed, and then if so,
            // ensure sexe is different.
            final boolean neutral = this.getSexe() == IPokemob.NOSEXE || otherMob.getSexe() == IPokemob.NOSEXE;
            if ((thisEntry.areRelated(thatEntry) || thatEntry.areRelated(thisEntry))
                    && (neutral || otherMob.getSexe() != this.getSexe()))
                return true;

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
    public AgeableMob getEntity()
    {
        return (AgeableMob) this.entity;
    }

    @Override
    public Object getChild(final IBreedingMob male)
    {
        if (!IPokemob.class.isInstance(male)) return null;
        boolean transforms = false;
        boolean otherTransforms = ((IPokemob) male).getTransformedTo() != null;
        final String[] moves = this.getMoves();
        for (final String s : moves) if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) transforms = true;
        if (!otherTransforms) for (final String s : ((IPokemob) male).getMoves())
            if (s != null && s.equalsIgnoreCase(IMoveNames.MOVE_TRANSFORM)) otherTransforms = true;
        if (transforms && !otherTransforms && ((IPokemob) male).getTransformedTo() != this.getEntity())
            return male.getChild(this);
        return this.getPokedexEntry().getChild(((IPokemob) male).getPokedexEntry());
    }

    public void lay(final IPokemob male)
    {
        this.here.set(this.getEntity());
        if (PokecubeMod.debug) PokecubeAPI.LOGGER.info(this + " lay()");
        if (this.getEntity().getLevel().isClientSide) return;
        final int num = PokemobTracker.countPokemobs(this.getEntity().getLevel(), this.here,
                PokecubeCore.getConfig().maxSpawnRadius);
        if (!(this.getOwner() instanceof Player) && num > PokecubeCore.getConfig().mobSpawnNumber * 1.25) return;
        if (num > PokecubeCore.getConfig().mobSpawnNumber * 10) return;
        final Vector3 pos = this.here.set(this.getEntity()).addTo(0,
                Math.max(this.getPokedexEntry().height * this.getSize() / 4, 0.5f), 0);
        if (pos.isClearOfBlocks(this.getEntity().getLevel()))
        {
            Entity eggItem = new EntityPokemobEgg(EntityTypes.getEgg(), this.getEntity().getLevel())
                    .setToPos(this.here).setStackByParents(this.getEntity(), male);
            EggEvent.Lay event;
            event = new EggEvent.Lay(eggItem);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
            {
                final ServerPlayer player = (ServerPlayer) (this.getOwner() instanceof ServerPlayer ? this.getOwner()
                        : male.getOwner() instanceof ServerPlayer ? male.getOwner() : null);
                if (player != null) Triggers.BREEDPOKEMOB.trigger(player, this, male);
                this.egg = eggItem;
                this.getEntity().getLevel().addFreshEntity(this.egg);
            }
            return;
        }
    }

    protected void mate(final IBreedingMob male)
    {
        final IPokemob mate = (IPokemob) male;
        if (this.getSexe() == IPokemob.MALE || male.getSexe() == IPokemob.FEMALE && male != this) return;
        if (male == null || !mate.getEntity().isAlive()) return;
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 2;
        mate.applyHunger(hungerValue);
        this.applyHunger(hungerValue);
        male.resetLoveStatus();
        BrainUtils.deagro(this.getEntity());
        BrainUtils.deagro(mate.getEntity());
        this.lay(mate);
        this.resetLoveStatus();
    }

    @Override
    public void mateWith(final IBreedingMob male)
    {
        if (ThutCore.proxy.isClientSide()) return;
        final MinecraftServer server = ThutCore.proxy.getServer();
        server.tell(new TickTask(0, () -> this.mate(male)));
    }

    @Override
    public void resetLoveStatus()
    {
        this.loveTimer = -this.getEntity().getRandom().nextInt(600 + this.getBreedingDelay(null));
        this.setGeneralState(GeneralStates.MATING, false);
    }

    @Override
    public void tickBreedDelay(final int amount)
    {
        this.loveTimer += amount;
        if (this.loveTimer > 6000) this.resetLoveStatus();
    }

    @Override
    public void setReadyToMate(final Player cause)
    {
        this.loveTimer = 0;
    }

    @Override
    public ServerPlayer getCause()
    {
        if (this.loveCause == null) return null;
        else
        {
            final Player playerentity = this.getEntity().getLevel().getPlayerByUUID(this.loveCause);
            return playerentity instanceof ServerPlayer ? (ServerPlayer) playerentity : null;
        }
    }

    @Override
    public boolean canBreed()
    {
        if (!this.isRoutineEnabled(AIRoutine.MATE)) return false;
        PokedexEntry thisEntry = this.getPokedexEntry();
        if (thisEntry.isMega()) thisEntry = this.getMegaBase();
        if (!thisEntry.breeds) return false;
        final float hunger = HungerTask.calculateHunger(this);
        if (HungerTask.hitThreshold(hunger, HungerTask.MATERESET)) return false;
        return this.loveTimer >= 0;
    }

    @Override
    public boolean isBreeding()
    {
        return this.getGeneralState(GeneralStates.MATING);
    }
}
