package thut.core.client.render.model.parts;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import thut.core.client.render.model.parts.textures.BaseTexture;

public class Material implements Comparable<Material>
{
    static final RenderType WATER_MASK = RenderType.create("water_mask_", DefaultVertexFormat.POSITION,
            VertexFormat.Mode.TRIANGLES, 256,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_WATER_MASK_SHADER)
                    .setTextureState(RenderStateShard.NO_TEXTURE).setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                    .createCompositeState(false));

    public static final Map<String, RenderStateShard.ShaderStateShard> SHADERS = Maps.newHashMap();

    public static boolean HAS_IRIS;
    public static int SHADOW_ARGB;
    static
    {
        SHADERS.put("alpha_shader", RenderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER);
        SHADERS.put("eyes_shader", RenderStateShard.RENDERTYPE_EYES_SHADER);
        SHADERS.put("swirl_shader", RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER);

        HAS_IRIS = ModList.get().isLoaded("iris");
        SHADOW_ARGB = FastColor.ARGB32.color(0,0,0,0);
    }

    static long renderTick = 0;

    public static void startRender()
    {
        renderTick++;
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
    public Mode vertexMode = null;

    private long lastTick = -1;

    public String shader = "";

    public RenderTypeProvider renderType = RenderTypeProvider.NORMAL;

    MultiBufferSource bufferSource = null;

    final Map<String, RenderType> types = new Object2ObjectOpenHashMap<>(2);

    public Material(final String name)
    {
        this.name = name;
        this.render_name = "thutcore:mat_" + name + "_";
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
        this.tex = texture;
        bufferSource = buffer;
    }

    public RenderType makeRenderType(final ResourceLocation tex, Mode mode)
    {
        return renderType.makeRenderType(this, tex, mode);
    }

    public VertexConsumer preRender(final VertexConsumer buffer)
    {
        return preRender(buffer, Mode.TRIANGLES);
    }

    public VertexConsumer preRender(final VertexConsumer buffer, Mode mode)
    {
        isShadow = false;
        if(HAS_IRIS)
        {
            var s = RenderSystem.getShader();
            isShadow= s != null && s.getName().startsWith("shadow_terrain");
            if(isShadow) return buffer;
        }
        if (bufferSource == null) bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        if (this.tex == null || bufferSource == null) return buffer;
        // Incase someone swaps models faster than a tick can run?
        if (lastTick == renderTick && renderMode == mode && LAST_BUILDER == this) return renderCache;
        this.vertexMode = renderMode = mode;
        this.lastTick = renderTick;
        LAST_BUILDER = this;
        final RenderType type = this.makeRenderType(this.tex, mode);
        return renderCache = bufferSource.getBuffer(type);
    }

    private static Material LAST_BUILDER;

    private VertexConsumer renderCache;
    private Mode renderMode;

    public BaseTexture getTexture()
    {
        return texture_object;
    }
}
