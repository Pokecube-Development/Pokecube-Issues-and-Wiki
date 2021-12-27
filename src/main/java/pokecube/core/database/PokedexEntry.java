package pokecube.core.database;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.pokedex.PokedexEntryLoader.Action;
import pokecube.core.database.pokedex.PokedexEntryLoader.BodyNode;
import pokecube.core.database.pokedex.PokedexEntryLoader.DefaultFormeHolder;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.database.pokedex.PokedexEntryLoader.Evolution;
import pokecube.core.database.pokedex.PokedexEntryLoader.Interact;
import pokecube.core.database.pokedex.PokedexEntryLoader.MegaEvoRule;
import pokecube.core.database.pokedex.PokedexEntryLoader.StatsNode.Stats;
import pokecube.core.database.pokedex.PokedexEntryLoader.XMLMegaRule;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.pokemobs.DispenseBehaviourInteract;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.SpawnContext;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
import thut.api.Tracker;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.common.ThutCore;

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

    public static class EvolutionData
    {
        public SpawnBiomeMatcher matcher = null;
        public Evolution data;
        public boolean dayOnly = false;
        public final PokedexEntry evolution;
        public String FX = "";
        // 1 for male, 2 for female, 0 for either;
        public byte gender = 0;
        public boolean happy = false;
        // the item it must be holding, if null, any item is fine, or no items
        // is fine
        public ItemStack item = ItemStack.EMPTY;
        public ResourceLocation preset = null;
        // does it need to grow a level for the item to work
        public boolean itemLevel = false;
        public int level = -1;
        public String move = "";
        public boolean nightOnly = false;
        public boolean dawnOnly = false;
        public boolean duskOnly = false;
        public PokedexEntry preEvolution;
        public boolean rainOnly = false;
        public float randomFactor = 1.0f;
        public boolean traded = false;

        public List<String> evoMoves = Lists.newArrayList();

        // This is if it needs a specific formeHolder to evolve into this.
        public ResourceLocation neededForme = null;

        public EvolutionData(final PokedexEntry evol)
        {
            this.evolution = evol;
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
            if (this.level > 0) comps.add(new TranslatableComponent("pokemob.description.evolve.level", this.level));
            if (this.traded) comps.add(new TranslatableComponent("pokemob.description.evolve.traded"));
            if (this.gender == 1) comps.add(new TranslatableComponent("pokemob.description.evolve.male"));
            if (this.gender == 2) comps.add(new TranslatableComponent("pokemob.description.evolve.female"));
            if (!this.item.isEmpty()) comps.add(
                    new TranslatableComponent("pokemob.description.evolve.item", this.item.getHoverName().getString()));
            else if (this.preset != null)
            {
                final ItemStack stack = PokecubeItems.getStack(this.preset);
                if (!stack.isEmpty()) comps.add(
                        new TranslatableComponent("pokemob.description.evolve.item", stack.getHoverName().getString()));
            }
            if (this.happy) comps.add(new TranslatableComponent("pokemob.description.evolve.happy"));
            if (this.dawnOnly) comps.add(new TranslatableComponent("pokemob.description.evolve.dawn"));
            if (this.duskOnly) comps.add(new TranslatableComponent("pokemob.description.evolve.dusk"));
            if (this.dayOnly) comps.add(new TranslatableComponent("pokemob.description.evolve.day"));
            if (this.nightOnly) comps.add(new TranslatableComponent("pokemob.description.evolve.night"));
            if (this.rainOnly) comps.add(new TranslatableComponent("pokemob.description.evolve.rain"));

            // TODO add in info related to needed formes.

            if (this.randomFactor != 1)
            {
                final String var = (int) (100 * this.randomFactor) + "%";
                comps.add(new TranslatableComponent("pokemob.description.evolve.chance", var));
            }
            if (this.move != null && !this.move.isEmpty())
                comps.add(new TranslatableComponent("pokemob.description.evolve.move",
                        MovesUtils.getMoveName(this.move).getString()));
            if (this.matcher != null)
            {
                this.matcher.reset();
                this.matcher.parse();
                final List<String> biomeNames = Lists.newArrayList();
                for (final BiomeType t : this.matcher._validSubBiomes) biomeNames.add(I18n.get(t.readableName));
                for (SpawnBiomeMatcher m2 : this.matcher._or_children)
                    for (final BiomeType t : m2._validSubBiomes) biomeNames.add(I18n.get(t.readableName));
                for (SpawnBiomeMatcher m2 : this.matcher._and_children)
                    for (final BiomeType t : m2._validSubBiomes) biomeNames.add(I18n.get(t.readableName));
                for (final ResourceLocation test : SpawnBiomeMatcher.getAllBiomeKeys())
                {
                    final boolean valid = this.matcher.checkBiome(test);
                    if (valid)
                    {
                        final String key = String.format("biome.%s.%s", test.getNamespace(), test.getPath());
                        biomeNames.add(I18n.get(key));
                    }
                }
                comps.add(new TranslatableComponent("pokemob.description.evolve.locations", biomeNames));
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
            final PokedexEntry entry = this.preEvolution;
            final PokedexEntry nex = this.evolution;
            final MutableComponent subEvo = new TranslatableComponent("pokemob.description.evolve.to",
                    entry.getTranslatedName(), nex.getTranslatedName());
            final List<MutableComponent> list = this.getEvoClauses();
            for (final MutableComponent item : list) subEvo.append("\n").append(item);
            return subEvo;
        }

        public boolean isInBiome(final IPokemob mob)
        {
            if (this.matcher != null && mob.getEntity().level instanceof ServerLevel world)
            {
                final LivingEntity entity = mob.getEntity();
                final Vector3 loc = Vector3.getNewVector().set(entity);
                if (!world.isPositionEntityTicking(loc.getPos()))
                {
                    PokecubeCore.LOGGER.error("Error checking for evolution, this area is not loaded!");
                    PokecubeCore.LOGGER.error("For: {}, at: {},{},{}", entity, loc.x, loc.y, loc.z,
                            new IllegalStateException());
                    return false;
                }
                final SpawnCheck check = new SpawnCheck(loc, entity.getCommandSenderWorld());
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
            if (data.item != null) this.item = Tools.getStack(data.item.getValues());
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
            if (data.form_from != null) this.neededForme = PokecubeItems.toPokecubeResource(data.form_from);
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
            if (this.level < 0) return false;
            boolean ret = mob.traded() == this.traded || !this.traded;
            final Random rand = new Random(mob.getRNGValue());
            if (rand.nextFloat() > this.randomFactor) return false;
            if (this.neededForme != null)
            {
                if (mob.getCustomHolder() == null) return false;
                if (!mob.getCustomHolder().key.equals(this.neededForme)) return false;
            }
            if (this.rainOnly)
            {
                final Level world = mob.getEntity().getCommandSenderWorld();
                final boolean rain = world.isRaining();
                if (!rain)
                {
                    final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
                    final PokemobTerrainEffects teffect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
                    if (teffect != null && !teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.RAIN))
                        return false;
                }
            }

            boolean correctItem = true;
            if (this.preset != null || !this.item.isEmpty())
            {
                correctItem = false;
                if (!mobs.isEmpty()) if (this.preset != null) correctItem = ItemList.is(this.preset, mobs.getItem());
                else correctItem = Tools.isSameStack(mobs, this.item, true);
            }

            if (ItemList.is(ICanEvolve.EVERSTONE, mob.getHeldItem())) return false;
            if (ItemList.is(ICanEvolve.EVERSTONE, mobs)) return false;
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
                for (final String s : moves) if (s != null) if (s.equalsIgnoreCase(this.move))
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
                final double time = mob.getEntity().getCommandSenderWorld().getDayTime() % 24000 / 24000d;
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
                final LootTable loottable = pokemob.getEntity().getCommandSenderWorld().getServer().getLootTables()
                        .get(action.lootTable);
                final LootContext.Builder lootcontext$builder = new LootContext.Builder(
                        (ServerLevel) pokemob.getEntity().getCommandSenderWorld())
                                .withParameter(LootContextParams.THIS_ENTITY, pokemob.getEntity());
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
            if (consumeInput) held.shrink(1);
            if (held.isEmpty()) player.getInventory().setItem(player.getInventory().selected, result);
            else if (!player.getInventory().add(result)) player.drop(result, false);
            if (player != pokemob.getOwner()) BrainUtils.initiateCombat(entity, player);
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
                pokemob.megaEvolve(forme);
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

    public static interface MegaRule
    {
        boolean shouldMegaEvolve(IPokemob mobIn, PokedexEntry entryTo);
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
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
            {
                final SpawnEvent.Check evt = new SpawnEvent.Check(context, forSpawn);
                PokecubeCore.POKEMOB_BUS.post(evt);
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
            SpawnEvent.Check.Rate event = new SpawnEvent.Check.Rate(context, forSpawn, rate);
            PokecubeCore.POKEMOB_BUS.post(event);
            return event.getRate();
        }

        public boolean isValid(final ResourceLocation biome)
        {
            for (final SpawnBiomeMatcher matcher : this.matchers.keySet())
                if (matcher.getValidBiomes().contains(biome)) return true;
            return false;
        }

        public boolean isValid(final Biome biome)
        {
            return isValid(biome.getRegistryName());
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

    public static final String TEXTUREPATH = "entity/textures/";

    public static TimePeriod dawn = new TimePeriod(0.85, 0.05);
    public static TimePeriod day = new TimePeriod(0.0, 0.5);
    public static TimePeriod dusk = new TimePeriod(0.45, 0.6);
    public static TimePeriod night = new TimePeriod(0.6, 0.85);

    private static final PokedexEntry BLANK = new PokedexEntry(true);

    public static final ResourceLocation MODELNO = new ResourceLocation(PokecubeCore.MODID,
            "entity/models/missingno.x3d");

    public static final ResourceLocation TEXNO = new ResourceLocation(PokecubeCore.MODID,
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
    SoundEvent event;

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
    public HashMap<ItemStack, PokedexEntry> formeItems = Maps.newHashMap();

    public PokedexEntry noItemForm = null;

    /** Map of forms assosciated with this one. */
    @CopyToGender
    public Map<String, PokedexEntry> forms = new HashMap<>();
    /**
     * Used to stop gender formes from spawning, spawning rate is done by gender
     * ratio of base forme instead.
     */
    public boolean isGenderForme = false;

    /** Can it megaevolve */
    @CopyToGender
    public boolean hasMegaForm = false;
    @CopyToGender
    public boolean hasShiny = true;
    /** Materials which will hurt or make it despawn. */
    @CopyToGender
    public String[] hatedMaterial;
    @CopyToGender
    public float height = -1;
    @CopyToGender
    private boolean isMega = false;
    @CopyToGender
    private boolean isGMax = false;
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

    @CopyToGender
    public HashMap<PokedexEntry, MegaRule> megaRules = Maps.newHashMap();

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
    public double preferedHeight = 1.5;
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
    public PokeType type1;

    @CopyToGender
    public PokeType type2;

    @CopyToGender
    public EntityType<? extends Mob> entity_type;

    // This is the actual size of the model, if not null, will be used for
    // scaling of rendering in guis, order is length, height, width
    public Vector3f modelSize = null;

    /** Cached trimmed name. */
    private String trimmedName;

    private Component description;

    // "" for automatic assignment
    public String modelExt = "";
    public ResourceLocation model = PokedexEntry.MODELNO;
    public ResourceLocation texture = PokedexEntry.TEXNO;
    public ResourceLocation animation = PokedexEntry.ANIMNO;

    public Map<String, BodyNode> poseShapes = null;

    // Here we have things that need to wait until loaded for initialization, so
    // we cache them.
    public List<Interact> _loaded_interactions = Lists.newArrayList();
    public Stats _forme_items = null;
    public List<XMLMegaRule> _loaded_megarules = Lists.newArrayList();

    /** Times not included here the pokemob will go to sleep when idle. */
    @CopyToGender
    public List<TimePeriod> activeTimes = new ArrayList<>();

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

    /**
     * Applies various things which needed server to be initialized, such as
     * interactions for tag lists, etc
     */
    public void onResourcesReloaded()
    {
        this.formeItems.clear();
        this.megaRules.clear();
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
                    if (!fixed) PokecubeCore.LOGGER.error("Unfilled value {} for {}!", f.getName(), this);
                }
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        // Set the tag based values
        this.shouldFly = this.isType(PokeType.getType("flying"));
        this.shouldFly = this.shouldFly || Tags.POKEMOB.isIn("fly_allowed", this.getTrimmedName());
        if (Tags.POKEMOB.isIn("fly_disallowed", this.getTrimmedName())) this.shouldFly = false;
        this.shouldDive = Tags.POKEMOB.isIn("dive_allowed", this.getTrimmedName());
        this.shouldSurf = Tags.POKEMOB.isIn("surf_allowed", this.getTrimmedName());
        this.canSitShoulder = Tags.POKEMOB.isIn("shoulder_allowed", this.getTrimmedName());
        this.isHeatProof = Tags.POKEMOB.isIn("fire_proof", this.getTrimmedName());
        this.isStarter = Tags.POKEMOB.isIn("starters", this.getTrimmedName());
        this.legendary = Tags.POKEMOB.isIn("legends", this.getTrimmedName());
        this.isShadowForme = Tags.POKEMOB.isIn("shadow", this.getTrimmedName());

        // Breeding whitelist is generally for legends that are explicitly
        // allowed to breed, like manaphy
        this.breeds = Tags.POKEMOB.isIn("breeding_whitelist", this.getTrimmedName())
                || !Tags.POKEMOB.isIn("no_breeding", this.getTrimmedName());

        this.foods[0] = Tags.POKEMOB.isIn("eats_light", this.getTrimmedName());
        this.foods[1] = Tags.POKEMOB.isIn("eats_stone", this.getTrimmedName());
        this.foods[2] = Tags.POKEMOB.isIn("eats_redstone", this.getTrimmedName());
        this.foods[3] = Tags.POKEMOB.isIn("eats_plants", this.getTrimmedName());
        this.foods[4] = Tags.POKEMOB.isIn("eats_never", this.getTrimmedName());
        this.foods[5] = !Tags.POKEMOB.isIn("eats_no_berries", this.getTrimmedName());
        this.foods[6] = Tags.POKEMOB.isIn("eats_water", this.getTrimmedName());

        if (Tags.MOVEMENT.isIn("floats", this.getTrimmedName())) this.mobType |= MovementType.FLOATING.mask;
        if (Tags.MOVEMENT.isIn("flies", this.getTrimmedName())) this.mobType |= MovementType.FLYING.mask;
        if (Tags.MOVEMENT.isIn("swims", this.getTrimmedName())) this.mobType |= MovementType.WATER.mask;
        if (Tags.MOVEMENT.isIn("walks", this.getTrimmedName())) this.mobType |= MovementType.NORMAL.mask;

        if (this.isMega() || this.isGMax()) this.breeds = false;

        if (this._forme_items != null)
        {
            final Map<String, String> values = this._forme_items.values;
            for (final String key : values.keySet())
            {
                final String value = values.get(key);
                if (key.equals("forme"))
                {
                    final String[] args = value.split(",");
                    for (final String s : args)
                    {
                        String forme = "";
                        String item = "";
                        final String[] args2 = s.split(":");
                        for (final String s1 : args2)
                        {
                            final String arg1 = s1.trim().substring(0, 1);
                            final String arg2 = s1.trim().substring(1);
                            if (arg1.equals("N")) forme = arg2;
                            else if (arg1.equals("I")) item = arg2.replace("`", ":");
                        }

                        final PokedexEntry formeEntry = Database.getEntry(forme);
                        if (!forme.isEmpty() && formeEntry != null)
                        {
                            final ItemStack stack = PokecubeItems.getStack(item, false);
                            // TODO see if needs to add to holdables
                            this.formeItems.put(stack, formeEntry);
                            if (formeEntry.noItemForm != null)
                                PokecubeCore.LOGGER.warn("Changing Base forme of {} from {} to {}", formeEntry,
                                        formeEntry.noItemForm, this);
                            formeEntry.noItemForm = this;
                        }
                    }
                }
            }
        }
        for (final XMLMegaRule rule : this._loaded_megarules)
        {
            String forme = rule.name != null ? rule.name : null;
            if (forme == null) if (rule.preset != null) if (rule.preset.startsWith("Mega"))
            {
                forme = this.getTrimmedName() + "_" + ThutCore.trim(rule.preset);
                if (rule.item_preset == null)
                    rule.item_preset = this.getTrimmedName() + "" + ThutCore.trim(rule.preset);
            }
            final String move = rule.move;
            final String ability = rule.ability;
            final String item_preset = rule.item_preset;

            if (forme == null)
            {
                PokecubeCore.LOGGER.info("Error with mega evolution for " + this + " rule: preset=" + rule.preset
                        + " name=" + rule.name);
                continue;
            }

            final PokedexEntry formeEntry = Database.getEntry(forme);
            if (!forme.isEmpty() && formeEntry != null)
            {
                ItemStack stack = ItemStack.EMPTY;
                if (item_preset != null && !item_preset.isEmpty())
                {
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.info(forme + " " + item_preset);
                    stack = PokecubeItems.getStack(item_preset, false);
                    if (stack.isEmpty()) stack = PokecubeItems.getStack(Database.trim_loose(item_preset), false);
                }
                else if (rule.item != null) stack = Tools.getStack(rule.item.getValues());
                if (rule.item != null)
                    if (PokecubeMod.debug) PokecubeCore.LOGGER.info(stack + " " + rule.item.getValues());
                if ((move == null || move.isEmpty()) && stack.isEmpty() && (ability == null || ability.isEmpty()))
                {
                    PokecubeCore.LOGGER.info("Skipping Mega: " + this + " -> " + formeEntry
                            + " as it has no conditions, or conditions cannot be met.");
                    PokecubeCore.LOGGER
                            .info(" rule: preset=" + rule.preset + " name=" + rule.name + " item=" + rule.item_preset);
                    continue;
                }
                final MegaEvoRule mrule = new MegaEvoRule(this);
                if (item_preset != null && !item_preset.isEmpty()) mrule.oreDict = item_preset;
                if (ability != null) mrule.ability = ability;
                if (move != null) mrule.moveName = move;
                if (!stack.isEmpty()) mrule.stack = stack;
                formeEntry.setMega(true);
                formeEntry.setBaseForme(this);
                this.megaRules.put(formeEntry, mrule);
                if (PokecubeMod.debug) PokecubeCore.LOGGER.info("Added Mega: " + this + " -> " + formeEntry);
            }
        }
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

    public void addEVXP(final byte[] evs, final int baseXP, final int evolutionMode, final int sexRatio)
    {
        this.evs = evs;
        this.baseXP = baseXP;
        this.evolutionMode = evolutionMode;
        this.sexeRatio = sexRatio;
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

    public boolean canEvolve(final int level)
    {
        return this.canEvolve(level, ItemStack.EMPTY);
    }

    public boolean canEvolve(final int level, final ItemStack stack)
    {
        for (final EvolutionData d : this.evolutions)
        {

            boolean itemCheck = d.item == ItemStack.EMPTY;
            if (!itemCheck && stack != ItemStack.EMPTY) itemCheck = stack.sameItem(d.item);
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
        if (e.baseForme != null && e.baseForme != this)
            throw new IllegalArgumentException("Cannot add a second base form");
        e.pokedexNb = this.pokedexNb;

        if (e.possibleMoves == null) e.possibleMoves = this.possibleMoves;
        if (e.lvlUpMoves == null) e.lvlUpMoves = this.lvlUpMoves;
        if (e.stats == null) e.stats = this.stats.clone();
        if (this.evs == null) PokecubeCore.LOGGER.error(this + " " + this.baseForme, new IllegalArgumentException());
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
        return MovementType.FLOATING.is(this.mobType);
    }

    public boolean flys()
    {
        return MovementType.FLYING.is(this.mobType);
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
            if (this.getBaseForme() != null && this.getBaseForme() != this)
                this.baseName = this.getBaseForme().getTrimmedName();
            else this.baseName = this.getTrimmedName();
            if (this.getBaseForme() == this) PokecubeCore.LOGGER.error("Error with " + this);
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
    public Component getDescription()
    {
        if (this.description == null)
        {
            final PokedexEntry entry = this;
            final MutableComponent typeString = PokeType.getTranslatedName(entry.getType1());
            if (entry.getType2() != PokeType.unknown)
                typeString.append("/").append(PokeType.getTranslatedName(entry.getType2()));
            final MutableComponent typeDesc = new TranslatableComponent("pokemob.description.type",
                    entry.getTranslatedName(), typeString);
            MutableComponent evoString = null;
            if (entry.canEvolve()) for (final EvolutionData d : entry.evolutions)
            {
                if (d.evolution == null) continue;
                if (evoString == null) evoString = d.getEvoString();
                else evoString = evoString.append("\n").append(d.getEvoString());
                evoString.append("\n");
            }
            MutableComponent descString = typeDesc;
            if (evoString != null) descString = descString.append("\n").append(evoString);
            if (entry._evolvesFrom != null)
                descString = descString.append("\n").append(new TranslatableComponent("pokemob.description.evolve.from",
                        entry.getTranslatedName(), entry._evolvesFrom.getTranslatedName()));
            this.description = descString;
        }
        return this.description;
    }

    public EntityType<? extends Mob> getEntityType()
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
        if (this.getBaseForme() != null) return this.getBaseForme().getEvolutionMode();
        if (this.evolutionMode < 0)
        {
            PokecubeCore.LOGGER.error("Undefined evo mode for {}, setting to \"2\"");
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
        if (!this.base && this.isGenderForme && this.getBaseForme() != null)
            return this.getBaseForme().getForGender(gender);
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

    /** @return the pokedexNb */
    public int getPokedexNb()
    {
        return this.pokedexNb;
    }

    public ItemStack getRandomHeldItem(final Mob mob)
    {
        if (mob.getCommandSenderWorld().isClientSide) return ItemStack.EMPTY;
        if (this.heldTable != null)
        {
            final LootTable loottable = mob.getCommandSenderWorld().getServer().getLootTables().get(this.heldTable);
            final LootContext.Builder lootcontext$builder = new LootContext.Builder(
                    (ServerLevel) mob.getCommandSenderWorld()).withParameter(LootContextParams.THIS_ENTITY, mob)
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
            this.nameComp = new TranslatableComponent(key);
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
        if (this.isFemaleForme || this.isMaleForme) name = this.getBaseName();
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
        for (final String s : this.food) foodList.add(s);
        poke:
        for (final PokedexEntry e : Database.data.values())
        {
            final Set<String> tags = Tags.BREEDING.lookupTags(e.getTrimmedName());
            for (final String s : tags) if (foodList.contains(s))
            {
                this.prey.add(e);
                continue poke;
            }
        }
    }

    public void initRelations()
    {
        final List<EvolutionData> stale = Lists.newArrayList();
        for (final EvolutionData d : this.evolutions)
            if (!Pokedex.getInstance().isRegistered(d.evolution)) stale.add(d);
        this.evolutions.removeAll(stale);
        if (!stale.isEmpty()) PokecubeCore.LOGGER.debug(stale.size() + " stales for " + this);
        this.addRelation(this);
        for (final EvolutionData d : this.evolutions)
        {
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
        PokedexEntry newForme = null;
        if (this.formeItems.isEmpty() && this.getBaseForme() != null)
            for (final PokedexEntry entry : this.getBaseForme().formeItems.values()) if (entry == this)
        {
            this.getBaseForme().onHeldItemChange(oldStack, newStack, pokemob);
            return;
        }
        if (newStack.isEmpty() && this.noItemForm != null) newForme = this.noItemForm;
        for (final ItemStack key : this.formeItems.keySet()) if (Tools.isSameStack(oldStack, key, true))
        {
            newForme = this;
            break;
        }
        for (final ItemStack key : this.formeItems.keySet()) if (Tools.isSameStack(newStack, key, true))
        {
            newForme = this.formeItems.get(key);
            break;
        }
        if (newForme != null && newForme != pokemob.getPokedexEntry())
            ICanEvolve.setDelayedMegaEvolve(pokemob, newForme, null);
    }

    public void setBaseForme(final PokedexEntry baseForme)
    {
        if (this.baseForme != null && baseForme != this.baseForme)
            PokecubeCore.LOGGER.error("Trying to replace {} with {} as base for {}", this.baseForme, baseForme, this);
        this.baseForme = baseForme;
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
        if (this.modId != null && !this.modId.equals(modId)) PokecubeCore.LOGGER
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

    public boolean shouldEvolve(final IPokemob mob)
    {
        for (final EvolutionData d : this.evolutions) if (d.shouldEvolve(mob)) return true;
        return false;
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
        final String ret = this.name;
        return ret;
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
                this.icons[0][1] = new ResourceLocation(texture + "s.png");
                this.icons[1][0] = new ResourceLocation(texture + ".png");
                this.icons[1][1] = new ResourceLocation(texture + "s.png");
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
                this.icons[0][1] = new ResourceLocation(texture + male_ + "s.png");
                this.icons[1][0] = new ResourceLocation(texture + female_ + ".png");
                this.icons[1][1] = new ResourceLocation(texture + female_ + "s.png");
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

    public boolean isGMax()
    {
        return this.isGMax;
    }

    public void setGMax(final boolean isGMax)
    {
        this.isGMax = isGMax;
        // Mark gmax as mega as well.
        this.isMega = isGMax;
    }

    public boolean isMega()
    {
        return this.isMega;
    }

    public void setMega(final boolean isMega)
    {
        this.isMega = isMega;
    }
}
