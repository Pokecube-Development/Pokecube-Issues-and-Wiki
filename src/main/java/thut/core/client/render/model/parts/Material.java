package thut.core.client.render.model.parts;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.ShadeModelState;
import net.minecraft.client.renderer.RenderState.WriteMaskState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;

public class Material
{
    public final String     name;
    private final String    render_name;
    public String           texture;
    public Vector3f         diffuseColor;
    public Vector3f         specularColor;
    public Vector3f         emissiveColor;
    public ResourceLocation tex;
    public float            emissiveMagnitude;
    public float            ambientIntensity;
    public float            shininess;
    public float            alpha         = 1;
    public boolean          transluscent  = false;
    public boolean          flat          = true;

    IVertexBuilder          override_buff = null;
    IRenderTypeBuffer       typeBuff      = null;
    RenderType              type          = null;

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name;
    }

    public Material(final String name, final String texture, final Vector3f diffuse, final Vector3f specular,
            final Vector3f emissive, final float ambient, final float shiny)
    {
        this(name);
        this.texture = texture;
        this.diffuseColor = diffuse;
        this.specularColor = specular;
        this.emissiveColor = emissive;
        this.emissiveMagnitude = Math.min(emissive.x / 0.8f, 1);
        this.ambientIntensity = ambient;
        this.shininess = shiny;
    }

    public void makeVertexBuilder(final ResourceLocation texture, final IRenderTypeBuffer buffer)
    {
        this.type = this.makeRenderType(texture);
        if (buffer instanceof Impl)
        {
            final Impl impl = (Impl) buffer;
            IVertexBuilder buff = impl.getBuffer(this.type);
            // This means we didn't actually make one for this texture!
            if (buff == impl.defaultBuffer)
            {
                final BufferBuilder builder = new BufferBuilder(256);
                // Add a new bufferbuilder to the maps.
                impl.buffersByType.put(this.type, builder);
                // This starts the buffer, and registers it to the Impl.
                builder.begin(this.type.getGlMode(), this.type.getVertexFormat());
                impl.startedBuffers.add(builder);
                buff = builder;
            }
            this.override_buff = buff;
            this.typeBuff = buffer;
        }
    }

    private RenderType makeRenderType(final ResourceLocation tex)
    {
        this.tex = tex;
        RenderType.State.Builder builder = RenderType.State.builder();
        builder.texture(new RenderState.TextureState(tex, false, false));
        builder.transparency(new RenderState.TransparencyState("material_transparency", () ->
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }, () ->
        {
            RenderSystem.disableBlend();
        }));
        if (emissiveMagnitude == 0) builder.diffuseLighting(new RenderState.DiffuseLightingState(true));
        builder.alpha(new RenderState.AlphaState(0.003921569F));
        builder.lightmap(new RenderState.LightmapState(true));
        builder.overlay(new RenderState.OverlayState(true));
        boolean transp = (alpha < 1 || transluscent);
        if (transp) builder.writeMask(new WriteMaskState(true, true));
        if (!this.flat) builder.shadeModel(new ShadeModelState(true));

        final RenderType.State rendertype$state = builder.build(true);

        final String id = this.render_name + tex;
        final RenderType type = RenderType.get(id, DefaultVertexFormats.ITEM, GL11.GL_TRIANGLES, 256, true, false,
                rendertype$state);
        return type;
    }

    public IVertexBuilder preRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        return this.override_buff == null ? buffer : this.override_buff;
    }

    public void postRender(final MatrixStack mat)
    {
        this.override_buff = null;
        this.typeBuff = null;
    }
}
