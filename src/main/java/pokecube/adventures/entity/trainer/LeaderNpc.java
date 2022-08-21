package pokecube.adventures.entity.trainer;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasRewards.Reward;
import pokecube.api.utils.PokeType;
import pokecube.core.database.Database;
import thut.core.common.ThutCore;

public class LeaderNpc extends TrainerNpc
{
    public LeaderNpc(final EntityType<? extends TrainerBase> type, final Level worldIn)
    {
        super(type, worldIn);
        // Stuff below here is not null for real worlds, null for fake ones, so
        // lets return here if null.
        if (this.aiStates == null) return;
        this.aiStates.setAIState(AIState.TRADES_MOBS, false);
        this.pokemobsCap.resetTimeLose = 0;
    }

    @Override
    public void initTeam(final int level)
    {
        PokeType type;
        if (this.rewardsCap != null && this.rewardsCap.getRewards().isEmpty() || (type = PokecubeAdv.BADGEINV.get(
                this.rewardsCap.getRewards().get(0).stack.getItem())) == null)
        {
            type = PokeType.values()[ThutCore.newRandom().nextInt(PokeType.values().length)];
            final Item badge = PokecubeAdv.BADGES.get(type);
            this.rewardsCap.getRewards().add(0, new Reward(new ItemStack(badge)));
        }
        final List<PokedexEntry> options = Lists.newArrayList();
        for (final PokedexEntry e : Database.spawnables)
            if (e.isType(type)) options.add(e);
        TypeTrainer.getRandomTeam(this.pokemobsCap, this, level, this.level, options);
    }

    @Override
    public void setTypedName(final String name)
    {
        this.setNPCName("pokecube.gym_leader.named:" + name);
    }
}
