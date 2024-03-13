package pokecube.api.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.ModLoadingContext;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.effects.materials.IMaterialAction;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.pokedex.EvolutionDataLoader;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Action;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Evolution;
import pokecube.api.data.pokedex.InteractsAndEvolutions.FormeItem;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Interact;
import pokecube.api.data.pokedex.conditions.PokemobCondition;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.entity.pokemob.ICanEvolve;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.events.pokemobs.SpawnEvent.Variance;
import pokecube.api.moves.Battle;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.pokemobs.DispenseBehaviourInteract;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.eventhandlers.PokemobEventsHandler.MegaEvoTicker;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.TimePeriod;
import thut.api.Tracker;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.item.ItemList;
import thut.api.level.terrain.BiomeType;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vec3f;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;
import thut.lib.TComponent;

/** @author Manchou */
public class PokedexEntry
{
    // Annotation used to specify which fields should be shared to all gender
    // formes.
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CopyToGender
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Required
    {
    }

    public static class EvolutionData implements Comparable<EvolutionData>
    {
        public PokemobCondition _condition;
        public PokedexEntry result;

        public final Evolution data;

        public final PokedexEntry evolution;
        public String FX = "";

        public List<String> evoMoves = Lists.newArrayList();

        public EvolutionData(final PokedexEntry evol, Evolution data)
        {
            this.evolution = evol;
            this.data = data;
        }

        public Entity getEvolution(final LevelAccessor world)
        {
            if (this.evolution == null) return null;
            final Entity ret = PokecubeCore.createPokemob(this.evolution, (Level) world);
            return ret;
        }

        @OnlyIn(Dist.CLIENT)
        public List<MutableComponent> getEvoClauses()
        {
            final List<MutableComponent> comps = Lists.newArrayList();
            if (this._condition != null)
            {
                if ("eevee".equals(data.user)) ThutCore.conf.debug = true;
                List<Component> baseComps = PokemobCondition.getDescriptions(_condition);
                if ("eevee".equals(data.user)) ThutCore.conf.debug = false;
                for (var c : baseComps) comps.add(TComponent.translatable("pokemob.description.tabbed", c));
            }
            return comps;
        }

        @OnlyIn(Dist.CLIENT)
        public MutableComponent getEvoString()
        {
            /*
             * //@formatter:off
             *
             *  It should work as follows:
             *
             *  X evolves into Y under the following circumstances:
             *  - Upon reaching level L
             *  - When sufficiently Happy
             *  - When Raining
             *  - Etc
             *
             *
             */
            // @formatter:on
            final PokedexEntry entry = this.data.getUser();
            final PokedexEntry nex = this.data.getResult();
            final MutableComponent subEvo = TComponent.translatable("pokemob.description.evolve.to",
                    entry.getTranslatedName(), nex.getTranslatedName());
            final List<MutableComponent> list = this.getEvoClauses();
            for (final MutableComponent item : list) subEvo.append("\n").append(item);
            return subEvo;
        }

        private void parse(final Evolution data)
        {
            // This is what we will actually use for the tests.
            this._condition = data.toCondition();

            if (data.animation != null) this.FX = data.animation;
            this.evoMoves.clear();
            if (data.evoMoves != null && !data.evoMoves.isEmpty())
            {
                final String[] vals = data.evoMoves.split(",");
                for (final String s : vals) this.evoMoves.add(s.trim());
            }
        }

        public void postInit()
        {
            if (this.data != null) this.parse(this.data);
        }

        public boolean shouldEvolve(final IPokemob mob)
        {
            return this.shouldEvolve(mob, mob.getHeldItem());
        }

        public boolean shouldEvolve(final IPokemob mob, final ItemStack mobs)
        {
            if (ItemList.is(ICanEvolve.EVERSTONE, mob.getHeldItem())) return false;
            if (ItemList.is(ICanEvolve.EVERSTONE, mobs)) return false;
            ItemStack old = mob.getEvolutionStack();
            if (ItemList.is(ICanEvolve.EVERSTONE, old)) return false;
            mob.setEvolutionStack(mobs);
            boolean matched = _condition.matches(mob);
            mob.setEvolutionStack(old);
            return matched;
        }

        @Override
        public int compareTo(EvolutionData o)
        {
            return this.data.compareTo(o.data);
        }
    }

    public static class InteractionLogic
    {
        private static final ResourceLocation SHEARS = new ResourceLocation(PokecubeCore.MODID, "shears");

        public static Predicate<ItemStack> isShears = (s) -> ItemList.is(InteractionLogic.SHEARS, s);

        public static class Interaction
        {
            public PokedexEntry forme;

            public List<ItemStack> stacks = Lists.newArrayList();

            public ResourceLocation lootTable;

            public boolean male = true;
            public boolean female = true;

            public int cooldown = 100;
            public int variance = 1;
            public int hunger = 100;

            public Interaction()
            {}
        }

        static HashMap<PokeType, List<Interact>> defaults = new HashMap<>();

        private static void cleanInteract(final Interact interact)
        {
            final Interact defs = new Interact();
            for (final Field f : Interact.class.getDeclaredFields())
            {
                f.setAccessible(true);
                try
                {
                    if (f.get(interact) == null) f.set(interact, f.get(defs));
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public static void initDefaults()
        {
            final Interact fire = new Interact();
            fire.key = new Drop();
            fire.action = new Action();
            fire.key.values.put("id", "minecraft:stick");
            fire.action.values.put("type", "item");
            final Drop firedrop = new Drop();
            firedrop.values.put("id", "minecraft:torch");
            fire.action.drops.add(firedrop);

            final Interact water = new Interact();
            water.key = new Drop();
            water.action = new Action();
            water.key.values.put("id", "minecraft:bucket");
            water.action.values.put("type", "item");
            final Drop waterdrop = new Drop();
            waterdrop.values.put("id", "minecraft:water_bucket");
            water.action.drops.add(waterdrop);

            if (PokecubeCore.getConfig().defaultInteractions)
            {
                InteractionLogic.defaults.put(PokeType.getType("fire"), Lists.newArrayList(fire));
                InteractionLogic.defaults.put(PokeType.getType("water"), Lists.newArrayList(water));
            }
        }

        public static void initForEntry(final PokedexEntry entry)
        {
            // Here we deal with the defaulted interactions for this type.
            final List<Interact> val = Lists.newArrayList();
            for (final PokeType t : InteractionLogic.defaults.keySet())
                if (entry.isType(t)) val.addAll(InteractionLogic.defaults.get(t));
            if (!val.isEmpty()) InteractionLogic.initForEntry(entry, val, false);
        }

        public static void initForEntry(final PokedexEntry entry, final List<Interact> data, final boolean replace)
        {
            if (data == null || data.isEmpty())
            {
                InteractionLogic.initForEntry(entry);
                return;
            }
            for (final Interact interact : data)
            {
                InteractionLogic.cleanInteract(interact);
                final Drop key = interact.key;
                final Action action = interact.action;
                final boolean isForme = action.values.get("type").equals("forme");
                Map<String, String> values = key.getValues();

                final Interaction interaction = new Interaction();
                if (interact.isTag)
                {
                    final ResourceLocation tag = new ResourceLocation(key.id);
                    if (!replace && entry.interactionLogic.canInteract(tag)) continue;
                    entry.interactionLogic.tagActions.put(tag, interaction);
                    DispenseBehaviourInteract.registerBehavior(tag);
                }
                else
                {
                    final ItemStack keyStack = Tools.getStack(values);
                    if (!replace && entry.interactionLogic.canInteract(keyStack)) continue;
                    entry.interactionLogic.stackActions.put(keyStack, interaction);
                    DispenseBehaviourInteract.registerBehavior(keyStack);
                }

                interaction.male = interact.male;
                interaction.female = interact.female;
                interaction.cooldown = interact.cooldown;
                interaction.variance = Math.max(1, interact.variance);
                interaction.hunger = interact.baseHunger;

                if (isForme)
                {
                    final PokedexEntry forme = Database.getEntry(action.values.get("forme"));
                    if (forme != null) interaction.forme = forme;
                }
                else
                {
                    final List<ItemStack> stacks = Lists.newArrayList();
                    for (final Drop d : action.drops)
                    {
                        values = d.getValues();
                        final ItemStack stack = Tools.getStack(values);
                        if (stack != ItemStack.EMPTY) stacks.add(stack);
                    }
                    interaction.stacks = stacks;
                    if (action.lootTable != null) interaction.lootTable = new ResourceLocation(action.lootTable);
                }
            }
        }

        public HashMap<ItemStack, Interaction> stackActions = Maps.newHashMap();

        public HashMap<ResourceLocation, Interaction> tagActions = Maps.newHashMap();

        boolean canInteract(final ItemStack key)
        {
            return this.getFor(key) != null;
        }

        boolean canInteract(final ResourceLocation tag)
        {
            return this.tagActions.containsKey(tag);
        }

        public Interaction getFor(final ItemStack held)
        {
            for (final ItemStack stack : this.stackActions.keySet())
                if (Tools.isSameStack(stack, held)) return this.stackActions.get(stack);
            for (final ResourceLocation loc : this.tagActions.keySet())
                if (ItemList.is(loc, held)) return this.tagActions.get(loc);
            return null;
        }

        public boolean applyInteraction(final Player player, final IPokemob pokemob, final boolean consumeInput)
        {
            final Mob entity = pokemob.getEntity();
            final ItemStack held = player.getMainHandItem();
            final CompoundTag data = entity.getPersistentData();
            final Interaction action = this.getFor(held);
            ItemStack result = null;
            if (action.lootTable != null)
            {
                final LootTable loottable = pokemob.getEntity().getLevel().getServer().getLootTables()
                        .get(action.lootTable);
                final LootContext.Builder lootcontext$builder = new LootContext.Builder(
                        (ServerLevel) pokemob.getEntity().getLevel()).withParameter(LootContextParams.THIS_ENTITY,
                                pokemob.getEntity());
                for (final ItemStack itemstack : loottable
                        .getRandomItems(lootcontext$builder.create(loottable.getParamSet())))
                    if (!itemstack.isEmpty())
                {
                    result = itemstack;
                    break;
                }
            }
            else
            {

                final List<ItemStack> results = action.stacks;
                final int index = player.getRandom().nextInt(results.size());
                result = results.get(index).copy();
            }
            if (result.isEmpty()) return false;
            final long dt = (long) ((action.cooldown + ThutCore.newRandom().nextInt(action.variance))
                    * PokecubeCore.getConfig().interactDelayScale);
            final long now = Tracker.instance().getTick();
            final long timer = dt + now;
            data.putLong("lastInteract", timer);
            pokemob.applyHunger((int) (action.hunger * PokecubeCore.getConfig().interactHungerScale));
            if (consumeInput && !player.isCreative()) held.shrink(1);
            if (held.isEmpty()) player.getInventory().setItem(player.getInventory().selected, result);
            else if (!player.getInventory().add(result)) player.drop(result, false);
            if (player != pokemob.getOwner()) Battle.createOrAddToBattle(entity, player);
            return true;
        }

        boolean interact(final Player player, final IPokemob pokemob, final boolean doInteract)
        {
            final Mob entity = pokemob.getEntity();
            final ItemStack held = player.getMainHandItem();
            final Interaction action = this.getFor(held);
            if (action == null || action.stacks.isEmpty())
            {
                if (action == null) return false;
                if (!doInteract) return true;
                final PokedexEntry forme = action.forme;
                pokemob.changeForm(forme);
                return true;
            }
            final CompoundTag data = entity.getPersistentData();
            if (data.contains("lastInteract"))
            {
                final long now = Tracker.instance().getTick();
                final long time = data.getLong("lastInteract");
                final long diff = now - time;
                if (diff < 0) return false;
            }
            if (!action.male && pokemob.getSexe() == IPokemob.MALE) return false;
            if (!action.female && pokemob.getSexe() == IPokemob.FEMALE) return false;
            if (action.stacks.isEmpty() && action.lootTable == null) return false;
            if (InteractionLogic.isShears.test(held))
            {
                if (pokemob.isSheared()) return true;
                if (doInteract) pokemob.shear(held);
                return true;
            }
            if (!doInteract) return true;
            return this.applyInteraction(player, pokemob, true);
        }
    }

    public static enum MovementType
    {
        FLYING(8), FLOATING(4), WATER(2), NORMAL(1);

        public final int mask;

        private MovementType(final int mask)
        {
            this.mask = mask;
        }

        public boolean is(final int test)
        {
            return (this.mask & test) != 0;
        }

        public static MovementType getType(final String type)
        {
            for (final MovementType t : MovementType.values()) if (t.toString().equalsIgnoreCase(type)) return t;
            return NORMAL;
        }
    }

    public static class SpawnData
    {
        public static class SpawnEntry
        {
            public int max = 4;
            public int min = 2;
            public int minY = Integer.MIN_VALUE;
            public int maxY = Integer.MAX_VALUE;
            public float rate = 0.0f;
            public int level = -1;
            public Variance variance = null;
        }

        final PokedexEntry entry;

        public Map<SpawnBiomeMatcher, SpawnEntry> matchers = Maps.newHashMap();

        public SpawnData(final PokedexEntry entry)
        {
            this.entry = entry;
        }

        public PokedexEntry entry()
        {
            return this.entry;
        }

        public int getLevel(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? -1 : entry.level;
        }

        public SpawnBiomeMatcher getMatcher(final SpawnContext context, final SpawnCheck checker)
        {
            return this.getMatcher(context, checker, true);
        }

        public SpawnBiomeMatcher getMatcher(final SpawnContext context)
        {
            SpawnCheck checker = new SpawnCheck(context.location(), context.level());
            return this.getMatcher(context, checker, true);
        }

        public SpawnBiomeMatcher getMatcher(final SpawnContext context, final SpawnCheck checker,
                final boolean forSpawn)
        {
            List<SpawnBiomeMatcher> matchers = Lists.newArrayList(this.matchers.keySet());
            Collections.shuffle(matchers);
            for (var matcher : matchers)
            {
                final SpawnEvent.Check evt = new SpawnEvent.Check(context, forSpawn);
                PokecubeAPI.POKEMOB_BUS.post(evt);
                if (evt.isCanceled()) continue;
                if (evt.getResult() == Result.ALLOW) return matcher;
                if (matcher.matches(checker)) return matcher;
            }
            return null;
        }

        public SpawnBiomeMatcher getMatcher(final ServerLevel world, final Vector3 location,
                @Nullable ServerPlayer player)
        {
            SpawnContext context = new SpawnContext(player, world, entry, location);
            final SpawnCheck checker = new SpawnCheck(location, world);
            return this.getMatcher(context, checker);
        }

        public int getMax(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? 4 : entry.max;
        }

        public int getMin(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? 2 : entry.min;
        }

        public Variance getVariance(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? new Variance() : entry.variance;
        }

        public float getWeight(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? 0 : entry.rate;
        }

        public float getWeight(final SpawnContext context, SpawnCheck checker, boolean forSpawn)
        {
            final SpawnEntry entry = this.matchers.get(getMatcher(context, checker, forSpawn));
            float rate = entry == null ? 0 : entry.rate;
            if (entry != null)
            {
                if (context.location().y > entry.maxY) rate = 0;
                if (context.location().y < entry.minY) rate = 0;
            }
            SpawnEvent.Check.Rate event = new SpawnEvent.Check.Rate(context, forSpawn, rate);
            PokecubeAPI.POKEMOB_BUS.post(event);
            return event.getRate();
        }

        public boolean isValid(final ResourceLocation biome)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
                if (matcher.getValidBiomes().contains(TagKey.create(Registry.BIOME_REGISTRY, biome))) return true;
            return false;
        }

        public boolean isValid(final Biome biome)
        {
            return isValid(RegHelper.getKey(biome));
        }

        public boolean isValid(final ResourceKey<Biome> biome)
        {
            return isValid(biome.location());
        }

        public boolean isValid(final BiomeType biome)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
                if (matcher._validSubBiomes.contains(biome)) return true;
            return false;
        }

        /**
         * Only checks one biome type for vailidity
         *
         * @param b
         * @return
         */
        public boolean isValid(final SpawnContext context, SpawnCheck checker)
        {
            return this.getMatcher(context, checker) != null;
        }

        public void postInit()
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet()) matcher.reset();
        }
    }

    public static final String TEXTUREPATH = "textures/entity/pokemob/";
    public static final String MODELPATH = "models/entity/pokemob/";

    public static TimePeriod dawn = new TimePeriod(0.85, 0.05);
    public static TimePeriod day = new TimePeriod(0.05, 0.45);
    public static TimePeriod dusk = new TimePeriod(0.45, 0.6);
    public static TimePeriod night = new TimePeriod(0.6, 0.85);

    private static final PokedexEntry BLANK = new PokedexEntry(true);

    public static final ResourceLocation MODELNO = new ResourceLocation(PokecubeCore.MODID, MODELPATH + "missingno");
    public static final ResourceLocation TEXNO = new ResourceLocation(PokecubeCore.MODID,
            TEXTUREPATH + "missingno.png");
    public static final ResourceLocation ANIMNO = new ResourceLocation(PokecubeCore.MODID, MODELPATH + "missingno.xml");

    private static void addFromEvolution(final PokedexEntry a, final PokedexEntry b)
    {
        for (final EvolutionData d : a.evolutions)
        {
            d.postInit();
            final PokedexEntry c = d.evolution;
            if (c == null) continue;
            b.addRelation(c);
            c.addRelation(b);
        }
    }

    // Core Values
    @CopyToGender
    @Required
    public int pokedexNb = -1;

    @Required
    public String name = null;

    /**
     * if True, this is considered the "main" form for the type, this is what is
     * returned from any number based lookups.
     */
    public boolean base = false;
    /**
     * If True, this form won't be registered, this is used for mobs with a
     * single base template form, and then a bunch of alternate ones for things
     * to be copied from.
     */
    public boolean dummy = false;

    /**
     * This is true for any pokemob which is supposed to be an EntityPokemob,
     * with a DefaultPokemob IPokemob, this can be set false by addons to make
     * their own custom pokemob implementations
     */
    public boolean stock = true;

    public boolean generated = false;

    // Values in Stats

    @CopyToGender
    @Required
    public int[] stats = null;

    /** The abilities available to the pokedex entry. */
    @CopyToGender
    public ArrayList<String> abilities = Lists.newArrayList();
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    public ArrayList<String> abilitiesHidden = Lists.newArrayList();

    // Simple values from Stats

    /** base xp given from defeating */
    @CopyToGender
    @Required
    public int baseXP = -1;
    @CopyToGender
    @Required
    public int catchRate = -1;

    /** Initial Happiness of the pokemob */
    @CopyToGender
    @Required
    public int baseHappiness = -1;
    /** The relation between xp and level */
    @CopyToGender
    @Required
    public int evolutionMode = -1;

    @CopyToGender
    @Required
    public int sexeRatio = -1;
    /** Mass of the pokemon in kg. */
    @CopyToGender
    @Required
    public double mass = -1;

    /**
     * If the forme is supposed to have a custom sound, rather than using base,
     * it will be set to this.
     */
    public String customSound = null;
    @CopyToGender
    private PokedexEntry baseForme = null;
    @CopyToGender
    public String baseName;
    @CopyToGender
    public boolean breeds = true;
    @CopyToGender
    public boolean canSitShoulder = false;
    @CopyToGender
    public PokedexEntry _childNb = null;
    /**
     * Default value of specialInfo, used to determine default colour of
     * recolourable parts
     */
    @CopyToGender
    public int defaultSpecial = 0;
    /**
     * Default value of specialInfo for shiny variants, used to determine
     * default colour of recolourable parts
     */
    @CopyToGender
    public int defaultSpecials = 0;
    /**
     * If the IPokemob supports this, then this will be the loot table used for
     * its drops.
     */
    @CopyToGender
    public ResourceLocation lootTable = null;
    /**
     * indicatees of the specified special texture exists. Index 4 is used for
     * if the mob can be dyed
     */
    @CopyToGender
    public boolean dyeable = false;
    /** A Set of valid dye colours, if empty, any dye is valid. */
    @CopyToGender
    public Set<DyeColor> validDyes = Sets.newHashSet();
    @CopyToGender
    public SoundEvent soundEvent;

    @CopyToGender
    public SoundEvent replacedEvent;
    /** The list of pokemon this can evolve into */
    @CopyToGender
    public List<EvolutionData> evolutions = new ArrayList<>();
    @CopyToGender
    public EvolutionData _evolvesBy = null;
    /** Who this pokemon evolves from. */
    @CopyToGender
    public PokedexEntry _evolvesFrom = null;
    @CopyToGender
    public byte[] evs;
    public PokedexEntry female = null;
    /** Inital list of species which are prey */
    @CopyToGender
    public String[] food;

    /**
     * light,<br>
     * rock,<br>
     * power (near redstone blocks),<br>
     * grass,<br>
     * never hungry,<br>
     * berries,<br>
     * water (filter feeds from water)
     */
    @CopyToGender
    public boolean[] foods =
    { false, false, false, false, false, true, false };

    @CopyToGender
    public HashMap<ItemStack, FormeItem> formeItems = Maps.newHashMap();

    public PokedexEntry noItemForm = null;

    /** Map of forms assosciated with this one. */
    @CopyToGender
    public Map<String, PokedexEntry> forms = new HashMap<>();
    /**
     * Used to stop gender formes from spawning, spawning rate is done by gender
     * ratio of base forme instead.
     */
    public boolean isGenderForme = false;

    @CopyToGender
    public boolean hasShiny = true;
    /** Materials which will hurt or make it despawn. */
    @CopyToGender
    public List<IMaterialAction> materialActions = Lists.newArrayList();
    @CopyToGender
    public float height = -1;
    @CopyToGender
    public boolean ridable = true;
    /**
     * This is a loot table to be used for held item. if this isn't null, the
     * above held is ignored.
     */
    @CopyToGender
    public ResourceLocation heldTable = null;
    /** Interactions with items from when player right clicks. */
    @CopyToGender
    public InteractionLogic interactionLogic = new InteractionLogic();

    public boolean isFemaleForme = false;

    public boolean isMaleForme = false;

    @CopyToGender
    public boolean isShadowForme = false;

    /** Will it protect others. */
    @CopyToGender
    public boolean isSocial = true;

    public boolean isStarter = false;
    @CopyToGender
    public boolean isStationary = false;

    @CopyToGender
    public boolean legendary = false;

    @CopyToGender
    public float width = -1;
    @CopyToGender
    public float length = -1;

    /** Map of Level to Moves learned. */
    @CopyToGender
    private Map<Integer, ArrayList<String>> lvlUpMoves;

    public PokedexEntry male = null;

    /** Movement type for this mob, this is a bitmask for MovementType */
    @CopyToGender
    public int mobType = 0;

    /** Mod which owns the pokemob, used for texture location. */
    @CopyToGender
    private String modId;

    /** Particle Effects. */
    @CopyToGender
    public String[] particleData;
    /** Offset between top of hitbox and where player sits */
    @CopyToGender
    public double[][] passengerOffsets =
    {
            { 0, 0.75, 0 } };

    /** All possible moves */
    @CopyToGender
    private List<String> possibleMoves;

    /** If the above is floating, how high does it try to float */
    @CopyToGender
    public double preferedHeight = 1.25;
    /** Pokemobs with these entries will be hunted. */
    @CopyToGender
    private final List<PokedexEntry> prey = new ArrayList<>();

    /**
     * This list will contain all pokemon that are somehow related to this one
     * via evolution chains
     */
    @CopyToGender
    public final List<PokedexEntry> related = new ArrayList<>();

    @CopyToGender
    public PokedexEntry shadowForme = null;

    @CopyToGender
    public boolean shouldDive = false;

    @CopyToGender
    public boolean shouldFly = false;

    @CopyToGender
    public boolean shouldSurf = false;

    @CopyToGender
    public boolean isHeatProof = false;

    @CopyToGender
    public boolean isMega = false;

    @CopyToGender
    public ResourceLocation sound;

    @CopyToGender
    /**
     * This is copied to the gender as it will allow specifying where that
     * gender spawns in pokedex.
     */
    private SpawnData spawns;
    /**
     * Array used for animated or gender based textures. Index 0 is the male
     * textures, index 1 is the females
     */
    @CopyToGender
    public String[][] textureDetails =
    {
            { "" }, null };

    public DefaultFormeHolder _default_holder = null;
    public DefaultFormeHolder _male_holder = null;
    public DefaultFormeHolder _female_holder = null;

    public FormeHolder default_holder = null;
    public FormeHolder male_holder = null;
    public FormeHolder female_holder = null;

    // Icons for the entry, ordering is male/maleshiny, female/female shiny.
    // genderless fills the male slot.
    private final ResourceLocation[][] icons =
    {
            { null, null },
            { null, null } };

    @CopyToGender
    public String texturePath = PokedexEntry.TEXTUREPATH;
    @CopyToGender
    public String modelPath = PokedexEntry.MODELPATH;

    @CopyToGender
    public PokeType type1;

    @CopyToGender
    public PokeType type2;

    @CopyToGender
    public EntityType<? extends Mob> entity_type;

    // This is the actual size of the model, if not null, will be used for
    // scaling of rendering in guis, order is length, height, width
    public Vec3f modelSize = null;

    /** Cached trimmed name. */
    private String trimmedName;

    private ResourceLocation _base_description = new ResourceLocation("null");
    private Map<ResourceLocation, MutableComponent> _descriptions = new HashMap<>();

    // "" for automatic assignment
    public String modelExt = "";
    public ResourceLocation model = PokedexEntry.MODELNO;
    public ResourceLocation texture = PokedexEntry.TEXNO;
    public ResourceLocation animation = PokedexEntry.ANIMNO;

    public Map<String, BodyNode> poseShapes = null;

    // Here we have things that need to wait until loaded for initialization, so
    // we cache them.
    public List<Interact> _loaded_interactions = Lists.newArrayList();
    public List<FormeItem> _forme_items = Lists.newArrayList();

    /** Times not included here the pokemob will go to sleep when idle. */
    @CopyToGender
    public List<TimePeriod> activeTimes = new ArrayList<>();

    public JsonPokedexEntry _root_json;

    /**
     * This constructor is used for making blank entry for copy comparisons.
     *
     * @param blank
     */
    private PokedexEntry(final boolean blank)
    {
        // Nothing
    }

    public PokedexEntry(final int nb, final String name, boolean isExtraForm)
    {
        this.name = name;
        this.pokedexNb = nb;
        if (Database.getEntry(name) == null) Database.allFormes.add(this);
        else new NullPointerException("Trying to add another " + name + " " + Database.getEntry(name))
                .printStackTrace();
        this.generated = isExtraForm;
    }

    public void postTagsReloaded()
    {
        this.formeItems.clear();
        if (this._forme_items != null)
        {
            List<FormeItem> rules = new ArrayList<>();
            Set<String> uniques = Sets.newHashSet();
            for (final FormeItem rule : this._forme_items)
            {
                if (uniques.add(JsonUtil.smol_gson.toJson(rule))) rules.add(rule);
            }
            this._forme_items.clear();
            this._forme_items.addAll(rules);

            for (FormeItem i : _forme_items)
            {
                PokedexEntry output = i.getOutput();
                if (output == null && i.getForme(this) == null)
                {
                    PokecubeAPI.LOGGER.error("Error loading output forme for " + this);
                    continue;
                }
                try
                {
                    ItemStack stack = i.getKey();
                    if (stack.isEmpty())
                    {
                        PokecubeAPI.LOGGER.error("Error with key  for " + this);
                        continue;
                    }
                    PokecubeItems.ADDED_HELD.add(RegHelper.getKey(stack));
                    this.formeItems.put(stack, i);
                    if (output != null)
                    {
                        if (output.noItemForm != null && output.noItemForm != this) PokecubeAPI.LOGGER
                                .warn("Changing Base forme of {} from {} to {}", output, output.noItemForm, this);
                        if (PokecubeCore.getConfig().debug_data)
                            PokecubeAPI.logInfo("Adding Forme with Key " + stack + " To " + output + " for " + this);
                        output.noItemForm = this;
                    }
                    else
                    {
                        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo(
                                "Adding Forme with Key " + stack + " To " + i.getForme(this).key + " for " + this);
                    }
                }
                catch (Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error loading forme " + output + " for " + this);
                    PokecubeAPI.LOGGER.error(e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void addInteractions(List<Interact> interactions)
    {
        if (interactions == null) return;
        interactions.forEach(i -> {
            if (!_loaded_interactions.contains(i)) _loaded_interactions.add(i);
        });
    }

    /**
     * Applies various things which needed server to be initialized, such as
     * interactions for tag lists, etc
     */
    public void onResourcesReloaded()
    {
        this.formeItems.clear();
        this.interactionLogic.stackActions.clear();
        // Apply loaded interactions
        if (!this._loaded_interactions.isEmpty()) InteractionLogic.initForEntry(this, this._loaded_interactions, true);
        // Apply default interactions
        InteractionLogic.initForEntry(this);

        if (this.getSpawnData() != null) this.getSpawnData().postInit();

        final Class<?> me = this.getClass();
        Required c;
        for (final Field f : me.getDeclaredFields())
        {
            c = f.getAnnotation(Required.class);
            if (c != null) try
            {
                f.setAccessible(true);
                if (this.isSame(f, this, PokedexEntry.BLANK))
                {
                    boolean fixed = false;
                    PokedexEntry base = this.getBaseForme();
                    if (base == null) base = Database.dummyMap.get(this.getPokedexNb());
                    if (base != null)
                    {
                        f.set(this, f.get(base));
                        fixed = !this.isSame(f, this, PokedexEntry.BLANK);
                    }
                    if (!fixed) PokecubeAPI.LOGGER.error("Unfilled value {} for {}!", f.getName(), this);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        // Set the tag based values
        this.shouldFly = this.isType(PokeType.getType("flying"));
        this.shouldFly = this.shouldFly || Tags.POKEMOB.isIn("hms/fly", this.getTrimmedName());
        if (Tags.POKEMOB.isIn("hms/no_fly", this.getTrimmedName())) this.shouldFly = false;
        this.shouldDive = Tags.POKEMOB.isIn("hms/dive", this.getTrimmedName());
        this.shouldSurf = Tags.POKEMOB.isIn("hms/surf", this.getTrimmedName());
        this.canSitShoulder = Tags.POKEMOB.isIn("shoulder_allowed", this.getTrimmedName());
        this.isHeatProof = Tags.POKEMOB.isIn("fire_proof", this.getTrimmedName());
        this.isStarter = Tags.POKEMOB.isIn("starters", this.getTrimmedName());
        this.legendary = Tags.POKEMOB.isIn("legends", this.getTrimmedName());
        this.isShadowForme = Tags.POKEMOB.isIn("shadow", this.getTrimmedName());
        this.isMega = Tags.POKEMOB.isIn("mega_gmax", this.getTrimmedName());

        // Breeding whitelist is generally for legends that are explicitly
        // allowed to breed, like manaphy
        this.breeds = Tags.POKEMOB.isIn("breeding_whitelist", this.getTrimmedName())
                || !Tags.POKEMOB.isIn("no_breeding", this.getTrimmedName());

        this.foods[0] = Tags.POKEMOB.isIn("food_types/light", this.getTrimmedName());
        this.foods[1] = Tags.POKEMOB.isIn("food_types/stone", this.getTrimmedName());
        this.foods[2] = Tags.POKEMOB.isIn("food_types/redstone", this.getTrimmedName());
        this.foods[3] = Tags.POKEMOB.isIn("food_types/plants", this.getTrimmedName());
        this.foods[4] = Tags.POKEMOB.isIn("food_types/never", this.getTrimmedName());
        this.foods[5] = !Tags.POKEMOB.isIn("food_types/no_berries", this.getTrimmedName());
        this.foods[6] = Tags.POKEMOB.isIn("food_types/water", this.getTrimmedName());

        this.activeTimes.clear();
        if (Tags.POKEMOB.isIn("active_times/day", this.getTrimmedName())) this.activeTimes.add(PokedexEntry.day);
        if (Tags.POKEMOB.isIn("active_times/night", this.getTrimmedName())) this.activeTimes.add(PokedexEntry.night);
        if (Tags.POKEMOB.isIn("active_times/dusk", this.getTrimmedName())) this.activeTimes.add(PokedexEntry.dusk);
        if (Tags.POKEMOB.isIn("active_times/dawn", this.getTrimmedName())) this.activeTimes.add(PokedexEntry.dawn);

        if (Tags.MOVEMENT.isIn("floats", this.getTrimmedName()))
        {
            Float amount = Tags.MOVEMENT.get("floats", this.getTrimmedName());
            if (amount != null) this.preferedHeight = amount;
            this.mobType |= MovementType.FLOATING.mask;
        }
        if (Tags.MOVEMENT.isIn("flies", this.getTrimmedName())) this.mobType |= MovementType.FLYING.mask;
        if (Tags.MOVEMENT.isIn("swims", this.getTrimmedName())) this.mobType |= MovementType.WATER.mask;
        if (Tags.MOVEMENT.isIn("walks", this.getTrimmedName())) this.mobType |= MovementType.NORMAL.mask;

        this.copyToGenderFormes();
    }

    public List<TimePeriod> activeTimes()
    {
        if (this.activeTimes.isEmpty()) this.activeTimes.add(TimePeriod.fullDay);
        return this.activeTimes;
    }

    public void addEvolution(final EvolutionData toAdd)
    {
        this.evolutions.add(toAdd);
    }

    public void addForm(final PokedexEntry form)
    {
        if (this.forms.containsValue(form)) return;
        final String key = form.getTrimmedName();
        form.baseName = this.getTrimmedName();
        form.setBaseForme(this);
        this.forms.put(key, form);
    }

    public void addMove(final String move)
    {
        for (final String s : this.possibleMoves) if (s.equals(move)) return;
        this.possibleMoves.add(move);
    }

    public void addMoves(final List<String> moves, final Map<Integer, ArrayList<String>> lvlUpMoves2)
    {
        this.lvlUpMoves = lvlUpMoves2;
        this.possibleMoves = moves;
    }

    private void addRelation(final PokedexEntry toAdd)
    {
        if (!this.getRelated().contains(toAdd) && toAdd != null && toAdd != this) this.getRelated().add(toAdd);
    }

    public ResourceLocation animation()
    {
        return this.animation;
    }

    public boolean areRelated(final PokedexEntry toTest)
    {
        return toTest == this || this.getRelated().contains(toTest);
    }

    public boolean canEvolve()
    {
        return this.evolutions.size() > 0;
    }

    public void copyFieldsToGenderForm(final PokedexEntry forme)
    {
        final Class<?> me = this.getClass();
        CopyToGender c;
        for (final Field f : me.getDeclaredFields())
        {
            c = f.getAnnotation(CopyToGender.class);
            if (c != null) try
            {
                f.setAccessible(true);
                if (this.isSame(f, forme, PokedexEntry.BLANK)) f.set(forme, f.get(this));
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void copyToForm(final PokedexEntry e)
    {
        if (e.baseForme != null && e.baseForme != this)
            throw new IllegalArgumentException("Cannot add a second base form");
        e.pokedexNb = this.pokedexNb;

        if (e.possibleMoves == null) e.possibleMoves = this.possibleMoves;
        if (e.lvlUpMoves == null) e.lvlUpMoves = this.lvlUpMoves;
        if (e.stats == null) e.stats = this.stats.clone();
        if (this.evs == null) PokecubeAPI.LOGGER.error(this + " " + this.baseForme, new IllegalArgumentException());
        if (e.evs == null) e.evs = this.evs.clone();
        if (e.height == -1) e.height = this.height;
        if (e.width == -1) e.width = this.width;
        if (e.length == -1) e.length = this.length;
        if (e.mobType == 0) e.mobType = this.mobType;
        if (e.catchRate == -1) e.catchRate = this.catchRate;
        if (e.sexeRatio == -1) e.sexeRatio = this.sexeRatio;
        if (e.type1 == null) e.type1 = this.type1;
        if (e.type2 == null) e.type2 = this.type2;
        if (e.mass == -1) e.mass = this.mass;
        for (int i = 0; i < this.foods.length; i++) e.foods[i] = this.foods[i];
        e.breeds = this.breeds;
        e.legendary = this.legendary;
        e.setBaseForme(this);
        this.addForm(e);
    }

    public void copyToGenderFormes()
    {
        if (this.male != null) this.copyFieldsToGenderForm(this.male);
        if (this.female != null) this.copyFieldsToGenderForm(this.female);
    }

    public void setGenderedForm(DefaultFormeHolder model, byte gender)
    {
        if (gender == IPokemob.MALE && model != null)
        {
            this._male_holder = model;
            this.male = model.getEntry();
            this.male.isGenderForme = true;
            this.male.isMaleForme = true;
            this.male.setBaseForme(this);
            this.copyToForm(male);
            this.copyFieldsToGenderForm(this.male);
        }
        if (gender == IPokemob.FEMALE && model != null)
        {
            this._female_holder = model;
            this.female = model.getEntry();
            this.female.isGenderForme = true;
            this.female.isFemaleForme = true;
            this.female.setBaseForme(this);
            this.copyToForm(female);
            this.copyFieldsToGenderForm(this.female);
        }
    }

    public boolean floats()
    {
        return MovementType.FLOATING.is(this.mobType);
    }

    public boolean flys()
    {
        return MovementType.FLYING.is(this.mobType);
    }

    public Ability getAbility(final int number, final IPokemob pokemob)
    {
        List<String> abilities = this.abilities;
        if (number < abilities.size()) return AbilityManager.getAbility(abilities.get(number));
        if (number == 2) return this.getHiddenAbility(pokemob);
        return null;
    }

    public PokedexEntry getBaseForme()
    {
        if (this.baseForme == null && !this.base) this.baseForme = Database.getEntry(this.getPokedexNb());
        if (this.baseForme == this) this.baseForme = null;
        return this.baseForme;
    }

    /**
     * For pokemon with multiple formes
     *
     * @return the base forme name.
     */
    public String getBaseName()
    {
        if (this.baseName == null)
        {
            if (this.getBaseForme() != null && this.getBaseForme() != this)
                this.baseName = this.getBaseForme().getTrimmedName();
            else this.baseName = this.getTrimmedName();
            if (this.getBaseForme() == this) PokecubeAPI.LOGGER.error("Error with " + this);
        }
        return this.baseName;
    }

    /** @return the baseXP */
    public int getBaseXP()
    {
        if (this.baseXP == -1)
            this.baseXP = this.getBaseForme() != null && this.getBaseForme() != this ? this.getBaseForme().getBaseXP()
                    : 0;
        return this.baseXP;
    }

    /** @return the catchRate */
    public int getCatchRate()
    {
        return this.catchRate;
    }

    public PokedexEntry getChild()
    {
        if (this._childNb == null)
        {
            for (final PokedexEntry e : this.getRelated())
                for (final EvolutionData d : e.evolutions) if (d.evolution == this) this._childNb = e.getChild();
            if (this._childNb == null) this._childNb = this;
        }

        return this._childNb;
    }

    public PokedexEntry getChild(final PokedexEntry fatherNb)
    {
        return this.getChild();
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getDescription(@Nullable IPokemob pokemob, @Nullable FormeHolder holder)
    {
        _descriptions.clear();
        ResourceLocation key = holder == null ? _base_description : holder.key;
        return _descriptions.computeIfAbsent(key, k -> {

            PokeType type1 = this.type1;
            PokeType type2 = this.type2;

            final PokedexEntry entry = this;
            final MutableComponent typeString = PokeType.getTranslatedName(type1);
            if (type2 != PokeType.unknown) typeString.append("/").append(PokeType.getTranslatedName(type2));
            final MutableComponent typeDesc = TComponent.translatable("pokemob.description.type",
                    entry.getTranslatedName(), typeString);
            MutableComponent evoString = TComponent.literal("");
            for (final EvolutionData d : entry.evolutions)
            {
                if (d.evolution == null) continue;

                var compDesc = d.getEvoString();
                if (pokemob != null && d.shouldEvolve(pokemob))
                    compDesc = compDesc.setStyle(compDesc.getStyle().withColor(ChatFormatting.GOLD));

                if (evoString == null) evoString = compDesc;
                else evoString = evoString.append("\n").append(compDesc);
                evoString.append("\n");
            }
            MutableComponent descString = typeDesc;
            if (evoString != null) descString = descString.append("\n").append(evoString);
            if (entry._evolvesFrom != null)
                descString = descString.append("\n").append(TComponent.translatable("pokemob.description.evolve.from",
                        entry.getTranslatedName(), entry._evolvesFrom.getTranslatedName()));
            return descString;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getDescription(@Nullable IPokemob pokemob)
    {
        return getDescription(pokemob, null);
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getDescription(@Nullable FormeHolder holder)
    {
        return getDescription(null, holder);
    }

    public EntityType<? extends Mob> getEntityType()
    {
        if (this.entity_type == null && this.getBaseForme() != null)
            this.entity_type = this.getBaseForme().getEntityType();
        return this.entity_type;
    }

    /** @return the evolutionMode */
    public int getEvolutionMode()
    {
        if (this.getBaseForme() != null) return this.getBaseForme().getEvolutionMode();
        if (this.evolutionMode < 0)
        {
            if (this != Database.missingno)
                PokecubeAPI.LOGGER.error("Undefined evo mode for {}, setting to \"2\"", this);
            this.evolutionMode = 2;
        }
        return this.evolutionMode;
    }

    public List<EvolutionData> getEvolutions()
    {
        return this.evolutions;
    }

    /** @return the EVs earned by enemy at the end of a fight */
    public byte[] getEVs()
    {
        return this.evs;
    }

    public PokedexEntry getForGender(final byte gender)
    {
        if (this.isGenderForme && this.getBaseForme() != null) return this.getBaseForme().getForGender(gender);
        var m = this.male;
        var f = this.female;
        if (this.male == null) m = this;
        if (this.female == null) f = this;
        return gender == IPokemob.MALE ? m : f;
    }

    public int getGen()
    {
        if (this.pokedexNb < 152) return 1;
        if (this.pokedexNb < 252) return 2;
        if (this.pokedexNb < 387) return 3;
        if (this.pokedexNb < 494) return 4;
        if (this.pokedexNb < 650) return 5;
        if (this.pokedexNb < 722) return 6;
        if (this.pokedexNb < 810) return 7;
        return 8;
    }

    public int getHappiness()
    {
        return this.baseHappiness;
    }

    public Ability getHiddenAbility(final IPokemob pokemob)
    {
        if (this.abilitiesHidden.isEmpty()) return this.getAbility(0, pokemob);
        else if (this.abilitiesHidden.size() == 1) return AbilityManager.getAbility(this.abilitiesHidden.get(0));
        else if (this.abilitiesHidden.size() == 2)
            return pokemob.getSexe() == IPokemob.MALE ? AbilityManager.getAbility(this.abilitiesHidden.get(0))
                    : AbilityManager.getAbility(this.abilitiesHidden.get(1));
        return null;

    }

    public Vec3f getModelSize()
    {
        if (this.modelSize == null) this.modelSize = new Vec3f(this.length, this.height, this.width);
        return this.modelSize;
    }

    /**
     * Gets the Mod which declares this mob.
     *
     * @return the modId
     */
    public String getModId()
    {
        if (this.modId == null && this.getBaseForme() != null) this.modId = this.getBaseForme().modId;
        return this.modId;
    }

    /** A list of all valid moves for this pokemob */
    public List<String> getMoves()
    {
        return this.possibleMoves;
    }

    public List<String> getMovesForLevel(final int level)
    {
        final List<String> ret = new ArrayList<>();

        if (this.lvlUpMoves == null) return ret;

        for (int i = 0; i <= level; i++)
        {
            if (this.lvlUpMoves.get(i) == null) continue;
            for (final String s : this.lvlUpMoves.get(i)) ret.add(s);
        }

        return ret;
    }

    public List<String> getMovesForLevel(final int level, final int oldLevel)
    {
        final List<String> ret = new ArrayList<>();

        if (this.lvlUpMoves == null) return ret;

        if (oldLevel <= 0) return this.getMovesForLevel(level);

        for (int i = oldLevel; i < level; i++)
        {
            if (this.lvlUpMoves.get(i + 1) == null) continue;
            for (final String s : this.lvlUpMoves.get(i + 1)) ret.add(s);
        }

        return ret;
    }

    public String getName()
    {
        return this.name;
    }

    public int getPokedexNb()
    {
        return this.pokedexNb;
    }

    public ItemStack getRandomHeldItem(final Mob mob)
    {
        if (mob.getLevel().isClientSide) return ItemStack.EMPTY;
        if (this.heldTable != null)
        {
            final LootTable loottable = mob.getLevel().getServer().getLootTables().get(this.heldTable);
            final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel) mob.getLevel())
                    .withParameter(LootContextParams.THIS_ENTITY, mob)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, DamageSource.GENERIC)
                    .withParameter(LootContextParams.ORIGIN, mob.position());
            for (final ItemStack itemstack : loottable.getRandomItems(
                    lootcontext$builder.create(loottable.getParamSet())))
                if (!itemstack.isEmpty()) return itemstack;
        }
        return ItemStack.EMPTY;
    }

    public List<PokedexEntry> getRelated()
    {
        return this.related;
    }

    /** @return the sexeRatio */
    public int getSexeRatio()
    {
        return this.sexeRatio;
    }

    public SoundEvent getSoundEvent()
    {
        if (this.replacedEvent != null) return this.replacedEvent;
        if (this.soundEvent == null) if (this.getBaseForme() != null && this.getBaseForme() != this)
        {
            this.soundEvent = this.getBaseForme().getSoundEvent();
            this.sound = this.getBaseForme().sound;
        }
        return this.soundEvent;
    }

    public SpawnData getSpawnData()
    {
        return this.spawns;
    }

    public int getStatATT()
    {
        return this.stats[1];
    }

    public int getStatATTSPE()
    {
        return this.stats[3];
    }

    public int getStatDEF()
    {
        return this.stats[2];
    }

    public int getStatDEFSPE()
    {
        return this.stats[4];
    }

    public int getStatHP()
    {
        return this.stats[0];
    }

    /** @return the stats */
    public int[] getStats()
    {
        return this.stats;
    }

    public int getStatVIT()
    {
        return this.stats[5];
    }

    public String getTexture(final byte gender)
    {
        final String original = this.getTrimmedName();
        final int index = gender == IPokemob.FEMALE && this.textureDetails[1] != null ? 1 : 0;
        final String[] textureSuffixs = this.textureDetails[index];
        final String suffix = textureSuffixs[0];
        String ret = original + suffix + ".png";
        ret = ret.replaceAll("([^a-zA-Z0-9/. _-])", "");
        if (!ret.contains(this.texturePath)) ret = this.texturePath + ret;
        return ret;
    }

    private MutableComponent nameComp;

    public MutableComponent getTranslatedName()
    {
        if (this.nameComp == null)
        {
            String key = this.getUnlocalizedName();
            if (!(this.getEntityType() instanceof PokemobType<?>)) key = this.getEntityType().getDescriptionId();
            this.nameComp = TComponent.translatable(key);
            this.nameComp.setStyle(this.nameComp.getStyle().withClickEvent(
                    new ClickEvent(net.minecraft.network.chat.ClickEvent.Action.CHANGE_PAGE, this.getTrimmedName())));
        }
        return this.nameComp;
    }

    /**
     * Returns the name in a format that will work for files, ie no . at the
     * end.
     *
     * @return
     */
    public String getTrimmedName()
    {
        if (this.trimmedName != null) return this.trimmedName;
        return this.trimmedName = Database.trim(this.name);
    }

    /** @return the type1 */
    public PokeType getType1()
    {
        return this.type1;
    }

    /** @return the type2 */
    public PokeType getType2()
    {
        return this.type2;
    }

    /** @return the name to be fed to the language formatter */
    public String getUnlocalizedName()
    {
        String name = this.getTrimmedName();
        if (this.generated) name = this.getBaseName();
        final String translated = "entity.pokecube." + name;
        return translated;
    }

    public boolean hasForm(final String form)
    {
        return this.forms.containsKey(Database.trim(form));
    }

    public Collection<PokedexEntry> getFormes()
    {
        return this.forms.values();
    }

    public boolean hasPrey()
    {
        return this.prey.size() > 0;
    }

    public void initPrey()
    {
        this.prey.clear();
        if (this.food == null) return;
        final List<String> foodList = new ArrayList<>();
        for (final String s : this.food) foodList.add(s.contains(":") ? s : "pokecube:" + s);
        poke:
        for (final PokedexEntry e : Database.data.values())
        {
            final Set<String> tags = Tags.CREATURES.lookupTags(e.getTrimmedName());
            for (final String s : tags) if (foodList.contains(s))
            {
                this.prey.add(e);
                continue poke;
            }
        }
    }

    public void initRelations()
    {
        this.addRelation(this);

        this.evolutions.clear();

        PokedexEntry breedEntry = this;
        if (this.isGenderForme) breedEntry = this.getBaseForme();
        List<Evolution> evos = EvolutionDataLoader.RULES.getOrDefault(breedEntry, Collections.emptyList());
        // Sort the list, this uses the priority, so some can be set to match
        // first.
        evos.sort(null);

        for (final Evolution evol : evos)
        {
            String name = evol.name;
            final PokedexEntry evolEntry = Database.getEntry(name);
            if (evolEntry == null)
            {
                PokecubeAPI.LOGGER.error("Entry {} not found for evolution of {}, skipping", name, this.name);
                continue;
            }
            EvolutionData d = new EvolutionData(evolEntry, evol);
            this.evolutions.add(d);
            d.postInit();
            final PokedexEntry temp = d.evolution;
            if (temp == null) continue;
            temp._evolvesFrom = this;
            temp._evolvesBy = d;
            temp.addRelation(this);
            this.addRelation(temp);
            for (final PokedexEntry d1 : temp.getRelated())
            {
                d1.addRelation(this);
                this.addRelation(d1);
            }
            PokedexEntry.addFromEvolution(this, temp);
            PokedexEntry.addFromEvolution(temp, this);
        }
        final Set<String> ourTags = Tags.BREEDING.lookupTags(this.getTrimmedName());

        List<PokedexEntry> sorted = Database.getSortedFormes();
        entries:
        for (int i = sorted.indexOf(this) + 1; i < sorted.size(); i++)
        {
            // By this point, we are already in a loop of getSortedFormes(), and
            // anyone below us in the list has already checked us for relations.
            // So we start from our index + 1, as to not have n^2 complexity in
            // this lookup.

            PokedexEntry e = sorted.get(i);
            // Already related, skip
            if (this.areRelated(e)) continue;
            final Set<String> theirTags = Tags.BREEDING.lookupTags(e.getTrimmedName());
            for (final String s : theirTags) if (ourTags.contains(s))
            {
                this.addRelation(e);
                e.addRelation(this);
                continue entries;
            }

        }
        this.getRelated().sort(Database.COMPARATOR);
    }

    /**
     * returns whether the interaction logic has a response listed for the given
     * key.
     *
     * @param cube
     * @param doInteract - if false, will not actually do anything.
     * @return
     */
    public boolean interact(final ItemStack stack)
    {
        return this.interactionLogic.canInteract(stack);
    }

    /**
     * Call whenever player right clicks a pokemob to run special interaction
     * logic
     *
     * @param player
     * @param pokemob
     * @param doInteract - if false, will not actually do anything.
     * @return
     */
    public boolean interact(final Player player, final IPokemob pokemob, final boolean doInteract)
    {
        return this.interactionLogic.interact(player, pokemob, doInteract);
    }

    public boolean isFood(final PokedexEntry toTest)
    {
        return this.prey.contains(toTest);
    }

    private boolean isSame(final Field field, final Object one, final Object two) throws Exception
    {
        if (one == two) return true;
        field.setAccessible(true);
        final Object a = field.get(one);
        final Object b = field.get(two);
        if (a == b) return true;
        if (a != null) return a.equals(b);
        return false;
    }

    public boolean isType(final PokeType type)
    {
        return this.type1 != null && this.type1 == type || this.type2 != null && this.type2 == type;
    }

    public ResourceLocation model()
    {
        return this.model;
    }

    /**
     * to be called after the new stack is applied as held item.
     *
     * @param oldStack
     * @param newStack
     * @param pokemob
     */
    public void onHeldItemChange(final ItemStack oldStack, final ItemStack newStack, final IPokemob pokemob)
    {
        if (newStack.isEmpty() && oldStack.isEmpty()) return;
        if (!ThutCore.proxy.isServerSide()) return;
        boolean isChangedForme = pokemob.getCustomHolder() != null && pokemob.getCustomHolder()._is_item_forme;
        PokedexEntry base = this;
        if (!isChangedForme && this.formeItems.isEmpty() && this.getBaseForme() != null)
        {
            for (var entry : this.getBaseForme().formeItems.entrySet())
            {
                if (entry.getValue().getOutput() == this)
                {
                    isChangedForme = true;
                    base = this.getBaseForme();
                    break;
                }
            }
        }
        if (isChangedForme && base != this)
        {
            base.onHeldItemChange(oldStack, newStack, pokemob);
            return;
        }
        PokedexEntry newForme = null;
        FormeHolder newHolder = null;
        if (newStack.isEmpty() && this.noItemForm != null) newForme = this.noItemForm;
        for (final ItemStack key : this.formeItems.keySet()) if (Tools.isSameStack(oldStack, key, true))
        {
            newForme = this;
            newHolder = this.getModel(pokemob.getSexe());
            break;
        }
        for (final ItemStack key : this.formeItems.keySet()) if (Tools.isSameStack(newStack, key, true))
        {
            FormeItem forme = this.formeItems.get(key);
            newForme = forme.getOutput();
            newHolder = forme.getForme(this);
            break;
        }
        // Set the custom holder regardless incase it was needed.
        if (newHolder != null) pokemob.setCustomHolder(newHolder);
        if (newForme != null && newForme != pokemob.getPokedexEntry())
            MegaEvoTicker.scheduleEvolve(newForme, pokemob, null);
    }

    public void setBaseForme(final PokedexEntry baseForme)
    {
        if (baseForme == this) return;
        if (this.baseForme != null && baseForme != this.baseForme && this.baseForme != Database.missingno)
            PokecubeAPI.LOGGER.error("Trying to replace {} with {} as base for {}", this.baseForme, baseForme, this);
        this.baseForme = baseForme;
        this.base = false;
        this.pokedexNb = baseForme.pokedexNb;
        this.modId = baseForme.modId;
    }

    public void setEntityType(final EntityType<? extends Mob> type)
    {
        this.entity_type = type;
    }

    /**
     * Sets the Mod which declares this mob.
     *
     * @param modId the modId to set
     */
    public void setModId(final String modId)
    {
        if (this.modId != null && !this.modId.equals(modId)) PokecubeAPI.LOGGER
                .debug("Modid changed to: " + modId + " for " + this + " from " + this.modId, new Exception());
        this.modId = modId;
    }

    /**
     * @param sound
     */
    public void setSound(String sound)
    {
        boolean mobs = false;
        if (mobs = sound.startsWith("mobs.")) sound = sound.replaceFirst("mobs.", "");
        // Replace all non word chars.
        sound = ThutCore.trim(sound);
        if (mobs) sound = "mobs." + sound;
        this.sound = new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), sound);
    }

    public void setSpawnData(final SpawnData data)
    {
        this.spawns = data;
    }

    public boolean swims()
    {
        return MovementType.WATER.is(this.mobType);
    }

    public ResourceLocation texture()
    {
        return this.texture;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    public void updateMoves()
    {
        final List<String> moves = new ArrayList<>();

        if (this.possibleMoves == null)
        {
            this.possibleMoves = this.getBaseForme().possibleMoves;
            this.possibleMoves.isEmpty();
        }
        if (this.lvlUpMoves == null) this.lvlUpMoves = this.getBaseForme().lvlUpMoves;

        for (final String s : this.possibleMoves)
            if (MovesUtils.isMoveImplemented(s) && !moves.contains(s)) moves.add(s);
        this.possibleMoves.clear();
        this.possibleMoves.addAll(moves);
        final List<Integer> toRemove = new ArrayList<>();
        for (final int level : this.lvlUpMoves.keySet())
        {
            moves.clear();
            final List<String> lvls = this.lvlUpMoves.get(level);
            for (int i = 0; i < lvls.size(); i++)
            {
                final String s = lvls.get(i);
                if (MovesUtils.isMoveImplemented(s)) moves.add(s);
            }
            lvls.clear();
            lvls.addAll(moves);
            if (lvls.size() == 0) toRemove.add(level);

        }
        for (final int i : toRemove) this.lvlUpMoves.remove(i);
    }

    public boolean genderDiffers(final byte sexe)
    {
        final boolean hasFemale = this.female_holder != null;
        final boolean hasMale = this.male_holder != null;
        if (hasFemale && sexe == IPokemob.FEMALE) return true;
        if (hasMale && sexe == IPokemob.MALE) return true;
        if (this.getForGender(sexe).isGenderForme) return true;
        return sexe == IPokemob.FEMALE && this.textureDetails[1] != null;
    }

    public FormeHolder getModel(final byte sexe)
    {
        final boolean hasFemale = this.female_holder != null;
        final boolean hasMale = this.male_holder != null;
        if (hasFemale && sexe == IPokemob.FEMALE) return this.female_holder;
        if (hasMale && sexe == IPokemob.MALE) return this.male_holder;
        return this.default_holder;
    }

    public ResourceLocation getIcon(final boolean male, final boolean shiny)
    {
        if (this.icons[0][0] == null)
        {
            final String path = this.texturePath.replace("entity", "entity_icon");
            final String texture = path + this.getTrimmedName();
            if (this.isGenderForme)
            {
                this.icons[0][0] = new ResourceLocation(texture + ".png");
                this.icons[0][1] = new ResourceLocation(texture + "_s.png");
                this.icons[1][0] = new ResourceLocation(texture + ".png");
                this.icons[1][1] = new ResourceLocation(texture + "_s.png");
            }
            else
            {
                String male_ = this.genderDiffers(IPokemob.MALE) ? "_male" : "";
                String female_ = this.genderDiffers(IPokemob.FEMALE) ? "_female" : "";

                // 0 is male
                boolean noGender = this.getSexeRatio() == 0;
                // 254 is female, 255 is no gender
                noGender = noGender || this.getSexeRatio() >= 254;
                if (noGender) male_ = female_ = "";

                this.icons[0][0] = new ResourceLocation(texture + male_ + ".png");
                this.icons[0][1] = new ResourceLocation(texture + male_ + "_s.png");
                this.icons[1][0] = new ResourceLocation(texture + female_ + ".png");
                this.icons[1][1] = new ResourceLocation(texture + female_ + "_s.png");
            }
        }
        final int i = male ? 0 : 1;
        final int j = shiny ? 1 : 0;
        return this.icons[i][j];
    }

    public boolean isLegendary()
    {
        final boolean baseLegend = this.getBaseForme() != null && this.getBaseForme() != this
                && this.getBaseForme().isLegendary();
        return baseLegend || this.legendary || SpecialCaseRegister.getCaptureCondition(this) != null
                || SpecialCaseRegister.getSpawnCondition(this) != null;
    }

    public boolean isMega()
    {
        return this.isMega;
    }
}
