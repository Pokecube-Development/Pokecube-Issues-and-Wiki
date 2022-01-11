package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import thut.api.maths.vecmath.Mat4f;

/**
 * This is a section of an animation, it specifics a particular set of
 * transformation matrices, each one is for a different bone.
 */
public class Frame
{
    public final int           ID;
    public Animation           owner;
    public ArrayList<Mat4f> invertTransforms = Lists.newArrayList();
    public ArrayList<Mat4f> transforms       = Lists.newArrayList();

    public Frame(final Animation parent)
    {
        this.owner = parent;
        this.ID = parent.newFrameID();
    }

    public Frame(final Frame anim, final Animation parent)
    {
        this.owner = parent;
        this.ID = anim.ID;
        this.transforms = anim.transforms;
        this.invertTransforms = anim.invertTransforms;
    }

    public void addTransforms(final int index, final Mat4f invertedData)
    {
        this.transforms.add(index, invertedData);
        final Mat4f inv = new Mat4f(invertedData);
        inv.invert();
        this.invertTransforms.add(index, inv);
    }

    /** Applies the appropriate transforms to the various bones. */
    public void applyTransforms()
    {
        for (int i = 0; i < this.transforms.size(); i++)
        {
            final Bone bone = this.owner.bones.get(i);
            if (bone.parent != null)
            {
                final Mat4f temp = Mat4f.mul(this.transforms.get(bone.parent.ID), this.transforms.get(i), null);
                this.transforms.set(i, temp);
                this.invertTransforms.set(i, Mat4f.invert(temp, null));
            }
        }
    }

    /**
     * Sets up the transforms for the given index.
     *
     * @param id
     *            - transform index
     * @param degrees
     */
    public void setTransforms(final int id)
    {
        final Mat4f rotator = Helpers.makeMatrix(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        Mat4f.mul(rotator, this.transforms.get(id), this.transforms.get(id));
        Mat4f.mul(Mat4f.invert(rotator, null), this.invertTransforms.get(id), this.invertTransforms.get(id));
    }
}