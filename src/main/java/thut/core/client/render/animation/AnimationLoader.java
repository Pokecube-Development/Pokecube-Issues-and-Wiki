package thut.core.client.render.animation;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.AnimationXML.Merge;
import thut.core.client.render.animation.AnimationXML.Metadata;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.AnimationXML.TexPart;
import thut.core.client.render.animation.AnimationXML.Worn;
import thut.core.client.render.animation.AnimationXML.XMLFile;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

public class AnimationLoader
{

    public static void addStrings(final String key, final Set<String> toAddTo)
    {
        if (key == null) return;
        final String[] names = key.split(":");
        for (final String s : names) toAddTo.add(ThutCore.trim(s));
    }

    public static Vector3 getVector3(final String shift, final Vector3 default_)
    {
        if (shift == null || shift.isEmpty()) return default_;
        final Vector3 vect = new Vector3().set(default_);
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
        try
        {
            if (rotation.contains("x:") || rotation.contains("y:") || rotation.contains("z:"))
            {
                float x = 0;
                float y = 0;
                float z = 0;

                for (String s : r)
                {
                    s = s.trim();
                    if (s.contains("x:"))
                    {
                        x = Float.parseFloat(s.replace("x:", ""));
                    }
                    else if (s.contains("y:"))
                    {
                        y = Float.parseFloat(s.replace("y:", ""));
                    }
                    else if (s.contains("z:"))
                    {
                        z = Float.parseFloat(s.replace("z:", ""));
                    }
                }
                final Quaternion quat = new Quaternion(0, 0, 0, 1);
                if (z != 0) quat.mul(Vector3f.YN.rotationDegrees(z));
                if (x != 0) quat.mul(Vector3f.XP.rotationDegrees(x));
                if (y != 0) quat.mul(Vector3f.ZP.rotationDegrees(y));
                ro.set(quat);
            }
            else
            {
                t = Integer.parseInt(time);
                ro.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()),
                        Float.parseFloat(r[3].trim()));
                ro.toQuaternion();
            }
        }
        catch (final Exception e)
        {
            ro.set(0, 0, 0, 1);
        }
        return new Vector5(ro, t);
    }

    public static void parse(@Nonnull InputStream stream, @Nonnull ModelHolder holder, @Nonnull IModel model,
            @Nullable IModelRenderer<?> renderer)
    {
        try
        {
            final XMLFile file = AnimationXML.load(stream);

            // Variables for the head rotation info
            int headDir = 2;
            int headDir2 = 2;
            int headAxis = 2;
            int headAxis2 = 1;
            final float[] headCaps =
            { -100, 100 };
            final float[] headCaps1 =
            { -30, 70 };

            if (file.model.customTex != null) file.model.customTex.init();
            if (renderer != null) renderer.getAnimations().clear();

            Vector5 noRotation = new Vector5();

            // Global model transforms
            Vector3 offset = new Vector3();
            Vector5 rotation = noRotation;
            Vector3 scale = new Vector3(1, 1, 1);

            // Custom tagged parts.
            final Set<String> headNames = Sets.newHashSet();
            final Set<String> shear = Sets.newHashSet();
            final Set<String> dye = Sets.newHashSet();

            // Loaded animations
            final List<Animation> tblAnims = Lists.newArrayList();
            final Map<String, String> mergedAnimations = new Object2ObjectOpenHashMap<>();
            final Map<String, WornOffsets> wornOffsets = new Object2ObjectOpenHashMap<>();
            final Map<String, List<Vector5>> phaseList = new Object2ObjectOpenHashMap<>();
            List<Phase> texPhases = Lists.newArrayList();

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
                else if (name.equals("textures")) texPhases.add(phase);
                else if (AnimationRegistry.animations.containsKey(name))
                {
                    if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Loading " + name + " for " + holder.name);
                    try
                    {
                        final Animation anim = AnimationRegistry.make(phase, null);
                        if (anim != null) tblAnims.add(anim);
                    }
                    catch (final Exception e)
                    {
                        ThutCore.LOGGER.error("Error with animation for model: " + holder.name + " Anim: " + name, e);
                    }
                }
            }
                // Handle manual animations
                else if (phase.type != null)
            {
                if (ThutCore.conf.debug_models)
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

            // Handle materials
            for (final Mat mat : file.model.materials) model.updateMaterial(mat);

            if (renderer != null) synchronized (renderer)
            {
                // Objects for modifying textures/animations
                IPartTexturer texturer = renderer.getTexturer();
                IAnimationChanger animator = renderer.getAnimationChanger();

                if (texturer == null) renderer.setTexturer(texturer = new TextureHelper());
                else texturer.reset();
                if (animator == null) renderer.setAnimationChanger(animator = new AnimationChanger());
                else animator.reset();

                final IAnimationHolder animHolder = renderer.getAnimationHolder();
                if (animHolder != null) animHolder.clean();

                // Handle customTextures
                if (file.model.customTex != null)
                {
                    texturer.init(file.model.customTex);
                    if (file.model.customTex.defaults != null) holder.texture = new ResourceLocation(
                            holder.texture.toString().replace(holder.name, file.model.customTex.defaults));
                }
                else
                {
                    CustomTex tex = new CustomTex();
                    tex.defaults = holder.texture.toString();
                    texturer.init(tex);
                }

                // Apply texture phases (ie texture animations)
                for (Phase p : texPhases) texturer.applyTexturePhase(p);

                // Add the animation randomiser for the sub animations
                if (!file.model.subanim.isEmpty()) animator.addChild(new AnimationRandomizer(file.model.subanim));

                renderer.updateModel(phaseList, holder);

                // Set the global transforms
                renderer.setRotationOffset(offset);
                renderer.setScale(scale);
                renderer.setRotations(rotation);

                model.getHeadParts().addAll(headNames);

                // Cleanup the animation stuff.
                for (final Animation anim : tblAnims)
                {
                    List<Animation> anims = renderer.getAnimations().get(anim.name);
                    if (anims == null) renderer.getAnimations().put(anim.name, anims = Lists.newArrayList());
                    anims.add(anim);
                }
                for (final String from : mergedAnimations.keySet())
                {
                    if (!renderer.getAnimations().containsKey(from)) continue;
                    final String to = mergedAnimations.get(from);
                    if (!renderer.getAnimations().containsKey(to)) continue;
                    final List<Animation> fromSet = Lists.newArrayList();
                    final List<Animation> toSet = renderer.getAnimations().get(to);
                    for (final Animation anim : renderer.getAnimations().get(from))
                    {
                        final Animation newAnim = new Animation();
                        newAnim.identifier = anim.identifier;
                        newAnim.name = to;
                        newAnim.loops = anim.loops;
                        newAnim.priority = 20;
                        newAnim.length = -1;
                        for (final String s : anim.sets.keySet()) newAnim.sets.put(s, anim.sets.get(s));
                        fromSet.add(newAnim);
                    }
                    toSet.addAll(fromSet);
                }

                // Finalize animation initialization
                final List<Animation> allAnims = Lists.newArrayList();
                // Process the animations
                for (final List<Animation> anims : renderer.getAnimations().values())
                {
                    AnimationBuilder.processAnimations(anims);
                    // Processing edits the list, so we need to re-add them
                    // here.
                    allAnims.addAll(anims);
                }

                // Pre-process the animations via the model
                model.preProcessAnimations(allAnims);

                // Process Dyeable parts.
                animator.parseDyeables(dye);

                // Deal with shearable parts.
                animator.parseShearables(shear);

                // Initialize based on existing anims
                animator.init(allAnims);

                // Add the worn offsets
                animator.parseWornOffsets(wornOffsets);

                // Update these incase they were replaced.
                renderer.setTexturer(texturer);
                renderer.setAnimationChanger(animator);

                // Process the head rotation information.
                if (headDir2 == 2) headDir2 = headDir;
                if (headDir != 2) renderer.getHeadInfo().yawDirection = headDir;
                if (headDir2 != 2) renderer.getHeadInfo().pitchDirection = headDir2;
                renderer.getHeadInfo().yawAxis = headAxis;
                renderer.getHeadInfo().pitchAxis = headAxis2;
                renderer.getHeadInfo().yawCapMin = headCaps[0];
                renderer.getHeadInfo().yawCapMax = headCaps[1];
                renderer.getHeadInfo().pitchCapMin = headCaps1[0];
                renderer.getHeadInfo().pitchCapMax = headCaps1[1];
            }
            else
            {
                // Handle customTextures
                if (file.model.customTex != null)
                    if (file.model.customTex.defaults != null) holder.texture = holder.texture != null
                            ? new ResourceLocation(
                                    holder.texture.toString().replace(holder.name, file.model.customTex.defaults))
                            : new ResourceLocation(holder.model.getNamespace(), file.model.customTex.defaults);

                for (IExtendedModelPart p : model.getParts().values())
                {
                    // Handle customTextures
                    if (file.model.customTex != null)
                    {
                        if (file.model.customTex.defaults != null) holder.texture = holder.texture != null
                                ? new ResourceLocation(
                                        holder.texture.toString().replace(holder.name, file.model.customTex.defaults))
                                : new ResourceLocation(holder.model.getNamespace(), file.model.customTex.defaults);
                        List<String> matNames = Lists.newArrayList();
                        for (TexPart part : file.model.customTex.parts)
                        {
                            ResourceLocation tex = new ResourceLocation(holder.model.getNamespace(), part.tex);
                            if (p.getName().equals(part.name))
                            {
                                for (Material m3 : p.getMaterials())
                                {
                                    m3.tex = tex;
                                    matNames.add(m3.name);
                                }
                            }
                            else
                            {
                                // In this case, we convert to a Material
                                Material m = new Material(part.name);
                                m.tex = tex;
                                Mat m2 = new Mat();
                                m2.name = part.name;
                                p.updateMaterial(m2, m);
                                matNames.add(part.name);
                            }
                        }
                        // Now do the same for the base material
                        if (file.model.customTex.defaults != null) for (Material m : p.getMaterials())
                        {
                            if (matNames.contains(m.name)) continue;
                            m.tex = holder.texture;
                        }
                    }
                    if (p.getParent() == null)
                    {
                        p.setPreScale(scale);
                        p.setPreTranslations(offset);
                        if (noRotation != rotation) p.setPreRotations(rotation.rotations);
                    }
                }

            }
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.debug("No Animation found for " + holder.name + " " + holder.model, e);
        }
    }

    public static boolean parse(final ModelHolder holder, final IModel model, final IModelRenderer<?> renderer,
            ResourceLocation animations)
    {
        try
        {
            InputStream stream = ResourceHelper.getStream(animations, Minecraft.getInstance().getResourceManager());
            if (stream == null) throw new FileNotFoundException(animations.toString());
            if (ThutCore.conf.debug_models) ThutCore.LOGGER.debug("Loading " + animations + " for " + holder.name);
            AnimationLoader.parse(stream, holder, model, renderer);
            stream.close();
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
        if (anims == null && holder.backupAnimations.isEmpty()) return;
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
