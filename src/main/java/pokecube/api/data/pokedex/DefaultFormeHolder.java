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

    public String key = null;
    // These three allow specific models/textures for evos
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

    private List<PokeType> _types = Lists.newArrayList();
    private List<String> _abilities = Lists.newArrayList();

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof DefaultFormeHolder holder)) return false;
        if (this.key == null) return super.equals(obj);
        return this.key.equals(holder.key);
    }

    public List<PokeType> getTypes(PokedexEntry baseEntry)
    {
        if (_types.isEmpty())
        {
            if (this.types == null)
            {
                _types.add(baseEntry.getType1());
                _types.add(baseEntry.getType2());
            }
            else
            {
                String[] types = this.types.split(",");
                for (var t : types) _types.add(PokeType.getType(t.strip()));
                while (_types.size() < 2) _types.add(PokeType.unknown);
            }
        }
        return _types;
    }

    public List<String> getAbilities(PokedexEntry baseEntry)
    {
        if (_abilities.isEmpty())
        {
            if (ability == null)
            {
                _abilities.addAll(baseEntry.abilities);
            }
            else
            {
                String[] abilities = this.ability.split(",");
                for (var a : abilities) this._abilities.add(a);
            }
        }
        return _abilities;
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
            String model = baseEntry.modelPath;

            String modid = baseEntry.getModId();
            if (modid == null) modid = "pokecube_mobs";
            if (!baseEntry.texturePath.contains(":")) baseEntry.texturePath = modid + ":" + baseEntry.texturePath;
            String tex = baseEntry.texturePath;

            ResourceLocation texl = this.tex != null ? PokecubeItems.toResource(tex + this.tex, modid) : null;
            ResourceLocation modell = this.model != null ? PokecubeItems.toResource(model + this.model, modid) : null;
            ResourceLocation animl = this.anim != null ? PokecubeItems.toResource(model + this.anim, modid) : null;

            if (texl != null && !texl.getPath().endsWith(".png"))
                texl = new ResourceLocation(texl.getNamespace(), texl.getPath() + ".png");
            if (animl != null && !animl.getPath().endsWith(".xml"))
                animl = new ResourceLocation(animl.getNamespace(), animl.getPath() + ".xml");

            final FormeHolder holder = FormeHolder.get(baseEntry, modell, texl, animl, key);
            holder.loaded_from = this;
            Database.registerFormeHolder(baseEntry, holder);
            return holder;
        }
        return null;
    }
}
