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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;

public class Material extends RenderState
{
    protected static final RenderState.TransparencyState DEFAULTTRANSP = new RenderState.TransparencyState(
            "material_transparency", () ->
            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }, () ->
            {
                RenderSystem.disableBlend();
            });

    protected static final DepthTestState LESSTHAN = new DepthTestState("<", 513);

    public final String  name;
    private final String render_name;

    public String   texture;
    public Vector3f diffuseColor;
    public Vector3f specularColor;
    public Vector3f emissiveColor;

    public ResourceLocation tex;

    public float   emissiveMagnitude;
    public float   ambientIntensity;
    public float   shininess;
    public float   alpha        = 1;
    public boolean transluscent = false;
    public boolean flat         = true;

    static IRenderTypeBuffer.Impl lastImpl = null;

    private static IVertexBuilder getOrAdd(final Material mat, final RenderType type, final IRenderTypeBuffer buffer)
    {
        final Impl impl = (Impl) buffer;
        IVertexBuilder buff = impl.getBuffer(type);

        final boolean transp = mat.alpha < 1 || mat.transluscent;
        // This means we didn't actually make one for this texture!
        if (transp && buff == impl.buffer)
        {
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

        return buff;
    }

    private final Map<ResourceLocation, RenderType> types = Maps.newHashMap();

    public Material(final String name)
    {
        super(name, () ->
        {
        }, () ->
        {
        });
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
        if (this.types.containsKey(tex)) return this.types.get(tex);
        this.tex = tex;
        final RenderType.State.Builder builder = RenderType.State.getBuilder();
        // No blur, No MipMap
        builder.texture(new RenderState.TextureState(tex, false, false));

        builder.transparency(Material.DEFAULTTRANSP);

        // Some materials are "emissive", so for those, we don't do this.
        if (this.emissiveMagnitude == 0) builder.diffuseLighting(RenderState.DIFFUSE_LIGHTING_ENABLED);
        // Normal alpha
        builder.alpha(RenderState.DEFAULT_ALPHA);

        // These are needed in general for world lighting
        builder.lightmap(RenderState.LIGHTMAP_ENABLED);
        builder.overlay(RenderState.OVERLAY_ENABLED);

        final boolean transp = this.alpha < 1 || this.transluscent;
        if (transp)
        {
            // These act like masking
            builder.writeMask(RenderState.COLOR_WRITE);
            builder.depthTest(Material.LESSTHAN);
        }
        // Otheerwise disable culling entirely
        else builder.cull(RenderState.CULL_DISABLED);

        // Some models have extra bits that are not flat shaded, like coatings
        if (!this.flat) builder.shadeModel(RenderState.SHADE_ENABLED);
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
