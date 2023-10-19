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
import pokecube.api.utils.PokeType;
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

    // If this is not null, then pokemob's base type will be overriden with
    // this.
    public String types = null;
    // If this is not null, then the pokemob's ability will be defaulted to
    // this, instead of what is in the pokedex entry.
    public String ability = null;
    // As of gen 9, we have many cosmetic forms that also change mass, so we
    // inclide this here, if this is not -1, we will then apply it.
    public double mass = -1;

    public String key = null;
    // These three allow specific models/textures for evos
    public String tex = null;
    public String model = null;
    public String anim = null;
    public Boolean hasShiny = null;

    public String parent = null;
    public String root_entry = null;

    public List<TexColours> colours = Lists.newArrayList();
    public List<MatTexs> matTex = Lists.newArrayList();
    public String[] hidden = {};

    private PokedexEntry _entry = null;

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

    public PokedexEntry getEntry()
    {
        PokedexEntry fromKey = Database.getEntry(this.key);
        if (fromKey == null)
        {
            fromKey = new PokedexEntry(0, this.key, true);
            if (this.types != null)
            {
                String[] types = this.types.split(",");
                fromKey.type1 = PokeType.getType(types[0]);
                if (types.length > 1) fromKey.type2 = PokeType.getType(types[1]);
            }
            if (this.ability != null)
            {
                String[] abilities = this.ability.split(",");
                for (String s : abilities) fromKey.abilities.add(s);
            }
            if (mass > 0) fromKey.mass = mass;
            if (hasShiny != null) fromKey.hasShiny = this.hasShiny;
        }
        else if (fromKey.pokedexNb != 0)
        {
            new IllegalArgumentException("Duplicate entry!");
        }
        this._entry = fromKey;
        return _entry;
    }

    public FormeHolder getForme(final PokedexEntry baseEntry)
    {
        if (this.key.endsWith("*"))
        {
            if (DefaultFormeHolder._main_init_)
            {
                final String key = this.key.substring(0, this.key.length() - 1);
                if (this._matches.isEmpty()) for (final ResourceLocation test : FormeHolder.formeHolders.keySet())
                    if (test.getPath().startsWith(key)) this._matches.add(FormeHolder.formeHolders.get(test));
                if (!this._matches.isEmpty())
                    return this._matches.get(ThutCore.newRandom().nextInt(this._matches.size()));
            }
            return null;
        }
        if (this.key != null)
        {
            final ResourceLocation key = PokecubeItems.toPokecubeResource(this.key);
            if (FormeHolder.formeHolders.containsKey(key)) return FormeHolder.formeHolders.get(key);

            parent_check:
            if (this.parent != null)
            {
                final ResourceLocation pkey = PokecubeItems.toPokecubeResource(this.parent);
                final FormeHolder parent = FormeHolder.formeHolders.get(pkey);
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
            var base = this.getEntry().getBaseForme();
            if (base == null || base == Database.missingno)
            {
                this.getEntry().setBaseForme(baseEntry);
                baseEntry.copyToForm(this.getEntry());
            }
            String model = this.getEntry().modelPath;
            String modid = this.getEntry().getModId();
            String tex = this.getEntry().texturePath;
            if (modid == null) modid = "pokecube_mobs";
            if (!tex.contains(":")) tex = modid + ":" + tex;
            if (!model.contains(":")) model = modid + ":" + model;

            this.getEntry().texturePath = tex;
            this.getEntry().modelPath = model;

            ResourceLocation texl = this.tex != null ? PokecubeItems.toResource(tex + this.tex, modid) : null;
            ResourceLocation modell = this.model != null ? PokecubeItems.toResource(model + this.model, modid) : null;
            ResourceLocation animl = this.anim != null ? PokecubeItems.toResource(model + this.anim, modid) : null;

            if (texl != null && !texl.getPath().endsWith(".png"))
                texl = new ResourceLocation(texl.getNamespace(), texl.getPath() + ".png");
            if (animl != null && !animl.getPath().endsWith(".xml"))
                animl = new ResourceLocation(animl.getNamespace(), animl.getPath() + ".xml");

            final FormeHolder holder = FormeHolder.get(this.getEntry(), modell, texl, animl, key);
            holder.loaded_from = this;
            holder._entry = this.getEntry();
            Database.registerFormeHolder(this.getEntry(), holder);
            return holder;
        }
        return null;
    }
}
