package pokecube.adventures.capabilities.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.Tasks;
import pokecube.adventures.ai.tasks.battle.CaptureMob;
import pokecube.adventures.ai.tasks.battle.agro.AgroTargets;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.pokemobs.SpawnEvent.Variance;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.database.Database;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

@SuppressWarnings("unchecked")
public class TypeTrainer extends NpcType
{

    public static interface ITypeMapper
    {
        /**
         * Mapping of LivingEntity to a TypeTrainer. EntityTrainers set this on
         * spawn, so it isn't needed for them. <br>
         * <br>
         * if forSpawn, it means this is being initialized, otherwise it is
         * during the check for whether this mob should have trainers.
         */
        TypeTrainer getType(LivingEntity mob, boolean forSpawn);
    }

    public static interface AIAdder
    {
        List<Pair<Integer, Behavior<? super LivingEntity>>> process(Mob mob);
    }

    private static final List<ITypeMapper> mappers = Lists.newArrayList();
    private static final List<AIAdder> aiAdders = Lists.newArrayList();

    public static void registerTypeMapper(final ITypeMapper mapper)
    {
        TypeTrainer.mappers.add(mapper);
    }

    public static void registerAIAdder(final AIAdder adder)
    {
        TypeTrainer.aiAdders.add(adder);
    }

    public static void addAI(final Mob mob)
    {
        final List<Pair<Integer, Behavior<? super LivingEntity>>> tasks = Lists.newArrayList();
        for (final AIAdder adder : TypeTrainer.aiAdders) tasks.addAll(adder.process(mob));
        Tasks.addBattleTasks(mob, tasks);
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

    public static Predicate<LivingEntity> validPlayerTarget(Mob npc)
    {
        return e -> {
            boolean isPlayer = e instanceof Player;
            return isPlayer;
        };
    }

    public static Predicate<LivingEntity> validPokemobTarget(Mob npc)
    {
        return e -> {
            boolean isPokemob = PokemobCaps.getPokemobFor(e) != null;
            return isPokemob;
        };
    }

    public static Predicate<LivingEntity> validZombieTarget(Mob npc)
    {
        return e -> {
            boolean isZombie = e instanceof Zombie;
            return isZombie;
        };
    }

    // Register default instance.
    static
    {
        TypeTrainer.registerTypeMapper((mob, forSpawn) -> {
            if (!forSpawn)
            {
                if (mob instanceof TrainerBase) return TypeTrainer.merchant;
                if (Config.instance.shouldBeCustomTrainer(mob)) return TypeTrainer.merchant;
                return null;
            }

            if (mob instanceof TrainerBase npc)
            {
                final TypeTrainer type = npc.pokemobsCap.getType();
                if (type != null) return type;
                return TypeTrainer.merchant;
            }
            else if (mob instanceof NpcMob) return TypeTrainer.merchant;
            else if (Config.instance.npcsAreTrainers && mob instanceof Villager villager)
            {
                final String type = villager.getVillagerData().getProfession().toString();
                return TypeTrainer.getTrainer(type, true);
            }
            return null;
        });

        TypeTrainer.registerAIAdder((npc) -> {
            final Predicate<LivingEntity> noRunIfCrowded = e -> {
                // Leaders don't care if crowded.
                if (npc instanceof LeaderNpc) return true;
                final int dist = PokecubeAdv.config.trainer_crowding_radius;
                final int num = PokecubeAdv.config.trainer_crowding_number;
                if (TrainerTracker.countTrainers(e.level(), new Vector3().set(e), dist) > num)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to crowds", npc);
                    return false;
                }
                return true;
            };
            final Predicate<LivingEntity> noRunWhileRest = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (e instanceof Villager villager)
                {
                    if (villager.isSleeping())
                    {
                        if (PokecubeCore.getConfig().debug_ai)
                            PokecubeAPI.logDebug("NPC {} not agroing due to sleeping", npc);
                        return false;
                    }
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> noRunWhileMeet = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (e instanceof Villager villager)
                {
                    final Schedule s = villager.getBrain().getSchedule();
                    final Activity a = s.getActivityAt((int) (e.level.getDayTime() % 24000L));
                    if (a == Activity.MEET)
                    {
                        if (PokecubeCore.getConfig().debug_ai)
                            PokecubeAPI.logDebug("NPC {} not agroing due to meeting", npc);
                        return false;
                    }
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> onlyIfHasMobs = e -> {
                final IHasPokemobs other = TrainerCaps.getHasPokemobs(e);
                if (other == null) return noRunIfCrowded.test(e);
                final boolean hasMob = !other.getNextPokemob().isEmpty();
                if (hasMob) return noRunIfCrowded.test(e);
                if (other.getOutID() == null)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to no mobs on target", npc);
                    return false;
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> notNearHealer = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (!PokecubeAdv.config.no_battle_near_pokecenter) return true;
                final ServerLevel world = (ServerLevel) npc.level();
                final BlockPos blockpos = e.blockPosition();
                final PoiManager pois = world.getPoiManager();
                final long num = pois.getCountInRange(PointsOfInterest.HEALER, blockpos,
                        PokecubeAdv.config.pokecenter_radius, Occupancy.ANY);
                if (num > 0)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to nearby pokecenter", npc);
                    return false;
                }
                return num == 0;
            };

            final List<Pair<Integer, Behavior<? super LivingEntity>>> list = Lists.newArrayList();
            Behavior<?> task = new AgroTargets(npc, 1, 0, validZombieTarget(npc));
            list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));

            // Only trainers specifically target players.
            if (npc instanceof TrainerBase)
            {
                final Predicate<LivingEntity> validPlayer = onlyIfHasMobs.and(validPlayerTarget(npc));
                final Predicate<LivingEntity> shouldRun = noRunWhileRest;
                task = new AgroTargets(npc, 1, 0, validPlayer.and(notNearHealer)).setRunCondition(shouldRun);
                list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            }

            // 5% chance of battling a random nearby pokemob if they see it.
            if (Config.instance.trainersBattlePokemobs)
            {
                task = new AgroTargets(npc, 0.005f, 1200, validPokemobTarget(npc)).setRunCondition(noRunWhileRest);
                list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
                task = new CaptureMob(npc, 1);
                list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            }
            // 1% chance of battling another of same class if seen
            // Also this will stop the battle after 1200 ticks.
            if (Config.instance.trainersBattleEachOther)
            {
                final Predicate<LivingEntity> shouldRun = noRunWhileMeet.and(noRunWhileRest);
                task = new AgroTargets(npc, 0.0015f, 1200, z -> z.getClass() == npc.getClass())
                        .setRunCondition(shouldRun);
                list.add(Pair.of(1, (Behavior<? super LivingEntity>) task));
            }
            return list;
        });
    }

    public static class TrainerTrade extends MerchantOffer implements ItemListing
    {
        public static interface ResultModifier
        {
            ItemStack apply(Entity user, RandomSource random);
        }

        public final ItemStack _input_a;
        public final ItemStack _input_b;
        public final ItemStack _output;
        public int _uses = 0;
        public int _maxUses = 16;
        public int _demand = 0;
        public float _multiplier = 0.05f;
        public int _exp = 1;

        public int min = -1;
        public int max = -1;
        public float chance = 1;

        public ResultModifier outputModifier;

        public String debug_string = "";

        public TrainerTrade(ItemStack input_a, ItemStack input_b, ItemStack output, int uses, int maxUses, int exp,
                float multiplier, int demand)
        {
            super(input_a, input_b, output, uses, maxUses, exp, multiplier, demand);

            this._input_a = input_a;
            this._input_b = input_b;
            this._output = output;
            this._uses = uses;
            this._maxUses = maxUses;
            this._exp = exp;
            this._multiplier = multiplier;
            this._demand = demand;
            outputModifier = (u, r) -> this._output;
        }

        public TrainerTrade(final ItemStack buy1, final ItemStack buy2, final ItemStack sell, final Trade trade)
        {
            this(buy1, buy2, sell, 0, trade.maxUses, trade.exp, trade.multiplier, trade.demand);
        }

        public MerchantOffer randomise(RandomSource rand)
        {
            ItemStack buy1 = this.getBaseCostA();
            ItemStack buy2 = this.getCostB();
            if (!buy1.isEmpty()) buy1 = buy1.copy();
            if (!buy2.isEmpty()) buy2 = buy2.copy();
            ItemStack sell = this.getResult();
            if (!sell.isEmpty()) sell = sell.copy();
            else return null;
            if (this.min != -1 && this.max != -1)
            {
                if (this.max < this.min) this.max = this.min;
                sell.setCount(this.min + rand.nextInt(1 + this.max - this.min));
            }
            int maxUse = this._maxUses == Integer.MAX_VALUE ? 100000 : this._maxUses;
            final MerchantOffer ret = new MerchantOffer(buy1, buy2, sell, this._uses, maxUse, this._exp,
                    this._multiplier, this._demand);
            return ret;
        }

        @Override
        public MerchantOffer getOffer(Entity user, RandomSource random)
        {
            TrainerTrade newTrade = new TrainerTrade(this._input_a, this._input_b, outputModifier.apply(user, random),
                    this._uses, this._maxUses, this._exp, this._multiplier, this._demand);
            if (newTrade._output.isEmpty() || (newTrade._input_a.isEmpty() && newTrade._input_b.isEmpty()))
            {
                PokecubeAPI.LOGGER.error("Warning, invalid trade! " + debug_string);
                return null;
            }
            return newTrade.randomise(random);
        }
    }

    public static class TrainerTrades
    {
        public List<TrainerTrade> tradesList = Lists.newArrayList();

        public void addTrades(final Entity trader, final List<MerchantOffer> ret, final RandomSource rand)
        {
            for (final TrainerTrade trade : this.tradesList) if (rand.nextFloat() < trade.chance)
            {
                final MerchantOffer toAdd = trade.getOffer(trader, rand);
                if (toAdd != null) ret.add(toAdd);
            }
        }
    }

    public static HashMap<String, TrainerTrades> tradesMap = Maps.newHashMap();
    public static HashMap<String, TypeTrainer> typeMap = new HashMap<>();

    public static ArrayList<String> maleNames = new ArrayList<>();
    public static ArrayList<String> femaleNames = new ArrayList<>();

    public static TypeTrainer merchant = new TypeTrainer("merchant");
    static
    {
        TypeTrainer.merchant.tradeTemplate = "merchant";
    }

    public static void addTrainer(final String name, final TypeTrainer type)
    {
        TypeTrainer.typeMap.put(name, type);
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level,
            final LevelAccessor world, final List<PokedexEntry> values)
    {
        for (int i = 0; i < 6; i++) trainer.setPokemob(i, ItemStack.EMPTY);
        if (level == 0) level = 5;
        final Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        int number = 1 + ThutCore.newRandom().nextInt(6);
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

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level,
            final LevelAccessor world)
    {
        final TypeTrainer type = trainer.getType();
        final List<PokedexEntry> values = Lists.newArrayList();
        if (type.pokemon != null) values.addAll(type.pokemon);
        else PokecubeAPI.LOGGER.warn("No mobs for " + type);
        if (type.overrideLevel != -1) level = type.overrideLevel;
        if (PokecubeCore.getConfig().debug_spawning) PokecubeAPI.logInfo("Initializing team for " + owner);
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
            for (final TypeTrainer t : TypeTrainer.typeMap.values()) if (t != null) return t;
        }
        return ret;
    }

    public static void initSpawns()
    {
        for (final TypeTrainer type : TypeTrainer.typeMap.values())
            for (final SpawnBiomeMatcher matcher : type.spawns.keySet())
        {
            matcher.reset();
            matcher.parse();
        }
    }

    public static ItemStack makeStack(final PokedexEntry entry, final LivingEntity trainer, final LevelAccessor world,
            final int level)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(PokecubeCore.createPokemob(entry, trainer.level()));
        if (pokemob != null)
        {
            final double x = trainer.getX();
            final double y = trainer.getY();
            final double z = trainer.getZ();
            pokemob.getEntity().setPosRaw(x, y, z);
            pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
            pokemob.getEntity().getPersistentData().putBoolean("__need_init_evos__", true);
            pokemob = pokemob.setPokedexEntry(entry);
            pokemob.setOwner(trainer.getUUID());
            pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehaviour.DEFAULTCUBE)));
            final int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob = pokemob.setForSpawn(exp, false);
            final ItemStack item = PokecubeManager.pokemobToItem(pokemob);
            return item;
        }
        return ItemStack.EMPTY;
    }

    public static void postInitTrainers()
    {
        TradeEntryLoader.makeEntries();
        final List<TypeTrainer> toRemove = new ArrayList<>();
        for (final TypeTrainer t : TypeTrainer.typeMap.values())
        {
            t.pokemon.clear();
            if (t.pokelist != null && t.pokelist.length != 0)
                if (!t.pokelist[0].startsWith("-")) for (final String s : t.pokelist)
            {
                final PokedexEntry e = Database.getEntry(s);
                if (e != null && !t.pokemon.contains(e)) t.pokemon.add(e);
                else if (e == null) PokecubeAPI.LOGGER.error("Error in reading of " + s);
            }
                else
            {
                final String[] types = t.pokelist[0].replace("-", "").split(":");
                if (types[0].equalsIgnoreCase("all"))
                {
                    for (final PokedexEntry s : Database.spawnables) if (!s.isLegendary()) t.pokemon.add(s);
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
        if (!toRemove.isEmpty()) PokecubeAPI.logInfo("Removing Trainer Types: " + toRemove);
        for (final TypeTrainer t : toRemove) TypeTrainer.typeMap.remove(t.getName());
        TypeTrainer.initSpawns();
    }

    /** 1 = male, 2 = female, 3 = both */
    public byte genders = 1;

    public boolean hasBelt = false;

    public Map<String, List<ItemStack>> wornItems = Maps.newHashMap();

    public String tradeTemplate = "default";
    public List<PokedexEntry> pokemon = Lists.newArrayList();
    public TrainerTrades trades;
    private boolean checkedTex = false;
    public int overrideLevel = -1;

    private final ItemStack[] loot = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY).toArray(new ItemStack[4]);

    public String drops = "";
    public ItemStack held = ItemStack.EMPTY;

    // Temporary array used to load in the allowed mobs.
    public String[] pokelist;

    public TypeTrainer(final String name)
    {
        super(name);
        TypeTrainer.addTrainer(name, this);
        this.setFemaleTex(
                new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName()) + "_female.png"));
        this.setMaleTex(
                new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName()) + "_male.png"));
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
            if (!this.texExists(this.getFemaleTex())) this.setFemaleTex(
                    new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName()) + ".png"));
            if (!this.texExists(this.getMaleTex())) this.setMaleTex(
                    new ResourceLocation(PokecubeAdv.TRAINERTEXTUREPATH + Database.trim(this.getName()) + ".png"));

            // Second pass to override with vanilla
            if (!this.texExists(this.getFemaleTex()))
                this.setFemaleTex(new ResourceLocation("textures/entity/alex.png"));
            if (!this.texExists(this.getMaleTex())) this.setMaleTex(new ResourceLocation("textures/entity/steve.png"));
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
                {}
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
        return "" + this.getName();
    }
}
