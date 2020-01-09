/**
 *
 */
package pokecube.core.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.vecmath.Vector3f;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntryLoader.Action;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.Evolution;
import pokecube.core.database.PokedexEntryLoader.Interact;
import pokecube.core.database.PokedexEntryLoader.Key;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.DispenseBehaviourInteract;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou */
public class PokedexEntry
{
    // Annotation used to specify which fields should be shared to all gender
    // formes.
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface CopyToGender
    {
    }

    public static class EvolutionData
    {
        public SpawnBiomeMatcher  matcher = null;
        public Evolution          data;
        public boolean            dayOnly = false;
        public final PokedexEntry evolution;
        public String             FX      = "";
        // 1 for male, 2 for female, 0 for either;
        public byte    gender = 0;
        public boolean happy  = false;
        // the item it must be holding, if null, any item is fine, or no items
        // is fine
        public ItemStack        item   = ItemStack.EMPTY;
        public ResourceLocation preset = null;
        // does it need to grow a level for the item to work
        public boolean      itemLevel    = false;
        public int          level        = -1;
        public String       move         = "";
        public boolean      nightOnly    = false;
        public boolean      dawnOnly     = false;
        public boolean      duskOnly     = false;
        public PokedexEntry preEvolution;
        public boolean      rainOnly     = false;
        public float        randomFactor = 1.0f;
        public boolean      traded       = false;

        public EvolutionData(final PokedexEntry evol)
        {
            this.evolution = evol;
        }

        public Entity getEvolution(final World world)
        {
            if (this.evolution == null) return null;
            final Entity ret = PokecubeCore.createPokemob(this.evolution, world);
            return ret;
        }

        @OnlyIn(Dist.CLIENT)
        public String getEvoString()
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
            final PokedexEntry entry = this.preEvolution;
            final PokedexEntry nex = this.evolution;
            String subEvo = I18n.format("pokemob.description.evolve.to", entry.getTranslatedName(), nex
                    .getTranslatedName());
            if (this.level > 0) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.level", this.level);
            if (this.traded) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.traded");
            if (this.gender == 1) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.male");
            if (this.gender == 2) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.female");
            if (!this.item.isEmpty()) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.item", this.item
                    .getDisplayName().getFormattedText());
            else if (this.preset != null)
            {
                final ItemStack stack = PokecubeItems.getStack(this.preset);
                if (!stack.isEmpty()) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.item", stack
                        .getDisplayName().getFormattedText());
            }
            if (this.happy) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.happy");
            if (this.dawnOnly) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.dawn");
            if (this.duskOnly) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.dusk");
            if (this.dayOnly) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.day");
            if (this.nightOnly) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.night");
            if (this.rainOnly) subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.rain");
            if (this.randomFactor != 1)
            {
                final String var = (int) (100 * this.randomFactor) + "%";
                subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.chance", var);
            }
            if (this.move != null && !this.move.isEmpty()) subEvo = subEvo + "\n" + I18n.format(
                    "pokemob.description.evolve.move", MovesUtils.getMoveName(this.move).getUnformattedComponentText());
            if (this.matcher != null)
            {
                this.matcher.reset();
                this.matcher.parse();
                final List<String> biomeNames = Lists.newArrayList();
                for (final BiomeType t : this.matcher.validSubBiomes)
                    biomeNames.add(t.readableName);
                for (final Biome test : SpawnBiomeMatcher.getAllBiomes())
                {
                    final boolean valid = this.matcher.validBiomes.contains(test.getRegistryName());
                    if (valid) biomeNames.add(I18n.format(test.getTranslationKey()));
                }
                for (final SpawnBiomeMatcher matcher : this.matcher.children)
                {
                    for (final BiomeType t : matcher.validSubBiomes)
                        biomeNames.add(t.readableName);
                    for (final Biome test : SpawnBiomeMatcher.getAllBiomes())
                    {
                        final boolean valid = matcher.validBiomes.contains(test.getRegistryName());
                        if (valid) biomeNames.add(I18n.format(test.getTranslationKey()));
                    }
                }
                subEvo = subEvo + "\n" + I18n.format("pokemob.description.evolve.locations", biomeNames);
            }
            return subEvo;
        }

        public boolean isInBiome(final IPokemob mob)
        {
            if (this.matcher != null)
            {
                final SpawnCheck check = new SpawnCheck(Vector3.getNewVector().set(mob.getEntity()), mob.getEntity()
                        .getEntityWorld());
                return this.matcher.matches(check);
            }
            return true;
        }

        private void parse(final Evolution data)
        {
            this.preset = null;
            if (data.level != null) this.level = data.level;
            if (data.location != null) this.matcher = new SpawnBiomeMatcher(data.location);
            if (data.animation != null) this.FX = data.animation;
            if (data.item != null) this.item = Tools.getStack(data.item.values);
            if (data.item_preset != null) this.preset = PokecubeItems.toPokecubeResource(data.item_preset);
            if (data.time != null)
            {
                if (data.time.equalsIgnoreCase("day")) this.dayOnly = true;
                if (data.time.equalsIgnoreCase("night")) this.nightOnly = true;
                if (data.time.equalsIgnoreCase("dusk")) this.duskOnly = true;
                if (data.time.equalsIgnoreCase("dawn")) this.dawnOnly = true;
            }
            if (data.trade != null) this.traded = data.trade;
            if (data.rain != null) this.rainOnly = data.rain;
            if (data.happy != null) this.happy = data.happy;
            if (data.sexe != null)
            {
                if (data.sexe.equalsIgnoreCase("male")) this.gender = 1;
                if (data.sexe.equalsIgnoreCase("female")) this.gender = 2;
            }
            if (data.move != null) this.move = data.move;
            if (data.chance != null) this.randomFactor = data.chance;
            if (this.level == -1) this.level = 0;
            if (!this.item.isEmpty()) PokecubeItems.addToEvos(this.item);
        }

        protected void postInit()
        {
            try
            {
                if (this.data != null) this.parse(this.data);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing " + this, e);
            }
        }

        public boolean shouldEvolve(final IPokemob mob)
        {
            return this.shouldEvolve(mob, mob.getHeldItem());
        }

        public boolean shouldEvolve(final IPokemob mob, final ItemStack mobs)
        {
            if (this.level < 0) return false;
            boolean ret = mob.traded() == this.traded || !this.traded;
            final Random rand = new Random(mob.getRNGValue());
            if (rand.nextFloat() > this.randomFactor) return false;
            if (this.rainOnly)
            {
                final World world = mob.getEntity().getEntityWorld();
                boolean rain = world.isRaining();
                if (!rain)
                {
                    final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
                    final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
                    if (teffect != null) rain = teffect.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] > 0;
                }
                if (!rain) return false;
            }
            boolean correctItem = true;
            if (this.preset != null || !this.item.isEmpty())
            {
                correctItem = false;
                if (!mobs.isEmpty()) if (this.preset != null) correctItem = PokecubeItems.is(this.preset, mobs
                        .getItem());
                else correctItem = Tools.isSameStack(mobs, this.item, true);
            }
            if (PokecubeItems.is(ICanEvolve.EVERSTONE, mob.getHeldItem())) return false;
            if (PokecubeItems.is(ICanEvolve.EVERSTONE, mobs)) return false;
            ret = ret && correctItem;
            final boolean correctLevel = mob.getLevel() >= this.level;
            ret = ret && correctLevel;
            boolean rightGender = this.gender == 0;
            if (!rightGender) rightGender = mob.getSexe() == this.gender;
            ret = ret && rightGender;
            boolean rightMove = this.move.equals("");
            if (!rightMove)
            {
                final String[] moves = mob.getMoves();
                for (final String s : moves)
                    if (s != null) if (s.equalsIgnoreCase(this.move))
                    {
                        rightMove = true;
                        break;
                    }
            }
            ret = ret && rightMove;
            boolean rightTime = !this.dayOnly && !this.nightOnly && !this.dawnOnly && !this.duskOnly;
            if (!rightTime)
            {
                // TODO better way to choose current time.
                final double time = mob.getEntity().getEntityWorld().getDayTime() % 24000 / 24000d;
                rightTime = this.dayOnly ? PokedexEntry.day.contains(time)
                        : this.nightOnly ? PokedexEntry.night.contains(time)
                                : this.duskOnly ? PokedexEntry.dusk.contains(time) : PokedexEntry.dawn.contains(time);
            }
            ret = ret && rightTime;
            if (this.happy) ret = ret && mob.getHappiness() >= 220;
            if (ret && this.matcher != null) ret = ret && this.isInBiome(mob);
            return ret;
        }
    }

    public static class InteractionLogic
    {
        public static class Interaction
        {
            public final ItemStack  key;
            public PokedexEntry     forme;
            public List<ItemStack>  stacks   = Lists.newArrayList();
            public ResourceLocation lootTable;
            public boolean          male     = true;
            public boolean          female   = true;
            public int              cooldown = 100;
            public int              variance = 1;
            public int              hunger   = 100;

            public Interaction(final ItemStack key)
            {
                this.key = key;
            }
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
            fire.key = new Key();
            fire.action = new Action();
            fire.key.values.put(new QName("id"), "minecraft:stick");
            fire.action.values.put(new QName("type"), "item");
            final Drop firedrop = new Drop();
            firedrop.values.put(new QName("id"), "minecraft:torch");
            fire.action.drops.add(firedrop);

            final Interact water = new Interact();
            water.key = new Key();
            water.action = new Action();
            water.key.values.put(new QName("id"), "minecraft:bucket");
            water.action.values.put(new QName("type"), "item");
            final Drop waterdrop = new Drop();
            waterdrop.values.put(new QName("id"), "minecraft:water_bucket");
            water.action.drops.add(waterdrop);

            if (PokecubeCore.getConfig().defaultInteractions)
            {
                InteractionLogic.defaults.put(PokeType.getType("fire"), Lists.newArrayList(fire));
                InteractionLogic.defaults.put(PokeType.getType("water"), Lists.newArrayList(water));
            }
        }

        protected static void initForEntry(final PokedexEntry entry)
        {
            final List<Interact> val = Lists.newArrayList();
            for (final PokeType t : InteractionLogic.defaults.keySet())
                if (entry.isType(t)) val.addAll(InteractionLogic.defaults.get(t));
            if (!val.isEmpty()) InteractionLogic.initForEntry(entry, val);
        }

        protected static void initForEntry(final PokedexEntry entry, final List<Interact> data)
        {
            if (data == null || data.isEmpty())
            {
                InteractionLogic.initForEntry(entry);
                return;
            }
            for (final Interact interact : data)
            {
                InteractionLogic.cleanInteract(interact);
                final Key key = interact.key;
                final Action action = interact.action;
                final boolean isForme = action.values.get(new QName("type")).equals("forme");
                Map<QName, String> values = key.values;
                if (key.tag != null)
                {
                    final QName name = new QName("tag");
                    values.put(name, key.tag);
                }
                final ItemStack keyStack = Tools.getStack(values);
                final Interaction interaction = new Interaction(keyStack);
                interaction.male = interact.male;
                interaction.female = interact.female;
                interaction.cooldown = interact.cooldown;
                interaction.variance = Math.max(1, interact.variance);
                interaction.hunger = interact.baseHunger;
                entry.interactionLogic.actions.put(keyStack, interaction);
                if (isForme)
                {
                    final PokedexEntry forme = Database.getEntry(action.values.get(new QName("forme")));
                    if (forme != null) interaction.forme = forme;
                }
                else
                {
                    final List<ItemStack> stacks = Lists.newArrayList();
                    for (final Drop d : action.drops)
                    {
                        values = d.values;
                        if (d.tag != null)
                        {
                            final QName name = new QName("tag");
                            values.put(name, d.tag);
                        }
                        final ItemStack stack = Tools.getStack(values);
                        if (stack != ItemStack.EMPTY) stacks.add(stack);
                    }
                    interaction.stacks = stacks;
                    if (action.lootTable != null) interaction.lootTable = new ResourceLocation(action.lootTable);
                }
                DispenseBehaviourInteract.registerBehavior(keyStack);
            }
        }

        public HashMap<ItemStack, Interaction> actions = Maps.newHashMap();

        boolean canInteract(final ItemStack key)
        {
            return this.getStackKey(key) != ItemStack.EMPTY;
        }

        private ItemStack getFormeKey(final ItemStack held)
        {
            if (held != null) for (final ItemStack stack : this.actions.keySet())
                if (Tools.isSameStack(stack, held) && this.actions.get(stack).forme != null) return stack;
            return ItemStack.EMPTY;
        }

        public ItemStack getKey(final ItemStack held)
        {
            if (held != null) for (final ItemStack stack : this.actions.keySet())
                if (Tools.isSameStack(stack, held)) return stack;
            return ItemStack.EMPTY;
        }

        private ItemStack getStackKey(final ItemStack held)
        {
            if (held != null) for (final ItemStack stack : this.actions.keySet())
            {
                Interaction action = null;
                if (Tools.isSameStack(stack, held) && (!(action = this.actions.get(stack)).stacks.isEmpty()
                        || action.lootTable != null)) return stack;
            }
            return ItemStack.EMPTY;
        }

        List<ItemStack> interact(final ItemStack key)
        {
            return this.actions.get(this.getStackKey(key)).stacks;
        }

        boolean interact(final PlayerEntity player, final IPokemob pokemob, final boolean doInteract)
        {
            final MobEntity entity = pokemob.getEntity();
            final ItemStack held = player.getHeldItemMainhand();
            ItemStack stack = this.getStackKey(held);
            if (stack.isEmpty())
            {
                stack = this.getFormeKey(held);
                if (stack.isEmpty()) return false;
                if (!doInteract) return true;
                final Interaction action = this.actions.get(stack);
                final PokedexEntry forme = action.forme;
                pokemob.megaEvolve(forme);
                return true;
            }
            final CompoundNBT data = entity.getPersistentData();
            final Interaction action = this.actions.get(stack);
            if (data.contains("lastInteract"))
            {
                final long time = data.getLong("lastInteract");
                final long diff = entity.getEntityWorld().getGameTime() - time;
                if (diff < action.cooldown + new Random(time).nextInt(action.variance)) return false;
            }
            if (!action.male && pokemob.getSexe() == IPokemob.MALE) return false;
            if (!action.female && pokemob.getSexe() == IPokemob.FEMALE) return false;
            if (action.stacks.isEmpty() && action.lootTable == null) return false;
            if (!doInteract) return true;
            ItemStack result = null;
            if (action.lootTable != null)
            {
                final LootTable loottable = pokemob.getEntity().getEntityWorld().getServer().getLootTableManager()
                        .getLootTableFromLocation(action.lootTable);
                final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld) pokemob
                        .getEntity().getEntityWorld()).withParameter(LootParameters.THIS_ENTITY, pokemob.getEntity());
                for (final ItemStack itemstack : loottable.generate(lootcontext$builder.build(loottable
                        .getParameterSet())))
                    if (!itemstack.isEmpty())
                    {
                        result = itemstack;
                        break;
                    }
            }
            else
            {

                final List<ItemStack> results = action.stacks;
                final int index = player.getRNG().nextInt(results.size());
                result = results.get(index).copy();
            }
            if (result.isEmpty()) return false;
            data.putLong("lastInteract", entity.getEntityWorld().getGameTime());
            final int time = pokemob.getHungerTime();
            pokemob.setHungerTime(time + action.hunger);
            held.shrink(1);
            if (held.isEmpty()) player.inventory.setInventorySlotContents(player.inventory.currentItem, result);
            else if (!player.inventory.addItemStackToInventory(result)) player.dropItem(result, false);
            if (player != pokemob.getOwner()) entity.setAttackTarget(player);
            return true;
        }
    }

    public static interface MegaRule
    {
        boolean shouldMegaEvolve(IPokemob mobIn, PokedexEntry entryTo);
    }

    public static enum MovementType
    {
        FLYING, FLOATING, WATER, NORMAL;

        public static MovementType getType(final String type)
        {
            for (final MovementType t : MovementType.values())
                if (t.toString().equalsIgnoreCase(type)) return t;
            return NORMAL;
        }
    }

    public static class SpawnData
    {
        public static class SpawnEntry
        {
            int      max      = 4;
            int      min      = 2;
            float    rate     = 0.0f;
            int      level    = -1;
            Variance variance = null;
        }

        final PokedexEntry entry;

        public Map<SpawnBiomeMatcher, SpawnEntry> matchers = Maps.newHashMap();

        public SpawnData(final PokedexEntry entry)
        {
            this.entry = entry;
        }

        public int getLevel(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? -1 : entry.level;
        }

        public SpawnBiomeMatcher getMatcher(final SpawnCheck checker)
        {
            return this.getMatcher(checker, true);
        }

        public SpawnBiomeMatcher getMatcher(final SpawnCheck checker, final boolean forSpawn)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
            {
                final SpawnEvent.Check evt = new SpawnEvent.Check(this.entry, checker.location, checker.world,
                        forSpawn);
                PokecubeCore.POKEMOB_BUS.post(evt);
                if (evt.isCanceled()) continue;
                if (evt.getResult() == Result.ALLOW) return matcher;
                if (matcher.matches(checker)) return matcher;
            }
            return null;
        }

        public SpawnBiomeMatcher getMatcher(final World world, final Vector3 location)
        {
            final SpawnCheck checker = new SpawnCheck(location, world);
            return this.getMatcher(checker);
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
            final Variance variance = entry == null ? new Variance() : entry.variance;
            return variance;
        }

        public float getWeight(final SpawnBiomeMatcher matcher)
        {
            final SpawnEntry entry = this.matchers.get(matcher);
            return entry == null ? 0 : entry.rate;
        }

        public boolean isValid(final Biome biome)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
                if (matcher.validBiomes.contains(biome.getRegistryName())) return true;
            return false;
        }

        public boolean isValid(final BiomeType biome)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
                if (matcher.validSubBiomes.contains(biome)) return true;
            return false;
        }

        public boolean isValid(final SpawnCheck checker)
        {
            return this.getMatcher(checker) != null;
        }

        /**
         * Only checks one biome type for vailidity
         *
         * @param b
         * @return
         */
        public boolean isValid(final World world, final Vector3 location)
        {
            return this.getMatcher(world, location) != null;
        }

        public void postInit()
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
            {
                matcher.reset();
                matcher.parse();
            }
        }
    }

    public static final String TEXTUREPATH = "entity/textures/";

    public static TimePeriod dawn  = new TimePeriod(0.85, 0.05);
    public static TimePeriod day   = new TimePeriod(0.0, 0.5);
    public static TimePeriod dusk  = new TimePeriod(0.45, 0.65);
    public static TimePeriod night = new TimePeriod(0.6, 0.9);

    private static final PokedexEntry BLANK = new PokedexEntry(true);

    public static final ResourceLocation MODELNO = new ResourceLocation(PokecubeCore.MODID,
            "entity/models/missingno.x3d");

    public static final ResourceLocation TEXNO  = new ResourceLocation(PokecubeCore.MODID,
            "entity/textures/missingno.png");
    public static final ResourceLocation ANIMNO = new ResourceLocation(PokecubeCore.MODID,
            "entity/animations/missingno.xml");

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

    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String> abilities       = Lists.newArrayList();
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String> abilitiesHidden = Lists.newArrayList();
    /** Times not included here the pokemob will go to sleep when idle. */
    @CopyToGender
    protected List<TimePeriod>  activeTimes     = new ArrayList<>();

    /**
     * if True, this is considered the "main" form for the type, this is what
     * is returned from any number based lookups.
     */
    public boolean                              base            = false;
    /**
     * If True, this form won't be registered, this is used for mobs with a
     * single base template form, and then a bunch of alternate ones for things
     * to be copied from.
     */
    public boolean                              dummy           = false;
    /**
     * If the forme is supposed to have a custom sound, rather than using base,
     * it will be set to this.
     */
    protected String                            customSound     = null;
    @CopyToGender
    private PokedexEntry                        baseForme       = null;
    /** Initial Happiness of the pokemob */
    @CopyToGender
    protected int                               baseHappiness;
    @CopyToGender
    protected String                            baseName;
    /** base xp given from defeating */
    @CopyToGender
    protected int                               baseXP          = -1;
    @CopyToGender
    public boolean                              breeds          = true;
    @CopyToGender
    public boolean                              canSitShoulder  = false;
    @CopyToGender
    protected int                               catchRate       = -1;
    @CopyToGender
    private PokedexEntry                        childNb         = null;
    /** A map of father pokedexnb : child pokedexNbs */
    @CopyToGender
    protected Map<PokedexEntry, PokedexEntry[]> childNumbers    = Maps.newHashMap();
    /** Will the pokemob try to build colonies with others of it's kind */
    @CopyToGender
    public boolean                              colonyBuilder   = false;
    /**
     * Default value of specialInfo, used to determine default colour of
     * recolourable parts
     */
    @CopyToGender
    public int                                  defaultSpecial  = 0;
    /**
     * Default value of specialInfo for shiny variants, used to determine
     * default colour of recolourable parts
     */
    @CopyToGender
    public int                                  defaultSpecials = 0;
    /**
     * If the IPokemob supports this, then this will be the loot table used for
     * its drops.
     */
    @CopyToGender
    public ResourceLocation                     lootTable       = null;
    /**
     * indicatees of the specified special texture exists. Index 4 is used for
     * if the mob can be dyed
     */
    @CopyToGender
    public boolean                              dyeable         = false;
    /** A Set of valid dye colours, if empty, any dye is valid. */
    @CopyToGender
    public Set<DyeColor>                        validDyes       = Sets.newHashSet();
    @CopyToGender
    SoundEvent                                  event;

    @CopyToGender
    public SoundEvent          replacedEvent;
    /** The relation between xp and level */
    @CopyToGender
    protected int              evolutionMode = 1;
    /** The list of pokemon this can evolve into */
    @CopyToGender
    public List<EvolutionData> evolutions    = new ArrayList<>();
    @CopyToGender
    public EvolutionData       evolvesBy     = null;
    /** Who this pokemon evolves from. */
    @CopyToGender
    public PokedexEntry        evolvesFrom   = null;
    @CopyToGender
    public byte[]              evs;
    protected PokedexEntry     female        = null;
    /** Inital list of species which are prey */
    @CopyToGender
    protected String[]         food;

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
    public boolean[]                           foods         = { false, false, false, false, false, true, false };
    @CopyToGender
    protected HashMap<ItemStack, PokedexEntry> formeItems    = Maps.newHashMap();
    /** Map of forms assosciated with this one. */
    @CopyToGender
    protected Map<String, PokedexEntry>        forms         = new HashMap<>();
    /**
     * Used to stop gender formes from spawning, spawning rate is done by
     * gender ratio of base forme instead.
     */
    public boolean                             isGenderForme = false;

    /** Can it megaevolve */
    @CopyToGender
    public boolean          hasMegaForm      = false;
    @CopyToGender
    public boolean          hasShiny         = true;
    /** Materials which will hurt or make it despawn. */
    @CopyToGender
    public String[]         hatedMaterial;
    @CopyToGender
    public float            height           = -1;
    @CopyToGender
    public boolean          isMega           = false;
    @CopyToGender
    public boolean          ridable          = true;
    /**
     * This is a loot table to be used for held item. if this isn't null, the
     * above held is ignored.
     */
    @CopyToGender
    public ResourceLocation heldTable        = null;
    /** Interactions with items from when player right clicks. */
    @CopyToGender
    public InteractionLogic interactionLogic = new InteractionLogic();

    protected boolean isFemaleForme = false;

    protected boolean isMaleForme = false;

    @CopyToGender
    public boolean isShadowForme = false;

    /** Will it protect others. */
    @CopyToGender
    public boolean isSocial = true;

    public boolean isStarter    = false;
    @CopyToGender
    public boolean isStationary = false;
    @CopyToGender
    public boolean legendary    = false;

    @CopyToGender
    public float length = -1;

    /** Map of Level to Moves learned. */
    @CopyToGender
    private Map<Integer, ArrayList<String>> lvlUpMoves;
    /** The abilities available to the pokedex entry. */
    @CopyToGender
    protected ArrayList<String>             evolutionMoves = Lists.newArrayList();

    protected PokedexEntry male = null;

    /** Mass of the pokemon in kg. */
    @CopyToGender
    public double                             mass      = -1;
    @CopyToGender
    protected HashMap<PokedexEntry, MegaRule> megaRules = Maps.newHashMap();

    /** Movement type for this mob */
    @CopyToGender
    protected MovementType mobType          = null;
    /** Mod which owns the pokemob, used for texture location. */
    @CopyToGender
    private String         modId;
    protected String       name;
    /** Particle Effects. */
    @CopyToGender
    public String[]        particleData;
    /** Offset between top of hitbox and where player sits */
    @CopyToGender
    public double[][]      passengerOffsets = { { 0, 1, 0 } };
    @CopyToGender
    protected int          pokedexNb;

    /** All possible moves */
    @CopyToGender
    private List<String> possibleMoves;

    /** If the above is floating, how high does it try to float */
    @CopyToGender
    public double                    preferedHeight = 1.5;
    /** Pokemobs with these entries will be hunted. */
    @CopyToGender
    private final List<PokedexEntry> prey           = new ArrayList<>();

    /**
     * This list will contain all pokemon that are somehow related to this one
     * via evolution chains
     */
    @CopyToGender
    private final List<PokedexEntry> related = new ArrayList<>();

    @CopyToGender
    protected int sexeRatio = -1;

    @CopyToGender
    public PokedexEntry shadowForme = null;

    @CopyToGender
    public boolean shouldDive = false;

    @CopyToGender
    public boolean shouldFly = false;

    @CopyToGender
    public boolean             shouldSurf = false;
    @CopyToGender
    protected ResourceLocation sound;

    @CopyToGender
    /**
     * This is copied to the gender as it will allow specifying where that
     * gender spawns in pokedex.
     */
    private SpawnData spawns;
    /** Used to determine egg group */
    @CopyToGender
    public String[]   species = {};

    @CopyToGender
    protected int[]   stats;
    /**
     * Array used for animated or gender based textures. Index 0 is the male
     * textures, index 1 is the females
     */
    @CopyToGender
    public String[][] textureDetails = { { "" }, null };

    @CopyToGender
    public String texturePath = PokedexEntry.TEXTUREPATH;

    @CopyToGender
    protected PokeType type1;

    @CopyToGender
    protected PokeType type2;

    @CopyToGender
    public float width = -1;

    @CopyToGender
    protected EntityType<?> entity_type;

    // This is the actual size of the model, if not null, will be used for
    // scaling of rendering in guis, order is length, height, width
    public Vector3f modelSize = null;

    /** Cached trimmed name. */
    private String trimmedName;

    private ITextComponent description;

    public ResourceLocation model = PokedexEntry.MODELNO;

    public ResourceLocation texture = PokedexEntry.TEXNO;

    public ResourceLocation animation = PokedexEntry.ANIMNO;

    /**
     * This constructor is used for making blank entry for copy comparisons.
     *
     * @param blank
     */
    private PokedexEntry(final boolean blank)
    {
        // Nothing
    }

    public PokedexEntry(final int nb, final String name)
    {
        this.name = name;
        this.pokedexNb = nb;
        if (Database.getEntry(name) == null) Database.allFormes.add(this);
        else new NullPointerException("Trying to add another " + name + " " + Database.getEntry(name))
                .printStackTrace();
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

    protected void addEVXP(final byte[] evs, final int baseXP, final int evolutionMode, final int sexRatio)
    {
        this.evs = evs;
        this.baseXP = baseXP;
        this.evolutionMode = evolutionMode;
        this.sexeRatio = sexRatio;
    }

    protected void addForm(final PokedexEntry form)
    {
        if (this.forms.containsValue(form)) return;
        final String key = form.getTrimmedName();
        form.baseName = this.getTrimmedName();
        form.setBaseForme(this);
        this.forms.put(key, form);
    }

    public void addMove(final String move)
    {
        for (final String s : this.possibleMoves)
            if (s.equals(move)) return;
        this.possibleMoves.add(move);
    }

    protected void addMoves(final List<String> moves, final Map<Integer, ArrayList<String>> lvlUpMoves2)
    {
        this.lvlUpMoves = lvlUpMoves2;
        this.possibleMoves = moves;
        // System.out.println("Adding moves for "+name);
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

    public boolean canEvolve(final int level)
    {
        return this.canEvolve(level, ItemStack.EMPTY);
    }

    public boolean canEvolve(final int level, final ItemStack stack)
    {
        for (final EvolutionData d : this.evolutions)
        {

            boolean itemCheck = d.item == ItemStack.EMPTY;
            if (!itemCheck && stack != ItemStack.EMPTY) itemCheck = stack.isItemEqual(d.item);
            if (d.level >= 0 && level >= d.level && itemCheck) return true;
        }

        return false;
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
        if (e.baseForme != null && e.baseForme != this) throw new IllegalArgumentException(
                "Cannot add a second base form");
        e.pokedexNb = this.pokedexNb;

        if (e.possibleMoves == null) e.possibleMoves = this.possibleMoves;
        if (e.lvlUpMoves == null) e.lvlUpMoves = this.lvlUpMoves;
        if (e.stats == null) e.stats = this.stats.clone();
        if (this.evs == null) PokecubeCore.LOGGER.error(this + " " + this.baseForme, new IllegalArgumentException());
        if (e.evs == null) e.evs = this.evs.clone();
        if (e.height == -1) e.height = this.height;
        if (e.width == -1) e.width = this.width;
        if (e.length == -1) e.length = this.length;
        if (e.childNumbers.isEmpty()) e.childNumbers = this.childNumbers;
        if (e.species == null) e.species = this.species;
        if (e.mobType == null) e.mobType = this.mobType;
        if (e.catchRate == -1) e.catchRate = this.catchRate;
        if (e.sexeRatio == -1) e.sexeRatio = this.sexeRatio;
        if (e.mass == -1) e.mass = this.mass;
        for (int i = 0; i < this.foods.length; i++)
            e.foods[i] = this.foods[i];
        e.breeds = this.breeds;
        e.legendary = this.legendary;
        e.setBaseForme(this);
        this.addForm(e);
    }

    protected void copyToGenderFormes()
    {
        if (this.male != null) this.copyFieldsToGenderForm(this.male);
        if (this.female != null) this.copyFieldsToGenderForm(this.female);
    }

    public PokedexEntry createGenderForme(final byte gender, String name)
    {
        if (name == null)
        {
            name = this.name;
            String suffix = "";
            if (gender == IPokemob.MALE) suffix = " Male";
            else suffix = " Female";
            name = name + suffix;
        }
        PokedexEntry forme = Database.getEntry(name);
        if (forme == null) forme = new PokedexEntry(this.pokedexNb, name);
        forme.setBaseForme(this);
        if (gender == IPokemob.MALE)
        {
            forme.isMaleForme = true;
            this.male = forme;
            forme.sexeRatio = 0;
        }
        else
        {
            forme.isFemaleForme = true;
            this.female = forme;
            forme.sexeRatio = 254;
        }
        forme.isGenderForme = true;
        return forme;
    }

    public boolean floats()
    {
        return this.mobType == MovementType.FLOATING;
    }

    public boolean flys()
    {
        return this.mobType == MovementType.FLYING;
    }

    public Ability getAbility(final int number, final IPokemob pokemob)
    {
        if (number < this.abilities.size()) return AbilityManager.getAbility(this.abilities.get(number));
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
            if (this.getBaseForme() != null && this.getBaseForme() != this) this.baseName = this.getBaseForme()
                    .getTrimmedName();
            else this.baseName = this.getTrimmedName();
            if (this.getBaseForme() == this) PokecubeCore.LOGGER.error("Error with " + this);
        }
        return this.baseName;
    }

    /** @return the baseXP */
    public int getBaseXP()
    {
        if (this.baseXP == -1) this.baseXP = this.getBaseForme() != null && this.getBaseForme() != this ? this
                .getBaseForme().getBaseXP() : 0;
        return this.baseXP;
    }

    /** @return the catchRate */
    public int getCatchRate()
    {
        return this.catchRate;
    }

    public PokedexEntry getChild()
    {
        if (this.childNb == null)
        {
            for (final PokedexEntry e : this.getRelated())
                for (final EvolutionData d : e.evolutions)
                    if (d.evolution == this) this.childNb = e.getChild();
            if (this.childNb == null) this.childNb = this;
        }

        return this.childNb;
    }

    public PokedexEntry getChild(final PokedexEntry fatherNb)
    {
        if (this.childNumbers.containsKey(fatherNb))
        {
            final PokedexEntry[] nums = this.childNumbers.get(fatherNb);
            final int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        else if (this.childNumbers.containsKey(Database.missingno))
        {
            final PokedexEntry[] nums = this.childNumbers.get(Database.missingno);
            final int index = new Random().nextInt(nums.length);
            return nums[index];
        }
        return this.getChild();
    }

    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDescription()
    {
        if (this.description == null)
        {
            final PokedexEntry entry = this;
            String typeString = PokeType.getTranslatedName(entry.getType1());
            if (entry.getType2() != PokeType.unknown) typeString += "/" + PokeType.getTranslatedName(entry.getType2());
            final String typeDesc = I18n.format("pokemob.description.type", entry.getTranslatedName(), typeString);
            String evoString = null;
            if (entry.canEvolve()) for (final EvolutionData d : entry.evolutions)
            {
                if (d.evolution == null) continue;
                if (evoString == null) evoString = d.getEvoString();
                else evoString = evoString + "\n" + d.getEvoString();
                evoString = evoString + "\n";
            }
            String descString = typeDesc;
            if (evoString != null) descString = descString + "\n" + evoString;
            if (entry.evolvesFrom != null) descString = descString + "\n" + I18n.format(
                    "pokemob.description.evolve.from", entry.getTranslatedName(), entry.evolvesFrom
                            .getTranslatedName());
            this.description = new StringTextComponent(descString);
        }
        return this.description;
    }

    public EntityType<?> getEntityType()
    {
        return this.entity_type;
    }

    public PokedexEntry getEvo(final IPokemob pokemob)
    {
        for (final Entry<PokedexEntry, MegaRule> e : this.megaRules.entrySet())
        {
            final MegaRule rule = e.getValue();
            final PokedexEntry entry = e.getKey();
            if (rule.shouldMegaEvolve(pokemob, entry)) return entry;
        }
        return null;
    }

    /** @return the evolutionMode */
    public int getEvolutionMode()
    {
        if (this.getBaseForme() != null) return this.getBaseForme().evolutionMode;
        return this.evolutionMode;
    }

    /** Moves to be learned right after evolution. */
    public List<String> getEvolutionMoves()
    {
        return this.evolutionMoves;
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
        if (!this.base && this.isGenderForme && this.getBaseForme() != null) return this.getBaseForme().getForGender(
                gender);
        if (this.male == null) this.male = this;
        if (this.female == null) this.female = this;
        return gender == IPokemob.MALE ? this.male : this.female;
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
        if (this.pokedexNb < 891) return 8;
        return 0;
    }

    public int getHappiness()
    {
        return this.baseHappiness;
    }

    public Ability getHiddenAbility(final IPokemob pokemob)
    {
        if (this.abilitiesHidden.isEmpty()) return this.getAbility(0, pokemob);
        else if (this.abilitiesHidden.size() == 1) return AbilityManager.getAbility(this.abilitiesHidden.get(0));
        else if (this.abilitiesHidden.size() == 2) return pokemob.getSexe() == IPokemob.MALE ? AbilityManager
                .getAbility(this.abilitiesHidden.get(0)) : AbilityManager.getAbility(this.abilitiesHidden.get(1));
        return null;

    }

    /**
     * returns whether the interaction logic has a response listed for the
     * given key.
     *
     * @param pokemob
     * @return the stack that maps to this key
     */
    public List<ItemStack> getInteractResult(final ItemStack stack)
    {
        return this.interactionLogic.interact(stack);
    }

    public Vector3f getModelSize()
    {
        if (this.modelSize == null) this.modelSize = new Vector3f(this.length, this.height, this.width);
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
            for (final String s : this.lvlUpMoves.get(i))
                ret.add(s);
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
            for (final String s : this.lvlUpMoves.get(i + 1))
                ret.add(s);
        }

        return ret;
    }

    public String getName()
    {
        return this.name;
    }

    /** @return the pokedexNb */
    public int getPokedexNb()
    {
        return this.pokedexNb;
    }

    public ItemStack getRandomHeldItem(final MobEntity mob)
    {
        if (mob.getEntityWorld().isRemote) return ItemStack.EMPTY;
        if (this.heldTable != null) // TODO fix parameters for the loot table.
            try
            {
            final LootTable loottable = mob.getEntityWorld().getServer().getLootTableManager().getLootTableFromLocation(this.heldTable);
            final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld) mob.getEntityWorld()).withRandom(mob.getRNG()).withParameter(LootParameters.THIS_ENTITY, mob).withParameter(LootParameters.POSITION, new BlockPos(mob)).withParameter(LootParameters.KILLER_ENTITY, null).withParameter(LootParameters.TOOL, null).withParameter(LootParameters.DIRECT_KILLER_ENTITY, null).withParameter(LootParameters.LAST_DAMAGE_PLAYER, null).withParameter(LootParameters.BLOCK_ENTITY, null).withParameter(LootParameters.DAMAGE_SOURCE, null).withParameter(LootParameters.EXPLOSION_RADIUS, 0f);
            for (final ItemStack itemstack : loottable.generate(lootcontext$builder.build(loottable.getParameterSet())))
            if (!itemstack.isEmpty()) return itemstack;
            }
            catch (final Exception e)
            {
            PokecubeCore.LOGGER.error("Error loading table: " + this.heldTable, e);
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
        if (this.event == null) if (this.getBaseForme() != null && this.getBaseForme() != this)
        {
            this.event = this.getBaseForme().getSoundEvent();
            this.sound = this.getBaseForme().sound;
        }
        return this.event;
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
        return this.stats.clone();
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
        if (!ret.contains(this.texturePath)) ret = this.texturePath + ret;
        ret = ret.toLowerCase(Locale.ENGLISH);
        return ret;
    }

    @OnlyIn(Dist.CLIENT)
    public String getTranslatedName()
    {
        return I18n.format(this.getUnlocalizedName());
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
        if (this.isFemaleForme || this.isMaleForme) name = this.getBaseName();
        final String translated = "entity.pokecube." + name;
        return translated;
    }

    public boolean hasForm(final String form)
    {
        return this.forms.containsKey(Database.trim(form));
    }

    public boolean hasPrey()
    {
        return this.prey.size() > 0;
    }

    protected void initPrey()
    {
        if (this.food == null) return;
        final List<String> foodList = new ArrayList<>();
        for (final String s : this.food)
            foodList.add(s);
        poke:
        for (final PokedexEntry e : Database.data.values())
            if (e.species != null) for (final String s : e.species)
                if (foodList.contains(s))
                {
                    this.prey.add(e);
                    continue poke;
                }
    }

    protected void initRelations()
    {
        final List<EvolutionData> stale = Lists.newArrayList();
        for (final EvolutionData d : this.evolutions)
            if (!Pokedex.getInstance().isRegistered(d.evolution)) stale.add(d);
        this.evolutions.removeAll(stale);
        if (!stale.isEmpty()) System.out.println(stale.size() + " stales for " + this);
        this.addRelation(this);
        for (final EvolutionData d : this.evolutions)
        {
            d.postInit();

            final PokedexEntry temp = d.evolution;

            if (temp == null) continue;
            temp.evolvesFrom = this;
            temp.evolvesBy = d;
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
        for (final PokedexEntry e : Pokedex.getInstance().getRegisteredEntries())
            if (e != null && e.species != null && this.species != null) for (final String s : this.species)
                for (final String s1 : e.species)
                    if (s.equals(s1)) this.addRelation(e);

        final Object[] temp = this.getRelated().toArray();
        final Double[] nums = new Double[temp.length];
        for (int i = 0; i < nums.length; i++)
            nums[i] = (double) ((PokedexEntry) temp[i]).getPokedexNb();
        new Cruncher().sort(nums, temp);
        this.getRelated().clear();
        for (final Object o : temp)
            this.getRelated().add((PokedexEntry) o);
    }

    /**
     * returns whether the interaction logic has a response listed for the
     * given key.
     *
     * @param pokemob
     * @param doInteract
     *            - if false, will not actually do anything.
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
     * @param doInteract
     *            - if false, will not actually do anything.
     * @return
     */
    public boolean interact(final PlayerEntity player, final IPokemob pokemob, final boolean doInteract)
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
        PokedexEntry newForme = null;
        if (this.formeItems.isEmpty() && this.getBaseForme() != null) for (final PokedexEntry entry : this
                .getBaseForme().formeItems.values())
            if (entry == this)
            {
                this.getBaseForme().onHeldItemChange(oldStack, newStack, pokemob);
                return;
            }
        for (final ItemStack key : this.formeItems.keySet())
            if (Tools.isSameStack(oldStack, key, true))
            {
                newForme = this;
                break;
            }
        for (final ItemStack key : this.formeItems.keySet())
            if (Tools.isSameStack(newStack, key, true))
            {
                newForme = this.formeItems.get(key);
                break;
            }
        if (newForme != null && newForme != pokemob.getPokedexEntry()) ICanEvolve.setDelayedMegaEvolve(pokemob,
                newForme, null);
    }

    public void setBaseForme(final PokedexEntry baseForme)
    {
        this.baseForme = baseForme;
    }

    public void setEntityType(final EntityType<?> type)
    {
        this.entity_type = type;
    }

    /**
     * Sets the Mod which declares this mob.
     *
     * @param modId
     *            the modId to set
     */
    public void setModId(final String modId)
    {
        if (this.modId != null && !this.modId.equals(modId)) PokecubeCore.LOGGER.debug("Modid changed to: " + modId
                + " for " + this + " from " + this.modId, new Exception());
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
        sound = sound.replaceAll("([\\W])", "");
        if (mobs) sound = "mobs." + sound;
        this.sound = new ResourceLocation(PokecubeCore.MODID, sound);
    }

    public void setSpawnData(final SpawnData data)
    {
        this.spawns = data;
    }

    public boolean shouldEvolve(final IPokemob mob)
    {
        for (final EvolutionData d : this.evolutions)
            if (d.shouldEvolve(mob)) return true;
        return false;
    }

    public boolean swims()
    {
        return this.mobType == MovementType.WATER;
    }

    public ResourceLocation texture()
    {
        return this.texture;
    }

    @Override
    public String toString()
    {
        final String ret = this.name;
        return ret;
    }

    public void updateMoves()
    {
        final List<String> moves = new ArrayList<>();

        if (this.possibleMoves == null) try
        {
            this.possibleMoves = this.getBaseForme().possibleMoves;
            this.possibleMoves.isEmpty();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(this.toString() + " no moves? " + this.getBaseForme());
        }
        if (this.lvlUpMoves == null) this.lvlUpMoves = this.getBaseForme().lvlUpMoves;

        for (final String s : this.possibleMoves)
            if (MovesUtils.isMoveImplemented(s) && !moves.contains(s)) moves.add(s);
        final List<String> staleEvoMoves = Lists.newArrayList();
        for (final String s : this.evolutionMoves)
        {
            final boolean implemented = MovesUtils.isMoveImplemented(s);
            if (implemented && !moves.contains(s)) moves.add(s);
            else if (!implemented) staleEvoMoves.add(s);
        }
        this.evolutionMoves.removeAll(staleEvoMoves);
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
        for (final int i : toRemove)
            this.lvlUpMoves.remove(i);
    }
}
