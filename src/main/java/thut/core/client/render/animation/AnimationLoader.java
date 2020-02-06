package thut.core.client.render.animation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureHelper;
import thut.core.common.ThutCore;

public class AnimationLoader
{

    public static HashMap<String, ModelHolder> models = new HashMap<>();

    public static void addStrings(final String key, final Node node, final Set<String> toAddTo)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem(key) != null)
        {
            final String[] names = node.getAttributes().getNamedItem(key).getNodeValue().split(":");
            for (final String s : names)
                toAddTo.add(ThutCore.trim(s));
        }
    }

    public static void clear()
    {
        AnimationLoader.models.clear();
    }

    public static Vector3 getAngles(final Node node, final Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        Vector3 vect = null;
        if (node.getAttributes().getNamedItem("angles") != null)
        {
            vect = Vector3.getNewVector();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("angles").getNodeValue();
            r = shift.split(",");
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            return vect;
        }
        return default_;
    }

    public static int getIntValue(final Node node, final String key, final int default_)
    {
        int ret = default_;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem(key) != null)
            ret = Integer.parseInt(node.getAttributes().getNamedItem(key).getNodeValue());
        return ret;
    }

    public static Vector3 getOffset(final Node node, final Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        Vector3 vect = null;
        if (node.getAttributes().getNamedItem("offset") != null)
        {
            vect = Vector3.getNewVector();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("offset").getNodeValue();
            r = shift.split(",");
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            return vect;
        }
        return default_;
    }

    public static Vector5 getRotation(final Node node, final Vector5 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("rotation") != null)
        {
            String rotation;
            String time = "0";
            final Vector4 ro = new Vector4();
            int t = 0;
            String[] r;
            rotation = node.getAttributes().getNamedItem("rotation").getNodeValue();
            if (node.getAttributes().getNamedItem("time") != null)
                time = node.getAttributes().getNamedItem("time").getNodeValue();
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
        return default_;
    }

    public static Vector3 getScale(final Node node, final Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("scale") != null)
        {
            Vector3 vect = null;
            vect = Vector3.getNewVector();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("scale").getNodeValue();
            r = shift.split(",");

            if (r.length == 3)
                vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            else vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()));
            return vect;
        }
        return default_;
    }

    public static void parse(final InputStream stream, final ModelHolder holder, final IModel model,
            final IModelRenderer<?> renderer)
    {
        try
        {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            final NodeList modelList = doc.getElementsByTagName("model");

            // Variables for the head rotation info
            int headDir = 2;
            int headDir2 = 2;
            int headAxis = 2;
            int headAxis2 = 1;
            final float[] headCaps = { -180, 180 };
            final float[] headCaps1 = { -30, 70 };

            // Global model transforms
            Vector3 offset = Vector3.getNewVector();
            Vector5 rotation = new Vector5();
            Vector3 scale = Vector3.getNewVector();

            // Objects for modifying textures/animations
            IPartTexturer texturer = renderer.getTexturer();
            IAnimationChanger animator = renderer.getAnimationChanger();

            if (texturer == null) renderer.setTexturer(texturer = new TextureHelper(null));
            if (animator == null) renderer.setAnimationChanger(animator = new AnimationChanger());

            // Custom tagged parts.
            final Set<String> headNames = Sets.newHashSet();
            final Set<String> shear = Sets.newHashSet();
            final Set<String> dye = Sets.newHashSet();

            // Loaded animations
            final List<Animation> tblAnims = Lists.newArrayList();
            final HashMap<String, String> mergedAnimations = Maps.newHashMap();
            final Map<String, WornOffsets> wornOffsets = Maps.newHashMap();
            for (int i = 0; i < modelList.getLength(); i++)
            {
                final Node modelNode = modelList.item(i);
                final String modelName = holder.name;
                final HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<>();
                final NodeList partsList = modelNode.getChildNodes();
                for (int j = 0; j < partsList.getLength(); j++)
                {
                    final Node part = partsList.item(j);
                    if (part.getNodeName().equals("metadata")) try
                    {
                        offset = AnimationLoader.getOffset(part, offset);
                        scale = AnimationLoader.getScale(part, scale);
                        rotation = AnimationLoader.getRotation(part, rotation);
                        headDir = AnimationLoader.getIntValue(part, "headDir", headDir);
                        headDir2 = AnimationLoader.getIntValue(part, "headDir2", headDir2);
                        headAxis = AnimationLoader.getIntValue(part, "headAxis", 2);
                        headAxis2 = AnimationLoader.getIntValue(part, "headAxis2", 0);
                        AnimationLoader.addStrings("head", part, headNames);
                        AnimationLoader.addStrings("shear", part, shear);
                        AnimationLoader.addStrings("dye", part, dye);
                        AnimationLoader.setHeadCaps(part, headCaps, headCaps1);
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                    }
                    else if (part.getNodeName().equals("worn"))
                    {
                        final Vector3 w_offset = AnimationLoader.getOffset(part, null);
                        final Vector3 w_angles = AnimationLoader.getAngles(part, null);
                        final Vector3 w_scale = AnimationLoader.getScale(part, null);
                        final String w_parent = part.getAttributes().getNamedItem("parent").getNodeValue();
                        final String w_ident = part.getAttributes().getNamedItem("id").getNodeValue();
                        wornOffsets.put(w_ident, new WornOffsets(w_parent, w_offset, w_scale, w_angles));
                    }
                    else if (part.getNodeName().equals("phase"))
                    {
                        final Node phase = part.getAttributes().getNamedItem("name") == null
                                ? part.getAttributes().getNamedItem("type") : part.getAttributes().getNamedItem("name");
                                final String phaseName = ThutCore.trim(phase.getNodeValue());
                                for (final String s : AnimationRegistry.animations.keySet())
                                    if (phaseName.equals(s))
                                    {
                                        ThutCore.LOGGER.debug("Loading " + s + " for " + holder.name);
                                        try
                                        {
                                            final Animation anim = AnimationRegistry.make(s, part.getAttributes(), null);
                                            if (anim != null) tblAnims.add(anim);
                                        }
                                        catch (final Exception e)
                                        {
                                            ThutCore.LOGGER
                                            .debug("Error with animation for model: " + holder.name + " Anim: " + s, e);
                                        }
                                    }
                                if (phaseName.equals("global")) try
                                {
                                    offset = AnimationLoader.getOffset(part, offset);
                                    scale = AnimationLoader.getScale(part, scale);
                                    rotation = AnimationLoader.getRotation(part, rotation);
                                    headDir = AnimationLoader.getIntValue(part, "headDir", headDir);
                                    headDir2 = AnimationLoader.getIntValue(part, "headDir2", headDir2);
                                    headAxis = AnimationLoader.getIntValue(part, "headAxis", 2);
                                    headAxis2 = AnimationLoader.getIntValue(part, "headAxis2", 0);
                                    AnimationLoader.addStrings("head", part, headNames);
                                    AnimationLoader.addStrings("shear", part, shear);
                                    AnimationLoader.addStrings("dye", part, dye);
                                    AnimationLoader.setHeadCaps(part, headCaps, headCaps1);
                                }
                                catch (final Exception e)
                                {
                                    e.printStackTrace();
                                }
                                else if (phaseName.equals("textures")) holder.handleCustomTextures(part);
                                else
                                {
                                    final Animation anim = AnimationBuilder.build(part, null);
                                    if (anim != null) tblAnims.add(anim);
                                }
                    }
                    else if (part.getNodeName().equals("merges"))
                    {
                        final String[] merges = part.getAttributes().getNamedItem("merge").getNodeValue().split("->");
                        mergedAnimations.put(merges[0], merges[1]);
                    }
                    else if (part.getNodeName().equals("customTex"))
                    {
                        texturer = new TextureHelper(part);
                        if (part.getAttributes().getNamedItem("default") != null)
                            holder.texture = new ResourceLocation(holder.texture.toString().replace(holder.name,
                                    part.getAttributes().getNamedItem("default").getNodeValue()));
                    }
                    else if (part.getNodeName().equals("customModel")) holder.model = new ResourceLocation(
                            part.getAttributes().getNamedItem("default").getNodeValue());
                    else if (part.getNodeName().equals("subAnims")) animator.addChild(new AnimationRandomizer(part));
                }

                final IModelRenderer<?> loaded = renderer;
                loaded.updateModel(phaseList, holder);

                // Test for messing with tbl models
                if (holder.extension.equals("tbl")) offset = offset.add(0, -0.5, 0);

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
                // TODO actually initialize animations if needed.
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

                // Add to the various maps.
                AnimationLoader.models.put(modelName, holder);
            }

            stream.close();
        }
        catch (final Exception e)
        {
            AnimationLoader.models.put(holder.name, holder);
            ThutCore.LOGGER.debug("No Animation found for " + holder.name + " " + holder.model, e);
        }
    }

    public static void parse(final ModelHolder holder, final IModel model, final IModelRenderer<?> renderer)
    {
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(holder.animation);
            final InputStream stream = res.getInputStream();
            AnimationLoader.parse(stream, holder, model, renderer);
            res.close();
        }
        catch (final Exception e)
        {
            System.err.println("No Animation found for " + holder.name + " " + holder.model);
            e.printStackTrace();
        }
    }

    public static void setHeadCaps(final Node node, final float[] toFill, final float[] toFill1)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem("headCap") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap").getNodeValue();
            r = shift.split(",");
            toFill[0] = Float.parseFloat(r[0]);
            toFill[1] = Float.parseFloat(r[1]);
        }
        if (node.getAttributes().getNamedItem("headCap1") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap1").getNodeValue();
            r = shift.split(",");
            toFill1[0] = Float.parseFloat(r[0]);
            toFill1[1] = Float.parseFloat(r[1]);
        }
    }
}
