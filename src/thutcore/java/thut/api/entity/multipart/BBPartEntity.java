package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BBPartEntity<E extends Entity> extends GenericPartEntity<E>
{
    public BBPartEntity(E parent, String id)
    {
        super(parent, id);
    }

    @Override
    public void update(Matrix4f transform, Vec3 dr)
    {

    }
}
