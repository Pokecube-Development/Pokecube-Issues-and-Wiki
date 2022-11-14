package pokecube.api.data.pokedex;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.MegaRule;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

public class InteractsAndEvolutions
{

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

    public static class BaseMegaRule
    {
        public String name;
        public String preset;
        public String move;
        public String ability;
        public Drop item;
        public String item_preset;
    }

    public static class MegaEvoRule implements MegaRule
    {
        public ItemStack stack;
        public String oreDict;
        public String moveName;
        public String ability;
        final PokedexEntry baseForme;

        public MegaEvoRule(final PokedexEntry baseForme)
        {
            this.stack = ItemStack.EMPTY;
            this.moveName = "";
            this.ability = "";
            this.baseForme = baseForme;
        }

        @Override
        public boolean shouldMegaEvolve(final IPokemob mobIn, final PokedexEntry entryTo)
        {
            boolean rightStack = true;
            boolean hasMove = true;
            boolean hasAbility = true;
            boolean rule = false;
            if (this.oreDict != null)
            {
                this.stack = PokecubeItems.getStack(this.oreDict);
                this.oreDict = null;
            }
            if (!this.stack.isEmpty())
            {
                rightStack = Tools.isSameStack(this.stack, mobIn.getHeldItem(), true);
                rule = true;
            }
            if (this.moveName != null && !this.moveName.isEmpty())
            {
                hasMove = Tools.hasMove(this.moveName, mobIn);
                rule = true;
            }
            if (this.ability != null && !this.ability.isEmpty())
            {
                hasAbility = AbilityManager.hasAbility(this.ability, mobIn);
                rule = true;
            }
            if (hasAbility && mobIn.getAbility() != null) hasAbility = mobIn.getAbility().canChange(mobIn, entryTo);
            return rule && hasMove && rightStack && hasAbility;
        }
    }
}
