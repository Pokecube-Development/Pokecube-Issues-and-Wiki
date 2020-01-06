package thut.core.client.render.mca;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import thut.core.client.render.mca.McaXML.Buffers;
import thut.core.client.render.mca.McaXML.Children;
import thut.core.client.render.mca.McaXML.GeometryNode;
import thut.core.client.render.mca.McaXML.Rot;
import thut.core.client.render.mca.McaXML.SceneNode;
import thut.core.client.render.mca.McaXML.Translation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.Shape;
import thut.core.client.render.x3d.X3dModel;
import thut.core.client.render.x3d.X3dObject;

public class McaModel extends X3dModel
{
    // public McaModel(ResourceLocation l)
    // {
    // super();
    // loadModel(l);
    // }

    public McaModel(InputStream l)
    {
        super();
        this.loadModel(l);
    }

    // public void loadModel(ResourceLocation model)
    // {
    // try
    // {
    // IResource res =
    // Minecraft.getInstance().getResourceManager().getResource(model);
    // InputStream stream = res.getInputStream();
    // McaXML xml = new McaXML(stream);
    // makeObjects(xml);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }

    private void addChildren(Set<Children> set, Children node)
    {
        if (node.geometry != null) set.add(node);
        for (final SceneNode child : node.scenes)
        {
            child.children.parent = child;
            this.addChildren(set, child.children);
        }
    }

    private Set<String> getChildren(Children parent)
    {
        final Set<String> ret = Sets.newHashSet();
        for (final SceneNode child : parent.scenes)
            if (child.children.geometry != null) ret.add(child.name);
        return ret;
    }

    public void loadModel(InputStream stream)
    {
        try
        {
            final McaXML xml = new McaXML(stream);
            this.makeObjects(xml);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    HashMap<String, IExtendedModelPart> makeObjects(McaXML xml) throws Exception
    {
        final Set<Children> scenes = Sets.newHashSet();
        this.addChildren(scenes, xml.model.node.children);
        final Map<String, Set<String>> childMap = Maps.newHashMap();

        for (final Children node : scenes)
        {
            final GeometryNode geom = node.geometry;
            final Translation trans = node.parent.transform.translation;
            final Rot rot = node.parent.transform.rotation;
            final Buffers buffers = geom.mesh.buffers;
            final String name = node.parent.name;
            final X3dObject o = new X3dObject(name);
            final List<Shape> shapes = Lists.newArrayList();
            final Shape shape = new Shape(buffers.getOrder(), buffers.getVerts(), buffers.getNormals(), buffers
                    .getTex());
            shapes.add(shape);
            o.shapes = shapes;

            // o.offset.set(bound.x / 16f, bound.y / 16f, bound.z / 16f);
            if (trans != null) o.offset.set(trans.x / 16f, trans.y / 16f, trans.z / 16f);
            if (rot != null) o.rotations.set(rot.x, rot.y, rot.z, rot.w);

            final Set<String> children = this.getChildren(node);
            if (!children.isEmpty()) childMap.put(name, children);
            this.parts.put(name, o);
        }
        System.out.println(childMap.size());
        System.out.println(this.parts.size() + " parts");
        for (final Map.Entry<String, Set<String>> entry : childMap.entrySet())
        {
            final String key = entry.getKey();

            if (this.parts.get(key) != null)
            {
                final IExtendedModelPart part = this.parts.get(key);
                for (final String s : entry.getValue())
                    if (this.parts.get(s) != null && this.parts.get(s) != part) part.addChild(this.parts.get(s));
            }
        }
        return this.parts;
    }
}
