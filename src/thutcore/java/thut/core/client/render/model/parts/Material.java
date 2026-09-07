package thut.core.client.render.model.parts;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import thut.core.client.render.model.parts.textures.BaseTexture;

import java.util.function.Function;

public class Material implements Comparable<Material>
{
    public static Function<String, Material> MATERIAL_FACTORY = Material::new;

    public static Material create(String name)
    {
        return MATERIAL_FACTORY.apply(name);
    }

    public String name;
    public String render_name;

    public String texture;
    public Vector3f diffuseColor;
    public Vector3f specularColor;
    public Vector3f emissiveColor;

    public ResourceLocation tex;

    public float emissiveMagnitude;
    public float ambientIntensity;
    public float shininess;
    public float alpha = 1;
    public boolean transluscent = false;
    public boolean cull = false;
    public boolean flat = true;
    public boolean edited = false;
    public boolean isShadow = false;
    // Generally you should use Mesh.rgbabro instead of this one.
    // this is here for possible custom mesh implementations, like SMD
    public int[] rgbabro = new int[6];

    public float expectedTexH = -1;
    public float expectedTexW = -1;

    public BaseTexture texture_object;

    public String shader = "";

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name + "_";
    }

    @SuppressWarnings("unchecked")
    public <T extends Material> T init(final String texture, final Vector3f diffuse, final Vector3f specular,
            final Vector3f emissive, final float ambient, final float shiny){
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.emissiveMagnitude = Math.min(emissive.x / 0.8f, 1);
        this.ambientIntensity = ambient;
        this.shininess = shiny;
        return (T) this;
    }

    @Override
    public String toString()
    {
        return "Material{" + "name='" + name + '\'' + ", emissiveColor=" + emissiveColor + ", transluscent=" + transluscent + ", alpha="
                + alpha + ", shininess=" + shininess + ", ambientIntensity=" + ambientIntensity + ", emissiveMagnitude="
                + emissiveMagnitude + ", flat=" + flat + ", cull=" + cull + ", shader='" + shader + '\'' + super.toString() + '}';
    }

    /**
     * This is set so that sorting a list will result in
     * the appropriate material order for rendering.
     */
    @Override
    public int compareTo(@NotNull Material o)
    {
        if(this.edited != o.edited) return this.edited ? -1: 1;
        boolean transp1 = this.transluscent || this.alpha < 1;
        boolean transp2 = o.transluscent || o.alpha < 1;
        if (transp1 != transp2) return transp1 ? 1 : -1;
        if (diffuseColor!=null && o.diffuseColor==null) return +1;
        if (o.diffuseColor!=null && diffuseColor==null) return -1;
        if (specularColor!=null && o.specularColor==null) return +1;
        if (o.specularColor!=null && specularColor==null) return -1;
        if (emissiveColor!=null && o.emissiveColor==null) return +1;
        if (o.emissiveColor!=null && emissiveColor==null) return -1;
        boolean emiss1 = this.emissiveMagnitude > 0;
        boolean emiss2 = o.emissiveMagnitude > 0;
        if (emiss1 != emiss2) return emiss1 ? 1 : -1;
        return name.compareTo(o.name);
    }

    @OnlyIn(Dist.CLIENT)
    public com.mojang.blaze3d.vertex.VertexConsumer preRender(final com.mojang.blaze3d.vertex.VertexConsumer buffer)
    {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void makeVertexBuilder(final ResourceLocation texture,
            final net.minecraft.client.renderer.MultiBufferSource buffer)
    {}
}
