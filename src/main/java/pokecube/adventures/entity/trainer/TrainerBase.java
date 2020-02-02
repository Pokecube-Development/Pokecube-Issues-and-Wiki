package pokecube.adventures.entity.trainer;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasTrades;
import pokecube.adventures.capabilities.CapabilityHasTrades.IHasTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.events.TrainerSpawnHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;

public abstract class TrainerBase extends NpcMob
{
    public static final ResourceLocation BRIBE = new ResourceLocation(PokecubeAdv.ID, "trainer_bribe");

    public List<IPokemob>  currentPokemobs = new ArrayList<>();
    public DefaultPokemobs pokemobsCap;
    public IHasMessages    messages;
    public IHasRewards     rewardsCap;
    public IHasNPCAIStates aiStates;
    public IHasTrades      trades;
    int                    despawncounter  = 0;
    public String          customTrades    = "";
    boolean                fixedTrades     = false;

    protected TrainerBase(final EntityType<? extends TrainerBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.pokemobsCap = (DefaultPokemobs) this.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP).orElse(null);
        this.rewardsCap = this.getCapability(CapabilityHasRewards.REWARDS_CAP).orElse(null);
        this.messages = this.getCapability(CapabilityNPCMessages.MESSAGES_CAP).orElse(null);
        this.aiStates = this.getCapability(CapabilityNPCAIStates.AISTATES_CAP).orElse(null);
        this.trades = this.getCapability(CapabilityHasTrades.CAPABILITY).orElse(null);

        this.aiStates.setAIState(IHasNPCAIStates.TRADES, PokecubeAdv.config.trainersTradeItems
                || PokecubeAdv.config.trainersTradeMobs);

        // Add some ai goals
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
    }

    @Override
    public boolean processInteract(final PlayerEntity player, final Hand hand)
    {
        final ItemStack stack = player.getHeldItem(hand);
        if (player.abilities.isCreativeMode && player.isSneaking())
        {
            if (this.pokemobsCap.getType() != null && !this.getEntityWorld().isRemote && stack.isEmpty())
            {
                String message = this.getName() + " " + this.aiStates.getAIState(IHasNPCAIStates.STATIONARY) + " "
                        + this.pokemobsCap.countPokemon() + " ";
                for (int ind = 0; ind < this.pokemobsCap.getMaxPokemobCount(); ind++)
                {
                    final ItemStack i = this.pokemobsCap.getPokemob(ind);
                    if (!i.isEmpty()) message += i.getDisplayName() + " ";
                }
                player.sendMessage(new StringTextComponent(message));
            }
            else if (!this.getEntityWorld().isRemote && player.isSneaking() && player.getHeldItemMainhand()
                    .getItem() == Items.STICK) this.pokemobsCap.throwCubeAt(player);
            else if (player.getHeldItemMainhand().getItem() == Items.STICK) this.pokemobsCap.setTarget(player);
            return true;
        }
        else if (PokecubeItems.is(TrainerBase.BRIBE, stack) && this.pokemobsCap.friendlyCooldown <= 0 && !this
                .getOffers().isEmpty())
        {
            stack.split(1);
            player.setHeldItem(hand, stack);
            this.pokemobsCap.setTarget(null);
            for (final IPokemob pokemob : this.currentPokemobs)
                pokemob.onRecall(false);
            this.pokemobsCap.friendlyCooldown = 2400;
            this.func_213711_eb();// Celebrate, should make some fancy sounds.
            return true;
        }
        else if (this.pokemobsCap.friendlyCooldown >= 0 && this.aiStates.getAIState(IHasNPCAIStates.TRADES))
        {
            this.setCustomer(player);
            if (!this.fixedTrades)
            {
                this.populateTradeData();
                this.addMobTrades(player, stack);
            }
            if (!this.getOffers().isEmpty()) this.func_213707_a(player, this.getDisplayName(), 0);
            else this.setCustomer(null);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
        if (!this.isServerWorld()) return;
        if (this.ticksExisted % 20 == 1) TrainerSpawnHandler.addTrainerCoord(this);
        if (this.pokemobsCap.getOutID() != null && this.pokemobsCap.getOutMob() == null)
        {
            final Entity mob = this.getServer().getWorld(this.dimension).getEntityByUuid(this.pokemobsCap.getOutID());
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            this.pokemobsCap.setOutMob(pokemob);
            if (this.pokemobsCap.getOutMob() == null) this.pokemobsCap.setOutID(null);
        }
        if (this.pokemobsCap.countPokemon() == 0 && !this.aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                && !this.aiStates.getAIState(IHasNPCAIStates.PERMFRIENDLY))
        {
            // Do not despawn if there is a player nearby.
            if (Tools.isAnyPlayerInRange(10, this)) return;
            this.despawncounter++;
            if (this.despawncounter > 200) this.remove();
            return;
        }
        if (this.ticksExisted % 20 == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0) this
                .setHealth(Math.min(this.getHealth() + 1, this.getMaxHealth()));
        this.despawncounter = 0;
    }

    @Override
    public void remove()
    {
        EventsHandler.recallAllPokemobs(this);
        TrainerSpawnHandler.removeTrainer(this);
        super.remove();
    }

    public void resetTrades()
    {
        this.trades.setOffers(null);
    }

    @Override
    protected void func_213713_b(final MerchantOffer offer)
    {
        this.trades.applyTrade(offer);
    }

    @Override
    public void setCustomer(final PlayerEntity player)
    {
        this.trades.setCustomer(player);
        super.setCustomer(player);
    }

    protected abstract void addMobTrades(final PlayerEntity player, final ItemStack stack);

    @Override
    protected void populateTradeData()
    {
        final Random rand = new Random(this.getUniqueID().getLeastSignificantBits());
        this.getOffers().clear();
        if (this.customTrades.isEmpty()) this.getOffers().addAll(this.pokemobsCap.getType().getRecipes(rand));
        else
        {
            final TrainerTrades trades = TypeTrainer.tradesMap.get(this.customTrades);
            if (trades != null) trades.addTrades(this.getOffers(), rand);
        }
    }

    @Override
    public MerchantOffers getOffers()
    {
        return this.trades.getOffers();
    }

    @Override
    public boolean func_213705_dZ()
    {
        // Not sure what this does, wandering is false, village is true?
        return super.func_213705_dZ();
    }

    @Override
    public void verifySellingItem(final ItemStack stack)
    {
        this.trades.verify(stack);
        super.verifySellingItem(stack);
    }

    @Override
    public void func_213707_a(final PlayerEntity player, final ITextComponent tittle, final int level)
    {
        // This is the player specific get recipes and open inventory thing
        final OptionalInt optionalint = player.openContainer(new SimpleNamedContainerProvider((int_unk_2,
                player_inventory, unk) ->
        {
            return new MerchantContainer(int_unk_2, player_inventory, this);
        }, tittle));
        if (optionalint.isPresent())
        {
            final MerchantOffers merchantoffers = this.getOffers();
            // TODO here we add in a hook to see if we want to trade pokemobs.
            if (!merchantoffers.isEmpty()) player.func_213818_a(optionalint.getAsInt(), merchantoffers, level, this
                    .getXp(), this.func_213705_dZ(), this.func_223340_ej());
        }

    }

    /**
     * @return the male
     */
    @Override
    public boolean isMale()
    {
        return this.pokemobsCap.getGender() == 1;
    }

    /**
     * @param male
     *            the male to set
     */
    @Override
    public void setMale(final boolean male)
    {
        if (male) this.pokemobsCap.setGender((byte) (male ? 1 : 2));
    }

    @Override
    public NpcType getNpcType()
    {
        return this.pokemobsCap.getType();
    }
}
