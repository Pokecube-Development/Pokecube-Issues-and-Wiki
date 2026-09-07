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
import thut.core.client.render.model.parts.textures.BaseTexture;

public class MaterialRenderable extends Material
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

    private long lastTick = -1;

    public static void startRender()
    {
        renderTick++;
    }

    public RenderTypeProvider renderType = RenderTypeProvider.NORMAL;

    public VertexFormat.Mode vertexMode = null;
    MultiBufferSource bufferSource = null;

    final Map<String, RenderType> types = new Object2ObjectOpenHashMap<>(2);

    public MaterialRenderable(final String name)
    {
        super(name);
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

    @Override
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
        if (this.tex == null) return buffer;
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
