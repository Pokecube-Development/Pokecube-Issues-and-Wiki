package pokecube.api.data.trainers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.trainers.TradeData.*;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.npc.NpcType;
import thut.api.ThutAPI;
import thut.api.util.ResourceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypeTrainer extends NpcType
{
    public static String TRAINERTEXTUREPATH;
    public static HashMap<String, TypeTrainer.TrainerTrades> tradesMap = Maps.newHashMap();
    public static HashMap<String, TypeTrainer> typeMap = new HashMap<>();

    public static ArrayList<String> maleNames = new ArrayList<>();
    public static ArrayList<String> femaleNames = new ArrayList<>();

    public static TypeTrainer merchant = new TypeTrainer("merchant");

    public static boolean trainerslevel = false;
    public static int trainerSightRange = 8;

    static
    {
        TypeTrainer.merchant.tradeTemplate = "merchant";
    }

    public static void addTrainer(final String name, final TypeTrainer type)
    {
        TypeTrainer.typeMap.put(name, type);
    }
    public static TypeTrainer getTrainer(final String name, final boolean create)
    {
        final TypeTrainer ret = TypeTrainer.typeMap.get(name);
        if (ret == null)
        {
            for (final TypeTrainer t : TypeTrainer.typeMap.values())
                if (t != null && t.getName().equalsIgnoreCase(name)) return t;
            if (create && !name.isEmpty())
            {
                NpcType existing = NpcType.byType(name);
                if (existing.getName().equalsIgnoreCase(name)) return new TypeTrainer(existing);
                return new TypeTrainer(name);
            }
            return merchant;
        }
        return ret;
    }

    public static TypeTrainer getTrainer(NpcType npcType)
    {
        if (npcType == null) return merchant;
        return getTrainer(npcType.getName(), true);
    }
    
    /** 1 = male, 2 = female, 3 = both */
    public byte genders = 1;

    public boolean hasBelt = true, holdsReward = true;

    public Map<String, List<ItemStack>> wornItems = Maps.newHashMap();
    public String tradeTemplate = "default";
    public List<PokedexEntry> pokemon = Lists.newArrayList();
    public TrainerTrades trades;
    private boolean checkedTex = false;
    public int overrideLevel = -1;


    private final ItemStack[] loot = NonNullList.withSize(4, ItemStack.EMPTY).toArray(new ItemStack[4]);

    public String drops = "";
    public ItemStack held = ItemStack.EMPTY;

    // Temporary list used to load in the allowed mobs.
    public List<String> pokelist;

    public TypeTrainer(NpcType wrapped)
    {
        super(wrapped.getName());
        TypeTrainer.addTrainer(wrapped.getName(), this);
        this.setFemaleTex(wrapped.getFemaleTex());
        this.setMaleTex(wrapped.getMaleTex());
        this.setProfession(wrapped.getProfession());
        this.setInteraction(wrapped.getInteraction());
        hasBelt = holdsReward = false;
    }

    public TypeTrainer(String name)
    {
        super(name);
        TypeTrainer.addTrainer(name, this);
        this.setFemaleTex(ResourceLocation.parse(TRAINERTEXTUREPATH + ThutAPI.trim(this.getName()) + "_female.png"));
        this.setMaleTex(ResourceLocation.parse(TRAINERTEXTUREPATH + ThutAPI.trim(this.getName()) + "_male.png"));
    }

    public Collection<MerchantOffer> getRecipes(final Entity trader, final RandomSource rand)
    {
        if (this.trades == null && this.tradeTemplate != null)
            this.trades = TypeTrainer.tradesMap.get(this.tradeTemplate);
        final List<MerchantOffer> ret = Lists.newArrayList();
        if (this.trades != null) this.trades.addTrades(trader, ret, rand);
        return ret;
    }

    @OnlyIn(Dist.CLIENT)
    private void checkTex()
    {
        if (!this.checkedTex)
        {
            this.checkedTex = true;
            // Initial pass to find a tex
            if (!this.texExists(this.getFemaleTex()))
                this.setFemaleTex(ResourceLocation.parse(TRAINERTEXTUREPATH + ThutAPI.trim(this.getName()) + ".png"));
            if (!this.texExists(this.getMaleTex()))
                this.setMaleTex(ResourceLocation.parse(TRAINERTEXTUREPATH + ThutAPI.trim(this.getName()) + ".png"));

            // Second pass to override with vanilla
            if (!this.texExists(this.getFemaleTex()))
                this.setFemaleTex(ResourceLocation.parse("textures/entity/alex.png"));
            if (!this.texExists(this.getMaleTex()))
                this.setMaleTex(ResourceLocation.parse("textures/entity/steve.png"));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getMaleTex()
    {
        this.checkTex();
        return super.getMaleTex();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getFemaleTex()
    {
        this.checkTex();
        return super.getFemaleTex();
    }

    private void initLoot()
    {
        if (!this.loot[0].isEmpty()) return;

        if (!this.drops.isEmpty())
        {
            final String[] args = this.drops.split(":");
            int num = 0;
            for (final String s : args)
            {
                if (s == null) continue;
                final String[] stackinfo = s.split("`");
                final ItemStack stack = PokecubeItems.getStack(stackinfo[0]);
                if (stackinfo.length > 1) try
                {
                    final int count = Integer.parseInt(stackinfo[1]);
                    stack.setCount(count);
                }
                catch (final NumberFormatException e)
                {
                    PokecubeAPI.LOGGER.error(e);
                }
                this.loot[num] = stack;
                num++;
            }
        }
        if (this.loot[0].isEmpty()) this.loot[0] = new ItemStack(Items.EMERALD);
    }

    public void initTrainerItems(final LivingEntity trainer)
    {
        this.initLoot();
        for (int i = 1; i < 5; i++)
        {
            if (i == 1 && !this.holdsReward) continue;
            final EquipmentSlot slotIn = EquipmentSlot.values()[i];
            trainer.setItemSlot(slotIn, this.loot[i - 1]);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean texExists(final ResourceLocation texture)
    {
        return ResourceHelper.exists(texture, Minecraft.getInstance().getResourceManager());
    }

    @Override
    public String toString()
    {
        return this.getName();
    }

    public static class TrainerTrade extends MerchantOffer implements VillagerTrades.ItemListing
    {
        public static interface ResultModifier
        {
            ItemStack apply(Entity user, RandomSource random);
        }

        public final ItemCost _input_a;
        public final Optional<ItemCost> _input_b;
        public final ItemStack _output;
        public int _uses;
        public int _maxUses;
        public int _demand;
        public float _multiplier;
        public int _exp;
        public boolean _gives_xp;

        public int min = -1;
        public int max = -1;
        public float chance = 1;

        public TypeTrainer.TrainerTrade.ResultModifier outputModifier;

        public String debug_string = "";

        public TrainerTrade(ItemCost input_a, Optional<ItemCost> input_b, ItemStack output, int uses, int maxUses,
                boolean giveExp, int exp, float multiplier, int demand)
        {
            super(input_a, input_b, output, uses, maxUses, exp, multiplier, demand);

            this._input_a = input_a;
            this._input_b = input_b;
            this._gives_xp = giveExp;
            this._output = output;
            this._uses = uses;
            this._maxUses = maxUses;
            this._exp = exp;
            this._multiplier = multiplier;
            this._demand = demand;
            outputModifier = (u, r) -> this._output;
        }

        public TrainerTrade(final ItemCost buy1, final Optional<ItemCost> buy2, final ItemStack sell, final Trade trade)
        {
            this(buy1, buy2, sell, 0, trade.maxUses, true, trade.exp, trade.multiplier, trade.demand);
        }

        public MerchantOffer randomise(RandomSource rand)
        {
            var sell = this.getResult();
            if (!sell.isEmpty()) sell = sell.copy();
            else return null;
            if (this.min != -1 && this.max != -1)
            {
                if (this.max < this.min) this.max = this.min;
                sell.setCount(this.min + rand.nextInt(1 + this.max - this.min));
            }
            int maxUse = this._maxUses == Integer.MAX_VALUE ? 100000 : this._maxUses;
            return new MerchantOffer(this._input_a, this._input_b, sell, this._uses, maxUse, this._exp,
                    this._multiplier, this._demand);
        }

        @Override
        public MerchantOffer getOffer(Entity user, RandomSource random)
        {
            TypeTrainer.TrainerTrade newTrade = new TypeTrainer.TrainerTrade(this._input_a, this._input_b, outputModifier.apply(user, random),
                    this._uses, this._maxUses, _gives_xp, this._exp, this._multiplier, this._demand);
            if (newTrade._output.isEmpty() || (newTrade._input_a.count() == 0 && newTrade._input_b.isEmpty()))
            {
                PokecubeAPI.LOGGER.error("Warning, invalid trade! {}", debug_string);
                return null;
            }
            return newTrade.randomise(random);
        }
    }

    public static class TrainerTrades
    {
        public List<TypeTrainer.TrainerTrade> tradesList = Lists.newArrayList();

        public void addTrades(final Entity trader, final List<MerchantOffer> ret, final RandomSource rand)
        {
            for (final TypeTrainer.TrainerTrade trade : this.tradesList)
                if (rand.nextFloat() < trade.chance)
                {
                    final MerchantOffer toAdd = trade.getOffer(trader, rand);
                    if (toAdd != null) ret.add(toAdd);
                }
        }
    }
}
