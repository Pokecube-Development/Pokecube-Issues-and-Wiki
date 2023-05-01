package thut.core.client.render.model.parts;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import thut.api.maths.vecmath.Vec3f;

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

    static final RenderType WATER_MASK = RenderType.create("water_mask_", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.TRIANGLES, 256,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_WATER_MASK_SHADER)
                    .setTextureState(RenderStateShard.NO_TEXTURE).setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .createCompositeState(false));

    public static final Map<String, RenderStateShard.ShaderStateShard> SHADERS = Maps.newHashMap();

    static
    {
        SHADERS.put("alpha_shader", RenderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER);
        SHADERS.put("eyes_shader", RenderStateShard.RENDERTYPE_EYES_SHADER);
        SHADERS.put("swirl_shader", RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER);
    }

    public static final DepthTestStateShard LESSTHAN = new DepthTestStateShard("<", 513);

    public final String name;
    final String render_name;

    public String texture;
    public Vec3f diffuseColor;
    public Vec3f specularColor;
    public Vec3f emissiveColor;

    public ResourceLocation tex;

    public float emissiveMagnitude;
    public float ambientIntensity;
    public float shininess;
    public float alpha = 1;
    public boolean transluscent = false;
    public boolean cull = false;
    public boolean flat = true;
    public int[] rgbabro = new int[6];

    public String shader = "";

    public RenderTypeProvider renderType = RenderTypeProvider.NORMAL;

    static MultiBufferSource.BufferSource lastImpl = null;

    final Map<ResourceLocation, RenderType> types = new Object2ObjectOpenHashMap<>();

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name + "_";
    }

    public Material(final String name, final String texture, final Vec3f diffuse, final Vec3f specular,
            final Vec3f emissive, final float ambient, final float shiny)
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
        makeVertexBuilder(texture, buffer, Mode.TRIANGLES);
    }

    public void makeVertexBuilder(final ResourceLocation texture, final MultiBufferSource buffer, Mode mode)
    {
        this.makeRenderType(texture, mode);
        if (buffer instanceof BufferSource) Material.lastImpl = (BufferSource) buffer;
    }

    private RenderType makeRenderType(final ResourceLocation tex, Mode mode)
    {
        RenderType type = renderType.makeRenderType(this, tex, mode);
        return type;
    }

    public VertexConsumer preRender(final PoseStack mat, final VertexConsumer buffer)
    {
        return preRender(mat, buffer, Mode.TRIANGLES);
    }

    public VertexConsumer preRender(final PoseStack mat, final VertexConsumer buffer, Mode mode)
    {
        if (Material.lastImpl == null) Material.lastImpl = Minecraft.getInstance().renderBuffers().bufferSource();
        if (this.tex == null || Material.lastImpl == null) return buffer;
        final RenderType type = this.makeRenderType(this.tex, mode);
        VertexConsumer newBuffer = Material.lastImpl.getBuffer(type);
        return newBuffer;
    }
}
