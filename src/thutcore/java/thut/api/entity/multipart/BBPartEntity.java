package thut.api.entity.multipart;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
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

    public final Vector4f r = new Vector4f(), r1 = new Vector4f();
    public final Vector3f dr = new Vector3f();
    private final Matrix4f m, m0;
    public final Part part;
    public final BBModel model;
    private float h1;
    private float w1;
    private boolean wasHidden = false;
    public boolean leftBounds = false;

    public BBPartEntity(E parent, Part part, BBModel model)
    {
        super(parent, part.getName());
        this.part = part;
        this.model = model;

        m = new Matrix4f();
        m0 = new Matrix4f();
        m0.identity();

        float s0 = ((Part) model.root_part).basePreScale.x;

        this.height = part.meshMax.z - part.meshMin.z;
        this.width = Math.max(part.meshMax.x - part.meshMin.x, part.meshMax.y - part.meshMin.y);
        this.height  *= s0;
        this.width *= s0;
        w1 = h1 = 1;
        this.dimensions = EntityDimensions.fixed(width, height);
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn)
    {
        return EntityDimensions.fixed(w1*width, h1*height);
    }

    @Override
    public void update(Matrix4f transform, Vec3 dr)
    {
        var poseInfo = part.getRenderPose();
        m.identity();
        m.mul(transform);
        m0.set(poseInfo.pose());
        m.rotate(AxisAngles.XN.rotationDegrees(90));

        m.mul(m0);
        r.set(part.meshMid.x, part.meshMid.y, part.meshMin.z, 1);

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
        r.y = (float) Math.max(r.y, getParent().getY());
        this.setPos(r.x, r.y, r.z);

        this.xOld = this.getX() + dr.x;
        this.yOld = this.getY() + dr.y;
        this.zOld = this.getZ() + dr.z;
//
//        var bb = this.getBoundingBox();
//        var test = this.getParent().getBoundingBox();
//        this.leftBounds = !bb.minmax(test).equals(test);
    }
}
