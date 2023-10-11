package pokecube.core.impl.capabilities.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
            final ResourceLocation texture = this.textures[texIndex];
            return texture;
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
            this.textures[i] = new ResourceLocation(texName + ".png");
            this.textures[i + effects] = this.textures[i];
        }
        return this.getTexture();

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation modifyTexture(ResourceLocation texture)
    {
        if (texture == null) return this.getTexture();
        PokedexEntry entry = this.getPokedexEntry();
        // If texture is the same as root entry, then we might need to adjust
        // it, so replace with out getter, which also checks the entry's root
        // texture.
        if (texture.equals(entry.texture())) texture = this.getTexture();

        if (this.getCustomHolder() != null && this.getCustomHolder().texture != null && !this.getCustomHolder().texture
                .getNamespace().equals(entry.texture().getNamespace()))
            return this.getCustomHolder().texture;

        if (!texture.getPath().contains("entity/"))
        {
            final int index = this.getSexe() == IPokemob.FEMALE && entry.textureDetails[1] != null ? 1 : 0;
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
                    tex[i] = new ResourceLocation(path);
                }
                this.texs.put(texture, tex);
            }
            final ResourceLocation[] tex = this.texs.get(texture);
            texture = tex[texIndex];
        }
        String texName = texture.toString();
        if (!texName.endsWith(".png")) texture = new ResourceLocation(texName = texName + ".png");
        if (this.isShiny())
        {
            if (!this.shinyTexs.containsKey(texture))
            {
                texName = texName.replace(".png", "_s.png");
                final ResourceLocation modified = new ResourceLocation(texName);
                this.shinyTexs.put(texture, modified);
                return modified;
            }
            else texture = this.shinyTexs.get(texture);
        }
        return texture;
    }
}