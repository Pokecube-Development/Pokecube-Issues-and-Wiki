package pokecube.adventures.entity.trainer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.world.World;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

public abstract class TrainerBase extends AbstractVillagerEntity
{
    public List<IPokemob>  currentPokemobs = new ArrayList<>();
    public DefaultPokemobs pokemobsCap;
    public IHasMessages    messages;
    public IHasRewards     rewardsCap;
    public IHasNPCAIStates aiStates;
    int                    despawncounter  = 0;

    protected TrainerBase(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.pokemobsCap = (DefaultPokemobs) this.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null).orElse(
                null);
        this.rewardsCap = this.getCapability(CapabilityHasRewards.REWARDS_CAP, null).orElse(null);
        this.messages = this.getCapability(CapabilityNPCMessages.MESSAGES_CAP, null).orElse(null);
        this.aiStates = this.getCapability(CapabilityNPCAIStates.AISTATES_CAP, null).orElse(null);
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
        if (!this.isServerWorld()) return;
        if (this.pokemobsCap.getOutID() != null && this.pokemobsCap.getOutMob() == null)
        {
            final Entity mob = this.getServer().getWorld(this.dimension).getEntityByUuid(this.pokemobsCap.getOutID());
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            this.pokemobsCap.setOutMob(pokemob);
            if (this.pokemobsCap.getOutMob() == null) this.pokemobsCap.setOutID(null);
        }
        if (this.pokemobsCap.countPokemon() == 0 && !this.aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                && !this.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
        {
            // Do not despawn if there is a player nearby.
            if (Tools.isAnyPlayerInRange(10, this)) return;
            this.despawncounter++;
            if (this.despawncounter > 200) this.remove();
            return;
        }
        if (this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0) this
                .setHealth(Math.min(this.getHealth() + 1, this.getMaxHealth()));
        this.despawncounter = 0;
    }

    @Override
    public void remove()
    {
        EventsHandler.recallAllPokemobs(this);
        // TrainerSpawnHandler.removeTrainer(this);
        super.remove();
    }

    public void resetTrades()
    {
        this.offers = null;
    }

}
