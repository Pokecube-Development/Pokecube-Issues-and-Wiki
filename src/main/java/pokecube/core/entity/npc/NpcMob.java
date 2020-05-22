package pokecube.core.entity.npc;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.npc.Tasks;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.maths.Vector3;

public class NpcMob extends VillagerEntity implements IEntityAdditionalSpawnData
{
    public static final EntityType<NpcMob> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(NpcMob::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> NpcMob.TYPE.create(w)).build("pokecube:npc");
    }
    private NpcType   type       = NpcType.NONE;
    public String     name       = "";
    public String     playerName = "";
    public String     urlSkin    = "";
    public String     customTex  = "";
    private boolean   male       = true;
    public boolean    stationary = false;
    protected boolean invuln     = false;
    public Vector3    location   = null;
    public GuardAI    guardAI;

    private Consumer<MerchantOffers> init_offers = t ->
    {
    };

    private Consumer<MerchantOffer> use_offer = t ->
    {
    };

    protected NpcMob(final EntityType<? extends NpcMob> type, final World world)
    {
        super(type, world);
        this.enablePersistence();
        this.location = Vector3.getNewVector();
    }

    private ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> addGuard(final GuardAI guardai,
            final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> addTo)
    {
        final List<Pair<Integer, ? extends Task<? super VillagerEntity>>> temp = Lists.newArrayList(addTo);
        final Pair<Integer, GuardTask<VillagerEntity>> pair = Pair.of(0, new GuardTask<>(guardai));
        temp.add(0, pair);
        return ImmutableList.copyOf(temp);
    }

    @Override
    protected void initBrain(final Brain<VillagerEntity> brain)
    {
        final IGuardAICapability guard = this.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        if (guard != null)
        {
            final GuardAI guardai = new GuardAI(this, guard);
            final VillagerProfession profession = this.getVillagerData().getProfession();
            if (this.getNpcType() != null && this.getNpcType().getProfession() != profession) this.setVillagerData(this
                    .getVillagerData().withLevel(3).withProfession(this.getNpcType().getProfession()));
            final float f = (float) this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            if (this.isChild())
            {
                brain.setSchedule(Schedules.CHILD);
                brain.registerActivity(Activity.PLAY, this.addGuard(guardai, Tasks.play(f)));
            }
            else
            {
                brain.setSchedule(Schedules.ADULT);
                brain.registerActivity(Activity.WORK, this.addGuard(guardai, Tasks.work(profession, f)), ImmutableSet
                        .of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)));
            }
            brain.registerActivity(Activity.CORE, this.addGuard(guardai, Tasks.core(profession, f)));
            brain.registerActivity(Activity.MEET, this.addGuard(guardai, Tasks.meet(profession, f)), ImmutableSet.of(
                    Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT)));
            brain.registerActivity(Activity.REST, this.addGuard(guardai, Tasks.rest(profession, f)));
            brain.registerActivity(Activity.IDLE, this.addGuard(guardai, Tasks.idle(profession, f)));
            brain.registerActivity(Activity.PANIC, this.addGuard(guardai, Tasks.panic(profession, f)));
            brain.registerActivity(Activity.PRE_RAID, this.addGuard(guardai, Tasks.preRaid(profession, f)));
            brain.registerActivity(Activity.RAID, this.addGuard(guardai, Tasks.raid(profession, f)));
            brain.registerActivity(Activity.HIDE, this.addGuard(guardai, Tasks.hide(profession, f)));
            brain.registerActivity(Activities.STATIONARY, this.addGuard(guardai, Tasks.stationary(profession, f)));
            brain.setDefaultActivities(ImmutableSet.of(Activity.CORE));
            brain.setFallbackActivity(Activity.IDLE);
            brain.switchTo(Activity.IDLE);
            brain.updateActivity(this.world.getDayTime(), this.world.getGameTime());
        }
        else super.initBrain(brain);
    }

    @Override
    public VillagerEntity createChild(final AgeableEntity ageable)
    {
        return null;
    }

    @Override
    public boolean attackEntityFrom(final DamageSource source, final float i)
    {
        final Entity e = source.getTrueSource();
        if (e instanceof PlayerEntity && ((PlayerEntity) e).abilities.isCreativeMode && e.isSneaking())
        {
            final PlayerEntity player = (PlayerEntity) e;
            if (player.getHeldItemMainhand().isEmpty()) this.remove();
        }
        if (this.invuln) return false;
        return super.attackEntityFrom(source, i);
    }

    @Override
    public void setVillagerData(final VillagerData data)
    {
        final MerchantOffers trades = this.offers;
        super.setVillagerData(data);
        this.offers = trades;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ILivingEntityData onInitialSpawn(final IWorld worldIn, final DifficultyInstance difficultyIn,
            final SpawnReason reason, final ILivingEntityData spawnDataIn, final CompoundNBT dataTag)
    {
        final VillagerProfession proff = this.getNpcType().getProfession();
        this.setVillagerData(this.getVillagerData().withProfession(proff).withLevel(3));
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier(
                "Random spawn bonus", this.rand.nextGaussian() * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
        if (this.rand.nextFloat() < 0.05F) this.setLeftHanded(true);
        else this.setLeftHanded(false);
        return spawnDataIn;
    }

    public ResourceLocation getTex()
    {
        if (!this.playerName.isEmpty()) return PokecubeCore.proxy.getPlayerSkin(this.playerName);
        else if (!this.customTex.isEmpty()) return new ResourceLocation(this.customTex);
        return this.isMale() ? this.getNpcType().getMaleTex() : this.getNpcType().getFemaleTex();
    }

    @Override
    public boolean processInteract(final PlayerEntity player, final Hand hand)
    {
        if (this.getNpcType().getInteraction().processInteract(player, hand, this)) return true;
        return super.processInteract(player, hand);
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
        if (this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0) this
                .setHealth(Math.min(this.getHealth() + 2, this.getMaxHealth()));
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        if (this.world instanceof ServerWorld) super.readAdditional(nbt);
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.name = nbt.getString("name");
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        try
        {
            if (nbt.contains("type")) this.setNpcType(NpcType.byType(nbt.getString("type")));
            else this.setNpcType(NpcType.PROFESSOR);
        }
        catch (final Exception e)
        {
            this.setNpcType(NpcType.PROFESSOR);
            e.printStackTrace();
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        if (this.name != null && !this.name.isEmpty())
        {
            ITextComponent display;
            if (this.name.startsWith("pokecube."))
            {
                final String[] args = this.name.split(":");
                if (args.length == 2) display = new TranslationTextComponent(args[0], args[1]);
                else display = new StringTextComponent(this.name);
            }
            else display = new StringTextComponent(this.name);
            display.applyTextStyle((style) ->
            {
                style.setHoverEvent(this.getHoverEvent()).setInsertion(this.getCachedUniqueIdString());
            });
            return display;
        }
        return super.getDisplayName();
    }

    @Override
    public void readSpawnData(final PacketBuffer additionalData)
    {
        final CompoundNBT nbt = additionalData.readCompoundTag();
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.name = nbt.getString("name");
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        try
        {
            if (nbt.contains("type")) this.setNpcType(NpcType.byType(nbt.getString("type")));
            else this.setNpcType(NpcType.PROFESSOR);
        }
        catch (final Exception e)
        {
            this.setNpcType(NpcType.PROFESSOR);
            e.printStackTrace();
        }
    }

    @Override
    public void writeAdditional(final CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.name);
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putString("type", this.getNpcType().getName());
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.name);
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putString("type", this.getNpcType().getName());
        buffer.writeCompoundTag(nbt);
    }

    @Override
    protected void onVillagerTrade(final MerchantOffer offer)
    {
        this.use_offer.accept(offer);
        super.onVillagerTrade(offer);
    }

    @Override
    protected void populateTradeData()
    {
        if (this.offers != null) this.offers.clear();
        this.init_offers.accept(this.offers);
    }

    public void setInitOffers(final Consumer<MerchantOffers> in)
    {
        this.init_offers = in;
        // Clear offers so that it can be reset.
        this.offers = null;
    }

    public void setUseOffers(final Consumer<MerchantOffer> in)
    {
        this.use_offer = in;
    }

    /**
     * @return the type
     */
    public NpcType getNpcType()
    {
        return this.type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setNpcType(final NpcType type)
    {
        this.type = type;
    }

    /**
     * @return the male
     */
    public boolean isMale()
    {
        return this.male;
    }

    /**
     * @param male
     *            the male to set
     */
    public void setMale(final boolean male)
    {
        this.male = male;
    }
}
