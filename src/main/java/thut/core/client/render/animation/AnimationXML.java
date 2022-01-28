package thut.core.client.render.animation;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import thut.core.common.ThutCore;
import thut.core.xml.bind.Factory;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class AnimationXML
{
    @XmlRootElement(name = "component")
    public static class Component
    {
        @XmlAttribute(name = "hidden")
        public boolean hidden = false;
        @XmlAttribute(name = "length")
        public int length = 0;
        @XmlAttribute(name = "name")
        public String name = "";

        @XmlAttribute(name = "opacityChange")
        public double opacityChange = 0.0D;
        @XmlAttribute(name = "opacityOffset")
        public double opacityOffset = 0.0D;

        @XmlAttribute(name = "posChange")
        public String posChange = "0,0,0";
        @XmlAttribute(name = "posOffset")
        public String posOffset = "0,0,0";

        @XmlAttribute(name = "rotChange")
        public String rotChange = "0,0,0";
        @XmlAttribute(name = "rotOffset")
        public String rotOffset = "0,0,0";

        @XmlAttribute(name = "scaleChange")
        public String scaleChange = "0,0,0";
        @XmlAttribute(name = "scaleOffset")
        public String scaleOffset = "0,0,0";

        @XmlAttribute(name = "startKey")
        public int startKey = 0;
    }

    @XmlRootElement(name = "worn")
    public static class Worn
    {
        @XmlAttribute(name = "angles")
        public String angles = "0,0,0";
        @XmlAttribute(name = "id")
        public String id = "";
        @XmlAttribute(name = "offset")
        public String offset = "0,0,0";
        @XmlAttribute(name = "parent")
        public String parent = "";
        @XmlAttribute(name = "scale")
        public String scale = "1,1,1";
    }

    @XmlRootElement(name = "part")
    public static class Part
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlElement(name = "component")
        public List<Component> components = Lists.newArrayList();
    }

    @XmlRootElement(name = "material")
    public static class Mat
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "meshs")
        public String meshs;
        @XmlAttribute(name = "alpha")
        public float alpha = 1;
        @XmlAttribute(name = "transluscent")
        public boolean transluscent = false;
        @XmlAttribute(name = "light")
        public float light = 0;
        @XmlAttribute(name = "smooth")
        public boolean smooth = false;
        @XmlAttribute(name = "cull")
        public boolean cull = true;
        @XmlAttribute(name = "shader")
        public String shader = "";
    }

    @XmlRootElement(name = "merges")
    public static class Merge
    {
        @XmlAttribute(name = "merge")
        public String merge;
    }

    @XmlRootElement(name = "subanim")
    public static class SubAnim
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "base")
        public String base;
        @XmlAttribute(name = "weight")
        public int weight = 1;
    }

    @XmlRootElement(name = "customModel")
    public static class CustomModel
    {
        @XmlAttribute(name = "model")
        public String model;
    }

    @XmlRootElement(name = "customTex")
    public static class CustomTex
    {
        @XmlAttribute(name = "default")
        public String defaults;
        @XmlAttribute(name = "smoothing")
        public String smoothing;
        @XmlElement(name = "animation")
        public List<TexAnim> anims = Lists.newArrayList();
        @XmlElement(name = "part")
        public List<TexPart> parts = Lists.newArrayList();
        @XmlElement(name = "custom")
        public List<TexCustom> custom = Lists.newArrayList();
        @XmlElement(name = "forme")
        public List<TexForm> forme = Lists.newArrayList();
        @XmlElement(name = "colour")
        public List<ColourTex> colours = Lists.newArrayList();
        @XmlElement(name = "rngfixed")
        public List<RNGFixed> rngfixeds = Lists.newArrayList();
    }

    @XmlRootElement(name = "metadata")
    public static class Metadata
    {
        @XmlAttribute(name = "head")
        public String head;
        @XmlAttribute(name = "shear")
        public String shear;
        @XmlAttribute(name = "dye")
        public String dye;
        @XmlAttribute(name = "headAxis")
        public int headAxis = 1;
        @XmlAttribute(name = "headAxis2")
        public int headAxis2 = 0;
        @XmlAttribute(name = "headCap")
        public String headCap = "-100, 100 ";
        @XmlAttribute(name = "headCap1")
        public String headCap1 = "-30, 70";
        @XmlAttribute(name = "headDir")
        public int headDir = 1;
        @XmlAttribute(name = "headDir2")
        public int headDir2 = 2;
    }

    @XmlRootElement(name = "model")
    public static class Model
    {
        @XmlElement(name = "customTex")
        public CustomTex customTex;
        @XmlElement(name = "customModel")
        public CustomModel customModel;
        @XmlElement(name = "metadata")
        public Metadata metadata;
        @XmlElement(name = "phase")
        public List<Phase> phases = Lists.newArrayList();
        @XmlElement(name = "worn")
        public List<Worn> worn = Lists.newArrayList();
        @XmlElement(name = "merges")
        public List<Merge> merges = Lists.newArrayList();
        @XmlElement(name = "material")
        public List<Mat> materials = Lists.newArrayList();
        @XmlElement(name = "subanim")
        public List<SubAnim> subanim = Lists.newArrayList();
    }

    @XmlRootElement(name = "phase")
    public static class Phase
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "type")
        public String type;
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "part")
        public List<Part> parts = Lists.newArrayList();
    }

    @XmlRootElement(name = "details")
    public static class Details
    {
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
    }

    @XmlRootElement(name = "animation")
    public static class TexAnim
    {
        @XmlAttribute(name = "diffs")
        public String diffs = "0,0";
        @XmlAttribute(name = "part")
        public String part;
        @XmlAttribute(name = "trigger")
        public String trigger;
    }

    @XmlRootElement(name = "part")
    public static class TexPart
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "tex")
        public String tex;
        @XmlAttribute(name = "smoothing")
        public String smoothing;
    }

    @XmlRootElement(name = "custom")
    public static class TexCustom
    {
        @XmlAttribute(name = "part")
        public String part;
        @XmlAttribute(name = "state")
        public String state;
        @XmlAttribute(name = "tex")
        public String tex;
    }

    @XmlRootElement(name = "forme")
    public static class TexForm
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "tex")
        public String tex;
    }

    @XmlRootElement(name = "colour")
    public static class ColourTex
    {
        @XmlAttribute(name = "forme")
        public String forme = "";
        @XmlAttribute(name = "material")
        public String material;
        @XmlAttribute(name = "red")
        public float red;
        @XmlAttribute(name = "green")
        public float green;
        @XmlAttribute(name = "blue")
        public float blue;
        @XmlAttribute(name = "alpha")
        public float alpha;
    }

    @XmlRootElement(name = "rngfixed")
    public static class RNGFixed
    {
        @XmlAttribute(name = "forme")
        public String forme = "";
        @XmlAttribute(name = "material")
        public String material;
        @XmlAttribute(name = "seed")
        public int seed;
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        @XmlElement(name = "model")
        public Model model;
    }

    public static XMLFile load(final InputStream res)
    {
        XMLFile database = null;
        try
        {
            database = Factory.make(res, XMLFile.class);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error parsing xml", e);
        }
        return database;
    }
}
