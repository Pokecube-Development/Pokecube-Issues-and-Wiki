package thut.core.client.render.animation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.AnimationXML.Merge;
import thut.core.client.render.animation.AnimationXML.Metadata;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.AnimationXML.Worn;
import thut.core.client.render.animation.AnimationXML.XMLFile;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.common.ThutCore;

public class AnimationLoader
{

    public static void addStrings(final String key, final Set<String> toAddTo)
    {
        if (key == null) return;
        final String[] names = key.split(":");
        for (final String s : names)
            toAddTo.add(ThutCore.trim(s));
    }

    public static Vector3 getVector3(final String shift, final Vector3 default_)
    {
        if (shift == null || shift.isEmpty()) return default_;
        final Vector3 vect = Vector3.getNewVector().set(default_);
        String[] r;
        r = shift.split(",");
        if (r.length == 1)
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()));
        else if (r.length == 3)
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
        return vect;
    }

    public static Vector5 getRotation(final String rotation, String time, final Vector5 default_)
    {
        if (rotation == null || rotation.isEmpty()) return default_;
        time = time == null ? "0" : time;
        final Vector4 ro = new Vector4();
        int t = 0;
        String[] r;
        r = rotation.split(",");
        t = Integer.parseInt(time);
        try
        {
            ro.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()),
                    Float.parseFloat(r[3].trim()));
            ro.toQuaternion();
        }
        catch (final Exception e)
        {
            ro.set(0, 0, 0, 1);
        }
        return new Vector5(ro, t);
    }

    public static void parse(final InputStream stream, final ModelHolder holder, final IModel model,
            final IModelRenderer<?> renderer)
    {
        try
        {
            final XMLFile file = AnimationXML.load(stream);

            // Variables for the head rotation info
            int headDir = 2;
            int headDir2 = 2;
            int headAxis = 2;
            int headAxis2 = 1;
            final float[] headCaps = { -100, 100 };
            final float[] headCaps1 = { -30, 70 };

            // Global model transforms
            Vector3 offset = Vector3.getNewVector();
            Vector5 rotation = new Vector5();
            Vector3 scale = Vector3.getNewVector();

            // Objects for modifying textures/animations
            IPartTexturer texturer = renderer.getTexturer();
            IAnimationChanger animator = renderer.getAnimationChanger();

            if (texturer == null) renderer.setTexturer(texturer = new TextureHelper());
            if (animator == null) renderer.setAnimationChanger(animator = new AnimationChanger());

            // Custom tagged parts.
            final Set<String> headNames = Sets.newHashSet();
            final Set<String> shear = Sets.newHashSet();
            final Set<String> dye = Sets.newHashSet();

            // Loaded animations
            final List<Animation> tblAnims = Lists.newArrayList();
            final HashMap<String, String> mergedAnimations = Maps.newHashMap();
            final Map<String, WornOffsets> wornOffsets = Maps.newHashMap();
            final HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<>();

            final Metadata meta = file.model.metadata;
            if (meta != null)
            {
                AnimationLoader.addStrings(meta.head, headNames);
                AnimationLoader.addStrings(meta.shear, shear);
                AnimationLoader.addStrings(meta.dye, dye);

                headDir = meta.headDir;
                headDir2 = meta.headDir2;
                headAxis = meta.headAxis;
                headAxis2 = meta.headAxis2;

                AnimationLoader.setHeadCaps(meta.headCap, headCaps);
                AnimationLoader.setHeadCaps(meta.headCap1, headCaps1);
            }
            for (final Phase phase : file.model.phases)
                // Handle global, merges and presets
                if (phase.name != null)
                {
                    final String name = ThutCore.trim(phase.name);
                    if (name.equals("global"))
                    {
                        offset = AnimationLoader.getVector3(phase.values.get(new QName("offset")), offset);
                        scale = AnimationLoader.getVector3(phase.values.get(new QName("scale")), scale);
                        rotation = AnimationLoader.getRotation(phase.values.get(new QName("rotation")), null, rotation);
                    }
                    else if (name.equals("textures")) texturer.applyTexturePhase(phase);
                    else if (AnimationRegistry.animations.containsKey(name))
                    {
                        ThutCore.LOGGER.debug("Loading " + name + " for " + holder.name);
                        try
                        {
                            final Animation anim = AnimationRegistry.make(phase, null);
                            if (anim != null) tblAnims.add(anim);
                        }
                        catch (final Exception e)
                        {
                            ThutCore.LOGGER.error("Error with animation for model: " + holder.name + " Anim: " + name,
                                    e);
                        }
                    }
                }
                // Handle manual animations
                else if (phase.type != null)
                {
                    ThutCore.LOGGER.debug("Building Animation " + phase.type + " for " + holder.name);
                    final Animation anim = AnimationBuilder.build(phase, model.getParts().keySet(), null);
                    if (anim != null) tblAnims.add(anim);
                }

            // Handle merges
            for (final Merge merge : file.model.merges)
            {
                final String[] merges = merge.merge.split("->");
                mergedAnimations.put(ThutCore.trim(merges[0]), ThutCore.trim(merges[1]));
            }

            // Handle worn offsets.
            for (final Worn worn : file.model.worn)
            {
                final Vector3 w_offset = AnimationLoader.getVector3(worn.offset, null);
                final Vector3 w_angles = AnimationLoader.getVector3(worn.angles, null);
                final Vector3 w_scale = AnimationLoader.getVector3(worn.scale, null);
                final String w_parent = worn.parent;
                final String w_ident = worn.id;
                wornOffsets.put(w_ident, new WornOffsets(w_parent, w_offset, w_scale, w_angles));
            }

            // Handle customTextures
            if (file.model.customTex != null)
            {
                texturer.init(file.model.customTex);
                if (file.model.customTex.defaults != null) holder.texture = new ResourceLocation(holder.texture
                        .toString().replace(holder.name, ThutCore.trim(file.model.customTex.defaults)));
            }

            // Handle materials
            for (final Mat mat : file.model.materials)
                model.updateMaterial(mat);

            final IModelRenderer<?> loaded = renderer;
            loaded.updateModel(phaseList, holder);

            // Set the global transforms
            loaded.setRotationOffset(offset);
            loaded.setScale(scale);
            loaded.setRotations(rotation);

            model.getHeadParts().addAll(headNames);

            // Cleanup the animation stuff.
            for (final Animation anim : tblAnims)
            {
                List<Animation> anims = loaded.getAnimations().get(anim.name);
                if (anims == null) loaded.getAnimations().put(anim.name, anims = Lists.newArrayList());
                anims.add(anim);
            }
            for (final String from : mergedAnimations.keySet())
            {
                if (!loaded.getAnimations().containsKey(from)) continue;
                final String to = mergedAnimations.get(from);
                if (!loaded.getAnimations().containsKey(to)) continue;
                final List<Animation> fromSet = Lists.newArrayList();
                final List<Animation> toSet = loaded.getAnimations().get(to);
                for (final Animation anim : loaded.getAnimations().get(from))
                {
                    final Animation newAnim = new Animation();
                    newAnim.identifier = anim.identifier;
                    newAnim.name = to;
                    newAnim.loops = anim.loops;
                    newAnim.priority = 20;
                    newAnim.length = -1;
                    for (final String s : anim.sets.keySet())
                        newAnim.sets.put(s, Lists.newArrayList(anim.sets.get(s)));
                    fromSet.add(newAnim);
                }
                toSet.addAll(fromSet);
            }

            // Process the animations
            for (final List<Animation> anims : loaded.getAnimations().values())
                AnimationBuilder.processAnimations(anims);

            // Process Dyeable parts.
            animator.parseDyeables(dye);

            // Deal with shearable parts.
            animator.parseShearables(shear);

            // Finalize animation initialization
            final Set<Animation> anims = Sets.newHashSet();

            animator.init(anims);

            // Add the worn offsets
            animator.parseWornOffsets(wornOffsets);

            // Update these incase they were replaced.
            loaded.setTexturer(texturer);
            loaded.setAnimationChanger(animator);

            // Process the head rotation information.
            if (model.getHeadInfo() != null)
            {
                if (headDir != 2) model.getHeadInfo().yawDirection = headDir;
                if (headDir2 != 2) model.getHeadInfo().pitchDirection = headDir2;
                model.getHeadInfo().yawAxis = headAxis;
                model.getHeadInfo().pitchAxis = headAxis2;
                model.getHeadInfo().yawCapMin = headCaps[0];
                model.getHeadInfo().yawCapMax = headCaps[1];
                model.getHeadInfo().pitchCapMin = headCaps1[0];
                model.getHeadInfo().pitchCapMax = headCaps1[1];
            }

            // Pre-process the animations via the model
            model.preProcessAnimations(loaded.getAnimations().values());

            stream.close();
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.debug("No Animation found for " + holder.name + " " + holder.model, e);
        }
    }

    public static boolean parse(final ModelHolder holder, final IModel model, final IModelRenderer<?> renderer,
            final ResourceLocation animations)
    {
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(animations);
            final InputStream stream = res.getInputStream();
            ThutCore.LOGGER.debug("Loading " + animations + " for " + holder.name);
            AnimationLoader.parse(stream, holder, model, renderer);
            res.close();
            return true;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    public static void parse(final ModelHolder holder, final IModel model, final IModelRenderer<?> renderer)
    {
        final ResourceLocation anims = holder.animation;
        if (!AnimationLoader.parse(holder, model, renderer, anims))
        {
            for (final ResourceLocation loc : holder.backupAnimations)
                if (AnimationLoader.parse(holder, model, renderer, loc)) return;
        }
        else return;

        ThutCore.LOGGER.error("Error in parsing animation file {} for {}, also checked {}", holder.animation,
                holder.name, holder.backupAnimations);
    }

    public static void setHeadCaps(final String toSplit, final float[] toFill)
    {
        final String[] r = toSplit.split(",");
        toFill[0] = Float.parseFloat(r[0]);
        toFill[1] = Float.parseFloat(r[1]);
    }

    public static void setHeadCaps(final Node node, final float[] toFill, final float[] toFill1)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem("headCap") != null)
            AnimationLoader.setHeadCaps(node.getAttributes().getNamedItem("headCap").getNodeValue(), toFill);
        if (node.getAttributes().getNamedItem("headCap1") != null)
            AnimationLoader.setHeadCaps(node.getAttributes().getNamedItem("headCap1").getNodeValue(), toFill1);
    }
}
