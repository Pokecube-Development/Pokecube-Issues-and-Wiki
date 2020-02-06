package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;

public class Material
{
    public final String  name;
    private final String render_name;
    public String        texture;
    public Vector3f      diffuseColor;
    public Vector3f      specularColor;
    public Vector3f      emissiveColor;
    public float         emissiveMagnitude;
    public float         ambientIntensity;
    public float         shininess;
    public float         transparency;

    IVertexBuilder       override_buff = null;

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name;
    }

    public Material(final String name, final String texture, final Vector3f diffuse, final Vector3f specular,
            final Vector3f emissive, final float ambient, final float shiny, final float transparent)
    {
        this(name);
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.ambientIntensity = ambient;
        this.shininess = shiny;
        this.transparency = transparent;
        this.emissiveMagnitude = Math.min(1, (float) (this.emissiveColor.length() / Math.sqrt(3)) / 0.8f);
    }

    public void makeVertexBuilder(ResourceLocation texture, IRenderTypeBuffer buffer)
    {
        override_buff = buffer.getBuffer(makeRenderType(texture));
    }

    private RenderType makeRenderType(ResourceLocation tex)
    {
        RenderType.State rendertype$state = RenderType.State.builder()
                .texture(new RenderState.TextureState(tex, true, false))
                .transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                }, () ->
                {
                    RenderSystem.disableBlend();
                })).diffuseLighting(new RenderState.DiffuseLightingState(true))
                .alpha(new RenderState.AlphaState(0.003921569F)).cull(new RenderState.CullState(false))
                .lightmap(new RenderState.LightmapState(true)).overlay(new RenderState.OverlayState(true)).build(false);
        // TODO see where we need to properly apply the material texture.
        return RenderType.get(render_name, DefaultVertexFormats.ITEM, GL11.GL_TRIANGLES, 256, true, false,
                rendertype$state);
    }

    public IVertexBuilder preRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        return override_buff == null ? buffer : override_buff;
    }
}
