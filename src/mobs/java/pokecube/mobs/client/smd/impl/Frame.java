package pokecube.mobs.client.smd.impl;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.joml.Matrix4f;

/**
 * This is a section of an animation, it specifics a particular set of
 * transformation matrices, each one is for a different bone.
 */
public class Frame
{
    public final int           ID;
    public Animation           owner;
    public ArrayList<Matrix4f> invertTransforms = Lists.newArrayList();
    public ArrayList<Matrix4f> transforms       = Lists.newArrayList();

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

    public void addTransforms(final int index, final Matrix4f invertedData)
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
                final Matrix4f temp = this.transforms.get(bone.parent.ID).mul(this.transforms.get(i), this.transforms.get(i));
                temp.invert(this.invertTransforms.get(i));
            }
        }
    }

    /**
     * Sets up the transforms for the given index.
     *
     */
    public void setTransforms(final int id)
    {
        final Matrix4f rotator = Helpers.makeMatrix(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rotator.mul(this.transforms.get(id), this.transforms.get(id));
        rotator.invert(new Matrix4f()).mul(this.invertTransforms.get(id), this.invertTransforms.get(id));
    }
}