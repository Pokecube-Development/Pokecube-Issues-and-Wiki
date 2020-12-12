package thut.core.client.render.model.parts;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
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

    static IRenderTypeBuffer.Impl lastImpl = null;

    private static IVertexBuilder getOrAdd(final Material mat, final RenderType type, final IRenderTypeBuffer buffer)
    {
        final Impl impl = (Impl) buffer;
        IVertexBuilder buff;

        final boolean transp = mat.alpha < 1 || mat.transluscent;
        // This means we didn't actually make one for this texture!
        if (transp)
        {
            buff = impl.fixedBuffers.get(type);

            // No need to add it to the fixed buffers,
            // just call this to get it directly.

            // If we don't do this, then it might make an empty buffer anyway,
            // which can then throw errors about not filled verticies.
            if (buff != null) return impl.getBuffer(type);

            final BufferBuilder builder = new BufferBuilder(256);

            final Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> fixed = (Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder>) impl.fixedBuffers;

            final List<RenderType> keys = Lists.newArrayList(fixed.keySet());

            final Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> after = new Object2ObjectLinkedOpenHashMap<>();
            boolean found = false;
            for (final RenderType type2 : keys)
            {
                found = found || type2 == RenderType.getWaterMask();
                if (found) after.put(type2, fixed.remove(type2));
            }
            // Add a new bufferbuilder to the maps.
            fixed.put(type, builder);
            after.forEach((k, v) -> fixed.put(k, v));

            // This starts the buffer, and registers it to the Impl.
            builder.begin(type.getDrawMode(), type.getVertexFormat());
            impl.startedBuffers.add(builder);
            buff = builder;
        }
        else buff = impl.getBuffer(type);
        return buff;
    }

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
        this.makeRenderType(texture);
        if (buffer instanceof Impl) Material.lastImpl = (Impl) buffer;
    }

    private RenderType makeRenderType(final ResourceLocation tex)
    {
        this.tex = tex;
        if (this.types.containsKey(tex)) return this.types.get(tex);
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
            builder.writeMask(new WriteMaskState(true, false));
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
        if (this.tex == null || Material.lastImpl == null) return buffer;
        final RenderType type = this.makeRenderType(this.tex);
        return Material.getOrAdd(this, type, Material.lastImpl);
    }

    public void postRender(final MatrixStack mat)
    {
    }
}
