package thut.core.client.render.mca;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.core.client.render.mca.McaXML.Buffers;
import thut.core.client.render.mca.McaXML.Children;
import thut.core.client.render.mca.McaXML.GeometryNode;
import thut.core.client.render.mca.McaXML.Rot;
import thut.core.client.render.mca.McaXML.SceneNode;
import thut.core.client.render.mca.McaXML.Translation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.x3d.X3dMesh;
import thut.core.client.render.x3d.X3dModel;
import thut.core.client.render.x3d.X3dPart;
import thut.core.client.render.x3d.X3dXML;
import thut.core.common.ThutCore;

public class McaModel extends X3dModel
{

    public McaModel(final ResourceLocation l)
    {
        super();
        this.loadModel(l);
    }

    private void addChildren(final Set<Children> set, final Children node)
    {
        if (node.geometry != null) set.add(node);
        for (final SceneNode child : node.scenes)
        {
            child.children.parent = child;
            this.addChildren(set, child.children);
        }
    }

    private Set<String> getChildren(final Children parent)
    {
        final Set<String> ret = Sets.newHashSet();
        for (final SceneNode child : parent.scenes)
            if (child.children.geometry != null) ret.add(child.name);
        return ret;
    }

    @Override
    public void loadModel(final ResourceLocation model)
    {
        this.valid = true;
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(model);
            if (res == null)
            {
                this.valid = false;
                return;
            }
            final X3dXML xml = new X3dXML(res.getInputStream());
            res.close();
            this.makeObjects(xml);
        }
        catch (final Exception e)
        {
            this.valid = false;
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
        }
    }

    HashMap<String, IExtendedModelPart> makeObjects(final McaXML xml) throws Exception
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
            final X3dPart o = new X3dPart(name);
            final List<Mesh> shapes = Lists.newArrayList();
            final X3dMesh shape = new X3dMesh(buffers.getOrder(), buffers.getVerts(), buffers.getNormals(),
                    buffers.getTex());
            shapes.add(shape);
            o.setShapes(shapes);
            ;

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
