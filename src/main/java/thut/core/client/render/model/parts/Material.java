package thut.core.client.render.model.parts;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;

public class Material
{
    public static final RenderStateShard.TransparencyStateShard DEFAULTTRANSP = new RenderStateShard.TransparencyStateShard(
            "material_transparency", () ->
            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }, () -> {
                RenderSystem.disableBlend();
            });

    private static final RenderType WATER_MASK = RenderType.create("water_mask_", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.TRIANGLES, 256,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_WATER_MASK_SHADER)
                    .setTextureState(RenderStateShard.NO_TEXTURE).setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .createCompositeState(false));

    public static final DepthTestStateShard LESSTHAN = new DepthTestStateShard("<", 513);

    public final String name;
    private final String render_name;

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
    public boolean cull = true;
    public boolean flat = true;

    static MultiBufferSource.BufferSource lastImpl = null;

    private final Map<ResourceLocation, RenderType> types = Maps.newHashMap();

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name + "_";
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

    public void makeVertexBuilder(final ResourceLocation texture, final MultiBufferSource buffer)
    {
        this.makeRenderType(texture);
        if (buffer instanceof BufferSource) Material.lastImpl = (BufferSource) buffer;
    }

    private RenderType makeRenderType(final ResourceLocation tex)
    {
        this.tex = tex;
        if (this.types.containsKey(tex)) return this.types.get(tex);
        RenderType type = null;
        if (this.render_name.contains("water_mask_"))
        {
            type = WATER_MASK;
            this.cull = false;
            this.types.put(tex, type);
            return type;
        }

        final String id = this.render_name + tex;
        // if (this.emissiveMagnitude == 0)
        {
            final RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder();
            // No blur, No MipMap
            builder.setTextureState(new RenderStateShard.TextureStateShard(tex, false, false));

            builder.setTransparencyState(Material.DEFAULTTRANSP);

            builder.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER);

            // These are needed in general for world lighting
            builder.setLightmapState(RenderStateShard.LIGHTMAP);
            builder.setOverlayState(RenderStateShard.OVERLAY);

            final boolean transp = this.alpha < 1 || this.transluscent;
            if (transp)
            {
                // These act like masking
                builder.setWriteMaskState(RenderStateShard.COLOR_WRITE);
                builder.setDepthTestState(Material.LESSTHAN);
            }
            // Otheerwise disable culling entirely
            else builder.setCullState(RenderStateShard.NO_CULL);

            final RenderType.CompositeState rendertype$state = builder.createCompositeState(true);
            type = RenderType.create(id, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false,
                    rendertype$state);
        }

        this.types.put(tex, type);
        return type;
    }

    public VertexConsumer preRender(final PoseStack mat, final VertexConsumer buffer)
    {
        if (this.tex == null || Material.lastImpl == null) return buffer;
        final RenderType type = this.makeRenderType(this.tex);
        VertexConsumer newBuffer = Material.lastImpl.getBuffer(type);
        return newBuffer;
    }
}
