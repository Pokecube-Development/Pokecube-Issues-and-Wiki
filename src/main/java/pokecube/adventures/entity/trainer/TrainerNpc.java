package pokecube.adventures.entity.trainer;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class TrainerNpc extends TrainerBase implements IEntityAdditionalSpawnData
{
    public static final EntityType<TrainerNpc> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(TrainerNpc::new, EntityClassification.CREATURE)
                .setCustomClientFactory((s, w) -> TrainerNpc.TYPE.create(w)).build("trainer");
    }

    boolean     added       = false;
    public long visibleTime = 0;

    public TrainerNpc(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.pokemobsCap.setType(TypeTrainer.get(this, true));
        this.enablePersistence();
    }

    @Override
    protected void addMobTrades(final PlayerEntity player, final ItemStack stack)
    {
        if (this.getCustomer() != null && PokecubeAdv.config.trainersTradeMobs) this.addMobTrades(stack);
    }

    @Override
    public ResourceLocation getTex()
    {
        return PokecubeAdv.proxy.getTrainerSkin(this, (TypeTrainer) this.getNpcType(), (byte) (this.isMale() ? 1 : 2));
    }

    protected void addMobTrades(final ItemStack buy1)
    {
        final ItemStack buy = buy1.copy();
        final IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, this.getEntityWorld());
        if (mon1 == null) return;
        final int stat1 = this.getBaseStats(mon1);
        for (int i = 0; i < this.pokemobsCap.getMaxPokemobCount(); i++)
        {
            ItemStack stack = this.pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                final IPokemob mon = PokecubeManager.itemToPokemob(stack, this.getEntityWorld());
                final int stat = this.getBaseStats(mon);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel() || SpecialCaseRegister.getCaptureCondition(mon
                        .getEvolutionEntry()) != null || SpecialCaseRegister.getSpawnCondition(mon
                                .getEvolutionEntry()) != null) continue;
                final UUID trader1 = mon1.getOwnerId();
                final boolean everstone = PokecubeItems.is(ICanEvolve.EVERSTONE, stack);
                mon.setOriginalOwnerUUID(this.getUniqueID());
                mon.setOwner(trader1);
                mon.setTraded(!everstone);
                stack = PokecubeManager.pokemobToItem(mon);
                stack.getTag().putInt("slotnum", i);
                this.getOffers().add(new MerchantOffer(buy, stack, 1, 1, 1));
            }
        }
    }

    @Override
    public void onTrade(final MerchantOffer recipe)
    {
        super.onTrade(recipe);
        // If this was our mob trade, we need to set our mob as it.
        ItemStack poke1 = recipe.getBuyingStackFirst();
        final ItemStack poke2 = recipe.getSellingStack();
        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) return;
        final int num = poke2.getTag().getInt("slotnum");
        final LivingEntity player2 = this;
        final IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, this.getEntityWorld());
        final UUID trader2 = player2.getUniqueID();
        mon1.setOwner(trader2);
        poke1 = PokecubeManager.pokemobToItem(mon1);
        this.pokemobsCap.setPokemob(num, poke1);
    }

    @Override
    public VillagerEntity createChild(final AgeableEntity ageable)
    {
        if (this.isChild() || this.getGrowingAge() > 0 || !this.aiStates.getAIState(IHasNPCAIStates.MATES)) return null;
        if (TrainerSpawnHandler.countTrainersNear(this, 64) > 5) return null;
        if (this.pokemobsCap.getGender() == 2)
        {
            final IHasPokemobs other = CapabilityHasPokemobs.getHasPokemobs(ageable);
            final IHasNPCAIStates otherAI = CapabilityNPCAIStates.getNPCAIStates(ageable);
            if (other != null && otherAI != null && otherAI.getAIState(IHasNPCAIStates.MATES) && other.getGender() == 1)
            {
                if (this.location == null) this.location = Vector3.getNewVector();
                final TrainerNpc baby = TrainerSpawnHandler.getTrainer(this.location.set(this), this.getEntityWorld());
                if (baby != null) baby.setGrowingAge(-24000);
                return baby;
            }
        }
        return null;
    }

    private int getBaseStats(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        return entry.getStatHP() + entry.getStatATT() + entry.getStatDEF() + entry.getStatATTSPE()
        + entry.getStatDEFSPE() + entry.getStatVIT();
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        if (nbt.contains("trades")) this.aiStates.setAIState(IHasNPCAIStates.TRADES, nbt.getBoolean("trades"));
        this.fixedTrades = nbt.getBoolean("fixedTrades");
        this.fixedMobs = nbt.getBoolean("fixedMobs");
        this.setTypes();
        if (nbt.contains("DefeatList"))
        {
            this.pokemobsCap.setGender((byte) (nbt.getBoolean("gender") ? 1 : 2));
            if (nbt.contains("resetTime")) this.pokemobsCap.resetTime = nbt.getLong("resetTime");
            if (nbt.contains("defeated", 9))
            {
                final ListNBT list = nbt.getList("defeated", 10);
                this.pokemobsCap.defeated.load(list);
            }
            if (nbt.contains("defeatedBy", 9))
            {
                final ListNBT list = nbt.getList("defeatedBy", 10);
                this.pokemobsCap.defeatedBy.load(list);
            }
            this.pokemobsCap.notifyDefeat = nbt.getBoolean("notifyDefeat");
        }
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
            this.aiStates.setAIState(IHasNPCAIStates.STATIONARY, false);
            this.guardAI.setPos(new BlockPos(0, 0, 0));
            this.guardAI.setTimePeriod(new TimePeriod(0, 0));
            return this;
        }
        this.guardAI.setTimePeriod(TimePeriod.fullDay);
        this.guardAI.setPos(this.getPosition());
        this.aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        return this;
    }

    public TrainerNpc setType(final TypeTrainer type)
    {
        this.pokemobsCap.setType(type);
        this.pokemobsCap.getType().initTrainerItems(this);
        return this;
    }

    @Override
    public void initTeam(final int level)
    {
        TypeTrainer.getRandomTeam(this.pokemobsCap, this, level, this.world);
    }

    public void setTypes()
    {
        if (this.pokemobsCap.getType() == null)
        {
            this.setType(TypeTrainer.get(this, false));
            this.initTeam(5);
        }
        if (this.name.isEmpty())
        {
            final List<String> names = this.isMale() ? TypeTrainer.maleNames : TypeTrainer.femaleNames;
            if (!names.isEmpty()) this.setRandomName(names.get(new Random().nextInt(names.size())));
            this.setCustomName(this.getDisplayName());
        }
    }

    @Override
    public void setRandomName(final String name)
    {
        this.name = "pokecube." + this.getNpcType().getName() + ".named:" + name;
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        if (this.getHeldItem(Hand.OFF_HAND).isEmpty() && !this.pokemobsCap.getType().held.isEmpty())
            this.setHeldItem(Hand.OFF_HAND, this.pokemobsCap.getType().held.copy());
        if (!this.pokemobsCap.getType().bag.isEmpty())
        {
            final PlayerWearables worn = ThutWearables.getWearables(this);
            if (worn.getWearable(EnumWearable.BACK).isEmpty())
                worn.setWearable(EnumWearable.BACK, this.pokemobsCap.getType().bag.copy());
        }
        this.setTypes(); // Ensure types are valid before saving.
        super.writeAdditional(compound);
        compound.putBoolean("fixedTrades", this.fixedTrades);
        compound.putBoolean("fixedMobs", this.fixedMobs);
    }
}
