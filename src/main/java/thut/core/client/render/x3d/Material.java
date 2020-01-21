package thut.core.client.render.x3d;

import java.nio.FloatBuffer;

import thut.api.maths.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;

import net.minecraft.util.ResourceLocation;

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

    boolean depth;

    boolean colour_mat;
    boolean light;
    boolean old_cull;
    float[] oldLight = { -1, -1 };

    public Material(String name)
    {
        this.name = name;
    }

    public Material(String name, String texture, Vector3f diffuse, Vector3f specular, Vector3f emissive, float ambient,
            float shiny, float transparent)
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

    private FloatBuffer makeBuffer(float value)
    {
        final FloatBuffer ret = BufferUtils.createFloatBuffer(1 + 4);
        ret.put(new float[] { value });
        return ret;
    }

    private FloatBuffer makeBuffer(Vector3f vector)
    {
        final FloatBuffer ret = BufferUtils.createFloatBuffer(3 + 4);
        ret.put(new float[] { vector.x, vector.y, vector.z });
        return ret;
    }

    public void postRender()
    {
        if (this.depth && this.transparency > 0) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else if (!this.depth) GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (!this.colour_mat) GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        if (!this.light) GL11.glDisable(GL11.GL_LIGHTING);
        else GL11.glEnable(GL11.GL_LIGHTING);
        if (this.emissiveMagnitude != 0 && this.oldLight[0] != -1 && this.oldLight[1] != -1) GLX.glMultiTexCoord2f(
                GLX.GL_TEXTURE1, this.oldLight[0], this.oldLight[1]);
    }

    public void preRender()
    {
        this.depth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        this.colour_mat = GL11.glGetBoolean(GL11.GL_COLOR_MATERIAL);
        this.light = GL11.glGetBoolean(GL11.GL_LIGHTING);
        this.old_cull = GL11.glGetBoolean(GL11.GL_CULL_FACE);
        if (this.transparency > 0) GL11.glDisable(GL11.GL_DEPTH_TEST);
        else GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT, this.makeBuffer(this.ambientIntensity));
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, this.makeBuffer(this.diffuseColor));
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, this.makeBuffer(this.specularColor));
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SHININESS, this.makeBuffer(this.shininess));
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_EMISSION, this.makeBuffer(this.emissiveColor));

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        if (this.emissiveMagnitude != 0)
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            this.oldLight[0] = GLX.lastBrightnessX;
            this.oldLight[1] = GLX.lastBrightnessY;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240 * this.emissiveMagnitude, GLX.lastBrightnessY);
        }
        else GL11.glEnable(GL11.GL_LIGHTING);
    }
}
