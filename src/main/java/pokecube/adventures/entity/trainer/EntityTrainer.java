package pokecube.adventures.entity.trainer;

import java.util.UUID;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs.DefeatEntry;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class EntityTrainer extends TrainerBase implements IEntityAdditionalSpawnData
{
    public static final EntityType<EntityTrainer> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(EntityTrainer::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> EntityTrainer.TYPE.create(w)).build("trainer");
    }

    private boolean randomize   = false;
    public Vector3  location    = null;
    public String   name        = "";
    public String   playerName  = "";
    public String   urlSkin     = "";
    boolean         added       = false;
    public GuardAI  guardAI;
    public long     visibleTime = 0;

    public EntityTrainer(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.enablePersistence();
        // this.pokemobsCap.setType(TypeTrainer.mobTypeMapper.getType(this,
        // true));
    }

    protected void addMobTrades(final ItemStack buy1)
    {
        final ItemStack buy = buy1.copy();
        final IPokemob mon1 = PokecubeManager.itemToPokemob(buy1, this.getEntityWorld());
        final int stat1 = this.getBaseStats(mon1);
        for (int i = 0; i < this.pokemobsCap.getMaxPokemobCount(); i++)
        {
            ItemStack stack = this.pokemobsCap.getPokemob(i);
            if (PokecubeManager.isFilled(stack))
            {
                final IPokemob mon = PokecubeManager.itemToPokemob(stack, this.getEntityWorld());
                final int stat = this.getBaseStats(mon);
                if (stat > stat1 || mon.getLevel() > mon1.getLevel()) continue;
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
    public AgeableEntity createChild(final AgeableEntity ageable)
    {
        if (this.isChild() || this.getGrowingAge() > 0 || !this.aiStates.getAIState(IHasNPCAIStates.MATES)) return null;
        if (this.pokemobsCap.getGender() == 2)
        {
            final IHasPokemobs other = CapabilityHasPokemobs.getHasPokemobs(ageable);
            final IHasNPCAIStates otherAI = CapabilityNPCAIStates.getNPCAIStates(ageable);
            if (other != null && otherAI != null && otherAI.getAIState(IHasNPCAIStates.MATES) && other.getGender() == 1)
            {
                if (this.location == null) this.location = Vector3.getNewVector();
                final EntityTrainer baby = TrainerSpawnHandler.getTrainer(this.location.set(this), this
                        .getEntityWorld());
                if (baby != null) baby.setGrowingAge(-24000);
                return baby;
            }
        }
        return null;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void func_213713_b(final MerchantOffer p_213713_1_)
    {
        // TODO Auto-generated method stub

    }

    private int getBaseStats(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        return entry.getStatHP() + entry.getStatATT() + entry.getStatDEF() + entry.getStatATTSPE() + entry
                .getStatDEFSPE() + entry.getStatVIT();
    }

    public boolean getShouldRandomize()
    {
        return this.randomize;
    }

    public ResourceLocation getTex()
    {
        if (!this.playerName.isEmpty()) return PokecubeCore.proxy.getPlayerSkin(this.playerName);
        else if (!this.urlSkin.isEmpty()) return PokecubeCore.proxy.getUrlSkin(this.urlSkin);
        else return PokecubeAdv.proxy.getTrainerSkin(this, this.pokemobsCap.getType(), this.pokemobsCap.getGender());
    }

    @Override
    protected void populateTradeData()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        if (nbt.contains("trades")) this.aiStates.setAIState(IHasNPCAIStates.TRADES, nbt.getBoolean("trades"));
        this.randomize = nbt.getBoolean("randomTeam");
        this.name = nbt.getString("name");
        this.setTypes();
        if (nbt.contains("DefeatList"))
        {
            this.pokemobsCap.defeaters.clear();
            this.pokemobsCap.setGender((byte) (nbt.getBoolean("gender") ? 1 : 2));
            if (nbt.contains("resetTime")) this.pokemobsCap.resetTime = nbt.getLong("resetTime");
            if (nbt.contains("DefeatList", 9))
            {
                final ListNBT ListNBT = nbt.getList("DefeatList", 10);
                for (int i = 0; i < ListNBT.size(); i++)
                    this.pokemobsCap.defeaters.add(DefeatEntry.createFromNBT(ListNBT.getCompound(i)));
            }
            this.pokemobsCap.notifyDefeat = nbt.getBoolean("notifyDefeat");
        }
    }

    @Override
    public void readSpawnData(final PacketBuffer additionalData)
    {
        this.readAdditional(additionalData.readCompoundTag());
    }

    public EntityTrainer setLevel(final int level)
    {
        TypeTrainer.getRandomTeam(this.pokemobsCap, this, level, this.getEntityWorld());
        return this;
    }

    public EntityTrainer setStationary(final Vector3 location)
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

    public EntityTrainer setType(final TypeTrainer type)
    {
        this.pokemobsCap.setType(type);
        this.pokemobsCap.getType().initTrainerItems(this);
        return this;
    }

    public void setTypes()
    {
        if (this.pokemobsCap.getType() == null)
        {
            this.setType(TypeTrainer.mobTypeMapper.getType(this, false));
            TypeTrainer.getRandomTeam(this.pokemobsCap, this, 5, this.world);
        }
        if (this.name.isEmpty())
        {
            final int index = this.getEntityId() % (this.pokemobsCap.getGender() == 1 ? TypeTrainer.maleNames.size()
                    : TypeTrainer.femaleNames.size());
            this.name = this.pokemobsCap.getGender() == 1 ? TypeTrainer.maleNames.get(index)
                    : TypeTrainer.femaleNames.get(index);
            this.setCustomName(new StringTextComponent(this.pokemobsCap.getType().name + " " + this.name));
        }
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putString("playerName", this.playerName);
        compound.putString("urlSkin", this.urlSkin);
        compound.putBoolean("randomTeam", this.randomize);
        compound.putString("name", this.name);
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.writeAdditional(tag);
        buffer.writeCompoundTag(tag);
    }
}
