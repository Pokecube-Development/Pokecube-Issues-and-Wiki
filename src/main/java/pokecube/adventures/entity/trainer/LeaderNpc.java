package pokecube.adventures.entity.trainer;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class LeaderNpc extends TrainerNpc
{
    public static final EntityType<LeaderNpc> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(LeaderNpc::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> LeaderNpc.TYPE.create(w)).build("leader");
    }

    public LeaderNpc(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        this.aiStates.setAIState(IHasNPCAIStates.TRADES, false);
        this.pokemobsCap.resetTime = 0;
    }

    @Override
    public void initTeam(final int level)
    {
        PokeType type;
        if (this.rewardsCap != null && this.rewardsCap.getRewards().isEmpty() || (type = PokecubeAdv.BADGEINV.get(
                this.rewardsCap.getRewards().get(0).stack.getItem())) == null)
        {
            type = PokeType.values()[new Random().nextInt(PokeType.values().length)];
            final Item badge = PokecubeAdv.BADGES.get(type);
            this.rewardsCap.getRewards().add(0, new Reward(new ItemStack(badge)));
        }
        final List<PokedexEntry> options = Lists.newArrayList();
        for (final PokedexEntry e : Database.spawnables)
            if (e.isType(type)) options.add(e);
        TypeTrainer.getRandomTeam(this.pokemobsCap, this, level, this.world, options);
    }

    @Override
    public void setRandomName(final String name)
    {
        this.name = "pokecube.gym_leader.named:" + name;
    }
}
