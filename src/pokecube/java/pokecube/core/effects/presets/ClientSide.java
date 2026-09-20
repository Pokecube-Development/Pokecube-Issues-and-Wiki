package pokecube.core.effects.presets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClientSide
{
    @OnlyIn(Dist.CLIENT)
    private static final RenderStateShard.TransparencyStateShard TRANSP = new RenderStateShard.TransparencyStateShard(
            "lightning_transparency", () ->
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static final RenderType RENDER_TYPE = RenderType.create("thrown_particle",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLES, 1536, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_TEXT_BACKGROUND_SHADER)
                    .setTextureState(RenderType.NO_TEXTURE).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderType.LIGHTMAP).createCompositeState(false));

    public static final RenderType EVO_RAY_EFFECT = RenderType.create("pokemob:evo_effect", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setTransparencyState(TRANSP).createCompositeState(false));
}
