package thut.core.client.render.tabula.model.modelbase;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author BobMowzie, gegy1000
 * @since 0.1.0
 */
@OnlyIn(Dist.CLIENT)
public class TabulaModelBase<T extends Entity> extends EntityModel<T>
{
    /** Store every MowzieRendererModel in this array */
    protected List<TabulaRenderer> parts;

    public void addPart(TabulaRenderer part)
    {
        if (this.parts == null) this.parts = new ArrayList<>();

        this.parts.add(part);
    }

}
