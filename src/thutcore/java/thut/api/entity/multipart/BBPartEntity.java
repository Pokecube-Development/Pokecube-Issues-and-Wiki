package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import thut.core.client.render.model.parts.Part;

public class BBPartEntity<E extends Entity> extends GenericPartEntity<E>
{
    public static interface Factory<T extends BBPartEntity<E>, E extends Entity>
    {
        T create(E parent, Part part);
    }

    private final Vector4f r = new Vector4f();
    private final Matrix4f m, m0;
    public final Part part;

    public BBPartEntity(E parent, Part part)
    {
        super(parent, part.getName());
        this.part = part;
        m = new Matrix4f();
        m0 = new Matrix4f();
        m0.identity();
    }

    @Override
    public void update(Matrix4f transform, Vec3 dr)
    {
        var poseInfo = part.getRenderPose();
        m.set(m0);
        m.mul(transform);
        m.mul(poseInfo.pose());
        r.set(r0, 1);
        this.setPos(r.x, r.y, r.z);
        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
    }
}
