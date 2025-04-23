package pokecube.adventures.entity.trainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

import java.util.List;
import java.util.UUID;

public class TrainerNpc extends TrainerBase
{
    public TrainerNpc(final EntityType<? extends TrainerBase> type, final Level worldIn)
    {
        super(type, worldIn);
        // This can be null in the case where fake worlds are used to initialize
        // us for testing.
        if (this.pokemobsCap != null) this.pokemobsCap.setType(TypeTrainer.get(this, true));
        this.setPersistenceRequired();
    }

    @Override
    protected void addMobTrades(final Player player, final ItemStack stack)
    {
        if (this.getTradingPlayer() != null) this.addMobTrades(stack);
    }

    protected void addMobTrades(final ItemStack buy1)
    {
        final ItemCost buy = new ItemCost(buy1.getItemHolder(), 1, DataComponentPredicate.allOf(buy1.getComponents()));
        final IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, this.level());
        if (mon1 == null) return;
        final int stat1 = this.getBaseStats(mon1);
        for (int i = 0; i < this.pokemobsCap.getMaxPokemobCount(); i++)
        {
            ItemStack stack = this.pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                final IPokemob mon = PokecubeManager.itemToPokemob(stack, this.level());
                final int stat = this.getBaseStats(mon);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()
                        || SpecialCaseRegister.getCaptureCondition(mon.getEvolutionEntry()) != null
                        || SpecialCaseRegister.getSpawnCondition(mon.getEvolutionEntry()) != null)
                    continue;
                final UUID trader1 = mon1.getOwnerId();
                final boolean everstone = ItemList.is(ICanEvolve.EVERSTONE, stack);
                mon.setOriginalOwnerUUID(this.getUUID());
                mon.setOwner(trader1);
                mon.setTraded(!everstone);
                stack = PokecubeManager.pokemobToItem(mon);
                this.getOffers().add(new MerchantOffer(buy, stack.copy(), 2, 2, 2));
            }
        }
    }

    @Override
    public void notifyTrade(final MerchantOffer recipe)
    {
        super.notifyTrade(recipe);
        // If this was our mob trade, we need to set our mob as it.
        ItemStack poke1 = recipe.getBaseCostA();
        final ItemStack poke2 = recipe.getResult();
        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) return;
        UUID id = PokecubeManager.getUUID(poke2, level);
        int num = -1;
        for (int i = 0; i < this.pokemobsCap.getMaxPokemobCount(); i++)
        {
            UUID test = PokecubeManager.getUUID(this.pokemobsCap.getItem(i), level);
            if (id.equals(test))
            {
                num = i;
                break;
            }
        }
        final LivingEntity player2 = this;
        final IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, this.level());
        final UUID trader2 = player2.getUUID();
        mon1.setOwner(trader2);
        poke1 = PokecubeManager.pokemobToItem(mon1);
        this.pokemobsCap.setPokemob(num, poke1);
    }

    @Override
    public Villager getBreedOffspring(final ServerLevel p_241840_1_, final AgeableMob ageable)
    {
        return super.getBreedOffspring(p_241840_1_, ageable);
    }

    private int getBaseStats(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        return entry.getStatHP() + entry.getStatATT() + entry.getStatDEF() + entry.getStatATTSPE()
                + entry.getStatDEFSPE() + entry.getStatVIT();
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.fixedMobs = nbt.getBoolean("fixedMobs");
        this.setTypes(false);
    }

    public TrainerNpc setStationary(final Vector3 location)
    {
        this.location = location;
        if (location == null)
        {
            this.aiStates.setAIState(AIState.STATIONARY, false);
            this.guardAI.setPos(new BlockPos(0, 0, 0));
            this.guardAI.setTimePeriod(new TimePeriod(0, 0));
            return this;
        }
        this.guardAI.setTimePeriod(TimePeriod.fullDay);
        this.guardAI.setPos(this.blockPosition());
        this.aiStates.setAIState(AIState.STATIONARY, true);
        return this;
    }

    public void setTypes(boolean resetName)
    {
        if (this.pokemobsCap.getType() == null)
        {
            this.setNpcType(TypeTrainer.get(this, false));
            TrainerSpawnHandler.initTrainer(this.pokemobsCap, 5);
        }
        if (this.getNPCName().isEmpty() || resetName)
        {
            final List<String> names = this.isMale() ? TypeTrainer.maleNames : TypeTrainer.femaleNames;
            if (!names.isEmpty()) this.setTypedName(names.get(ThutCore.newRandom().nextInt(names.size())));
            this.setCustomName(this.getDisplayName());
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        this.setTypes(false); // Ensure types are valid before saving.
        super.addAdditionalSaveData(compound);
        compound.putBoolean("fixedMobs", this.fixedMobs);
    }
}
