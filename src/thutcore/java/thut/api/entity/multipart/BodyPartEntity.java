package thut.api.entity.multipart;

import com.google.common.collect.Lists;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class BodyPartEntity<E extends Entity> extends GenericPartEntity<E>
{
    public static class BodyNode
    {
        public List<BodyPart> parts = Lists.newArrayList();

        public void onLoad()
        {
            this.parts.forEach(BodyPart::onLoad);
        }
    }

    public static class BodyPart
    {
        public String name;
        public String offset;
        public String size;

        public String ride;

        public Vec3 __pos__;
        public Vec3 __size__;
        public Vec3 __ride__;

        public void onLoad()
        {
            String[] args = this.offset.split(",");
            this.__pos__ = new Vec3(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]));
            args = this.size.split(",");
            this.__size__ = new Vec3(Double.parseDouble(args[0]), Double.parseDouble(args[1]),
                    Double.parseDouble(args[2]));
            if (this.ride != null)
            {
                args = this.ride.split(",");
                this.__ride__ = new Vec3(Double.parseDouble(args[0]) - __pos__.x,
                        Double.parseDouble(args[1]) - __pos__.y, Double.parseDouble(args[2]) - __pos__.z);
            }
        }
    }

    public static interface Factory<T extends BodyPartEntity<E>, E extends Entity>
    {
        T create(E parent, final float width, final float height, final float x, final float y, final float z,
                final String id);
    }

    public Vector3f r0;
    private final Vector4f r = new Vector4f();

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
