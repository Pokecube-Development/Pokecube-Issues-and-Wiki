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
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.npc.Tasks;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Vector3;

public class NpcMob extends VillagerEntity implements IEntityAdditionalSpawnData
{
    public static final EntityType<NpcMob> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(NpcMob::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> NpcMob.TYPE.create(w)).build("pokecube:npc");
    }

    static final DataParameter<String> NAMEDW = EntityDataManager.<String> defineId(NpcMob.class,
            DataSerializers.STRING);

    private NpcType   type       = NpcType.byType("none");
    public String     playerName = "";
    public String     urlSkin    = "";
    public String     customTex  = "";
    private boolean   male       = true;
    public boolean    stationary = false;
    protected boolean invuln     = false;
    public Vector3    location   = null;
    public GuardAI    guardAI;

    public String  customTrades = "";
    public boolean fixedTrades  = false;

    // Things relevant to fake mob rendering

    public ResourceLocation copyMob = null;
    public MobEntity        copied  = null;
    public CompoundNBT      copyNbt = new CompoundNBT();

    @OnlyIn(Dist.CLIENT)
    public net.minecraft.client.renderer.entity.EntityRenderer<? super MobEntity> customRender = null;

    private Consumer<MerchantOffers> init_offers = t ->
    {
    };

    private Consumer<MerchantOffer> use_offer = t ->
    {
    };

    protected NpcMob(final EntityType<? extends NpcMob> type, final World world)
    {
        super(type, world);
        this.setPersistenceRequired();
        this.location = Vector3.getNewVector();
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(NpcMob.NAMEDW, "");
    }

    private ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> addGuard(final GuardAI guardai,
            final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> addTo)
    {
        final List<Pair<Integer, ? extends Task<? super VillagerEntity>>> temp = Lists.newArrayList(addTo);
        final Pair<Integer, GuardTask<VillagerEntity>> pair = Pair.of(0, new GuardTask<>(this, guardai));
        temp.add(0, pair);
        return ImmutableList.copyOf(temp);
    }

    public void setTypedName(final String name)
    {
        this.setNPCName("pokecube." + this.getNpcType().getName() + ".named:" + name);
    }

    @Override
    protected void registerBrainGoals(final Brain<VillagerEntity> brain)
    {
        final IGuardAICapability guard = this.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        if (guard != null)
        {
            final GuardAI guardai = new GuardAI(this, guard);
            final VillagerProfession profession = this.getVillagerData().getProfession();
            if (this.getNpcType() != null && this.getNpcType().getProfession() != profession) this.setVillagerData(this
                    .getVillagerData().setLevel(3).setProfession(this.getNpcType().getProfession()));
            final float f = (float) this.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            if (this.isBaby())
            {
                brain.setSchedule(Schedules.CHILD);
                brain.addActivity(Activity.PLAY, this.addGuard(guardai, Tasks.play(f)));
            }
            else
            {
                brain.setSchedule(Schedules.ADULT);
                brain.addActivityWithConditions(Activity.WORK, this.addGuard(guardai, Tasks.work(profession, f)),
                        ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)));
            }
            brain.addActivity(Activity.CORE, this.addGuard(guardai, Tasks.core(profession, f)));
            brain.addActivityWithConditions(Activity.MEET, this.addGuard(guardai, Tasks.meet(profession, f)),
                    ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.VALUE_PRESENT)));
            brain.addActivity(Activity.REST, this.addGuard(guardai, Tasks.rest(profession, f)));
            brain.addActivity(Activity.IDLE, this.addGuard(guardai, Tasks.idle(profession, f)));
            brain.addActivity(Activity.PANIC, this.addGuard(guardai, Tasks.panic(profession, f)));
            brain.addActivity(Activity.PRE_RAID, this.addGuard(guardai, Tasks.preRaid(profession, f)));
            brain.addActivity(Activity.RAID, this.addGuard(guardai, Tasks.raid(profession, f)));
            brain.addActivity(Activity.HIDE, this.addGuard(guardai, Tasks.hide(profession, f)));
            brain.addActivity(Activities.STATIONARY, this.addGuard(guardai, Tasks.stationary(profession, f)));
            brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            brain.setDefaultActivity(Activity.IDLE);
            brain.setActiveActivityIfPossible(Activity.IDLE);
            brain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
        }
        else super.registerBrainGoals(brain);
    }

    @Override
    public VillagerEntity getBreedOffspring(final ServerWorld p_241840_1_, final AgeableEntity p_241840_2_)
    {
        return null;
    }

    @Override
    public void thunderHit(final ServerWorld p_241841_1_, final LightningBoltEntity p_241841_2_)
    {
        this.setRemainingFireTicks(this.getRemainingFireTicks() + 1);
        if (this.getRemainingFireTicks() == 0) this.setSecondsOnFire(8);
        this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
    }

    @Override
    public boolean hurt(final DamageSource source, final float i)
    {
        final Entity e = source.getEntity();
        if (e instanceof PlayerEntity && ((PlayerEntity) e).abilities.instabuild && e.isCrouching())
        {
            final PlayerEntity player = (PlayerEntity) e;
            if (player.getMainHandItem().isEmpty()) this.remove();
        }
        if (this.invuln) return false;
        return super.hurt(source, i);
    }

    @Override
    public void setVillagerData(final VillagerData data)
    {
        final MerchantOffers trades = this.offers;
        super.setVillagerData(data);
        this.offers = trades;
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ILivingEntityData finalizeSpawn(final IServerWorld worldIn, final DifficultyInstance difficultyIn,
            final SpawnReason reason, final ILivingEntityData spawnDataIn, final CompoundNBT dataTag)
    {
        final VillagerProfession proff = this.getNpcType().getProfession();
        this.setVillagerData(this.getVillagerData().setProfession(proff).setLevel(3));
        this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus",
                this.random.nextGaussian() * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
        if (this.random.nextFloat() < 0.05F) this.setLeftHanded(true);
        else this.setLeftHanded(false);
        return spawnDataIn;
    }

    public final ResourceLocation getTex()
    {
        if (!this.playerName.isEmpty()) return PokecubeCore.proxy.getPlayerSkin(this.playerName);
        else if (!this.customTex.isEmpty()) return new ResourceLocation(this.customTex);
        else if (!this.urlSkin.isEmpty()) return PokecubeCore.proxy.getUrlSkin(this.urlSkin);
        return this.isMale() ? this.getNpcType().getMaleTex() : this.getNpcType().getFemaleTex();
    }

    @Override
    public ActionResultType mobInteract(final PlayerEntity player, final Hand hand)
    {
        // if () return ActionResultType
        // .sidedSuccess(this.world.isRemote);
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        if (this.getVillagerData().getProfession() == VillagerProfession.NONE)
        {
            final VillagerProfession proff = this.getNpcType().getProfession();
            this.setVillagerData(this.getVillagerData().setProfession(proff).setLevel(3));
        }
        if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0) this.setHealth(
                Math.min(this.getHealth() + 2, this.getMaxHealth()));
    }

    @Override
    public void baseTick()
    {
        super.baseTick();
        if (this.getLevel().isClientSide() && this.copyMob != null)
        {
            if (this.copied == null)
            {
                final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(this.copyMob);
                final Entity entity = type.create(this.getLevel());
                if (entity instanceof MobEntity)
                {
                    this.copied = (MobEntity) entity;
                    try
                    {
                        this.copied.readAdditionalSaveData(this.copyNbt);
                    }
                    catch (final Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else
                {
                    this.copyMob = null;
                    return;
                }
            }
            EntityTools.copyEntityTransforms(this.copied, this);
        }
        else this.copyMob = null;
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT nbt)
    {
        if (this.level instanceof ServerWorld) super.readAdditionalSaveData(nbt);
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.setNPCName(nbt.getString("name"));
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        this.fixedTrades = nbt.getBoolean("fixedTrades");
        this.customTrades = nbt.getString("customTrades");
        this.copied = null;
        if (nbt.contains("copyMob"))
        {
            this.copyMob = new ResourceLocation(nbt.getString("copyMob"));
            this.copyNbt = nbt.getCompound("copyNbt");
        }
        try
        {
            if (nbt.contains("type")) this.setNpcType(NpcType.byType(nbt.getString("type")));
            else this.setNpcType(NpcType.byType("professor"));
        }
        catch (final Exception e)
        {
            this.setNpcType(NpcType.byType("professor"));
            e.printStackTrace();
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        if (this.getNPCName() != null && !this.getNPCName().isEmpty())
        {
            IFormattableTextComponent display;
            if (this.getNPCName().startsWith("pokecube."))
            {
                final String[] args = this.getNPCName().split(":");
                if (args.length == 2) display = new TranslationTextComponent(args[0], args[1]);
                else display = new StringTextComponent(this.getNPCName());
            }
            else display = new StringTextComponent(this.getNPCName());
            display.withStyle((style) ->
            {
                return style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID());
            });
            return display;
        }
        return super.getDisplayName();
    }

    @Override
    public void readSpawnData(final PacketBuffer additionalData)
    {
        final CompoundNBT nbt = additionalData.readNbt();
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.setNPCName(nbt.getString("name"));
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        this.copied = null;
        if (nbt.contains("copyMob"))
        {
            this.copyMob = new ResourceLocation(nbt.getString("copyMob"));
            this.copyNbt = nbt.getCompound("copyNbt");
        }
        try
        {
            if (nbt.contains("type")) this.setNpcType(NpcType.byType(nbt.getString("type")));
            else this.setNpcType(NpcType.byType("professor"));
        }
        catch (final Exception e)
        {
            this.setNpcType(NpcType.byType("professor"));
            e.printStackTrace();
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.getNPCName());
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putBoolean("fixedTrades", this.fixedTrades);
        nbt.putString("customTrades", this.customTrades);
        nbt.putString("type", this.getNpcType().getName());
        if (this.copyMob != null)
        {
            nbt.putString("copyMob", this.copyMob.toString());
            nbt.put("copyNbt", this.copyNbt);
        }
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.getNPCName());
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putString("type", this.getNpcType().getName());
        if (this.copyMob != null)
        {
            nbt.putString("copyMob", this.copyMob.toString());
            nbt.put("copyNbt", this.copyNbt);
        }
        buffer.writeNbt(nbt);
    }

    @Override
    protected void rewardTradeXp(final MerchantOffer offer)
    {
        this.use_offer.accept(offer);
        super.rewardTradeXp(offer);
    }

    public void resetTrades()
    {
        this.offers = null;
    }

    protected void onSetOffers()
    {
    }

    @Override
    public MerchantOffers getOffers()
    {
        if (this.offers == null)
        {
            this.offers = new MerchantOffers();
            this.onSetOffers();
            this.updateTrades();
        }
        return this.offers;
    }

    @Override
    protected void updateTrades()
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

    public String getNPCName()
    {
        return this.entityData.get(NpcMob.NAMEDW);
    }

    public void setNPCName(final String name)
    {
        this.entityData.set(NpcMob.NAMEDW, name);
    }
}
