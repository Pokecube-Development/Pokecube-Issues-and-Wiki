package thut.core.client.render.model;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import thut.api.entity.IAnimated;
import thut.api.entity.animation.Animation;
import thut.core.client.render.model.parts.Material;

public interface IModelCustom
{
    // Copied from PoseStack.Pose
    public static final class PoseInfo
    {
        final Matrix4f pose;
        final Matrix3f normal;
        boolean trustedNormals = true;

        public PoseInfo()
        {
            this.pose = new Matrix4f();
            this.normal = new Matrix3f();
        }

        public PoseInfo(PoseInfo pose)
        {
            this.pose = new Matrix4f(pose.pose);
            this.normal = new Matrix3f(pose.normal);
            this.trustedNormals = pose.trustedNormals;
        }

        public void set(PoseInfo other)
        {
            pose.set(other.pose);
            normal.set(other.normal);
        }

        public void set(PoseStack.Pose other)
        {
            pose.set(other.pose());
            normal.set(other.normal());
        }

        public Matrix4f pose()
        {
            return this.pose;
        }

        public Matrix3f normal()
        {
            return this.normal;
        }

        public PoseInfo copy()
        {
            return new PoseInfo(this);
        }

        public void translate(double x, double y, double z)
        {
            pose.translate((float)x, (float)y, (float)z);
        }

        public void scale(float x, float y, float z)
        {
            pose.scale(x, y, z);
            if (Math.abs(x) == Math.abs(y) && Math.abs(y) == Math.abs(z)) {
                if (x < 0.0F || y < 0.0F || z < 0.0F) {
                    normal.scale(Math.signum(x), Math.signum(y), Math.signum(z));
                }
            } else {
                normal.scale(1.0F / x, 1.0F / y, 1.0F / z);
                trustedNormals = false;
            }
        }

        public void rotate(Quaternionf rotation)
        {
            pose.rotateAffine(rotation);
            normal.rotate(rotation);
        }
    }

    default void render(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderLegacy(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void updateAnimation(List<Animation> playingAnims, IAnimated.IAnimationHolder holder){}

    default void prepareRender()
    {

    }

    List<Material> getMaterials();

    /**
     * This is used to ensure all sub parts share the same set of materials,
     * for use with sharing render types, etc
     */
    void updateMaterials(List<Material> materials);
}
