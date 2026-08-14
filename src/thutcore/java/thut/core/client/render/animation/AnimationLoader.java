package thut.core.client.render.animation;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import thut.api.ModelHolder;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.AnimationLoadEvent;
import thut.api.entity.animation.IAnimationChanger;
import thut.api.entity.animation.IAnimationChanger.WornOffsets;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Mat;
import thut.core.client.render.animation.AnimationXML.Merge;
import thut.core.client.render.animation.AnimationXML.Metadata;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.AnimationXML.TexPart;
import thut.core.client.render.animation.AnimationXML.Worn;
import thut.core.client.render.animation.AnimationXML.XMLFile;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static Vector3f getRotation(final String rotation, final Vector3f default_)
    {
        if (rotation == null || rotation.isEmpty()) return default_;

        float x = 0;
        float y = 0;
        float z = 0;
        String[] r = rotation.split(",");
        if (rotation.contains("x:") || rotation.contains("y:") || rotation.contains("z:"))
        {
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
        }
        else return default_;
        return new Vector3f(x, y, z);
    }

    private static void parse(@Nonnull InputStream stream, @Nonnull ModelHolder holder, @Nonnull IModel model,
            @Nullable IModelRenderer<?> renderer)
    {
        try
        {
            final XMLFile file = AnimationXML.load(stream);

            Metadata meta = new Metadata();
            // Variables for the head rotation info
            int headDir = meta.headDir;
            int headDir2 = meta.headDir2;
            int headAxis = meta.headAxis;
            int headAxis2 = meta.headAxis2;
            final float[] headCaps = { -100, 100 };
            final float[] headCaps1 = { -30, 70 };

            if (file.model.customTex != null) file.model.customTex.init();

            Vector3f noRotation = new Vector3f();

            // Global model transforms
            Vector3 offset = new Vector3();
            Vector3f rotation = noRotation;
            Vector3 scale = new Vector3(1, 1, 1);

            // Custom tagged parts.
            final Set<String> headNames = Sets.newHashSet();
            final Set<String> shear = Sets.newHashSet();
            final Set<String> dye = Sets.newHashSet();

            // Loaded animations
            final List<Animation> animations = new ArrayList<>();
            final Map<String, List<String>> mergedAnimations = new Object2ObjectOpenHashMap<>();
            final Map<String, WornOffsets> wornOffsets = new Object2ObjectOpenHashMap<>();
            List<Phase> texPhases = new ArrayList<>();

            meta = file.model.metadata;
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
            final List<Animation> xmlAnimations = new ArrayList<>();
            for (final Phase phase : file.model.phases)
                // Handle global, merges and presets
                if (phase.name != null)
                {
                    final String name = ThutCore.trim(phase.name);
                    if (name.equals("global"))
                    {
                        offset = AnimationLoader.getVector3(phase.values.get(new QName("offset")), offset);
                        scale = AnimationLoader.getVector3(phase.values.get(new QName("scale")), scale);
                        rotation = AnimationLoader.getRotation(phase.values.get(new QName("rotation")), rotation);
                    }
                    else if (name.equals("textures")) texPhases.add(phase);
                    else if (AnimationRegistry.animations.containsKey(name))
                    {
                        if (ThutCore.conf.debug_models)
                            ThutCore.LOGGER.debug("Loading " + name + " for " + holder.name);
                        try
                        {
                            final Animation anim = AnimationRegistry.make(phase, null);
                            if (anim != null) xmlAnimations.add(anim);
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
                    if (ThutCore.conf.debug_models)
                        ThutCore.LOGGER.debug("Building Animation " + phase.type + " for " + holder.name);
                    final Animation anim = AnimationBuilder.build(phase, model.getParts().keySet(), null);
                    if (anim != null) xmlAnimations.add(anim);
                }

            // Handle merges
            for (final Merge merge : file.model.merges)
            {
                final String[] merges = merge.merge.split("->");
                String key = ThutCore.trim(merges[0]);
                List<String> toList = mergedAnimations.computeIfAbsent(key, k -> new ArrayList<>());
                toList.add(ThutCore.trim(merges[1]));
                if (merge.limbs != null)
                {
                    for (String s : merge.limbs.split(":"))
                    {
                        var p = model.getParts().get(s);
                        if (p instanceof Part part) part.isOverridenLimb = true;
                    }
                }
            }

            if (renderer != null) renderer.getAnimations().clear();
            model.initBuiltInAnimations(renderer, animations);
            animations.addAll(xmlAnimations);

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

            CustomTex texs = file.model.customTex;
            if (texs == null)
            {
                texs = new CustomTex();
                if (holder.texture != null) texs.defaults = holder.texture.toString();
            }

            // Handle materials
            for (final Mat mat : file.model.materials)
            {
                try
                {
                    model.updateMaterial(mat);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (mat.tex.isBlank()) continue;
                TexPart part = new TexPart();
                part.name = mat.name;
                part.tex = mat.tex;
                texs.parts.add(part);
            }
            holder.setLoadedOffset(offset);
            holder.setLoadedScale(scale);

            if (file.model.particles != null)
            {
                for (var m : file.model.particles)
                    model.getParts().values().forEach(part -> {
                        part.addPartRenderAdder(m);
                    });
            }

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
                if (texs.defaults != null) holder.texture = ResourceLocation.parse(texs.defaults);
                texturer.init(texs);
                texturer.init(model);
                // Now, process materials appropriately
                Set<Material> notCustom = new HashSet<>();
                Material _default = null;
                for(var p: model.getParts().values())
                {
                    for(var m: p.getMaterials())
                    {
                        // If the material is a registered custom, this is true
                        boolean isCustom = texturer.hasMapping(m.name);
                        if(!isCustom)
                        {
                            // Collect not-custom ones, and then set them all equal.
                            notCustom.add(m);
                            if(_default==null && !"auto:null".equals(m.name)) _default = m;
                        }
                    }
                }
                // Copy from default
                if(_default!=null) for(var m: notCustom)
                {
                    m.name = _default.name;
                    m.render_name = _default.render_name;
                }
                // Apply texture phases (ie texture animations)
                for (Phase p : texPhases) texturer.applyTexturePhase(p);

                // Add the animation randomiser for the sub animations
                if (!file.model.subanim.isEmpty()) animator.addChild(new AnimationRandomizer(file.model.subanim));

                renderer.updateModel(holder);

                // Set the global transforms
                renderer.setRotationOffset(offset);
                renderer.setScale(scale);

                model.getHeadParts().addAll(headNames);

                // Cleanup the animation stuff.
                for (final Animation anim : animations)
                {
                    List<Animation> anims = renderer.getAnimations().computeIfAbsent(anim.name, k -> new ArrayList<>());
                    anims.add(anim);
                }
                for (final String from : mergedAnimations.keySet())
                {
                    if (!renderer.getAnimations().containsKey(from)) continue;
                    for (String to : mergedAnimations.get(from))
                    {
                        List<Animation> fromSet = new ArrayList<>();
                        List<Animation> toSet = null;
                        // In this case, we make an empty animation
                        if (!renderer.getAnimations().containsKey(to))
                        {
                            toSet = new ArrayList<>();
                            renderer.getAnimations().put(to, toSet);
                        }
                        else toSet = renderer.getAnimations().get(to);
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
                }

                // Finalize animation initialization
                final List<Animation> allAnims = new ArrayList<>();
                Map<String, List<Animation>> newAnims = new HashMap<>(renderer.getAnimations());
                // Process the animations
                for (var entry : newAnims.entrySet())
                {
                    List<Animation> copy = entry.getValue();
                    AnimationBuilder.processAnimations(copy);
                    // Processing edits the list, so we need to re-add them
                    // here.
                    allAnims.addAll(copy);
                }
                renderer.getAnimations().putAll(newAnims);

                // Process Dyeable parts.
                animator.parseDyeables(dye);

                // Deal with shearable parts.
                animator.parseShearables(shear);

                // Initialize based on existing anims
                animator.init(allAnims);
                for (final Animation anim : allAnims)
                {
                    if (anim.name.contains("faint") || anim.name.contains("dead"))
                    {
                        anim.loops = false;
                        anim.holdWhenDone = true;
                    }

                    if (!renderer.getAnimations().containsKey(anim.name))
                    {
                        List<Animation> anims = new ArrayList<>();
                        renderer.getAnimations().put(anim.name, anims);
                        anims.add(anim);
                    }
                }

                // And if this added any new animations, update renderer

                // Add the worn offsets
                animator.parseWornOffsets(wornOffsets);

                // Update these incase they were replaced.
                renderer.setTexturer(texturer);
                renderer.setAnimationChanger(animator);

                // Process the head rotation information.
                renderer.getHeadInfo().yawDirection = headDir;
                renderer.getHeadInfo().pitchDirection = headDir2;
                renderer.getHeadInfo().yawAxis = headAxis;
                renderer.getHeadInfo().pitchAxis = headAxis2;
                renderer.getHeadInfo().yawCapMin = headCaps[0];
                renderer.getHeadInfo().yawCapMax = headCaps[1];
                renderer.getHeadInfo().pitchCapMin = headCaps1[0];
                renderer.getHeadInfo().pitchCapMax = headCaps1[1];

                // Find custom parts to mark as animated
                Set<String> animatedSet = new HashSet<>(model.getHeadParts());
                animatedSet.addAll(shear);
                animatedSet.addAll(dye);
                animatedSet.forEach(key->model.getParts().computeIfPresent(key, (s, part)->{
                    part.markAsAnimated();
                    return part;
                }));

                // Pre-process the animations via the model
                model.preProcessAnimations(allAnims);
            }
            else
            {
                // Handle customTextures
                if (texs.defaults != null) holder.texture = holder.texture != null
                        ? ResourceLocation.parse(holder.texture.toString().replace(holder.name, texs.defaults))
                        : ResourceLocation.fromNamespaceAndPath(holder.model.getNamespace(), texs.defaults);

                for (IExtendedModelPart p : model.getParts().values())
                {
                    // Handle customTextures
                    if (texs.defaults != null) holder.texture = holder.texture != null
                            ? ResourceLocation.parse(holder.texture.toString().replace(holder.name, texs.defaults))
                            : ResourceLocation.fromNamespaceAndPath(holder.model.getNamespace(), texs.defaults);
                    List<String> matNames = new ArrayList<>();
                    for (TexPart part : texs.parts)
                    {
                        ResourceLocation tex = part.tex.contains(":")
                                ? ResourceLocation.parse(part.tex)
                                : ResourceLocation.fromNamespaceAndPath(holder.model.getNamespace(), part.tex);
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
                    if (texs.defaults != null) for (Material m : p.getMaterials())
                    {
                        if (matNames.contains(m.name)) continue;
                        m.tex = holder.texture;
                    }

                    boolean isRoot = p.getParent()!=null&&p.getParent().getType().equals("__root__");
                    // Apply one level down, the __root__ part doesn't actually do anything
                    if (isRoot && noRotation != rotation) p.setDefaultAngles(rotation.x(), rotation.y(), rotation.z());
                }

            }
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.debug("No Animation found for " + holder.name + " " + holder.model, e);
        }
    }

    private static boolean parse(final ModelHolder holder, final IModel model, final IModelRenderer<?> renderer,
            ResourceLocation animations)
    {
        try
        {
            InputStream stream = ResourceHelper.getStream(animations);
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
        ThutCore.FORGE_BUS.post(new AnimationLoadEvent.Pre(holder, model, renderer));
        final ResourceLocation anims = holder.animation;
        if (anims == null && holder.backupAnimations.isEmpty())
        {
            ThutCore.FORGE_BUS.post(new AnimationLoadEvent.Fail(holder, model, renderer));
            return;
        }
        if (!AnimationLoader.parse(holder, model, renderer, anims))
        {
            for (final ResourceLocation loc : holder.backupAnimations)
                if (AnimationLoader.parse(holder, model, renderer, loc))
                {
                    ThutCore.FORGE_BUS.post(new AnimationLoadEvent.Post(holder, model, renderer));
                    return;
                }
        }
        else
        {
            ThutCore.FORGE_BUS.post(new AnimationLoadEvent.Post(holder, model, renderer));
            return;
        }
        ThutCore.FORGE_BUS.post(new AnimationLoadEvent.Fail(holder, model, renderer));
        ThutCore.LOGGER.error("Error in parsing animation file {} for {}, also checked {}", holder.animation,
                holder.name, holder.backupAnimations);
    }

    public static void setHeadCaps(final String toSplit, final float[] toFill)
    {
        final String[] r = toSplit.split(",");
        toFill[0] = Float.parseFloat(r[0]);
        toFill[1] = Float.parseFloat(r[1]);
    }
}
