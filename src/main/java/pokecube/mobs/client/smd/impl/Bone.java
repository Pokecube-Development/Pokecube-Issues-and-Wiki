package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import thut.api.maths.vecmath.Matrix4f;

/** Bone, has associated Vertices for stretching. */
public class Bone
{
    public Bone            copy             = null;
    public String          name;
    public int             ID;
    public Bone            parent;
    public Body            owner;
    /** transformation matrix for when this bone has not been moved */
    public Matrix4f        rest;
    public Matrix4f        restInv;
    /** Transformation matrix for the new position of this bone. */
    public Matrix4f        transform        = new Matrix4f();
    /**
     * Transformation matrix set by dynamic animations, such as head
     * rotations.
     */
    public Matrix4f        dynamicTransform = new Matrix4f();
    /** Placeholder to prevent re-newing temporary matrices */
    private final Matrix4f dummy1           = new Matrix4f();
    private final Matrix4f dummy2           = new Matrix4f();

    public ArrayList<Bone>                      children           = new ArrayList<>(0);
    public HashMap<MutableVertex, Float>        verts              = new HashMap<>();
    public HashMap<String, ArrayList<Matrix4f>> animatedTransforms = new HashMap<>();
    public float[]                              currentVals        = new float[6];

    public Bone(final Bone b, final Bone parent, final Body owner)
    {
        this.name = b.name;
        this.ID = b.ID;
        this.owner = owner;
        this.parent = parent;

        final Iterator<Map.Entry<MutableVertex, Float>> iterator = b.verts.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<MutableVertex, Float> entry = iterator.next();
            this.verts.put(owner.verts.get(entry.getKey().ID), entry.getValue());
        }
        this.animatedTransforms = new HashMap<>(b.animatedTransforms);
        this.restInv = b.restInv;
        this.rest = b.rest;
        b.copy = this;
    }

    public Bone(final String name, final int ID, final Bone parent, final Body owner)
    {
        this.name = name;
        this.ID = ID;
        this.parent = parent;
        this.owner = owner;
    }

    public void addChild(final Bone child)
    {
        this.children.add(child);
    }

    public void addVertex(final MutableVertex v, final float weight)
    {
        if (this.name.equals("blender_implicit")) throw new UnsupportedOperationException(
                "Cannot add vertex to this part!");
        this.verts.put(v, Float.valueOf(weight));
    }

    /** Applies our rest matrix to all child matrices */
    public void applyChildrenToRest()
    {
        for (final Bone child : this.children)
            child.applyToRest(this.rest);
    }

    /**
     * modifies the rest matrix by the given matrix.
     *
     * @param parentMatrix
     */
    public void applyToRest(final Matrix4f parentMatrix)
    {
        this.rest = Matrix4f.mul(parentMatrix, this.rest, this.rest);
        this.applyChildrenToRest();
    }

    /** Applies our current transform to all associated vertices. */
    public void applyTransform()
    {
        final Frame currentFrame = this.owner.getCurrentFrame();
        if (currentFrame != null)
        {
            final ArrayList<Matrix4f> precalcArray = this.animatedTransforms.get(currentFrame.owner.name);
            final Matrix4f animated = precalcArray.get(currentFrame.ID);
            final Matrix4f animatedChange = Matrix4f.mul(animated, this.restInv, this.dummy1);
            this.transform = this.transform == null ? animatedChange
                    : Matrix4f.mul(this.transform, animatedChange, this.transform);
        }
        for (final Map.Entry<MutableVertex, Float> entry : this.verts.entrySet())
            entry.getKey().mutateFromBone(this, entry.getValue().floatValue());
        this.reset();
    }

    /**
     * Applies a custom transform to this bone and all children. This is to be
     * used for things like at-runtime animations, such as head rotations.
     *
     * @param transform
     */
    public void applyTransform(final Matrix4f transform)
    {
        Matrix4f.mul(this.dynamicTransform, transform, this.dynamicTransform);
    }

    protected Matrix4f getTransform()
    {
        return this.transform == null ? (this.transform = new Matrix4f()) : this.transform;
    }

    public void invertRestMatrix()
    {
        this.restInv = Matrix4f.invert(this.rest, this.restInv);
    }

    /**
     * Pre-calculates the needed transform for the given frame.
     *
     * @param key
     * @param animated
     */
    public void preloadAnimation(final Frame key, final Matrix4f animated)
    {
        ArrayList<Matrix4f> precalcArray;
        if (this.animatedTransforms.containsKey(key.owner.name)) precalcArray = this.animatedTransforms.get(
                key.owner.name);
        else precalcArray = new ArrayList<>();
        Helpers.ensureFits(precalcArray, key.ID);
        precalcArray.set(key.ID, animated);
        this.animatedTransforms.put(key.owner.name, precalcArray);
    }

    /**
     * Prepares the various matrices for the transforms for the current
     * pose.
     */
    public void prepareTransform()
    {
        final Matrix4f inverted = this.dummy1;
        final Matrix4f delta = this.dummy2;
        // Apply either transformation based on the animation, or based on the
        // rest matrices
        if (this.owner.parent.hasAnimations() && this.owner.currentAnim != null)
        {
            // We have an animation, so our delta matrix will be based on this.
            final Frame currentFrame = this.owner.currentAnim.frames.get(this.owner.currentAnim.index);
            Matrix4f.load(currentFrame.transforms.get(this.ID), delta);
            Matrix4f.load(currentFrame.invertTransforms.get(this.ID), inverted);
        }
        else
        {
            // No animation, so use rest matrix for the delta.
            Matrix4f.load(this.rest, delta);
            Matrix4f.load(this.restInv, inverted);
        }
        // Apply the dynamic transformation.
        Matrix4f.mul(delta, this.dynamicTransform, delta);
        Matrix4f.mul(delta, inverted, delta);
        this.transform = this.parent != null ? Matrix4f.mul(this.parent.transform, delta, this.getTransform())
                : this.getTransform();
        for (final Bone child : this.children)
            child.prepareTransform();
    }

    public void reset()
    {
        this.transform.setIdentity();
        this.dynamicTransform.setIdentity();
    }

    public void setChildren(final Bone b, final ArrayList<Bone> bones)
    {
        for (int i = 0; i < b.children.size(); i++)
        {
            final Bone child = b.children.get(i);
            this.children.add(bones.get(child.ID));
            bones.get(child.ID).parent = this;
        }
    }

    public void setRest(final Matrix4f resting)
    {
        this.rest = resting;
    }
}
