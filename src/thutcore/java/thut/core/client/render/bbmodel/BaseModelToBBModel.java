package thut.core.client.render.bbmodel;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pokecube.api.PokecubeAPI;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators;
import thut.api.entity.animation.CapabilityAnimation;
import thut.api.util.JsonUtil;
import thut.core.client.render.model.BaseModel;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.parts.Part;
import thut.lib.AxisAngles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BaseModelToBBModel
{
    public static String randomKey(Set<String> existing, int len)
    {
        String var = RandomStringUtils.randomAlphanumeric(len);
        while (!existing.add(var)) var = RandomStringUtils.randomAlphanumeric(len);
        return var;
    }

    public static BBModelTemplate convert(BaseModel model, Map<String, List<Animation>> animations)
    {
        BBModelTemplate result = new BBModelTemplate();
        result.name = model.name;
        if (result.name.contains("/"))
        {
            var arr = result.name.split("/");
            result.name = arr[arr.length - 1];
        }
        result.name = result.name.replace(".x3d", "");
        // Default settings on the metadata
        result.meta = new BBModelTemplate.Meta();

        // One element per part.
        Map<String, BBModelTemplate.Element> elements_by_id = new HashMap<>();
        // One group per part with children
        Map<String, BBModelTemplate.JsonGroup> groups_by_id = new HashMap<>();
        Map<String, BBModelTemplate.JsonGroup> outliner_by_id = new HashMap<>();

        Map<String, String> partsToGroup = new HashMap<>();
        Map<String, BBModelTemplate.JsonGroup> partNameToOutlinerGroup = new HashMap<>();
        Map<IExtendedModelPart, String> partsToUUID = new HashMap<>();
        Map<String, String> partNameToUUID = new HashMap<>();

        var holder = new CapabilityAnimation.DefaultImpl();
        holder.overridePlaying("");
        // this should result in all parts being translated to their root positions
        model.updateAnimation(List.of(), holder);
        List<ResourceLocation> textures = new ArrayList<>();
        Map<ResourceLocation, NativeImage> images = new HashMap<>();
        Set<String> keys = new HashSet<>();

        PoseStack pose = new PoseStack();
        pose.mulPose(AxisAngles.XN.rotationDegrees(90));
        pose.scale(16,16,16);
        var last = pose.last();

        Matrix4f posMat = new Matrix4f();

        var parts = model.getParts();
        parts.forEach((name, rawpart) -> {
            if (rawpart instanceof Part part)
            {
                BBModelTemplate.Element element = new BBModelTemplate.Element();
                element.name = name;
                element.type = "mesh";
                element.box_uv = null;
                element.origin = new float[] { 0, 0, 0 };
                element.rotation = new float[] { 0, 0, 0 };
                element.vertices = new HashMap<>();
                element.faces = new HashMap<>();

                if(!partsToUUID.containsKey(part))
                {
                    element.uuid = UUID.randomUUID().toString();
                    partsToUUID.put(part, element.uuid);
                }
                else element.uuid = partsToUUID.get(part);

                // Handle adding the element
                if (!part.getRenderMeshes().isEmpty())
                {
                    elements_by_id.put(element.uuid, element);
                    Map<Vector3f, String> vertices = new HashMap<>();
                    part.getRenderMeshes().forEach(mesh -> {

                        // Transform to global coordinates
                        last.pose().mul(mesh.poseInfo.pose(), posMat);
                        mesh.poseInfo.pose().set(posMat);

                        for (int i = 0; i < mesh.vertices.length; i++)
                        {
                            var vert = mesh.vertices[i];
                            if (!vertices.containsKey(vert))
                            {
                                String key = randomKey(keys, 4);
                                vertices.put(vert, key);
                                var mut = new Vector4f(vert, 1);
                                var pos = mesh.poseInfo.pose();
                                mut.mul(pos);
                                float[] _vert = { mut.x, mut.y, mut.z };
                                element.vertices.put(key, _vert);
                            }
                        }
                        int iter = mesh.vertexMode == VertexFormat.Mode.TRIANGLES ? 3 : 4;
                        var material = mesh.material;
                        var texture = material.getTexture();
                        var img = texture.getImage();
                        if (!textures.contains(material.tex))
                        {
                            textures.add(material.tex);
                            images.put(material.tex, img);
                        }
                        int texID = textures.indexOf(material.tex);
                        for (int i = 0; i < mesh.vertices.length; i += iter)
                        {
                            String faceKey = randomKey(keys, 8);
                            List<String> verts = new ArrayList<>();
                            Map<String, float[]> faceUVs = new HashMap<>();
                            for (int j = 0; j < iter; j++)
                            {
                                var vert = mesh.vertices[i + j];
                                var uv_ = mesh.textureCoordinates[i + j];
                                String vertKey = vertices.get(vert);
                                verts.add(vertKey);
                                var _uv = new Vector2f(uv_);
                                _uv.x *= img.getWidth();
                                _uv.y *= img.getHeight();
                                float[] uv = { _uv.x, _uv.y };
                                faceUVs.put(vertKey, uv);
                            }
                            BBModelTemplate.MeshFace face = new BBModelTemplate.MeshFace();
                            face.vertices = verts;
                            face.uv = faceUVs;
                            face.texture = texID;
                            String faceJsonStr = JsonUtil.gson.toJson(face);
                            element.faces.put(faceKey, JsonUtil.gson.fromJson(faceJsonStr, JsonObject.class));
                        }
                    });
                }
                var partID = partsToUUID.get(part);
                if (!partsToGroup.containsKey(partID))
                {
                    var id =  UUID.randomUUID().toString();
                    partsToGroup.put(partID, id);
                    partNameToUUID.put(part.getName(), id);
                }
                var groupID = partsToGroup.get(partID);
                boolean isAnimated = part.isAnimated();
                // Handle adding the group
                if (!part.getSubParts().isEmpty() || isAnimated)
                {
                    BBModelTemplate.JsonGroup groupCoord = groups_by_id.computeIfAbsent(groupID,
                            s -> new BBModelTemplate.JsonGroup());
                    BBModelTemplate.JsonGroup groupOutliner = outliner_by_id.computeIfAbsent(groupID,
                            s -> new BBModelTemplate.JsonGroup());
                    groupCoord.uuid = groupID;
                    groupCoord.name = part.name;

                    last.pose().mul(part.getRenderPose().pose(), posMat);
                    Vector4f origin = new Vector4f(0,0,0,1);
                    origin.mul(posMat);
                    groupCoord.origin = new float[] { origin.x, origin.y, origin.z };
                    groupCoord.rotation = new float[] { 0, 0, 0 };

                    groupOutliner.uuid = groupID;
                    if (groupOutliner.children == null) groupOutliner.children = new ArrayList<>();
                    if (!groupOutliner.children.contains(partID)) groupOutliner.children.add(partID);
                    partNameToOutlinerGroup.put(part.name, groupOutliner);
                }

                // Handle adding to parent's group
                if (part.getParent() != null)
                {
                    var parent = part.getParent();
                    if (!partsToUUID.containsKey(parent)) partsToUUID.put(parent, UUID.randomUUID().toString());
                    var parentID = partsToUUID.get(parent);
                    if (!partsToGroup.containsKey(parentID))
                    {
                        var id =  UUID.randomUUID().toString();
                        partsToGroup.put(parentID, id);
                        partNameToUUID.put(parent.getName(), id);
                    }
                    var parentGroupID = partsToGroup.get(parentID);
                    var parentGroup = outliner_by_id.computeIfAbsent(parentGroupID,
                            s -> new BBModelTemplate.JsonGroup());
                    var ourGroup = outliner_by_id.computeIfAbsent(groupID, s -> new BBModelTemplate.JsonGroup());
                    if(!partNameToOutlinerGroup.containsKey(parent.getName()))
                        partNameToOutlinerGroup.put(parent.getName(), parentGroup);
                    if (parentGroup.children == null) parentGroup.children = new ArrayList<>();
                    if (!parentGroup.children.contains(ourGroup)) parentGroup.children.add(ourGroup);

                    // If we are animated, we make a new group for us, and add ourselves to it.
                    // Otherwise we add ourself to our parent's group
                    if(part.getSubParts().isEmpty() && !isAnimated && !parentGroup.children.contains(partID))
                        parentGroup.children.add(partID);

                    ourGroup._parent = parentGroup;
                }
            }
        });

        for(var resource: textures)
        {
            var image = images.get(resource);
            var path = resource.getPath().split("/");
            var location = path[path.length-1];
            var texture = new BBModelTemplate.Texture();
            texture.uuid = UUID.randomUUID().toString();
            texture.id = ""+textures.indexOf(resource);
            texture.name = location.replace(".png", "");
            texture.width = texture.uv_width = image.getWidth();
            texture.height = texture.uv_height = image.getHeight();
            texture.file_format = "png";

            final BufferedImage img_buffer = new BufferedImage(texture.width, texture.height, BufferedImage.TYPE_INT_ARGB);
            for(int i = 0; i< texture.width; i++)
            {
                for(int j = 0; j< texture.height; j++)
                {
                    int abgr = image.getPixelRGBA(i,j);
                    int argb = FastColor.ARGB32.color(FastColor.ABGR32.alpha(abgr),
                            FastColor.ABGR32.red(abgr),
                            FastColor.ABGR32.green(abgr),
                            FastColor.ABGR32.blue(abgr));
                    img_buffer.setRGB(i, j, argb);
                }
            }
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img_buffer, texture.file_format, baos);
                var asString = Base64.getEncoder().encodeToString(baos.toByteArray());
                texture.source = "data:image/png;base64,"+asString;
            }
            catch (IOException ignored)
            {
            }

            result.textures.add(texture);
        }

        // Now for animations
        for (String animName : animations.keySet())
        {
            List<Animation> animlist = animations.get(animName);
            for (var _anim : animlist)
            {
                BBModelTemplate.BBAnimation anim = new BBModelTemplate.BBAnimation();
                anim.name = _anim.name;
                anim.uuid = UUID.randomUUID().toString();
                anim.loop = "loop";
                anim.animators = new HashMap<>();
                anim.length = _anim.getLength()/20f;
                for (var pair : _anim.sets.entrySet())
                {
                    if (!(pair.getValue() instanceof Animators.KeyframeAnimator frames)) continue;
                    var key = partNameToUUID.get(pair.getKey());
                    if(key==null)
                    {
                        PokecubeAPI.logInfo("Did not find mapping for {}", pair.getKey());
                        continue;
                    }
                    BBModelTemplate.BBAnimation.BBAnimator animator = new BBModelTemplate.BBAnimation.BBAnimator();
                    animator.name = pair.getKey();
                    animator.type = "bone";
                    for (var channel : frames.channels)
                    {
                        if (channel == null) continue;
                        var list = new ArrayList<>(channel.components());
                        boolean looped = _anim.loops && list.size() > 1;
                        if(looped)
                        {
                            list.add(channel.components().getFirst());
                        }
                        for(int i = 0; i<list.size(); i++)
                        {
                            var component = list.get(i);
                            BBModelTemplate.BBAnimation.BBKeyFrame frame = new BBModelTemplate.BBAnimation.BBKeyFrame();
                            BBModelTemplate.BBAnimation.BBDataPoint point = new BBModelTemplate.BBAnimation.BBDataPoint();
                            frame.channel = channel.channel();
                            frame.data_points.add(point);
                            frame.uuid = UUID.randomUUID().toString();
                            frame.time = component.startKey / 20f;
                            float rotXScale = model instanceof BBModel ? 1 : -1;
                            float rotZScale = model instanceof BBModel ? 1 : -1;
                            if (looped && i == list.size() - 1) frame.time = anim.length;
                            switch (frame.channel)
                            {
                            case "position":
                                var posOffset = component.posOffset;
                                point.x = posOffset[0] * 16;
                                point.y = posOffset[2] * 16;
                                point.z = posOffset[1] * 16;
                                break;
                            case "rotation":
                                var rotOffset = component.rotOffset;
                                if(component._rotFunctions[0]!=null)
                                {
                                    point.x = component._rotFunctions[0];
                                }
                                else
                                {
                                    point.x = rotXScale * rotOffset[0];
                                }
                                if(component._rotFunctions[1]!=null)
                                {
                                    point.y = component._rotFunctions[1];
                                }
                                else
                                {
                                    point.y = rotOffset[1];
                                }
                                if(component._rotFunctions[2]!=null)
                                {
                                    point.z = component._rotFunctions[2];
                                }
                                else
                                {
                                    point.z = rotZScale * rotOffset[2];
                                }
                                break;
                            case "scale":
                                break;
                            }
                            if(point.x instanceof String s){
                                s = s.replace("sin(", "math.sin(");
                                s = s.replace("cos(", "math.cos(");
                                s = s.replace("*l*", "*q.anim_time*20*");
                                s = s.replace("(0.05*", "(");
                                point.x = s;
                            }
                            if(point.y instanceof String s){
                                s = s.replace("sin(", "math.sin(");
                                s = s.replace("cos(", "math.cos(");
                                s = s.replace("*l*", "*q.anim_time*20*");
                                s = s.replace("(0.05*", "(");
                                point.y = s;
                            }
                            if(point.z instanceof String s){
                                s = s.replace("sin(", "math.sin(");
                                s = s.replace("cos(", "math.cos(");
                                s = s.replace("*l*", "*q.anim_time*20*");
                                s = s.replace("(0.05*", "(");
                                point.z = s;
                            }
                            animator.keyframes.add(frame);
                        }
                    }
                    anim.animators.put(key, animator);
                }
                result.animations.add(anim);
            }
        }

        result.outliner.addAll(outliner_by_id.values().stream().filter(g->g._parent==null).toList());
        result.groups.addAll(groups_by_id.values());
        result.elements.addAll(elements_by_id.values());

        result.groups.sort(Comparator.comparing(a -> a.name));
        result.elements.sort(Comparator.comparing(a -> a.name));
        return result;
    }
}
