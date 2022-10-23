package pokecube.api.data.pokedex;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import thut.core.common.ThutCore;

public class DefaultFormeHolder
{

    public static boolean _main_init_ = false;

    public static class TexColours
    {
        public String material = "";
        public float red = 1;
        public float green = 1;
        public float blue = 1;
        public float alpha = 1;
    }

    public static class MatTexs
    {
        public String material = "";
        public String tex = "";
    }

    // These three allow specific models/textures for evos
    public String key = null;
    public String tex = null;
    public String model = null;
    public String anim = null;

    public String parent = null;

    public List<TexColours> colours = Lists.newArrayList();
    public List<MatTexs> matTex = Lists.newArrayList();
    public String[] hidden = {};

    public Map<String, TexColours> _colourMap_ = Maps.newHashMap();
    public Map<String, MatTexs> _matsMap_ = Maps.newHashMap();
    public Set<String> _hide_ = Sets.newHashSet();
    private final List<FormeHolder> _matches = Lists.newArrayList();

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof DefaultFormeHolder holder)) return false;
        if (this.key == null) return super.equals(obj);
        return this.key.equals(holder.key);
    }

    public FormeHolder getForme(final PokedexEntry baseEntry)
    {
        if (this.key.endsWith("*"))
        {
            if (DefaultFormeHolder._main_init_)
            {
                final String key = this.key.substring(0, this.key.length() - 1);
                if (this._matches.isEmpty()) for (final ResourceLocation test : Database.formeHolders.keySet())
                    if (test.getPath().startsWith(key)) this._matches.add(Database.formeHolders.get(test));
                if (!this._matches.isEmpty())
                    return this._matches.get(ThutCore.newRandom().nextInt(this._matches.size()));
            }
            return null;
        }
        if (this.key != null)
        {
            final ResourceLocation key = PokecubeItems.toPokecubeResource(this.key);
            if (Database.formeHolders.containsKey(key)) return Database.formeHolders.get(key);

            parent_check:
            if (this.parent != null)
            {
                final ResourceLocation pkey = PokecubeItems.toPokecubeResource(this.parent);
                final FormeHolder parent = Database.formeHolders.get(pkey);
                if (parent == null || parent.loaded_from == null)
                {
                    PokecubeAPI.LOGGER.error(
                            "Error loading parent {} for {}, it needs to be registered earlier in the file!",
                            this.parent, this.key);
                    break parent_check;
                }
                final DefaultFormeHolder p = parent.loaded_from;
                if (p.tex != null && this.tex == null) this.tex = p.tex;
                if (p.model != null && this.model == null) this.model = p.model;
                if (p.anim != null && this.anim == null) this.anim = p.anim;
                if (p.hidden != null) if (p.hidden.length > 0)
                {
                    final List<String> ours = this.hidden == null ? Lists.newArrayList()
                            : Lists.newArrayList(this.hidden);
                    for (final String s : p.hidden) ours.add(s);
                    this.hidden = ours.toArray(new String[0]);
                }
                this.colours.addAll(p.colours);
                this.matTex.addAll(p.matTex);
            }
            if (this.hidden != null) for (final String element : this.hidden)
            {
                final String value = ThutCore.trim(element);
                this._hide_.add(value);
            }
            if (this.colours != null) for (final TexColours c : this.colours)
            {
                c.material = ThutCore.trim(c.material);
                this._colourMap_.put(c.material, c);
            }
            if (this.matTex != null) for (final MatTexs c : this.matTex)
            {
                c.material = ThutCore.trim(c.material);
                this._matsMap_.put(c.material, c);
            }

            final ResourceLocation texl = this.tex != null
                    ? PokecubeItems.toPokecubeResource(baseEntry.texturePath + this.tex)
                    : null;
            final ResourceLocation modell = this.model != null
                    ? PokecubeItems.toPokecubeResource(
                            baseEntry.model.toString().replace(baseEntry.getTrimmedName(), this.model))
                    : null;
            final ResourceLocation animl = this.anim != null
                    ? PokecubeItems.toPokecubeResource(
                            baseEntry.animation.toString().replace(baseEntry.getTrimmedName(), this.anim))
                    : null;
            final FormeHolder holder = FormeHolder.get(modell, texl, animl, key);
            holder.loaded_from = this;
            Database.registerFormeHolder(baseEntry, holder);
            return holder;
        }
        return null;
    }
}
