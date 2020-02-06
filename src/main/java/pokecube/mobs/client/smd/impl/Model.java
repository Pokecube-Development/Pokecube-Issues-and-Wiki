package pokecube.mobs.client.smd.impl;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;

/**
 * Base model object, this contains the body, a list of the bones, and the
 * animations.
 */
public class Model
{
    public Body                       body;
    public HashMap<String, Animation> anims         = new HashMap<>();
    public Bone                       root;
    public ArrayList<Bone>            allBones;
    public Animation                  currentAnimation;
    public boolean                    hasAnimations = true;
    public boolean                    usesMaterials = true;

    public Model(final Model model)
    {
        this.body = new Body(model.body, this);
        final Iterator<Map.Entry<String, Animation>> iterator = model.anims.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<String, Animation> entry = iterator.next();
            this.anims.put(entry.getKey(), new Animation(entry.getValue(), this));
        }
        this.hasAnimations = model.hasAnimations;
        this.usesMaterials = model.usesMaterials;
        this.currentAnimation = this.anims.get("idle");
    }

    public Model(final ResourceLocation resource) throws Exception
    {
        this.load(resource);
        this.reformBones();
        this.precalculateAnims();
    }

    public void animate()
    {
        this.resetVerts(this.body);
        if (this.body.currentAnim == null) this.setAnimation("idle");
        this.root.prepareTransform();
        for (final Bone b : this.allBones)
            b.applyTransform();
        this.applyVertChange(this.body);
    }

    private void applyVertChange(final Body body)
    {
        if (body == null) return;
        for (final MutableVertex v : body.verts)
            v.apply();
    }

    public boolean hasAnimations()
    {
        return this.hasAnimations;
    }

    private void load(final ResourceLocation resloc) throws Exception
    {
        try
        {
            final ResourceLocation modelPath = resloc;
            // Load the model.
            this.body = new Body(this, modelPath);

            final List<String> anims = Lists.newArrayList("idle", "walking", "flying", "sleeping", "swimming");
            final String resLoc = resloc.toString();
            // Check for valid animations, and load them in as well.
            for (final String s : anims)
            {
                final String anim = resLoc.endsWith("smd") ? resLoc.replace(".smd", "/" + s + ".smd")
                        : resLoc.replace(".SMD", "/" + s + ".smd");
                final ResourceLocation animation = new ResourceLocation(anim);
                try
                {
                    this.anims.put(s, new Animation(this, s, animation));
                    if (s.equalsIgnoreCase("idle")) this.currentAnimation = this.anims.get(s);
                }
                catch (final FileNotFoundException | NullPointerException e1)
                {
                    // Ignore these, we don't really care about them
                    PokecubeCore.LOGGER.debug("No animation of {} for {}", s, resloc);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error with animation " + s, e);
                }
            }
        }
        catch (final Exception e)
        {
            throw e;
        }
    }

    private void precalculateAnims()
    {
        for (final Animation anim : this.anims.values())
            anim.precalculateAnimation(this.body);
    }

    private void reformBones()
    {
        this.root.applyChildrenToRest();
        for (final Bone b : this.allBones)
            b.invertRestMatrix();
    }

    public void renderAll(final MatrixStack mat, final IVertexBuilder buffer, final int[] rgbbro)
    {
        this.body.render(mat, buffer, rgbbro);
    }

    private void resetVerts(final Body body)
    {
        if (body == null) return;
        for (final MutableVertex v : body.verts)
            v.reset();
    }

    public void setAnimation(final String name)
    {
        final Animation old = this.currentAnimation;
        if (this.anims.containsKey(name)) this.currentAnimation = this.anims.get(name);
        else this.currentAnimation = this.anims.get("idle");
        this.body.setAnimation(this.currentAnimation);
        if (old != this.currentAnimation)
        {
        }
    }

    void syncBones(final Body body)
    {
        this.allBones = body.bones;
        if (!body.partOfGroup) this.root = body.root;
    }
}