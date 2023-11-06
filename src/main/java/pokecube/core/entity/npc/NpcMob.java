package pokecube.core.entity.npc;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.network.NetworkHooks;
import pokecube.api.events.npcs.NpcBreedEvent;
import pokecube.api.events.npcs.NpcEvent;
import pokecube.api.events.npcs.NpcTradesEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.npc.Activities;
import pokecube.core.ai.npc.Tasks;
import pokecube.core.ai.routes.GuardAI;
import pokecube.core.ai.routes.GuardTask;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.entity.ai.BrainUtil;
import thut.api.inventory.npc.NpcContainer;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class NpcMob extends Villager implements IEntityAdditionalSpawnData
{
    static final EntityDataAccessor<String> NAMEDW = SynchedEntityData.<String>defineId(NpcMob.class,
            EntityDataSerializers.STRING);

    private NpcType type = NpcType.byType("none");
    public String playerName = "";
    public String urlSkin = "";
    public String customTex = "";
    private boolean male = true;
    public boolean stationary = false;
    protected boolean invuln = false;
    public Vector3 location = null;
    public GuardAI guardAI;

    public String customTrades = "";
    public boolean fixedTrades = false;

    private Consumer<MerchantOffers> init_offers = t -> {};

    private Consumer<MerchantOffer> use_offer = t -> {};

    public NpcMob(final EntityType<? extends NpcMob> type, final Level world)
    {
        super(type, world);
        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
        this.location = new Vector3();
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
        super.registerBrainGoals(brain);

        VillagerProfession villagerprofession = this.getVillagerData().getProfession();
        // Replace the default idle set with ours, with a different canBreed
        // system.
        brain.addActivity(Activity.IDLE, Tasks.getIdlePackage(villagerprofession, 0.5F));

        final IGuardAICapability guard = CapHolders.getGuardAI(this);
        if (guard != null)
        {
            final GuardAI guardai = new GuardAI(this, guard);
            VillagerProfession profession = this.getVillagerData().getProfession();
            final float f = 0.5f;

            Collection<Pair<Integer, ? extends Behavior<? super LivingEntity>>> args = Lists
                    .newArrayList(Pair.of(0, new GuardTask<>(this, guardai)));

            Set<Activity> acts = brain.activityRequirements.keySet();
            for (Activity act : acts) BrainUtil.addToActivity(brain, act, args);

            brain.addActivity(Activities.STATIONARY.get(), this.addGuard(guardai, Tasks.stationary(profession, f)));
            brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            brain.setDefaultActivity(Activity.IDLE);
            brain.setActiveActivityIfPossible(Activity.IDLE);
            brain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
        }
    }

    @Override
    public Villager getBreedOffspring(final ServerLevel level, final AgeableMob mob)
    {
        if (!(mob instanceof NpcMob npc)) return null;
        if (npc.isMale() == this.isMale()) return null;

        EntityType<?> other = level.getRandom().nextBoolean() ? this.getType() : mob.getType();
        NpcMob villager = (NpcMob) other.create(level);

        NpcType newType = NpcType.getRandomForLocation(new Vector3().set(mob), level);
        if (newType == null || level.getRandom().nextDouble() > 0.5)
        {
            newType = NpcType.byType("none");
            if (level.getRandom().nextDouble() > 0.5)
            {
                newType = level.getRandom().nextBoolean() ? this.getNpcType() : npc.getNpcType();
            }
        }

        villager.setMale(mob.getRandom().nextBoolean());

        villager.setNpcType(newType);

        villager.finalizeSpawn(level, level.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.BREEDING,
                (SpawnGroupData) null, (CompoundTag) null);
        return villager;
    }

    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt bolt)
    {
        this.setRemainingFireTicks(this.getRemainingFireTicks() + 1);
        if (this.getRemainingFireTicks() == 0) this.setSecondsOnFire(8);
        this.hurt(DamageSource.LIGHTNING_BOLT, 5.0F);
    }

    @Override
    public boolean hurt(final DamageSource source, final float i)
    {
        final Entity e = source.getEntity();
        if (e instanceof Player player && player.getAbilities().instabuild && e.isCrouching())
            if (player.getMainHandItem().isEmpty()) this.discard();
        if (this.invuln) return false;
        return super.hurt(source, i);
    }

    @Override
    public void setVillagerData(final VillagerData data)
    {
        final MerchantOffers trades = this.offers;
        super.setVillagerData(data);
        if (this.customTrades == null) this.customTrades = "";
        if (this.fixedTrades || !this.customTrades.isEmpty()) this.offers = trades;
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
        NpcEvent.OpenInventory event = new NpcEvent.OpenInventory(this);
        ThutCore.FORGE_BUS.post(event);
        if (event.getResult() == Result.ALLOW)
        {
            if (player instanceof ServerPlayer sp)
            {
                final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
                buffer.writeInt(this.getId());
                final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new NpcContainer(i, p, buffer),
                        this.getDisplayName());
                NetworkHooks.openGui(sp, provider, buf -> {
                    buf.writeInt(this.getId());
                });
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        if (this.getOffers().isEmpty()) this.updateTrades();
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canBreed()
    {
        var event = new NpcBreedEvent.Check(this);
        ThutCore.FORGE_BUS.post(event);
        if (event.isCanceled()) return false;
        return super.canBreed();
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        if (this.tickCount % 20 != 0) return;

        this.getNpcType().onAITick(this);

        if (this.getVillagerData().getProfession() != this.getNpcType().getProfession())
        {
            if (this.getNpcType().getProfession() == VillagerProfession.NONE)
            {
                String prof = this.getVillagerData().getProfession().toString();
                NpcType type = NpcType.byType(prof);
                this.setNpcType(type);
            }
            else
            {
                // Initialise exp as 1, so we don't get reset by the reset
                // profession task in the AI. This does get us a free 10% of the
                // way to the next level, but at least it prevents the
                // resetting.
                this.setVillagerXp(1);
                this.setVillagerData(this.getVillagerData().setProfession(this.getNpcType().getProfession()));
            }
        }

        if (this.getVillagerData().getProfession() != VillagerProfession.NONE
                && this.getNpcType().getName().equals("none"))
        {
            String prof = this.getVillagerData().getProfession().toString();
            NpcType type = NpcType.byType(prof);
            this.setNpcType(type);
        }
        if (this.getHealth() < this.getMaxHealth() && this.getHealth() > 0)
            this.setHealth(Math.min(this.getHealth() + 2, this.getMaxHealth()));
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
            if (nbt.contains("type")) this.type = (NpcType.byType(nbt.getString("type")));
            else this.type = (NpcType.byType("professor"));
        }
        catch (final Exception e)
        {
            this.type = (NpcType.byType("professor"));
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
                if (args.length == 2) display = TComponent.translatable(args[0], args[1]);
                else display = TComponent.literal(this.getNPCName());
            }
            else display = TComponent.literal(this.getNPCName());
            display.withStyle((style) -> {
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
            if (nbt.contains("type")) this.type = (NpcType.byType(nbt.getString("type")));
            else this.type = (NpcType.byType("professor"));
        }
        catch (final Exception e)
        {
            this.type = (NpcType.byType("professor"));
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
    {}

    @Override
    public MerchantOffers getOffers()
    {
        if (this.offers == null)
        {
            this.offers = new MerchantOffers();
            // Vanilla code will cancel our trade anyway here, so return early.
            if (this.getVillagerData().getProfession() == VillagerProfession.NONE) return this.offers;
            this.onSetOffers();
            this.updateTrades();
        }
        return this.offers;
    }

    @Override
    public void updateTrades()
    {
        if (this.offers == null) this.offers = new MerchantOffers();

        // Vanilla code will cancel our trade anyway here, so return early.
        if (this.getVillagerData().getProfession() == VillagerProfession.NONE) return;

        // If we don't have custom trades, then we try to apply by profession.
        if (customTrades.isEmpty())
        {
            // Villager type first:
            if (this.getNpcType().getProfession() != VillagerProfession.NITWIT) super.updateTrades();
            // Next try custom ones
            VillagerData villagerdata = this.getVillagerData();;
            VillagerTrades.ItemListing[] itemListings = type.getTrades(villagerdata.getLevel());
            if (itemListings != null) this.addOffersFromItemListings(this.offers, itemListings, 2);
        }
        // Now add the defaults
        this.init_offers.accept(this.offers);
        // Then post the event
        ThutCore.FORGE_BUS.post(new NpcTradesEvent(this, offers));
    }

    public void setInitOffers(final Consumer<MerchantOffers> in)
    {
        this.init_offers = in;
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
     * @param type the type to set
     */
    public void setNpcType(final NpcType type)
    {
        this.type = type;
        if (this.getVillagerData().getProfession() != type.getProfession())
        {
            // Force exp of 1, otherwise the profession gets reset by the code
            // for cleaning up professions of un-used npcs.
            if (this.getVillagerXp() < 1 && type.getProfession() != VillagerProfession.NITWIT) this.setVillagerXp(1);
            this.getVillagerData().setProfession(type.getProfession());
        }
    }

    /**
     * @return the male
     */
    public boolean isMale()
    {
        return this.male;
    }

    /**
     * @param male the male to set
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
