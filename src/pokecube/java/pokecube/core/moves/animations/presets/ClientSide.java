package pokecube.core.moves.animations.presets;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class ClientSide
{
    public static final RenderType RENDER_TYPE = RenderType.create("thrown_particle",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLES, 1536, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_TEXT_BACKGROUND_SHADER)
                    .setTextureState(RenderType.NO_TEXTURE).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderType.LIGHTMAP).createCompositeState(false));
}
