package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderType;

public class Utils
{
    public static IVertexBuilder makeBuilder(RenderType type, final IRenderTypeBuffer buffer)
    {
        final Impl impl = (Impl) buffer;
        IVertexBuilder buff = impl.getBuffer(type);
        // This means we didn't actually make one for this texture!
        if (buff == impl.defaultBuffer)
        {
//            final BufferBuilder builder = new BufferBuilder(256);
//            // Add a new bufferbuilder to the maps.
//            impl.buffersByType.put(type, builder);
//            // This starts the buffer, and registers it to the Impl.
//            builder.begin(type.getGlMode(), type.getVertexFormat());
//            impl.startedBuffers.add(builder);
//            buff = builder;
        }
        return buff;
    }
}
