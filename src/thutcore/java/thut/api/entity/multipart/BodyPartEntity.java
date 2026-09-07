package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BodyPartEntity<E extends Entity> extends GenericPartEntity<E>
{
    public Vector3f r0;

    public float width;
    public float height;

    public Vector4f r = new Vector4f();

    public BodyPartEntity(E parent, final float width, final float height, final float x, final float y,
            final float z, final String id)
    {
        super(parent, id);

        this.width = width;
        this.height = height;

        this.dimensions = EntityDimensions.scalable(width, height);

        this.r0 = new Vector3f(x + width / 2, y, z + width / 2);
    }

    public void update(Matrix4f transform, Vec3 dr)
    {
        r.set(r0, 1);
        r.mul(transform);
        this.setPos(r.x, r.y, r.z);
        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
    }
}
