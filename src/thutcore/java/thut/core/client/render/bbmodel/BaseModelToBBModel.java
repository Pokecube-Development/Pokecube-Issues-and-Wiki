package thut.core.client.render.bbmodel;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pokecube.api.PokecubeAPI;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators;
import thut.api.entity.animation.CapabilityAnimation;
import thut.api.entity.multipart.IBBPartMultipart;
import thut.api.util.JsonUtil;
import thut.core.client.render.model.BaseModel;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.model.parts.textures.BaseTexture;
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

    private static record BBConstructions(Map<String, BBModelTemplate.Element> elements_by_id,
            Map<String, BBModelTemplate.JsonGroup> groups_by_id, Map<String, BBModelTemplate.JsonGroup> outliner_by_id,
            Map<String, String> partsToGroup, Map<String, BBModelTemplate.JsonGroup> partNameToOutlinerGroup,
            Map<IExtendedModelPart, String> partsToUUID, Map<String, String> partNameToUUID, Set<String> locator_names)
    {
        public BBConstructions()
        {
            this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), new HashSet<>());
        }
    }

    public static BBModelTemplate convert(BaseModel model, boolean simplifiy)
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

        BBConstructions construct = new BBConstructions();

        // One element per part.
        var elements_by_id = construct.elements_by_id;
        // One group per part with children
        var groups_by_id = construct.groups_by_id;
        var outliner_by_id = construct.outliner_by_id;

        var partsToGroup = construct.partsToGroup;
        var partsToUUID = construct.partsToUUID;
        var partNameToUUID = construct.partNameToUUID;

        var holder = new CapabilityAnimation.DefaultImpl();
        holder.overridePlaying("");
        // this should result in all parts being translated to their root positions
        model.updateAnimation(List.of(), holder);
        List<ResourceLocation> textures = new ArrayList<>();
        Map<ResourceLocation, NativeImage> images = new HashMap<>();
        Set<ResourceLocation> emissives = new HashSet<>();
        Set<String> keys = new HashSet<>();

        PoseStack pose = new PoseStack();
        pose.mulPose(AxisAngles.XN.rotationDegrees(90));
        pose.scale(16, 16, 16);
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

                if (!partsToUUID.containsKey(part))
                {
                    element.uuid = UUID.randomUUID().toString();
                    partsToUUID.put(part, element.uuid);
                }
                else
                {
                    element.uuid = partsToUUID.get(part);
                }

                // Handle adding the element
                if (!part.getRenderMeshes().isEmpty())
                {
                    Map<Vector3f, String> vertices = new HashMap<>();

                    // Maps containing relevant information pulled from the mesh
                    Map<String, Mesh> namedMesh = new HashMap<>();
                    Map<String, Vector3f[]> faceVerts = new HashMap<>();
                    Map<String, Vector2f[]> faceTex = new HashMap<>();
                    Map<String, Integer> faceModes = new HashMap<>();
                    Map<String, ResourceLocation> faceMats = new HashMap<>();

                    // Collect info for the maps
                    part.getRenderMeshes().forEach(mesh -> {
                        var mesh_key = randomKey(keys, 4);
                        namedMesh.put(mesh_key, mesh);
                        var material = mesh.getRenderMaterial();
                        NativeImage img;
                        try
                        {
                            var vertexMode = mesh.GL_FORMAT == Mesh.TRIANGLE_FMT ? Mode.TRIANGLES : Mode.QUADS;
                            material.makeRenderType(material.tex, vertexMode);
                            BaseTexture texture = material.getTexture();
                            img = texture.getImage();
                        }
                        catch (Exception e)
                        {
                            img = null;
                        }
                        var matTex = material.tex;
                        // Make a "fake" texture that is emissive
                        if(material.emissiveMagnitude!=0)
                        {
                            matTex = ResourceLocation.fromNamespaceAndPath(matTex.getNamespace(), matTex.getPath()+"_e");
                            emissives.add(matTex);
                        }
                        if (!textures.contains(matTex))
                        {
                            textures.add(matTex);
                            images.put(matTex, img);
                        }
                        faceMats.put(mesh_key, matTex);
                        faceVerts.put(mesh_key, mesh.vertices);
                        faceTex.put(mesh_key, mesh.textureCoordinates);
                        faceModes.put(mesh_key, mesh.GL_FORMAT == Mesh.TRIANGLE_FMT ? 3 : 4);
                    });

                    // Here we can perform whatever edits on the maps as needed.
                    List<String> faces = new ArrayList<>(faceMats.keySet());

                    // Lets try to simplify, down to just a bounding cube for each one
                    if (simplifiy)
                    {
                        List<String> remove = new ArrayList<>();
                        Map<String, Matrix3f> boxes = new HashMap<>();
                        for (var meshKey : faces)
                        {
                            var meshVerts = faceVerts.get(meshKey);
                            var meshTex = faceTex.get(meshKey);

                            Vector3f min = new Vector3f();
                            Vector3f max = new Vector3f();
                            Vector2f minU = new Vector2f();
                            Vector2f maxU = new Vector2f();
                            for (int i = 0; i < meshVerts.length; i++)
                            {
                                var t = meshTex[i];
                                var v = meshVerts[i];
                                min = min.min(v);
                                max = max.max(v);
                                minU = minU.min(t);
                                maxU = maxU.max(t);
                            }

                            float volume = IBBPartMultipart.computeSimpleVolume(meshVerts);
                            float newVolume = (max.x - min.x) * (max.y - min.y) * (max.z - min.z);
                            if (Math.abs(volume) < 1e-4 || newVolume < 1e-4)
                            {
                                remove.add(meshKey);
                                continue;
                            }
                            boxes.put(meshKey, new Matrix3f(min, max, new Vector3f()));
                            if (newVolume / volume > 5)
                            {
                                PokecubeAPI.LOGGER.warn("Warning, volume expanded greatly for part {} in {}",
                                        part.getName(), model.name);
                            }

                            List<Vector3f> cube = new ArrayList<>();
                            List<Vector2f> cubeTex = new ArrayList<>();
                            // Now build the cube
                            cube.add(new Vector3f(min.x, min.y, min.z));
                            cube.add(new Vector3f(min.x, max.y, min.z));
                            cube.add(new Vector3f(min.x, max.y, max.z));
                            cube.add(new Vector3f(min.x, min.y, max.z));

                            cube.add(new Vector3f(min.x, min.y, min.z));
                            cube.add(new Vector3f(max.x, min.y, min.z));
                            cube.add(new Vector3f(max.x, max.y, min.z));
                            cube.add(new Vector3f(min.x, max.y, min.z));

                            cube.add(new Vector3f(min.x, min.y, min.z));
                            cube.add(new Vector3f(max.x, min.y, min.z));
                            cube.add(new Vector3f(max.x, min.y, max.z));
                            cube.add(new Vector3f(min.x, min.y, max.z));

                            cube.add(new Vector3f(max.x, max.y, max.z));
                            cube.add(new Vector3f(max.x, min.y, max.z));
                            cube.add(new Vector3f(max.x, min.y, min.z));
                            cube.add(new Vector3f(max.x, max.y, min.z));

                            cube.add(new Vector3f(max.x, max.y, max.z));
                            cube.add(new Vector3f(min.x, max.y, max.z));
                            cube.add(new Vector3f(min.x, min.y, max.z));
                            cube.add(new Vector3f(max.x, min.y, max.z));

                            cube.add(new Vector3f(max.x, max.y, max.z));
                            cube.add(new Vector3f(min.x, max.y, max.z));
                            cube.add(new Vector3f(min.x, max.y, min.z));
                            cube.add(new Vector3f(max.x, max.y, min.z));

                            for (int i = 0; i < 6; i++)
                            {
                                cubeTex.add(new Vector2f(minU.x, minU.y));
                                cubeTex.add(new Vector2f(minU.x, maxU.y));
                                cubeTex.add(new Vector2f(maxU.x, maxU.y));
                                cubeTex.add(new Vector2f(maxU.x, minU.y));
                            }

                            faceModes.put(meshKey, 4);
                            faceVerts.put(meshKey, cube.toArray(new Vector3f[0]));
                            faceTex.put(meshKey, cubeTex.toArray(new Vector2f[0]));
                        }
                        faces.removeAll(remove);
                        // Now check if any added cubes entirely fit inside ours
                        remove.clear();
                        Vector3f testA = new Vector3f(), testB = new Vector3f(), testC = new Vector3f();
                        for (var meshKeyA : faces)
                        {
                            var mA = boxes.get(meshKeyA);
                            mA.getColumn(0, testA);
                            mA.getColumn(1, testB);
                            // Loop over others, see if
                            for (var meshKeyB : faces)
                            {
                                if (meshKeyA == meshKeyB || remove.contains(meshKeyB)) continue;
                                // If b is inside A, quit
                                var mB = boxes.get(meshKeyB);
                                mB.getColumn(0, testC);
                                boolean inside = testC.max(testB).equals(testB);
                                inside &= testC.max(testA).equals(testA);
                                if (inside) remove.add(meshKeyB);
                            }
                        }
                        faces.removeAll(remove);
                    }
                    // Now make the faces
                    for (var meshKey : faces)
                    {
                        var matTex = faceMats.get(meshKey);
                        var img = images.get(matTex);

                        int imgW = img != null ? img.getWidth() : 16;
                        int imgH = img != null ? img.getHeight() : 16;

                        int texID = textures.indexOf(matTex);
                        var meshVerts = faceVerts.get(meshKey);
                        var iter = faceModes.get(meshKey);
                        var uvs = faceTex.get(meshKey);
                        var mesh = namedMesh.get(meshKey);

                        // Transform to global coordinates
                        last.pose().mul(mesh.poseInfo.pose(), posMat);
                        mesh.poseInfo.pose().set(posMat);
                        for (Vector3f vert : meshVerts)
                        {
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

                        for (int i = 0; i < meshVerts.length; i += iter)
                        {
                            String faceKey = randomKey(keys, 8);
                            List<String> verts = new ArrayList<>();
                            Map<String, float[]> faceUVs = new HashMap<>();
                            for (int j = 0; j < iter; j++)
                            {
                                var vert = meshVerts[i + j];
                                var uv_ = uvs[i + j];
                                String vertKey = vertices.get(vert);
                                verts.add(vertKey);
                                var _uv = new Vector2f(uv_);
                                _uv.x *= imgW;
                                _uv.y *= imgH;
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
                    }
                    if (!simplifiy || !faces.isEmpty()) elements_by_id.put(element.uuid, element);
                }
                var partID = partsToUUID.get(part);
                if (!partsToGroup.containsKey(partID))
                {
                    var id = UUID.randomUUID().toString();
                    partsToGroup.put(partID, id);
                    partNameToUUID.put(part.getName(), id);
                }
                // Handle adding the group
                addElementToGroup(construct, part, partID, last, posMat);
            }
        });

        // Now make the textures
        for (int index = 0; index < textures.size(); index++)
        {
            var resource = textures.get(index);
            var image = images.get(resource);

            var texture = new BBModelTemplate.Texture();
            if (emissives.contains(resource))
            {
                texture.render_mode = "emissive";
                int end = resource.getPath().length() - 2;
                resource = ResourceLocation.fromNamespaceAndPath(resource.getNamespace(),
                        resource.getPath().substring(0, end));
            }
            texture.uuid = UUID.randomUUID().toString();
            if (resource == null)
            {
                resource = ResourceLocation.parse("null:null");
                if (!simplifiy) PokecubeAPI.LOGGER.error("Error with a texture for {}", model.name);
            }
            texture.id = "" + index;
            var path = resource.getPath().split("/");
            var location = path[path.length - 1];
            texture.name = location.replace(".png", "");
            result.textures.add(texture);

            if (image == null) continue;

            texture.width = texture.uv_width = image.getWidth();
            texture.height = texture.uv_height = image.getHeight();
            texture.file_format = "png";

            final BufferedImage img_buffer = new BufferedImage(texture.width, texture.height,
                    BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < texture.width; i++)
            {
                for (int j = 0; j < texture.height; j++)
                {
                    int abgr = image.getPixelRGBA(i, j);
                    int argb = FastColor.ARGB32.color(FastColor.ABGR32.alpha(abgr), FastColor.ABGR32.red(abgr),
                            FastColor.ABGR32.green(abgr), FastColor.ABGR32.blue(abgr));
                    img_buffer.setRGB(i, j, argb);
                }
            }
            try
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img_buffer, texture.file_format, baos);
                var asString = Base64.getEncoder().encodeToString(baos.toByteArray());
                texture.source = "data:image/png;base64," + asString;
            }
            catch (IOException ignored)
            {
            }
        }

        var animations = model.getAnimationChanger().getAnimations();
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
                anim.length = _anim.getLength() / 20f;
                for (var pair : _anim.sets.entrySet())
                {
                    if (!(pair.getValue() instanceof Animators.KeyframeAnimator frames)) continue;
                    var key = partNameToUUID.get(pair.getKey());
                    if (key == null)
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
                        if (looped)
                        {
                            list.add(channel.components().getFirst());
                        }
                        for (int i = 0; i < list.size(); i++)
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
                                if (component._rotFunctions[0] != null)
                                {
                                    point.x = component._rotFunctions[0];
                                }
                                else
                                {
                                    point.x = rotXScale * rotOffset[0];
                                }
                                if (component._rotFunctions[1] != null)
                                {
                                    point.y = component._rotFunctions[1];
                                }
                                else
                                {
                                    point.y = rotOffset[1];
                                }
                                if (component._rotFunctions[2] != null)
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
                            case "hidden":
                                frame.channel = "scale";
                                point.x = 0;
                                point.y = 0;
                                point.z = 1;
                                break;
                            }
                            if (point.x instanceof String s)
                            {
                                s = s.replace("sin(", "math.sin(");
                                s = s.replace("cos(", "math.cos(");
                                s = s.replace("*l*", "*q.anim_time*20*");
                                s = s.replace("(0.05*", "(");
                                point.x = s;
                            }
                            if (point.y instanceof String s)
                            {
                                s = s.replace("sin(", "math.sin(");
                                s = s.replace("cos(", "math.cos(");
                                s = s.replace("*l*", "*q.anim_time*20*");
                                s = s.replace("(0.05*", "(");
                                point.y = s;
                            }
                            if (point.z instanceof String s)
                            {
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

        result.outliner.addAll(outliner_by_id.values().stream().filter(g -> g._parent == null).toList());
        result.groups.addAll(groups_by_id.values());
        result.elements.addAll(elements_by_id.values());

        result.groups.sort(Comparator.comparing(a -> a.name));
        result.elements.sort(Comparator.comparing(a -> a.name));
        return result;
    }

    private static void addElementToGroup(BBConstructions construct, Part part, String partID, PoseStack.Pose last,
            Matrix4f posMat)
    {
        var elements_by_id = construct.elements_by_id;
        var groups_by_id = construct.groups_by_id;
        var outliner_by_id = construct.outliner_by_id;

        var partsToGroup = construct.partsToGroup;
        var partsToUUID = construct.partsToUUID;
        var partNameToUUID = construct.partNameToUUID;
        var partNameToOutlinerGroup = construct.partNameToOutlinerGroup;
        var locator_names = construct.locator_names;

        var groupID = partsToGroup.get(partID);
        boolean isAnimated = part.isAnimated();
        if (!part.getSubParts().isEmpty() || isAnimated)
        {
            BBModelTemplate.JsonGroup groupCoord = groups_by_id.computeIfAbsent(groupID,
                    s -> new BBModelTemplate.JsonGroup());
            BBModelTemplate.JsonGroup groupOutliner = outliner_by_id.computeIfAbsent(groupID,
                    s -> new BBModelTemplate.JsonGroup());
            groupCoord.uuid = groupID;
            groupCoord.name = part.name;

            last.pose().mul(part.getRenderPose().pose(), posMat);
            Vector4f origin = new Vector4f(0, 0, 0, 1);
            origin.mul(posMat);
            groupCoord.origin = new float[] { origin.x, origin.y, origin.z };
            groupCoord.rotation = new float[] { 0, 0, 0 };

            groupOutliner.uuid = groupID;
            if (groupOutliner.children == null) groupOutliner.children = new ArrayList<>();
            if (!groupOutliner.children.contains(partID)) groupOutliner.children.add(partID);

            // Make a locator element for each one of these, and add it to the listings
            if (!part.attachmentPoints.isEmpty())
            {
                var vMid = part.getCentre();
                part.attachmentPoints.forEach((name, matrix) -> {
                    BBModelTemplate.Element element = new BBModelTemplate.Element();
                    while (!locator_names.add(name)) name = name + ":";
                    element.uuid = UUID.randomUUID().toString();
                    element.name = name;
                    element.type = "locator";
                    element.box_uv = null;
                    vMid.length();
                    var v = matrix.getColumn(0, new Vector3f());
                    origin.set(v, 1);
                    Matrix4f mat = new Matrix4f(posMat);
                    origin.mul(mat);
                    element.position = new float[] { origin.x, origin.y, origin.z };
                    v = matrix.getColumn(1, new Vector3f());
                    element.rotation = new float[] { v.x, v.y, v.z };
                    elements_by_id.put(element.uuid, element);
                    groupOutliner.children.add(element.uuid);
                });
            }
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
                var id = UUID.randomUUID().toString();
                partsToGroup.put(parentID, id);
                partNameToUUID.put(parent.getName(), id);
            }
            var parentGroupID = partsToGroup.get(parentID);
            var parentGroup = outliner_by_id.computeIfAbsent(parentGroupID, s -> new BBModelTemplate.JsonGroup());
            var ourGroup = outliner_by_id.computeIfAbsent(groupID, s -> new BBModelTemplate.JsonGroup());
            if (!partNameToOutlinerGroup.containsKey(parent.getName()))
                partNameToOutlinerGroup.put(parent.getName(), parentGroup);
            if (parentGroup.children == null) parentGroup.children = new ArrayList<>();
            if (!parentGroup.children.contains(ourGroup)) parentGroup.children.add(ourGroup);

            // If we are animated, we make a new group for us, and add ourselves to it.
            // Otherwise we add ourself to our parent's group
            if (part.getSubParts().isEmpty() && !isAnimated && !parentGroup.children.contains(partID))
                parentGroup.children.add(partID);

            ourGroup._parent = parentGroup;
        }
    }
}
