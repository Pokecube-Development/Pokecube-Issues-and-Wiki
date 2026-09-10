package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.parts.Part;
import thut.lib.AxisAngles;

public class BBPartEntity<E extends Entity> extends GenericPartEntity<E>
{
    public static interface Factory<T extends BBPartEntity<E>, E extends Entity>
    {
        T create(E parent, Part part, BBModel model);
    }

    public final Vector4f r1 = new Vector4f();
    public final Vector3f dr = new Vector3f(), min = new Vector3f(), max = new Vector3f(), mid = new Vector3f(), shift = new Vector3f();
    private final Matrix4f m, m0;
    public final Part part;
    public final BBModel model;
    private float h1;
    private float w1;
    private boolean wasHidden = false;

    public AABB walkBox = null;

    public BBPartEntity(E parent, Part part, BBModel model)
    {
        super(parent, part.getName());
        this.part = part;
        this.model = model;

        m = new Matrix4f();
        m0 = new Matrix4f();
        m0.identity();

        float s0 = ((Part) model.root_part).basePreScale.x;
        shift.set(((Part) model.root_part).basePreTrans);
        min.set(part.meshMin);
        max.set(part.meshMax);
        mid.set(part.meshMid);

        if (parent instanceof LivingEntity e)
        {
            s0 *= e.getScale();
        }

        if (part.getName().equals("head"))
        {
            this.ride_point = new Vector3f();
        }

        this.height = max.z - min.z;
        this.width = Math.max(max.x - min.x, max.y - min.y);
        this.height *= s0;
        this.width *= s0;
        w1 = h1 = 1;
        this.dimensions = EntityDimensions.fixed(width, height);
    }

    @Override
    public AABB makeBoundingBox()
    {
        if (walkBox != null) return walkBox;
        else return super.makeBoundingBox();
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn)
    {
        return EntityDimensions.fixed(w1*width, h1*height);
    }

    @Override
    public void update(Matrix4f transform)
    {
        var poseInfo = part.getRenderPose();
        m.identity();
        m.mul(transform);
        m0.set(poseInfo.pose());
        m.rotate(AxisAngles.XN.rotationDegrees(90));

        m.mul(m0);
        m0.translate(shift);

        Vector4f r2 = new Vector4f(mid.x, max.y, mid.z, 1);

        r.set(mid.x, mid.y, min.z, 1);

        r2.mul(m);
        r.mul(m);

        boolean isHidden = (part.isHidden()) && part.getParent() != null;
        if (isHidden)
        {
            h1 = 0.01f;
            w1 = 0.01f;
            if(!wasHidden) refreshDimensions();
            wasHidden = true;
        }
        else if (wasHidden)
        {
            w1 = h1 = 1;
            refreshDimensions();
            wasHidden = false;
        }
        this.r1.set(r);
        if (this.ride_point != null)
        {
            r2.sub(r);
            this.ride_point.set(r2.x, r2.y, r2.z);
        }
    }

}
