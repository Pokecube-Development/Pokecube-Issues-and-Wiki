package thut.core.client.render.model.parts;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.CullState;
import net.minecraft.client.renderer.RenderState.DepthTestState;
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
    public float            alpha        = 1;
    public boolean          transluscent = false;
    public boolean          flat         = true;

    private int fix_counter = 0;

    IVertexBuilder    override_buff = null;
    IRenderTypeBuffer typeBuff      = null;

    private final Map<ResourceLocation, RenderType> types = Maps.newHashMap();

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
        final RenderType type = this.makeRenderType(texture);
        if (buffer instanceof Impl)
        {
            final Impl impl = (Impl) buffer;
            IVertexBuilder buff = impl.getBuffer(type);
            // This means we didn't actually make one for this texture!
            if (buff == impl.buffer)
            {
                final BufferBuilder builder = new BufferBuilder(256);
                // Add a new bufferbuilder to the maps.
                impl.fixedBuffers.put(type, builder);
                // This starts the buffer, and registers it to the Impl.
                builder.begin(type.getDrawMode(), type.getVertexFormat());
                impl.startedBuffers.add(builder);
                buff = builder;
            }
            this.override_buff = buff;
            this.typeBuff = buffer;
        }
    }

    private RenderType makeRenderType(final ResourceLocation tex)
    {
        if (this.types.containsKey(tex) && this.fix_counter++ > 10) return this.types.get(tex);
        this.tex = tex;
        final RenderType.State.Builder builder = RenderType.State.getBuilder();
        builder.texture(new RenderState.TextureState(tex, false, false));
        builder.transparency(new RenderState.TransparencyState("material_transparency", () ->
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }, () ->
        {
            RenderSystem.disableBlend();
        }));
        if (this.emissiveMagnitude == 0) builder.diffuseLighting(new RenderState.DiffuseLightingState(true));
        builder.alpha(new RenderState.AlphaState(0.003921569F));
        builder.lightmap(new RenderState.LightmapState(true));
        builder.overlay(new RenderState.OverlayState(true));
        final boolean transp = this.alpha < 1 || this.transluscent;
        if (transp)
        {
            if (this.fix_counter < 2) builder.writeMask(new WriteMaskState(true, true));
            else builder.writeMask(new WriteMaskState(true, false));
            builder.depthTest(new DepthTestState(513));
        }
        else builder.cull(new CullState(false));
        if (!this.flat) builder.shadeModel(new ShadeModelState(true));
        final RenderType.State rendertype$state = builder.build(true);
        final String id = this.render_name + tex;
        final RenderType type = RenderType.makeType(id, DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256, true,
                false, rendertype$state);
        this.types.put(tex, type);
        return type;
    }

    public IVertexBuilder preRender(final MatrixStack mat, final IVertexBuilder buffer)
    {
        final IVertexBuilder buff = this.override_buff == null ? buffer : this.override_buff;
        if (buff instanceof BufferBuilder && this.tex != null)
        {
            final BufferBuilder builder = (BufferBuilder) buff;
            if (!builder.isDrawing())
            {
                final RenderType type = this.makeRenderType(this.tex);
                builder.begin(type.getDrawMode(), type.getVertexFormat());
            }
        }
        return buff;
    }

    public void postRender(final MatrixStack mat)
    {
    }
}
