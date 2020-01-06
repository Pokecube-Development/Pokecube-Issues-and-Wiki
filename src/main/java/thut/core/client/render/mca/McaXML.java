package thut.core.client.render.mca;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.TextureCoordinate;
import thut.core.client.render.x3d.ModelFormatException;

public class McaXML
{
    @XmlRootElement(name = "mca.project.MCStandard.animation.Animation")
    public static class Animation
    {

    }

    @XmlRootElement(name = "animations")
    public static class Animations
    {

    }

    @XmlRootElement(name = "buffers")
    public static class Buffers
    {
        @XmlElement(name = "MapEntry")
        List<MapEntry> entries = Lists.newArrayList();

        public Vertex[] getNormals()
        {
            for (final MapEntry entry : this.entries)
                if (entry.savable.type.equals("Normal")) return entry.savable.getFloats();
            return null;
        }

        public Integer[] getOrder()
        {
            for (final MapEntry entry : this.entries)
                if (entry.savable.type.equals("Index"))
                {
                    final String[] offset = entry.savable.data2.data.split(" ");
                    final Integer[] order = new Integer[offset.length];
                    for (int i = 0; i < offset.length; i++)
                    {
                        final String s1 = offset[i];
                        order[i] = Integer.parseInt(s1);
                    }
                    return order;
                }
            return null;
        }

        public TextureCoordinate[] getTex()
        {
            for (final MapEntry entry : this.entries)
                if (entry.savable.type.equals("TexCoord")) return entry.savable.getTexture(entry.savable.data1.data);
            return null;
        }

        public Vertex[] getVerts()
        {
            for (final MapEntry entry : this.entries)
                if (entry.savable.type.equals("Position")) return entry.savable.getFloats();
            return null;
        }
    }

    @XmlRootElement(name = "center")
    public static class Center
    {
        @XmlAttribute(name = "x")
        float x = 0;
        @XmlAttribute(name = "y")
        float y = 0;
        @XmlAttribute(name = "z")
        float z = 0;;
    }

    @XmlRootElement(name = "children")
    public static class Children
    {
        @XmlAttribute(name = "size")
        int             size;
        @XmlElement(name = "com.jme3.scene.Geometry")
        GeometryNode    geometry;
        @XmlElement(name = "com.jme3.scene.Node")
        List<SceneNode> scenes = Lists.newArrayList();
        SceneNode       parent;
    }

    @XmlRootElement(name = "dataFloat")
    public static class DataFloat
    {
        @XmlAttribute(name = "data")
        String data;
        @XmlAttribute(name = "size")
        int    size;
    }

    @XmlRootElement(name = "dataUnsignedInt")
    public static class DataUnsignedInt
    {
        @XmlAttribute(name = "data")
        String data;
        @XmlAttribute(name = "size")
        int    size;
    }

    @XmlRootElement(name = "com.jme3.scene.Geometry")
    public static class GeometryNode
    {
        @XmlElement(name = "mesh")
        Mesh       mesh;
        @XmlElement(name = "world_bound")
        WorldBound bound;
    }

    @XmlRootElement(name = "MapEntry")
    public static class MapEntry
    {
        @XmlAttribute(name = "key")
        String  key;
        @XmlElement(name = "Savable")
        Savable savable;
    }

    @XmlRootElement(name = "mca.project.MCStandard.data.ProjectSavable")
    public static class Mca
    {
        @XmlElement(name = "modelNode")
        ModelNode node;
    }

    @XmlRootElement(name = "mesh")
    public static class Mesh
    {
        @XmlElement(name = "buffers")
        Buffers    buffers;
        @XmlElement(name = "modelBound")
        ModelBound bound;
    }

    @XmlRootElement(name = "modelBound")
    public static class ModelBound
    {
        @XmlAttribute(name = "xExtent")
        float  x = 0;
        @XmlAttribute(name = "yExtent")
        float  y = 0;
        @XmlAttribute(name = "zExtent")
        float  z = 0;;
        @XmlElement(name = "center")
        Center center;
    }

    @XmlRootElement(name = "modelNode")
    public static class ModelNode
    {
        @XmlElement(name = "world_bound")
        WorldBound bound;
        @XmlElement(name = "children")
        Children   children;
    }

    @XmlRootElement(name = "rot")
    public static class Rot
    {
        @XmlAttribute(name = "x")
        float x = 0;
        @XmlAttribute(name = "y")
        float y = 0;
        @XmlAttribute(name = "z")
        float z = 0;
        @XmlAttribute(name = "w")
        float w = 0;
    }

    @XmlRootElement(name = "Savable")
    public static class Savable
    {
        private static Vertex[] parseVertices(String line, String type) throws ModelFormatException
        {
            final ArrayList<Vertex> ret = new ArrayList<>();
            final float scale = type.equals("Position") ? 1 / 16f : 1;
            final String[] points = line.split(" ");
            if (points.length % 3 != 0) throw new ModelFormatException(
                    "Invalid number of elements in the points string");
            for (int i = 0; i < points.length; i += 3)
            {
                final Vertex toAdd = new Vertex(Float.parseFloat(points[i]) * scale, Float.parseFloat(points[i + 1])
                        * scale, Float.parseFloat(points[i + 2]) * scale);
                ret.add(toAdd);
            }
            return ret.toArray(new Vertex[ret.size()]);
        }

        @XmlAttribute(name = "buffer_type")
        String    type;
        @XmlElement(name = "dataFloat")
        DataFloat data1;

        @XmlElement(name = "dataUnsignedInt")
        DataUnsignedInt data2;

        public Vertex[] getFloats()
        {
            return Savable.parseVertices(this.data1.data, this.type);
        }

        public TextureCoordinate[] getTexture(String point)
        {
            final ArrayList<TextureCoordinate> ret = new ArrayList<>();
            final String[] points = point.split(" ");
            if (points.length % 2 != 0) throw new ModelFormatException(
                    "Invalid number of elements in the points string " + points.length);
            for (int i = 0; i < points.length; i += 2)
            {
                final TextureCoordinate toAdd = new TextureCoordinate(Float.parseFloat(points[i]), 1 - Float.parseFloat(
                        points[i + 1]));
                ret.add(toAdd);
            }
            return ret.toArray(new TextureCoordinate[ret.size()]);
        }
    }

    @XmlRootElement(name = "com.jme3.scene.Node")
    public static class SceneNode
    {
        @XmlAttribute(name = "name")
        String     name;
        @XmlElement(name = "world_bound")
        WorldBound bound;
        @XmlElement(name = "transform")
        Transform  transform;
        @XmlElement(name = "children")
        Children   children;
    }

    @XmlRootElement(name = "transform")
    public static class Transform
    {
        @XmlElement(name = "rot")
        Rot         rotation;
        @XmlElement(name = "translation")
        Translation translation;
    }

    @XmlRootElement(name = "translation")
    public static class Translation
    {
        @XmlAttribute(name = "x")
        float x = 0;
        @XmlAttribute(name = "y")
        float y = 0;
        @XmlAttribute(name = "z")
        float z = 0;
    }

    @XmlRootElement(name = "world_bound")
    public static class WorldBound
    {
        @XmlAttribute(name = "xExtent")
        float  x = 0;
        @XmlAttribute(name = "yExtent")
        float  y = 0;
        @XmlAttribute(name = "zExtent")
        float  z = 0;;
        @XmlElement(name = "center")
        Center center;
    }

    public Mca model;

    public McaXML(InputStream stream) throws JAXBException
    {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Mca.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        this.model = (Mca) unmarshaller.unmarshal(new InputStreamReader(stream));
    }
}
