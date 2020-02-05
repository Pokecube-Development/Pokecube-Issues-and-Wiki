package thut.core.client.render.model.parts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;

public class Material
{
    public final String     name;
    public String           texture;
    public ResourceLocation texresource;
    public Vector3f         diffuseColor;
    public Vector3f         specularColor;
    public Vector3f         emissiveColor;
    public float            emissiveMagnitude;
    public float            ambientIntensity;
    public float            shininess;
    public float            transparency;

    boolean                 depth;

    boolean                 colour_mat;
    boolean                 light;
    boolean                 old_cull;
    float[]                 oldLight = { -1, -1 };

    public Material(final String name)
    {
        this.name = name;
    }

    public Material(final String name, final String texture, final Vector3f diffuse, final Vector3f specular,
            final Vector3f emissive, final float ambient, final float shiny, final float transparent)
    {
        this.name = name;
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.ambientIntensity = ambient;
        this.shininess = shiny;
        this.transparency = transparent;
        this.emissiveMagnitude = Math.min(1, (float) (this.emissiveColor.length() / Math.sqrt(3)) / 0.8f);
    }

    public IVertexBuilder preRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        // TODO we should apply our things here and decide on the buffer to
        // use...
        return buffer;
    }
}
