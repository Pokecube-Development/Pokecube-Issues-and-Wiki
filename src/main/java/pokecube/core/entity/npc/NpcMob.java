package pokecube.core.entity.npc;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Schedules;
import pokecube.core.ai.npc.Tasks;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.maths.Vector3;

public class NpcMob extends Villager implements IEntityAdditionalSpawnData
{
    public static final EntityType<NpcMob> TYPE;

    static
    {
        TYPE = EntityType.Builder.of(NpcMob::new, MobCategory.CREATURE).setCustomClientFactory((s,
                w) -> NpcMob.TYPE.create(w)).build("pokecube:npc");
    }

    static final EntityDataAccessor<String> NAMEDW = SynchedEntityData.<String> defineId(NpcMob.class,
            EntityDataSerializers.STRING);

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

    private Consumer<MerchantOffers> init_offers = t ->
    {
    };

    private Consumer<MerchantOffer> use_offer = t ->
    {
    };

    protected NpcMob(final EntityType<? extends NpcMob> type, final Level world)
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

    private ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> addGuard(final GuardAI guardai,
            final ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> addTo)
    {
        final List<Pair<Integer, ? extends Behavior<? super Villager>>> temp = Lists.newArrayList(addTo);
        final Pair<Integer, GuardTask<Villager>> pair = Pair.of(0, new GuardTask<>(this, guardai));
        temp.add(0, pair);
        return ImmutableList.copyOf(temp);
    }

    public void setTypedName(final String name)
    {
        this.setNPCName("pokecube." + this.getNpcType().getName() + ".named:" + name);
    }

    @Override
    public void registerBrainGoals(final Brain<Villager> brain)
    {
        final IGuardAICapability guard = this.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        if (guard != null)
        {
            final GuardAI guardai = new GuardAI(this, guard);
            final VillagerProfession profession = this.getVillagerData().getProfession();
            if (this.getNpcType() != null && this.getNpcType().getProfession() != profession) this.setVillagerData(this
                    .getVillagerData().setLevel(3).setProfession(this.getNpcType().getProfession()));
            final float f = 0.5f;
            if (this.isBaby())
            {
                brain.setSchedule(Schedules.CHILD);
                brain.addActivity(Activity.PLAY, this.addGuard(guardai, Tasks.play(f)));
            }
            else
            {
                brain.setSchedule(Schedules.ADULT);
                brain.addActivityWithConditions(Activity.WORK, this.addGuard(guardai, Tasks.work(profession, f)),
                        ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));
            }
            brain.addActivity(Activity.CORE, this.addGuard(guardai, Tasks.core(profession, f)));
            brain.addActivityWithConditions(Activity.MEET, this.addGuard(guardai, Tasks.meet(profession, f)),
                    ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
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
    public Villager getBreedOffspring(final ServerLevel p_241840_1_, final AgeableMob p_241840_2_)
    {
        return null;
    }

    @Override
    public void thunderHit(final ServerLevel p_241841_1_, final LightningBolt p_241841_2_)
    {
        this.setRemainingFireTicks(this.getRemainingFireTicks() + 1);
        if (this.getRemainingFireTicks() == 0) this.setSecondsOnFire(8);
        this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
    }

    @Override
    public boolean hurt(final DamageSource source, final float i)
    {
        final Entity e = source.getEntity();
        if (e instanceof Player && ((Player) e).getAbilities().instabuild && e.isCrouching())
        {
            final Player player = (Player) e;
            if (player.getMainHandItem().isEmpty()) this.discard();
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
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor worldIn, final DifficultyInstance difficultyIn,
            final MobSpawnType reason, final SpawnGroupData spawnDataIn, final CompoundTag dataTag)
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
    public InteractionResult mobInteract(final Player player, final InteractionHand hand)
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
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        if (this.level instanceof ServerLevel) super.readAdditionalSaveData(nbt);
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.setNPCName(nbt.getString("name"));
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        this.fixedTrades = nbt.getBoolean("fixedTrades");
        this.customTrades = nbt.getString("customTrades");
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
    public Component getDisplayName()
    {
        if (this.getNPCName() != null && !this.getNPCName().isEmpty())
        {
            MutableComponent display;
            if (this.getNPCName().startsWith("pokecube."))
            {
                final String[] args = this.getNPCName().split(":");
                if (args.length == 2) display = new TranslatableComponent(args[0], args[1]);
                else display = new TextComponent(this.getNPCName());
            }
            else display = new TextComponent(this.getNPCName());
            display.withStyle((style) ->
            {
                return style.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID());
            });
            return display;
        }
        return super.getDisplayName();
    }

    @Override
    public void readSpawnData(final FriendlyByteBuf additionalData)
    {
        final CompoundTag nbt = additionalData.readNbt();
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.setNPCName(nbt.getString("name"));
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
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
    public void addAdditionalSaveData(final CompoundTag nbt)
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
    }

    @Override
    public void writeSpawnData(final FriendlyByteBuf buffer)
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.getNPCName());
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putString("type", this.getNpcType().getName());
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
