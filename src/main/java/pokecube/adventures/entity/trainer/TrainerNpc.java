package pokecube.adventures.entity.trainer;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class TrainerNpc extends TrainerBase implements IEntityAdditionalSpawnData
{
    public static final EntityType<TrainerNpc> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(TrainerNpc::new, MobCategory.CREATURE)
                .setCustomClientFactory((s, w) -> TrainerNpc.TYPE.create(w)).build("trainer");
    }

    boolean added = false;
    public long visibleTime = 0;

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
        final ItemStack buy = buy1.copy();
        final IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, this.getLevel());
        if (mon1 == null) return;
        final int stat1 = this.getBaseStats(mon1);
        for (int i = 0; i < this.pokemobsCap.getMaxPokemobCount(); i++)
        {
            ItemStack stack = this.pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                final IPokemob mon = PokecubeManager.itemToPokemob(stack, this.getLevel());
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
                stack.getTag().putInt("slotnum", i);
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
        final int num = poke2.getTag().getInt("slotnum");
        final LivingEntity player2 = this;
        final IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, this.getLevel());
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
        this.setTypes();
    }

    public TrainerNpc setLevel(final int level)
    {
        this.initTeam(level);
        return this;
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

    @Override
    public void initTeam(final int level)
    {
        TypeTrainer.getRandomTeam(this.pokemobsCap, this, level, this.level);
    }

    public void setTypes()
    {
        if (this.pokemobsCap.getType() == null)
        {
            this.setNpcType(TypeTrainer.get(this, false));
            this.initTeam(5);
        }
        if (this.getNPCName().isEmpty())
        {
            final List<String> names = this.isMale() ? TypeTrainer.maleNames : TypeTrainer.femaleNames;
            if (!names.isEmpty()) this.setTypedName(names.get(ThutCore.newRandom().nextInt(names.size())));
            this.setCustomName(this.getDisplayName());
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        if (this.getItemInHand(InteractionHand.OFF_HAND).isEmpty() && !this.pokemobsCap.getType().held.isEmpty())
            this.setItemInHand(InteractionHand.OFF_HAND, this.pokemobsCap.getType().held.copy());
        if (!this.pokemobsCap.getType().bag.isEmpty())
        {
            final PlayerWearables worn = ThutWearables.getWearables(this);
            if (worn.getWearable(EnumWearable.BACK).isEmpty())
                worn.setWearable(EnumWearable.BACK, this.pokemobsCap.getType().bag.copy());
        }
        this.setTypes(); // Ensure types are valid before saving.
        super.addAdditionalSaveData(compound);
        compound.putBoolean("fixedMobs", this.fixedMobs);
    }
}
