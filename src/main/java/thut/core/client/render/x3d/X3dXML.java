package thut.core.client.render.x3d;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.common.ThutCore;
import thut.core.xml.bind.Factory;

public class X3dXML
{
    @XmlRootElement(name = "Appearance")
    public static class Appearance
    {
        @XmlElement(name = "ImageTexture")
        ImageTexture     tex;
        @XmlElement(name = "Material")
        Material         material;
        @XmlElement(name = "TextureTransform")
        TextureTransform texTransform;
    }

    @XmlRootElement(name = "Group")
    public static class Group
    {
        @XmlAttribute(name = "DEF")
        String      DEF;
        @XmlElement(name = "Shape")
        List<Shape> shapes = Lists.newArrayList();
    }

    @XmlRootElement(name = "ImageTexture")
    public static class ImageTexture
    {
        @XmlAttribute(name = "DEF")
        String DEF;
    }

    @XmlRootElement(name = "IndexedTriangleSet")
    public static class IndexedTriangleSet
    {
        @XmlRootElement(name = "Coordinate")
        public static class Coordinate
        {
            @XmlAttribute(name = "point")
            String point;

            public Vertex[] getVertices()
            {
                return IndexedTriangleSet.parseVertices(this.point);
            }
        }

        @XmlRootElement(name = "Normal")
        public static class Normal
        {
            @XmlAttribute(name = "vector")
            String vector;

            public Vertex[] getNormals()
            {
                return IndexedTriangleSet.parseVertices(this.vector);
            }
        }

        @XmlRootElement(name = "TextureCoordinate")
        public static class TextureCoordinate
        {
            @XmlAttribute(name = "point")
            String point;

            public thut.core.client.render.texturing.TextureCoordinate[] getTexture()
            {
                final ArrayList<thut.core.client.render.texturing.TextureCoordinate> ret = new ArrayList<>();
                final String[] points = this.point.split(" ");
                if (points.length % 2 != 0) throw new ModelFormatException(
                        "Invalid number of elements in the points string " + points.length);
                for (int i = 0; i < points.length; i += 2)
                {
                    final thut.core.client.render.texturing.TextureCoordinate toAdd = new thut.core.client.render.texturing.TextureCoordinate(
                            Float.parseFloat(points[i]), 1 - Float.parseFloat(points[i + 1]));
                    ret.add(toAdd);
                }
                return ret.toArray(new thut.core.client.render.texturing.TextureCoordinate[ret.size()]);
            }
        }

        private static Vertex[] parseVertices(final String line) throws ModelFormatException
        {
            final ArrayList<Vertex> ret = new ArrayList<>();

            final String[] points = line.split(" ");
            if (points.length % 3 != 0) throw new ModelFormatException(
                    "Invalid number of elements in the points string");
            for (int i = 0; i < points.length; i += 3)
            {
                final Vertex toAdd = new Vertex(Float.parseFloat(points[i]), Float.parseFloat(points[i + 1]), Float
                        .parseFloat(points[i + 2]));
                ret.add(toAdd);
            }
            return ret.toArray(new Vertex[ret.size()]);
        }

        @XmlAttribute(name = "solid")
        boolean solid;

        @XmlAttribute(name = "normalPerVertex")
        boolean normalPerVertex;

        @XmlAttribute(name = "index")
        String index;

        @XmlElement(name = "Coordinate")
        Coordinate        points;
        @XmlElement(name = "Normal")
        Normal            normals;
        @XmlElement(name = "TextureCoordinate")
        TextureCoordinate textures;

        public Vertex[] getNormals()
        {
            return this.normals.getNormals();
        }

        public Integer[] getOrder()
        {
            final String[] offset = this.index.split(" ");
            final Integer[] order = new Integer[offset.length];
            for (int i = 0; i < offset.length; i++)
            {
                final String s1 = offset[i];
                order[i] = Integer.parseInt(s1);
            }
            return order;
        }

        public thut.core.client.render.texturing.TextureCoordinate[] getTexture()
        {
            if (this.textures == null) return null;
            return this.textures.getTexture();
        }

        public Vertex[] getVertices()
        {
            return this.points.getVertices();
        }
    }

    @XmlRootElement(name = "Material")
    public static class Material
    {
        @XmlAttribute(name = "DEF")
        String           DEF;
        @XmlAttribute(name = "USE")
        String           USE;
        @XmlAttribute(name = "diffuseColor")
        private String   diffuseColor;
        @XmlAttribute(name = "specularColor")
        private String   specularColor;
        @XmlAttribute(name = "emissiveColor")
        private String   emissiveColor;
        @XmlAttribute(name = "ambientIntensity")
        float            ambientIntensity;
        @XmlAttribute(name = "shininess")
        float            shininess;
        @XmlAttribute(name = "transparency")
        float            transparency;
        private Vector3f diffuse;

        private Vector3f specular;

        private Vector3f emissive;

        public Vector3f getDiffuse()
        {
            if (this.diffuse == null) this.diffuse = X3dXML.fromString(this.diffuseColor);
            return this.diffuse;
        }

        public Vector3f getEmissive()
        {
            if (this.emissive == null) this.emissive = X3dXML.fromString(this.emissiveColor);
            return this.emissive;
        }

        public Vector3f getSpecular()
        {
            if (this.specular == null) this.specular = X3dXML.fromString(this.specularColor);
            return this.specular;
        }
    }

    @XmlRootElement(name = "Scene")
    public static class Scene
    {
        @XmlElement(name = "Transform")
        List<Transform> transforms = Lists.newArrayList();
    }

    @XmlRootElement(name = "Shape")
    public static class Shape
    {
        @XmlElement(name = "Appearance")
        Appearance         appearance;
        @XmlElement(name = "IndexedTriangleSet")
        IndexedTriangleSet triangleSet;
    }

    @XmlRootElement(name = "TextureTransform")
    public static class TextureTransform
    {
        @XmlAttribute(name = "translation")
        String translation;
        @XmlAttribute(name = "scale")
        String scale;
        @XmlAttribute(name = "rotation")
        float  rotation;
    }

    @XmlRootElement(name = "Transform")
    public static class Transform
    {
        @XmlAttribute(name = "DEF")
        String          DEF;
        @XmlAttribute(name = "translation")
        String          translation;
        @XmlAttribute(name = "scale")
        String          scale;
        @XmlAttribute(name = "rotation")
        String          rotation;
        @XmlElement(name = "Transform")
        List<Transform> transforms = Lists.newArrayList();
        @XmlElement(name = "Group")
        Group           group;

        public Set<String> getChildNames()
        {
            final Set<String> ret = Sets.newHashSet();
            for (final Transform t : this.transforms)
                if (t.getGroupName() != null) ret.add(ThutCore.trim(t.getGroupName()));
            return ret;
        }

        public String getGroupName()
        {
            if (this.group == null && this.getIfsTransform() != this) return this.getIfsTransform().getGroupName();
            if (this.group == null || this.group.DEF == null) return this.getIfsTransform().DEF.replace(
                    "_ifs_TRANSFORM", "");
            return this.group.DEF.substring("group_ME_".length());
        }

        public Transform getIfsTransform()
        {
            if (this.DEF.endsWith("ifs_TRANSFORM")) return this;

            for (final Transform t : this.transforms)
                if (t.DEF.equals(this.DEF.replace("_TRANSFORM", "_ifs_TRANSFORM"))) return t;
            return null;
        }

        @Override
        public String toString()
        {
            return this.DEF + " " + this.transforms;
        }
    }

    @XmlRootElement(name = "X3D")
    public static class X3D
    {
        @XmlElement(name = "Scene")
        Scene scene;
    }

    private static Vector3f fromString(String vect)
    {
        if (vect == null) vect = "0 0 0";
        final String[] var = vect.split(" ");
        return new Vector3f(Float.parseFloat(var[0]), Float.parseFloat(var[1]), Float.parseFloat(var[2]));
    }

    public X3D model;

    public X3dXML(final InputStream stream)
    {
        try
        {
            this.model = Factory.make(stream, X3D.class);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}
