package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import thut.api.maths.vecmath.Matrix4f;

import com.google.common.collect.Lists;

/** This is a section of an animation, it specifics a particular set of
 * transformation matrices, each one is for a different bone. */
public class Frame
{
    public final int           ID;
    public Animation           owner;
    public ArrayList<Matrix4f> invertTransforms = Lists.newArrayList();
    public ArrayList<Matrix4f> transforms       = Lists.newArrayList();

    public Frame(Animation parent)
    {
        this.owner = parent;
        this.ID = parent.newFrameID();
    }

    public Frame(Frame anim, Animation parent)
    {
        this.owner = parent;
        this.ID = anim.ID;
        this.transforms = anim.transforms;
        this.invertTransforms = anim.invertTransforms;
    }

    public void addTransforms(int index, Matrix4f invertedData)
    {
        this.transforms.add(index, invertedData);
        final Matrix4f inv = new Matrix4f(invertedData);
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
                final Matrix4f temp = new Matrix4f();
                temp.mul(this.transforms.get(bone.parent.ID), this.transforms.get(i));
                this.transforms.set(i, temp);
                final Matrix4f inv = new Matrix4f(temp);
                inv.invert();
                this.invertTransforms.set(i, inv);
            }
        }
    }

    /** Sets up the transforms for the given index.
     *
     * @param id
     *            - transform index
     * @param degrees */
    public void setTransforms(int id)
    {
        final Matrix4f rotator = Helpers.makeMatrix(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.transforms.get(id).mul(rotator, this.transforms.get(id));
        final Matrix4f invRot = new Matrix4f(rotator);
        invRot.invert();
        this.invertTransforms.get(id).mul(invRot, this.invertTransforms.get(id));
    }
}