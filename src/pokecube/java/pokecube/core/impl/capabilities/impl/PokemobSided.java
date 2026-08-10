package pokecube.core.impl.capabilities.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;

public abstract class PokemobSided extends PokemobBase
{
    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture()
    {
        final PokedexEntry entry = this.getPokedexEntry();
        if (this.textures != null)
        {
            final int index = this.getSexe() == IPokemob.FEMALE && entry.textureDetails[1] != null ? 1 : 0;
            final boolean shiny = this.isShiny();
            final int effects = entry.textureDetails[index].length;
            final int texIndex = this.getEntity().tickCount % effects * 3 / effects + (shiny ? effects : 0);
            return this.textures[texIndex];
        }
        final int index = this.getSexe() == IPokemob.FEMALE && entry.textureDetails[1] != null ? 1 : 0;
        final int effects = entry.textureDetails[index].length;
        final int size = 2 * effects;
        this.textures = new ResourceLocation[size];

        String texName = entry.texturePath + entry.getTrimmedName();

        if (!texName.contains(":")) texName = entry.getModId() + ":" + texName;

        if (this.getCustomHolder() != null && this.getCustomHolder().texture != null)
            texName = this.getCustomHolder().texture.toString();
        texName = texName.replace(".png", "");

        final String baseName = texName;

        for (int i = 0; i < effects; i++)
        {
            texName = baseName + entry.textureDetails[index][i];
            this.textures[i] = ResourceLocation.parse(texName + ".png");
            this.textures[i + effects] = this.textures[i];
        }
        return this.getTexture();

    }

    protected PokedexEntry _renderEntryCache, _lastEntryCache;
    protected FormeHolder _renderHolderCache, _lastHolderCache;
    protected byte _renderSexe, _lastRenderSexe;

    protected Object2ObjectOpenHashMap<ResourceLocation, ResourceLocation> _TEXCACHE = new Object2ObjectOpenHashMap<>();

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        if (texture == null) return this.getTexture();

        // These return or update the cache
        PokedexEntry entry = this.getPokedexEntry();
        FormeHolder holder = this.getCustomHolder();
        byte sexe = this.getSexe();

        if (entry==_lastEntryCache && holder == _lastHolderCache && _TEXCACHE.containsKey(texture))
        {
            return _TEXCACHE.get(texture);
        }
        ResourceLocation orig = texture;
        _lastEntryCache = entry;
        _lastHolderCache = holder;

        // If texture is the same as root entry, then we might need to adjust
        // it, so replace with out getter, which also checks the entry's root
        // texture.
        if (texture.equals(entry.texture())) texture = this.getTexture();

        if (holder != null && holder.texture != null
                && !holder.texture.getNamespace().equals(entry.texture().getNamespace()))
        {
            _TEXCACHE.put(orig, holder.texture);
            return holder.texture;
        }

        if (!texture.getPath().contains("entity/"))
        {
            final int index = sexe == IPokemob.FEMALE && entry.textureDetails[1] != null ? 1 : 0;
            final int effects = entry.textureDetails[index].length;
            final int texIndex = this.getEntity().tickCount % effects * 3 / effects;
            if (!this.texs.containsKey(texture))
            {
                final int maxNum = entry.textureDetails.length * entry.textureDetails[0].length;
                final ResourceLocation[] tex = new ResourceLocation[maxNum];
                String base = entry.texturePath + texture.getPath();

                if (!base.contains(":")) base = entry.getModId() + ":" + base;

                if (base.endsWith(".png")) base = base.substring(0, base.length() - 4);
                for (int i = 0; i < maxNum; i++)
                {
                    final String path = base + entry.textureDetails[index][texIndex] + ".png";
                    tex[i] = ResourceLocation.parse(path);
                }
                this.texs.put(texture, tex);
            }
            final ResourceLocation[] tex = this.texs.get(texture);
            texture = tex[texIndex];
        }
        String texName = texture.toString();
        if (!texName.endsWith(".png")) texture = ResourceLocation.parse(texName = texName + ".png");
        if (this.isShiny())
        {
            if (!this.shinyTexs.containsKey(texture))
            {
                texName = texName.replace(".png", "_s.png");
                final ResourceLocation modified = ResourceLocation.parse(texName);
                this.shinyTexs.put(texture, modified);
                return modified;
            }
            else texture = this.shinyTexs.get(texture);
        }
        _TEXCACHE.put(orig, texture);
        return texture;
    }
}