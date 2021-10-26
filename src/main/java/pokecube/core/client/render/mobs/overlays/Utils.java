package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

public class Utils
{
    public static IVertexBuilder makeBuilder(final RenderType type, final IRenderTypeBuffer buffer)
    {
        return buffer.getBuffer(type);
    }
}
