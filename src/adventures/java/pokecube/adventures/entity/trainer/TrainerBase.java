package pokecube.adventures.entity.trainer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasTrades;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasTrades;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.utils.Tools;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.eventhandlers.SpawnHandler;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.List;

public abstract class TrainerBase extends NpcMob
{
    public static final ResourceLocation BRIBE = ResourceLocation.parse(PokecubeAdv.MODID + ":trainer_bribe");

    public List<IPokemob> currentPokemobs = new ArrayList<>();

    private IHasTrades trades;

    int despawncounter = 0;
    boolean fixedMobs = false;

    protected TrainerBase(final EntityType<? extends TrainerBase> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    public DefaultPokemobs getPokemobs()
    {
        return (DefaultPokemobs) this.getData(TrainerCaps.TRAINER);
    }

    public IHasNPCAIStates getAIStates()
    {
        return this.getData(TrainerCaps.AISTATES);
    }

    protected IHasTrades getTradesHolder()
    {
        if (trades == null)
        {
            this.setData(TrainerCaps.TRADES, this.trades = new CapabilityHasTrades.DefaultTrades());
        }
        return trades;
    }

    public boolean canTrade(final Player player)
    {
        final boolean friend = this.getPokemobs().friendlyCooldown >= 0;
        final boolean pity = this.getPokemobs().defeated(player);
        final boolean lost = this.getPokemobs().defeatedBy(player);
        final boolean trades = this.getAIStates().getAIState(AIState.TRADES_ITEMS) || this.getAIStates()
                .getAIState(AIState.TRADES_MOBS);
        return trades && (friend || pity || lost);
    }

    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand)
    {
        final ItemStack stack = player.getItemInHand(hand);
        if (player.getAbilities().instabuild && player.isCrouching())
        {
            if (!this.level().isClientSide && player.isCrouching() && player.getMainHandItem().getItem() == Items.STICK)
                this.getPokemobs().throwCubeAt(player);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (ItemList.is(TrainerBase.BRIBE, stack) && this.getPokemobs().friendlyCooldown <= 0 && !this.getOffers()
                .isEmpty())
        {
            stack.split(1);
            player.setItemInHand(hand, stack);
            this.getPokemobs().onSetTarget(null);
            for (final IPokemob pokemob : this.currentPokemobs) pokemob.onRecall(false);
            this.getPokemobs().friendlyCooldown = 2400;
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
                if (this.getAIStates().getAIState(AIState.TRADES_MOBS) && !fixedTrades)
                    this.addMobTrades(player, stack);

                if (!this.getOffers().isEmpty())
                    this.openTradingScreen(player, this.getDisplayName(), this.getVillagerData().getLevel());
                else this.setTradingPlayer(null);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
        else if (this.getPokemobs().getCooldown() <= 0 && stack.getItem() == Items.STICK)
            this.getPokemobs().onSetTarget(player);

        return InteractionResult.PASS;
    }

    @Override
    public void tick()
    {
        this.invuln = true;
        if (PokecubeAdv.config.trainerAIPause)
        {
            final Player near = this.level().getNearestPlayer(this, -1);
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
    public void aiStep()
    {
        super.aiStep();
        if (!this.isEffectiveAi()) return;
        if (this.getPokemobs().countPokemon() == 0 && !this.fixedMobs)
        {
            final TypeTrainer type = this.getPokemobs().getType();
            if (type != null && !type.pokemon.isEmpty() && !this.checkedMobs)
            {
                this.checkedMobs = true;
                SpawnContext context = new SpawnContext(null, (ServerLevel) level, type.pokemon.getFirst(),
                        new Vector3().set(this), level.getDayTime(), SpawnEvent.SpawnSurface.of(type.pokemon.getFirst()));
                final int level = SpawnHandler.getSpawnLevel(context);
                TrainerSpawnHandler.initTrainer(this.getPokemobs(), level);
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
        this.getTradesHolder().setOffers(this.offers = null);
    }

    @Override
    protected void rewardTradeXp(final MerchantOffer offer)
    {
        this.getTradesHolder().applyTrade(offer);
        super.rewardTradeXp(offer);
    }

    @Override
    public void setTradingPlayer(final Player player)
    {
        this.getTradesHolder().setCustomer(player);
        super.setTradingPlayer(player);
    }

    protected abstract void addMobTrades(final Player player, final ItemStack stack);

    @Override
    protected void onSetOffers()
    {
        this.getTradesHolder().setOffers(this.offers);
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
        this.getTradesHolder().verify(stack);
        super.notifyTradeUpdated(stack);
    }

    /** @return the male */
    @Override
    public boolean isMale()
    {
        return this.getPokemobs().getGender() == 1;
    }

    /**
     * @param male the male to set
     */
    @Override
    public void setMale(final boolean male)
    {
        super.setMale(male);
        this.getPokemobs().setGender((byte) (male ? 1 : 2));
    }
}
