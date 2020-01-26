package thut.core.client.render.tabula.model;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;

public class TblModelBox extends ModelBox
{

    public TblModelBox(final RendererModel renderer, final int texU, final int texV, final float x, final float y,
            final float z, final int dx, final int dy, final int dz, final float delta)
    {
        super(renderer, texU, texV, x, y, z, dx, dy, dz, delta);
    }

    @Override
    public void render(final BufferBuilder renderer, final float scale)
    {
        super.render(renderer, scale);
    }
}
