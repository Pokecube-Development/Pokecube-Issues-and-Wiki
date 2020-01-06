package pokecube.mobs.client.smd.impl;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import thut.core.client.render.model.Vertex;

/**
 * This is a Vertex which has a mutable position. It is for used in objects
 * where the vertices for a face can be moved around during animation.
 */
public class MutableVertex extends Vertex
{
    private final Vector4f defPos;
    public Vector4f        mutPos  = new Vector4f();
    private final Vector4f defNorm;
    public Vector4f        mutNorm = new Vector4f();

    // Temproary vectors used in transforms.
    final Vector4f posTemp  = new Vector4f();
    final Vector4f normTemp = new Vector4f();

    // Normals
    public float xn;
    public float yn;
    public float zn;
    // Used for searching
    public final int ID;

    public MutableVertex(float x, float y, float z, float xn, float yn, float zn, int ID)
    {
        super(x, y, z);
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        this.defPos = new Vector4f(x, y, z, 1.0F);
        this.defNorm = new Vector4f(xn, yn, zn, 0.0F);
        this.ID = ID;
    }

    public MutableVertex(MutableVertex vertex)
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
        if (this.mutPos == null)
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
        if (this.mutNorm == null)
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

    public boolean equals(float x, float y, float z)
    {
        return this.x == x && this.y == y && this.z == z;
    }

    protected void init()
    {
        if (this.mutPos == null) this.mutPos = new Vector4f();
        if (this.mutNorm == null) this.mutNorm = new Vector4f();
    }

    /**
     * Sets the mutable positions and normals based on the transform of the
     * given bone, scaled by the given weight.
     *
     * @param bone
     * @param weight
     */
    public void mutateFromBone(Bone bone, float weight)
    {
        final Matrix4f transform = bone.transform;
        if (transform != null)
        {
            this.init();
            transform.transform(this.defPos, this.posTemp);
            transform.transform(this.defNorm, this.normTemp);
            this.posTemp.scale(weight);
            this.normTemp.scale(weight);
            this.mutPos.add(this.posTemp, this.mutPos);
            this.mutNorm.add(this.normTemp, this.mutNorm);
        }
    }

    public void reset()
    {
        this.mutPos = null;
        this.mutNorm = null;
    }
}