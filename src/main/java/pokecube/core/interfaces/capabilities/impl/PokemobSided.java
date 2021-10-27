package pokecube.core.interfaces.capabilities.impl;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public abstract class PokemobSided extends PokemobBase
{
    protected FormeHolder forme_holder = null;

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

        if (this.getCustomHolder() != null && this.getCustomHolder().texture != null) texName = this
                .getCustomHolder().texture.toString();
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

        if (this.getCustomHolder() != null && this.getCustomHolder().texture != null && !this.getCustomHolder().texture
                .getNamespace().equals(this.getPokedexEntry().texture().getNamespace())) return this
                        .getCustomHolder().texture;

        if (!texture.getPath().contains("entity/"))
        {
            final int index = this.getSexe() == IPokemob.FEMALE && this.entry.textureDetails[1] != null ? 1 : 0;
            final int effects = this.entry.textureDetails[index].length;
            final int texIndex = this.getEntity().tickCount % effects * 3 / effects;
            if (!this.texs.containsKey(texture))
            {
                final int maxNum = this.entry.textureDetails.length * this.entry.textureDetails[0].length;
                final ResourceLocation[] tex = new ResourceLocation[maxNum];
                String base = this.getPokedexEntry().texturePath + texture.getPath();
                if (base.endsWith(".png")) base = base.substring(0, base.length() - 4);
                for (int i = 0; i < maxNum; i++)
                {
                    final String path = base + this.entry.textureDetails[index][texIndex] + ".png";
                    tex[i] = new ResourceLocation(path);
                }
                this.texs.put(texture, tex);
            }
            final ResourceLocation[] tex = this.texs.get(texture);
            texture = tex[texIndex];
        }
        if (this.isShiny()) if (!this.shinyTexs.containsKey(texture))
        {
            String texName = texture.toString();
            texName = texName.replace(".png", "s.png");
            final ResourceLocation modified = new ResourceLocation(texName);
            this.shinyTexs.put(texture, modified);
            return modified;
        }
        else texture = this.shinyTexs.get(texture);
        return texture;
    }

    @Override
    public void setCustomHolder(FormeHolder holder)
    {
        if (holder != null) holder = Database.formeHolders.getOrDefault(holder.key, holder);
        this.forme_holder = holder;
    }

    @Override
    public FormeHolder getCustomHolder()
    {
        if (this.forme_holder == null) return this.getPokedexEntry().getModel(this.getSexe());
        if (Database.formeToEntry.getOrDefault(this.forme_holder.key, this.getPokedexEntry()) != this.getPokedexEntry())
            return this.getPokedexEntry().getModel(this.getSexe());
        return this.forme_holder;
    }
}