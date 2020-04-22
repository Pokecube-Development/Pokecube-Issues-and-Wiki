package pokecube.adventures.capabilities.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.resources.IResource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.AIBattle;
import pokecube.adventures.ai.tasks.AICapture;
import pokecube.adventures.ai.tasks.AIFindTarget;
import pokecube.adventures.ai.tasks.AIMate;
import pokecube.adventures.ai.tasks.AIRetaliate;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.utils.DBLoader;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.entity.ai.GoalsWrapper;
import thut.api.entity.ai.IAIRunnable;

public class TypeTrainer extends NpcType
{

    public static interface ITypeMapper
    {
        /**
         * Mapping of LivingEntity to a TypeTrainer. EntityTrainers set
         * this on spawn, so it isn't needed for them. <br>
         * <br>
         * if forSpawn, it means this is being initialized, otherwise it is
         * during the check for whether this mob should have trainers.
         */
        TypeTrainer getType(LivingEntity mob, boolean forSpawn);
    }

    public static interface AIAdder
    {
        void process(MobEntity mob);
    }

    private static final List<ITypeMapper> mappers  = Lists.newArrayList();
    private static final List<AIAdder>     aiAdders = Lists.newArrayList();

    public static void registerTypeMapper(final ITypeMapper mapper)
    {
        TypeTrainer.mappers.add(mapper);
    }

    public static void registerAIAdder(final AIAdder adder)
    {
        TypeTrainer.aiAdders.add(adder);
    }

    public static void addAI(final MobEntity mob)
    {
        for (final AIAdder adder : TypeTrainer.aiAdders)
            adder.process(mob);
    }

    public static TypeTrainer get(final LivingEntity mob, final boolean forSpawn)
    {
        for (final ITypeMapper mapper : TypeTrainer.mappers)
        {
            final TypeTrainer type = mapper.getType(mob, forSpawn);
            if (type != null) return type;
        }
        return null;
    }

    // Register default instance.
    static
    {
        TypeTrainer.registerTypeMapper((mob, forSpawn) ->
        {
            if (!forSpawn)
            {
                if (mob instanceof TrainerBase) return TypeTrainer.merchant;
                if (Config.instance.npcsAreTrainers && mob instanceof INPC) return TypeTrainer.merchant;
                return null;
            }

            if (mob instanceof TrainerBase)
            {
                final TypeTrainer type = ((TrainerBase) mob).pokemobsCap.getType();
                if (type != null) return type;
                return TypeTrainer.merchant;
            }
            else if (mob instanceof NpcMob) return TypeTrainer.merchant;
            else if (mob instanceof VillagerEntity && Config.instance.npcsAreTrainers)
            {
                final VillagerEntity villager = (VillagerEntity) mob;
                final String type = villager.getVillagerData().getProfession().toString();
                return TypeTrainer.getTrainer(type, true);
            }
            return null;
        });

        TypeTrainer.registerAIAdder((npc) ->
        {
            final List<IAIRunnable> ais = Lists.newArrayList();
            // All can battle, but only trainers will path during battle.
            ais.add(new AIBattle(npc, !(npc instanceof TrainerBase)).setPriority(0));

            // All attack zombies.
            ais.add(new AIFindTarget(npc, ZombieEntity.class).setPriority(20));

            // All retaliate
            ais.add(new AIRetaliate(npc));

            final Predicate<LivingEntity> noRunWhileRest = e ->
            {
                if (e instanceof VillagerEntity)
                {
                    final VillagerEntity villager = (VillagerEntity) e;
                    if (villager.getBrain().hasActivity(Activity.REST)) return false;
                }
                return true;
            };
            final Predicate<LivingEntity> noRunWhileMeet = e ->
            {
                if (e instanceof VillagerEntity)
                {
                    final VillagerEntity villager = (VillagerEntity) e;
                    if (villager.getBrain().hasActivity(Activity.MEET)) return false;
                }
                return true;
            };
            // Only trainers specifically target players.
            if (npc instanceof TrainerBase)
            {
                ais.add(new AIFindTarget(npc, PlayerEntity.class).setRunCondition(noRunWhileRest).setPriority(10));
                ais.add(new AIMate(npc, ((TrainerBase) npc).getClass()));
            }

            // 5% chance of battling a random nearby pokemob if they see it.
            if (Config.instance.trainersBattlePokemobs)
            {
                ais.add(new AIFindTarget(npc, 0.05f, EntityPokemob.class).setRunCondition(noRunWhileRest).setPriority(
                        20));
                ais.add(new AICapture(npc).setPriority(10));
            }
            // 1% chance of battling another of same class if seen
            // Also this will stop the battle after 1200 ticks.
            if (Config.instance.trainersBattleEachOther)
            {
                final Predicate<LivingEntity> shouldRun = noRunWhileMeet.and(noRunWhileRest);
                ais.add(new AIFindTarget(npc, 0.001f, 1200, npc.getClass()).setRunCondition(shouldRun).setPriority(20));
            }

            npc.goalSelector.addGoal(0, new GoalsWrapper(npc, ais.toArray(new IAIRunnable[0])));
        });
    }

    public static class TrainerTrade extends MerchantOffer
    {
        public int   min    = -1;
        public int   max    = -1;
        public float chance = 1;

        public TrainerTrade(final ItemStack buy1, final ItemStack buy2, final ItemStack sell)
        {
            super(buy1, buy2, sell, Integer.MAX_VALUE, -1, 1);
        }

        public MerchantOffer getRecipe(final Random rand)
        {
            ItemStack buy1 = this.getBuyingStackFirst();
            ItemStack buy2 = this.getBuyingStackSecond();
            if (!buy1.isEmpty()) buy1 = buy1.copy();
            if (!buy2.isEmpty()) buy2 = buy2.copy();
            ItemStack sell = this.getSellingStack();
            if (!sell.isEmpty()) sell = sell.copy();
            else return null;
            if (this.min != -1 && this.max != -1)
            {
                if (this.max < this.min) this.max = this.min;
                sell.setCount(this.min + rand.nextInt(1 + this.max - this.min));
            }
            final MerchantOffer ret = new MerchantOffer(buy1, buy2, sell, Integer.MAX_VALUE, 10, 1);
            return ret;
        }
    }

    public static class TrainerTrades
    {
        public List<TrainerTrade> tradesList = Lists.newArrayList();

        public void addTrades(final List<MerchantOffer> ret, final Random rand)
        {
            for (final TrainerTrade trade : this.tradesList)
                if (rand.nextFloat() < trade.chance)
                {
                    final MerchantOffer toAdd = trade.getRecipe(rand);
                    if (toAdd != null) ret.add(toAdd);
                }
        }
    }

    public static HashMap<String, TrainerTrades> tradesMap   = Maps.newHashMap();
    public static HashMap<String, TypeTrainer>   typeMap     = new HashMap<>();
    public static ArrayList<String>              maleNames   = new ArrayList<>();
    public static ArrayList<String>              femaleNames = new ArrayList<>();

    public static TypeTrainer merchant = new TypeTrainer("merchant");
    static
    {
        TypeTrainer.merchant.tradeTemplate = "merchant";
    }

    public static void addTrainer(final String name, final TypeTrainer type)
    {
        TypeTrainer.typeMap.put(name, type);
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level, final World world,
            final List<PokedexEntry> values)
    {
        for (int i = 0; i < 6; i++)
            trainer.setPokemob(i, ItemStack.EMPTY);
        if (level == 0) level = 5;
        final Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        int number = 1 + new Random().nextInt(6);
        number = Math.min(number, trainer.getMaxPokemobCount());
        for (int i = 0; i < number; i++)
        {
            Collections.shuffle(values);
            ItemStack item = ItemStack.EMPTY;
            for (final PokedexEntry s : values)
            {
                if (s != null) item = TypeTrainer.makeStack(s, owner, world, variance.apply(level));
                if (!item.isEmpty()) break;
            }
            trainer.setPokemob(i, item);
        }
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level, final World world)
    {
        final TypeTrainer type = trainer.getType();
        final List<PokedexEntry> values = Lists.newArrayList();
        if (type.pokemon != null) values.addAll(type.pokemon);
        else PokecubeCore.LOGGER.warn("No mobs for " + type);
        if (type.overrideLevel != -1) level = type.overrideLevel;
        TypeTrainer.getRandomTeam(trainer, owner, level, world, values);
    }

    public static TypeTrainer getTrainer(final String name)
    {
        return TypeTrainer.getTrainer(name, false);
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
                final TypeTrainer t = new TypeTrainer(name);
                return t;
            }
            for (final TypeTrainer t : TypeTrainer.typeMap.values())
                if (t != null) return t;
        }
        return ret;
    }

    public static void initSpawns()
    {
        for (final TypeTrainer type : TypeTrainer.typeMap.values())
            for (final SpawnBiomeMatcher matcher : type.matchers.keySet())
            {
                matcher.reset();
                matcher.parse();
            }
    }

    public static ItemStack makeStack(final PokedexEntry entry, final LivingEntity trainer, final World world,
            final int level)
    {
        final int num = entry.getPokedexNb();
        if (Pokedex.getInstance().getEntry(num) == null) return ItemStack.EMPTY;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(entry, world));
        if (pokemob != null)
        {
            for (int i = 1; i < level; i++)
                if (pokemob.getPokedexEntry().canEvolve(i)) for (final EvolutionData d : pokemob.getPokedexEntry()
                        .getEvolutions())
                    if (d.shouldEvolve(pokemob))
                    {
                        final IPokemob temp = CapabilityPokemob.getPokemobFor(d.getEvolution(world));
                        if (temp != null)
                        {
                            pokemob = temp;
                            break;
                        }
                    }
            pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
            pokemob = pokemob.setPokedexEntry(entry);
            pokemob.setOwner(trainer.getUniqueID());
            pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            final int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob = pokemob.setForSpawn(exp, false);
            final ItemStack item = PokecubeManager.pokemobToItem(pokemob);
            pokemob.getEntity().remove();
            return item;
        }

        return ItemStack.EMPTY;
    }

    public static void postInitTrainers()
    {

        for (final ResourceLocation s : DBLoader.tradeDatabases)
            try
            {
                TradeEntryLoader.makeEntries(s);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error loading trades from " + s, e);
            }

        final List<TypeTrainer> toRemove = new ArrayList<>();
        for (final TypeTrainer t : TypeTrainer.typeMap.values())
        {
            t.pokemon.clear();
            if (t.pokelist != null && t.pokelist.length != 0) if (!t.pokelist[0].startsWith("-"))
                for (final String s : t.pokelist)
                {
                    final PokedexEntry e = Database.getEntry(s);
                    if (e != null && !t.pokemon.contains(e)) t.pokemon.add(e);
                    else if (e == null) PokecubeCore.LOGGER.error("Error in reading of " + s);
                }
            else
            {
                final String[] types = t.pokelist[0].replace("-", "").split(":");
                if (types[0].equalsIgnoreCase("all"))
                {
                    for (final PokedexEntry s : Database.spawnables)
                        if (!s.isLegendary()) t.pokemon.add(s);
                }
                else for (final String type2 : types)
                {
                    final PokeType pokeType = PokeType.getType(type2);
                    if (pokeType != PokeType.unknown) for (final PokedexEntry s : Database.spawnables)
                        if (s.isType(pokeType) && !s.isLegendary()) t.pokemon.add(s);
                }
            }
            // Remove large pokemobs from their list.
            t.pokemon.removeIf(e -> (e.length > 8 || e.height > 8 || e.width > 8));
            if (t.pokemon.size() == 0 && t != TypeTrainer.merchant) toRemove.add(t);
        }
        if (!toRemove.isEmpty()) PokecubeCore.LOGGER.debug("Removing Trainer Types: " + toRemove);
        for (final TypeTrainer t : toRemove)
            TypeTrainer.typeMap.remove(t.getName());
        TypeTrainer.initSpawns();
    }

    /** 1 = male, 2 = female, 3 = both */
    public byte genders = 1;

    public Map<SpawnBiomeMatcher, Float> matchers = Maps.newHashMap();
    public boolean                       hasBag   = false;
    public ItemStack                     bag      = ItemStack.EMPTY;
    public boolean                       hasBelt  = false;

    public String             tradeTemplate = "default";
    public List<PokedexEntry> pokemon       = Lists.newArrayList();
    public TrainerTrades      trades;
    private boolean           checkedTex    = false;
    public int                overrideLevel = -1;

    private final ItemStack[] loot = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY).toArray(new ItemStack[4]);

    public String    drops = "";
    public ItemStack held  = ItemStack.EMPTY;

    // Temporary array used to load in the allowed mobs.
    public String[] pokelist;

    public TypeTrainer(final String name)
    {
        super(name);
        TypeTrainer.addTrainer(name, this);
        this.setFemaleTex(new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName())
        + "female.png"));
        this.setFemaleTex(new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName())
        + "male.png"));
    }

    public Collection<MerchantOffer> getRecipes(final Random rand)
    {
        if (this.trades == null && this.tradeTemplate != null) this.trades = TypeTrainer.tradesMap.get(
                this.tradeTemplate);
        final List<MerchantOffer> ret = Lists.newArrayList();
        if (this.trades != null) this.trades.addTrades(ret, rand);
        return ret;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture(final LivingEntity trainer)
    {
        final IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(trainer);
        if (!this.checkedTex)
        {
            this.checkedTex = true;
            if (!this.texExists(this.getFemaleTex())) this.setFemaleTex(new ResourceLocation(
                    PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName()) + ".png"));
            if (!this.texExists(this.getMaleTex())) this.setMaleTex(new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH
                    + Database.trim(this.getName()) + ".png"));
        }
        return cap.getGender() == 1 ? this.getMaleTex() : this.getFemaleTex();
    }

    private void initLoot()
    {
        if (!this.loot[0].isEmpty()) return;

        if (!this.drops.equals(""))
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
            final EquipmentSlotType slotIn = EquipmentSlotType.values()[i];
            trainer.setItemStackToSlot(slotIn, this.loot[i - 1]);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private boolean texExists(final ResourceLocation texture)
    {
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(texture);
            res.close();
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "" + this.getName();
    }
}
