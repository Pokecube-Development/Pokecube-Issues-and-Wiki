package pokecube.mobs.client.smd.impl;

import thut.api.maths.vecmath.Matrix4f;
import thut.api.maths.vecmath.Vector4f;
import thut.core.client.render.model.Vertex;

/**
 * This is a Vertex which has a mutable position. It is for used in objects
 * where the vertices for a face can be moved around during animation.
 */
public class MutableVertex extends Vertex
{
    /**
     *
     */
    private static final long serialVersionUID = 7245933158127493865L;
    private final Vector4f    defPos;
    private Vector4f          mutPos           = new Vector4f();
    private final Vector4f    defNorm;
    private Vector4f          mutNorm          = new Vector4f();

    // Temproary vectors used in transforms.
    private final Vector4f posTemp  = new Vector4f();
    private final Vector4f normTemp = new Vector4f();

    private boolean reset = true;

    // Normals
    public float xn;
    public float yn;
    public float zn;
    // Used for searching
    public final int ID;

    public MutableVertex(final float x, final float y, final float z, final float xn, final float yn, final float zn,
            final int ID)
    {
        super(x, y, z);
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        this.defPos = new Vector4f(x, y, z, 1.0F);
        this.defNorm = new Vector4f(xn, yn, zn, 0.0F);
        this.ID = ID;
    }

    public MutableVertex(final MutableVertex vertex)
    {
        super(vertex.x, vertex.y, vertex.z);
        this.xn = vertex.xn;
        this.yn = vertex.yn;
        this.zn = vertex.zn;
        this.defPos = new Vector4f(vertex.defPos);
        this.defNorm = new Vector4f(vertex.defNorm);
        this.ID = vertex.ID;
        this.mutPos = vertex.mutPos;
        this.mutNorm = vertex.mutNorm;
    }

    /**
     * Sets the internal x,y,z and xn,yn,zn to the values in the mutable
     * vectors.
     */
    public void apply()
    {
        if (this.reset)
        {
            this.x = this.defPos.x;
            this.y = this.defPos.y;
            this.z = this.defPos.z;
        }
        else
        {
            this.x = this.mutPos.x;
            this.y = this.mutPos.y;
            this.z = this.mutPos.z;
        }
        if (this.reset)
        {
            this.xn = this.defNorm.x;
            this.yn = this.defNorm.y;
            this.zn = this.defNorm.z;
        }
        else
        {
            this.xn = this.mutNorm.x;
            this.yn = this.mutNorm.y;
            this.zn = this.mutNorm.z;
        }
    }

    public boolean equals(final float x, final float y, final float z)
    {
        return this.x == x && this.y == y && this.z == z;
    }

    protected void init()
    {
        if (this.reset)
        {
            this.mutPos.set(0, 0, 0, 0);
            this.mutNorm.set(0, 0, 0, 0);
            this.reset = false;
            return;
        }
    }

    /**
     * Sets the mutable positions and normals based on the transform of the
     * given bone, scaled by the given weight.
     *
     * @param bone
     * @param weight
     */
    public void mutateFromBone(final Bone bone, final float weight)
    {
        final Matrix4f transform = bone.transform;
        if (transform != null)
        {
            this.init();
            Matrix4f.transform(transform, this.defPos, this.posTemp);
            Matrix4f.transform(transform, this.defNorm, this.normTemp);
            this.posTemp.scale(weight);
            this.normTemp.scale(weight);
            Vector4f.add(this.posTemp, this.mutPos, this.mutPos);
            Vector4f.add(this.normTemp, this.mutNorm, this.mutNorm);
        }
    }

    public void reset()
    {
        this.mutPos.set(0, 0, 0, 0);
        this.mutNorm.set(0, 0, 0, 0);
        this.reset = true;
    }
}