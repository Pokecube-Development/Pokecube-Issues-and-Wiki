package pokecube.adventures.entity.trainer;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.network.NetworkHooks;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasTrades.IHasTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.npc.NpcEvent;
import pokecube.core.events.pokemob.SpawnEvent.SpawnContext;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;
import thut.api.Tracker;
import thut.api.inventory.npc.NpcContainer;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public abstract class TrainerBase extends NpcMob
{
    public static final ResourceLocation BRIBE = new ResourceLocation(PokecubeAdv.MODID, "trainer_bribe");

    public List<IPokemob> currentPokemobs = new ArrayList<>();
    public DefaultPokemobs pokemobsCap;
    public IHasMessages messages;
    public IHasRewards rewardsCap;
    public IHasNPCAIStates aiStates;
    public IHasTrades trades;

    int despawncounter = 0;
    boolean fixedMobs = false;

    protected TrainerBase(final EntityType<? extends TrainerBase> type, final Level worldIn)
    {
        super(type, worldIn);
        this.pokemobsCap = (DefaultPokemobs) this.getCapability(TrainerCaps.HASPOKEMOBS_CAP).orElse(null);
        this.rewardsCap = this.getCapability(TrainerCaps.REWARDS_CAP).orElse(null);
        this.messages = this.getCapability(TrainerCaps.MESSAGES_CAP).orElse(null);
        this.aiStates = this.getCapability(TrainerCaps.AISTATES_CAP).orElse(null);
        this.trades = this.getCapability(TrainerCaps.TRADES_CAP).orElse(null);
    }

    public boolean canTrade(final Player player)
    {
        final boolean friend = this.pokemobsCap.friendlyCooldown >= 0;
        final boolean pity = this.pokemobsCap.defeated(player);
        final boolean lost = this.pokemobsCap.defeatedBy(player);
        final boolean trades = this.aiStates.getAIState(AIState.TRADES_ITEMS)
                || this.aiStates.getAIState(AIState.TRADES_MOBS);
        return trades && (friend || pity || lost);
    }

    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand)
    {
        NpcEvent.OpenInventory event = new NpcEvent.OpenInventory(this);
        MinecraftForge.EVENT_BUS.post(event);

        boolean creativeStick = player.isCreative() && player.getItemInHand(hand).getItem() == Items.STICK;

        if (event.getResult() == Result.ALLOW || creativeStick)
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

        final ItemStack stack = player.getItemInHand(hand);
        if (player.getAbilities().instabuild && player.isCrouching())
        {
            if (!this.getLevel().isClientSide && player.isCrouching()
                    && player.getMainHandItem().getItem() == Items.STICK)
                this.pokemobsCap.throwCubeAt(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (ItemList.is(TrainerBase.BRIBE, stack) && this.pokemobsCap.friendlyCooldown <= 0
                && !this.getOffers().isEmpty())
        {
            stack.split(1);
            player.setItemInHand(hand, stack);
            this.pokemobsCap.onSetTarget(null);
            for (final IPokemob pokemob : this.currentPokemobs) pokemob.onRecall(false);
            this.pokemobsCap.friendlyCooldown = 2400;
            this.playCelebrateSound();
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (this.canTrade(player))
        {
            final boolean customer = player == this.getTradingPlayer();
            if (customer) return InteractionResult.sidedSuccess(this.level.isClientSide);
            this.setTradingPlayer(player);

            boolean reset_trades = !this.getNpcType().hasTrades(this.getVillagerData().getLevel()) && !fixedTrades;

            if (reset_trades && !this.level.isClientSide)
            {
                this.resetTrades();
                // This re-fills the default trades
                this.updateTrades();
            }

            if (!this.level.isClientSide)
            {
                // This adds in pokemobs to trade.
                if (this.aiStates.getAIState(AIState.TRADES_MOBS) && !fixedTrades) this.addMobTrades(player, stack);

                if (!this.getOffers().isEmpty())
                    this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
                else this.setTradingPlayer(null);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (this.pokemobsCap.getCooldown() <= 0 && stack.getItem() == Items.STICK)
            this.pokemobsCap.onSetTarget(player);

        return InteractionResult.PASS;
    }

    @Override
    public void tick()
    {
        this.invuln = true;
        if (PokecubeAdv.config.trainerAIPause)
        {
            final Player near = this.getLevel().getNearestPlayer(this, -1);
            if (near != null)
            {
                final float dist = near.distanceTo(this);
                if (dist > PokecubeAdv.config.aiPauseDistance) return;
            }
        }
        this.invuln = false;
        super.tick();
    }

    private boolean checkedMobs = false;

    @Override
    public void onAddedToWorld()
    {
        TrainerTracker.add(this);
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld()
    {
        TrainerTracker.removeTrainer(this);
        super.onRemovedFromWorld();
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        if (!this.isEffectiveAi()) return;

        ItemStack cube = this.pokemobsCap.getNextPokemob();
        ItemStack reward = this.rewardsCap.getRewards().isEmpty() ? ItemStack.EMPTY
                : this.rewardsCap.getRewards().get(0).stack;
        if (this.pokemobsCap.getCooldown() > Tracker.instance().getTick())
        {
            cube = ItemStack.EMPTY;
            reward = ItemStack.EMPTY;
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, cube);
        this.setItemInHand(InteractionHand.OFF_HAND, reward);

        if (this.pokemobsCap.countPokemon() == 0 && !this.fixedMobs)
        {
            final TypeTrainer type = this.pokemobsCap.getType();
            if (type != null && !type.pokemon.isEmpty() && !this.checkedMobs)
            {
                this.checkedMobs = true;
                SpawnContext context = new SpawnContext(null, (ServerLevel) level, type.pokemon.get(0),
                        new Vector3().set(this));
                final int level = SpawnHandler.getSpawnLevel(context);
                this.initTeam(level);
                type.initTrainerItems(this);
            }
            if (PokecubeAdv.config.cullNoMobs)
            {
                // Do not despawn if there is a player nearby.
                if (Tools.isAnyPlayerInRange(10, this)) return;
                this.despawncounter++;
                if (this.despawncounter > 200) this.discard();
                return;
            }
        }
        this.despawncounter = 0;
    }

    @Override
    public void remove(final Entity.RemovalReason removalReason)
    {
        EventsHandler.recallAllPokemobs(this);
        super.remove(removalReason);
    }

    @Override
    public void resetTrades()
    {
        super.resetTrades();
        this.trades.setOffers(this.offers = null);
    }

    @Override
    protected void rewardTradeXp(final MerchantOffer offer)
    {
        this.trades.applyTrade(offer);
        super.rewardTradeXp(offer);
    }

    @Override
    public void setTradingPlayer(final Player player)
    {
        this.trades.setCustomer(player);
        super.setTradingPlayer(player);
    }

    @Override
    public void setNpcType(final NpcType type)
    {
        super.setNpcType(type);
        if (this.pokemobsCap != null && type instanceof TypeTrainer)
        {
            this.pokemobsCap.setType((TypeTrainer) type);
            this.pokemobsCap.getType().initTrainerItems(this);
        }
    }

    public abstract void initTeam(int level);

    protected abstract void addMobTrades(final Player player, final ItemStack stack);

    @Override
    protected void onSetOffers()
    {
        this.trades.setOffers(this.offers);
    }

    @Override
    public boolean showProgressBar()
    {
        // Not sure what this does, wandering is false, village is true?
        return super.showProgressBar();
    }

    @Override
    public void notifyTradeUpdated(final ItemStack stack)
    {
        this.trades.verify(stack);
        super.notifyTradeUpdated(stack);
    }

    @Override
    public boolean canRestock()
    {
        return true;
    }

    /** @return the male */
    @Override
    public boolean isMale()
    {
        return this.pokemobsCap.getGender() == 1;
    }

    /**
     * @param male the male to set
     */
    @Override
    public void setMale(final boolean male)
    {
        super.setMale(male);
        this.pokemobsCap.setGender((byte) (male ? 1 : 2));
    }

    @Override
    public NpcType getNpcType()
    {
        if (this.pokemobsCap == null) return super.getNpcType();
        return this.pokemobsCap.getType();
    }
}
