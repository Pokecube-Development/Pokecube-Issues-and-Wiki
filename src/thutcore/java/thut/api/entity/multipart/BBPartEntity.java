package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
    private float h1, w1;

    public BBPartEntity(E parent, Part part)
    {
        super(parent, part.getName());
        this.part = part;
        // We all start at origin, our pose translates us accordingly.
        this.r0 = new Vector3f();
        m = new Matrix4f();
        m0 = new Matrix4f();
        m0.identity();
        this.height = part.meshMax.y - part.meshMin.y;
        this.width = Math.max(part.meshMax.x - part.meshMin.x, part.meshMax.z - part.meshMin.z);
        h1 = this.height;
        w1 = this.width;
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn)
    {
        return this.dimensions.scale(h1/this.height, w1/this.width);
    }

    @Override
    public void update(Matrix4f transform, Vec3 dr)
    {
        var poseInfo = part.getRenderPose();
        m.identity();
        m.mul(transform);
        m0.set(poseInfo.pose());
        m0.rotateZ(90);
        m.mul(m0);
        r.set(r0, 1);

        Vector4f m1 = new Vector4f(part.meshMin,1);
        Vector4f m2 = new Vector4f(part.meshMax,1);

        r.mul(m);
        m1.mul(m);
        m2.mul(m);

        h1 = m2.y - m1.y;
        w1 = Math.max(m2.x - m1.x, m2.z - m1.z);

        this.setPos(r.x, r.y, r.z);
        if(!this.level().isClientSide()) System.out.println(part.getName()+" "+r);

        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
    }
}
