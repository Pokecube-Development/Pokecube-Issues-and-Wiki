package pokecube.core.interfaces.capabilities.impl;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import pokecube.core.ai.logic.Logic;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public abstract class PokemobAI extends PokemobEvolves
{
    private final boolean[] routineStates = new boolean[AIRoutine.values().length];
    private int             cachedGeneralState;
    private int             cachedCombatState;
    private int             cachedLogicState;

    @Override
    public boolean getCombatState(final CombatStates state)
    {
        if (this.getEntity().getEntityWorld().isRemote) this.cachedCombatState = this.dataSync().get(
                this.params.COMBATSTATESDW);
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
        if (state == GeneralStates.TAMED) return this.getOwnerId() != null;
        if (this.getEntity().getEntityWorld().isRemote) this.cachedGeneralState = this.dataSync().get(
                this.params.GENERALSTATESDW);
        return (this.cachedGeneralState & state.getMask()) != 0;
    }

    @Override
    public boolean getLogicState(final LogicStates state)
    {
        if (this.getEntity().getEntityWorld().isRemote) this.cachedLogicState = this.dataSync().get(
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
                .isServerWorld()).getNextID();
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
        return this.routineStates[routine.ordinal()];
    }

    @Override
    public void onSendOut()
    {
        // Reset some values to prevent spontaneous damage.
        this.getEntity().fallDistance = 0;
        this.getEntity().extinguish();
        // After here is server side only.
        if (this.getEntity().getEntityWorld().isRemote) return;
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
                particleLoc.set(this.getEntity().posX + this.rand.nextFloat() * this.getEntity().getWidth() * 2.0F
                        - this.getEntity().getWidth(), this.getEntity().posY + 0.5D + this.rand.nextFloat() * this
                                .getEntity().getHeight(), this.getEntity().posZ + this.rand.nextFloat() * this
                                        .getEntity().getWidth() * 2.0F - this.getEntity().getWidth());
                this.getEntity().getEntityWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, particleLoc.x,
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
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
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
        final int newState = flag ? byte0 | state.getMask() : byte0 & -state.getMask() - 1;
        this.setTotalGeneralState(newState);
    }

    @Override
    public void setLogicState(final LogicStates state, final boolean flag)
    {
        final int byte0 = this.dataSync().get(this.params.LOGICSTATESDW);
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
    }
}
