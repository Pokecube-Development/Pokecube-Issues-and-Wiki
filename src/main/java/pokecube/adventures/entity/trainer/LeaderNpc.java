package pokecube.adventures.entity.trainer;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;
import thut.core.common.ThutCore;

public class LeaderNpc extends TrainerNpc
{
    public static final EntityType<LeaderNpc> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(LeaderNpc::new, MobCategory.CREATURE).setCustomClientFactory((s,
                w) -> LeaderNpc.TYPE.create(w)).build("leader");
    }

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
