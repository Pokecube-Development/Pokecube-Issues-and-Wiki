package thut.api.entity.multipart;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.parts.Part;
import thut.lib.AxisAngles;

import javax.annotation.Nullable;
import java.util.List;

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
    public boolean needSizeCheck = false;
    private boolean wasHidden = false;

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
        this.dimensions = EntityDimensions.fixed(width, height);
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
            height = 0.0f;
            width = 0.0f;
            if (!wasHidden) refreshDimensions();
            wasHidden = true;
        }
        else if (wasHidden || needSizeCheck)
        {
            float s0 = ((Part) model.root_part).basePreScale.x;
            this.height = max.z - min.z;
            this.width = Math.max(max.x - min.x, max.y - min.y);
            this.height *= s0;
            this.width *= s0;
            refreshDimensions();
            wasHidden = needSizeCheck = false;
        }
        this.r1.set(r);
        if (this.ride_point != null)
        {
            r2.sub(r);
            this.ride_point.set(r2.x, r2.y, r2.z);
        }
    }

    public Vector3f limitMove(MoverType type, Vec3 vel)
    {
        if (this.noPhysics) return new Vector3f((float) vel.x, (float) vel.y, (float) vel.z);

        this.level().getProfiler().push("move");
        vel = this.maybeBackOffFromEdge(vel, type);
        Vec3 vec3 = this.collide(vel);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("rest");
        boolean hitX = !Mth.equal(vel.x, vec3.x);
        boolean hitZ = !Mth.equal(vel.z, vec3.z);
        this.horizontalCollision = hitX || hitZ;
        this.verticalCollision = vel.y != vec3.y;
        this.verticalCollisionBelow = this.verticalCollision && vel.y < 0.0;
        if (this.horizontalCollision)
        {
            this.minorHorizontalCollision = this.isHorizontalCollisionMinor(vec3);
        }
        else
        {
            this.minorHorizontalCollision = false;
        }
        this.level().getProfiler().pop();
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    /**
     * Given a motion vector, return an updated vector that takes into account restrictions such as collisions (from all
     * directions) and step-up from stepHeight
     */
    private Vec3 collide(Vec3 vec)
    {
        AABB aabb = this.getBoundingBox();
        var step = this.maxUpStep();
        if (vec.y <= 0)
        {
            // We replaced collide for this addition, we need to ensure
            // that the mob can walk up stairs, even if other bit of the
            // hitbox overlap some.
            var dy = this.getY() - this.getParent().getY();
            if (dy < step) aabb = aabb.setMinY(this.getParent().getY());
        }
        List<VoxelShape> list = this.level().getEntityCollisions(this, aabb.expandTowards(vec));
        Vec3 vec3 = vec.lengthSqr() == 0.0 ? vec : collideBoundingBox(this, vec, aabb, this.level(), list);
        boolean hitX = vec.x != vec3.x;
        boolean hitY = vec.y != vec3.y;
        boolean hitZ = vec.z != vec3.z;
        boolean hitYDownwards = hitY && vec.y < 0.0;
        if (step > 0.0F && (hitYDownwards || this.onGround()) && (hitX || hitZ))
        {
            AABB aabb1 = hitYDownwards ? aabb.move(0.0, vec3.y, 0.0) : aabb;
            AABB aabb2 = aabb1.expandTowards(vec.x, step, vec.z);
            if (!hitYDownwards)
            {
                aabb2 = aabb2.expandTowards(0.0, -1.0E-5F, 0.0);
            }

            List<VoxelShape> list1 = collectColliders(this, this.level, list, aabb2);
            float f = (float) vec3.y;
            float[] afloat = collectCandidateStepUpHeights(aabb1, list1, step, f);

            for (float f1 : afloat)
            {
                Vec3 vec31 = collideWithShapes(new Vec3(vec.x, f1, vec.z), aabb1, list1);
                if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr())
                {
                    double d0 = aabb.minY - aabb1.minY;
                    return vec31.add(0.0, -d0, 0.0);
                }
            }
        }

        return vec3;
    }

    // Copied from Entity.class
    private static float[] collectCandidateStepUpHeights(AABB box, List<VoxelShape> colliders, float deltaY,
            float maxUpStep)
    {
        FloatSet floatset = new FloatArraySet(4);

        for (VoxelShape voxelshape : colliders)
        {
            for (double d0 : voxelshape.getCoords(Direction.Axis.Y))
            {
                float f = (float) (d0 - box.minY);
                if (!(f < 0.0F) && f != maxUpStep)
                {
                    if (f > deltaY)
                    {
                        break;
                    }

                    floatset.add(f);
                }
            }
        }

        float[] afloat = floatset.toFloatArray();
        FloatArrays.unstableSort(afloat);
        return afloat;
    }

    // Copied from Entity.class
    public static Vec3 collideBoundingBox(@Nullable Entity entity, Vec3 vec, AABB collisionBox, Level level,
            List<VoxelShape> potentialHits)
    {
        List<VoxelShape> list = collectColliders(entity, level, potentialHits, collisionBox.expandTowards(vec));
        return collideWithShapes(vec, collisionBox, list);
    }

    // Copied from Entity.class
    private static List<VoxelShape> collectColliders(@Nullable Entity entity, Level level, List<VoxelShape> collisions,
            AABB boundingBox)
    {
        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(collisions.size() + 1);
        if (!collisions.isEmpty())
        {
            builder.addAll(collisions);
        }

        WorldBorder worldborder = level.getWorldBorder();
        boolean flag = entity != null && worldborder.isInsideCloseToBorder(entity, boundingBox);
        if (flag)
        {
            builder.add(worldborder.getCollisionShape());
        }

        builder.addAll(level.getBlockCollisions(entity, boundingBox));
        return builder.build();
    }

    // Copied from Entity.class
    private static Vec3 collideWithShapes(Vec3 deltaMovement, AABB entityBB, List<VoxelShape> shapes)
    {
        if (shapes.isEmpty())
        {
            return deltaMovement;
        }
        else
        {
            double d0 = deltaMovement.x;
            double d1 = deltaMovement.y;
            double d2 = deltaMovement.z;
            if (d1 != 0.0)
            {
                d1 = Shapes.collide(Direction.Axis.Y, entityBB, shapes, d1);
                if (d1 != 0.0)
                {
                    entityBB = entityBB.move(0.0, d1, 0.0);
                }
            }

            boolean flag = Math.abs(d0) < Math.abs(d2);
            if (flag && d2 != 0.0)
            {
                d2 = Shapes.collide(Direction.Axis.Z, entityBB, shapes, d2);
                if (d2 != 0.0)
                {
                    entityBB = entityBB.move(0.0, 0.0, d2);
                }
            }

            if (d0 != 0.0)
            {
                d0 = Shapes.collide(Direction.Axis.X, entityBB, shapes, d0);
                if (!flag && d0 != 0.0)
                {
                    entityBB = entityBB.move(d0, 0.0, 0.0);
                }
            }

            if (!flag && d2 != 0.0)
            {
                d2 = Shapes.collide(Direction.Axis.Z, entityBB, shapes, d2);
            }

            return new Vec3(d0, d1, d2);
        }
    }
}
