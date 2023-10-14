package pokecube.api.data.pokedex;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

public class InteractsAndEvolutions
{
    public static interface PokemobCondition
    {
        default PokemobCondition and(PokemobCondition other)
        {
            return (mobIn) -> {
                return this.matches(mobIn) && other.matches(mobIn);
            };
        }

        boolean matches(IPokemob mobIn);

        default void init()
        {}
    }

    public static class Ability implements PokemobCondition
    {
        public String ability;

        @Override
        public boolean matches(IPokemob mobIn)
        {
            return AbilityManager.hasAbility(this.ability, mobIn);
        }
    }

    public static class Move implements PokemobCondition
    {
        public String move;

        @Override
        public boolean matches(IPokemob mobIn)
        {
            return Tools.hasMove(this.move, mobIn);
        }
    }

    public static class HeldItem implements PokemobCondition
    {
        public JsonObject item;
        public String tag = "";
        private ItemStack _value = ItemStack.EMPTY;
        private TagKey<Item> _tag = null;

        @Override
        public boolean matches(IPokemob mobIn)
        {
            if (_tag != null && mobIn.getHeldItem().is(_tag)) return true;
            if (!this._value.isEmpty())
            {
                boolean rightStack = Tools.isSameStack(this._value, mobIn.getHeldItem(), true);
                return rightStack;
            }
            return false;
        }

        @Override
        public void init()
        {
            if (item != null) _value = CraftingHelper.getItemStack(item, true, true);
            if (!tag.isEmpty())
            {
                _tag = TagKey.create(Keys.ITEMS, new ResourceLocation(tag));
            }
        }
    }

    public static class Location implements PokemobCondition
    {
        public SpawnRule location;

        private SpawnBiomeMatcher _matcher;

        @Override
        public boolean matches(IPokemob mobIn)
        {
            if (_matcher == null)
            {
                _matcher = SpawnBiomeMatcher.get(location);
            }
            if (mobIn.getEntity().level instanceof ServerLevel world)
            {
                final LivingEntity entity = mobIn.getEntity();
                final Vector3 loc = new Vector3().set(entity);
                final SpawnCheck check = new SpawnCheck(loc, world);
                return _matcher.matches(check);
            }
            return false;
        }
    }

    public static PokemobCondition makeFromElement(JsonElement element)
    {
        if (element.isJsonArray())
        {
            var arr = element.getAsJsonArray();
            return makeFromArray(arr);
        }
        else if (element.isJsonObject())
        {
            JsonObject obj = element.getAsJsonObject();
            return makeFromObject(obj);
        }
        return null;
    }

    public static PokemobCondition makeFromArray(JsonArray array)
    {
        PokemobCondition root = null;
        for (int i = 0; i < array.size(); i++)
        {
            JsonElement e = array.get(i);
            var made = makeFromElement(e);
            if (root == null) root = made;
            else if (made != null) root = root.and(made);
        }
        return root;
    }

    public static PokemobCondition makeFromObject(JsonObject obj)
    {
        if (!obj.has("key"))
        {
            PokecubeAPI.LOGGER.error("missing key {} for a mega evo rule!", obj);
            return null;
        }
        String key = obj.get("key").getAsString();
        Class<? extends PokemobCondition> condClass = CONDITIONS.get(key);
        if (condClass == null)
        {
            PokecubeAPI.LOGGER.error("invalid type key {} for a mega evo rule!", key);
            return null;
        }
        PokemobCondition condition = JsonUtil.gson.fromJson(obj, condClass);
        condition.init();
        return condition;
    }

    public static Map<String, Class<? extends PokemobCondition>> CONDITIONS = new HashMap<>();

    public static void init()
    {
        CONDITIONS.put("item", HeldItem.class);
        CONDITIONS.put("ability", Ability.class);
        CONDITIONS.put("move", Move.class);
    }

    public static class Evolution
    {
        public Boolean clear;
        public String name;
        public Integer level;
        public Integer priority;
        public SpawnRule location;
        public String animation;
        public Drop item;
        public String item_preset;
        public String time;
        public Boolean trade;
        public Boolean rain;
        public Boolean happy;
        public String sexe;
        public String move;
        public Float chance;
        public String evoMoves;

        public String form_from = null;

        protected DefaultFormeHolder model = null;

        public FormeHolder getForme(final PokedexEntry baseEntry)
        {
            if (this.model != null) return this.model.getForme(baseEntry);
            return null;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (super.equals(obj)) return true;
            if (obj instanceof Evolution)
            {
                for (final Field f : this.getClass().getFields()) try
                {
                    final Object ours = f.get(this);
                    final Object theirs = f.get(obj);
                    if (ours != null && !ours.equals(theirs)) return false;
                    if (theirs != null && !theirs.equals(ours)) return false;
                    if (ours == null && theirs != null) return false;
                    if (theirs == null && ours != null) return false;
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            return super.equals(obj);
        }

        @Override
        public String toString()
        {
            return PokedexEntryLoader.gson.toJson(this);
        }
    }

    public static class Interact
    {
        public Boolean male = true;
        public Boolean female = true;
        public Integer cooldown = 50;
        public Integer variance = 100;
        public Integer baseHunger = 100;
        public Boolean isTag = false;
        public Drop key;
        public Action action;

        private String _ser_cache;

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj instanceof Interact a)
            {
                if (this._ser_cache == null) this._ser_cache = JsonUtil.gson.toJson(this);
                if (a._ser_cache == null) a._ser_cache = JsonUtil.gson.toJson(this);
                return this._ser_cache.equals(a._ser_cache);
            }
            return false;
        }
    }

    public static class Action
    {
        public Map<String, String> values = Maps.newHashMap();
        public String tag;
        public String lootTable;
        public List<Drop> drops = Lists.newArrayList();
    }

    public static class DyeInfo implements Consumer<PokedexEntry>
    {
        // Colour for the normal mob
        public String base;
        // Colour for the shiny mob
        public String shiny;
        // If this is populated, only the listed dyes will work!
        public List<String> dyes = Lists.newArrayList();

        @Override
        public void accept(PokedexEntry entry)
        {
            final List<String> opts = dyes;
            entry.dyeable = true;
            // Parse base colour
            base = ThutCore.trim(base);
            for (final DyeColor dye : DyeColor.values()) if (ThutCore.trim(dye.name()).equals(base))
            {
                entry.defaultSpecial = dye.getId();
                break;
            }
            // Parse shiny colour
            shiny = ThutCore.trim(shiny);
            for (final DyeColor dye : DyeColor.values()) if (ThutCore.trim(dye.name()).equals(shiny))
            {
                entry.defaultSpecials = dye.getId();
                break;
            }
            entry.validDyes.clear();
            // Parse any limits on colours
            for (String s : opts)
            {
                s = ThutCore.trim(s);
                for (final DyeColor dye : DyeColor.values()) if (ThutCore.trim(dye.name()).equals(s))
                {
                    entry.validDyes.add(dye);
                    break;
                }
            }
        }
    }

    public static class FormeItem
    {
        protected String item;
        protected String forme;

        protected DefaultFormeHolder model = null;

        private PokedexEntry _entry = null;
        private ItemStack _itemstack = ItemStack.EMPTY;
        private FormeHolder _forme = null;

        public FormeHolder getForme(final PokedexEntry baseEntry)
        {
            if (this.model != null)
            {
                if (this._forme == null)
                {
                    this._forme = this.model.getForme(baseEntry);
                    if (this._forme != null)
                    {
                        this._forme._is_item_forme = true;
                    }
                    else
                    {
                        PokecubeAPI.LOGGER.error("No forme found for model: {}", model.key);
                        this.model = null;
                    }
                }
                return this._forme;
            }
            return this._forme;
        }

        public PokedexEntry getOutput()
        {
            if (_entry == null) _entry = Database.getEntry(forme);
            return _entry;
        }

        public ItemStack getKey()
        {
            if (_itemstack.isEmpty()) _itemstack = PokecubeItems.getStack(new ResourceLocation(item));
            return _itemstack;
        }
    }
}
