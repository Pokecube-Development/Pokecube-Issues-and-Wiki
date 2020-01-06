package thut.core.common.xml;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

public class AnimationXML
{
    @XmlRootElement(name = "component")
    public static class AnimationComponent
    {
        @XmlAttribute(name = "hidden")
        boolean hidden = false;
        @XmlAttribute(name = "length")
        int     length = 0;
        @XmlAttribute(name = "name")
        String  name   = "";

        @XmlAttribute(name = "opacityChange")
        double opacityChange = 0.0D;
        @XmlAttribute(name = "opacityOffset")
        double opacityOffset = 0.0D;

        @XmlAttribute(name = "posChange")
        String posChange = "0,0,0";
        @XmlAttribute(name = "posOffset")
        String posOffset = "0,0,0";

        @XmlAttribute(name = "rotChange")
        String rotChange = "0,0,0";
        @XmlAttribute(name = "rotOffset")
        String rotOffset = "0,0,0";

        @XmlAttribute(name = "scaleChange")
        String scaleChange = "0,0,0";
        @XmlAttribute(name = "scaleOffset")
        String scaleOffset = "0,0,0";

        @XmlAttribute(name = "startKey")
        int startKey = 0;
    }

    @XmlRootElement(name = "part")
    public static class AnimationPart
    {
        @XmlAttribute(name = "part")
        String part;
    }

    @XmlRootElement(name = "customTex")
    public static class CustomTex
    {
        @XmlAttribute(name = "default")
        String             defaults;
        @XmlAttribute(name = "smoothing")
        String             smoothing;
        @XmlAttribute(name = "animation")
        List<TexAnimation> texAnimations = Lists.newArrayList();
    }

    @XmlRootElement(name = "metadata")
    public static class Metadata
    {
        @XmlAttribute(name = "head")
        String head;
        @XmlAttribute(name = "headAxis")
        int    headAxis = 1;
        @XmlAttribute(name = "headCap")
        String headCap;
        @XmlAttribute(name = "headCap1")
        String headCap1;
        @XmlAttribute(name = "headDir")
        int    headDir  = 1;
    }

    @XmlRootElement(name = "model")
    public static class Model
    {
        @XmlElement(name = "customTex")
        CustomTex   customTex;
        @XmlElement(name = "metadata")
        Metadata    metadata;
        @XmlElement(name = "phase")
        List<Phase> phases = Lists.newArrayList();
    }

    @XmlRootElement(name = "phase")
    public static class Phase
    {
        @XmlElement(name = "name")
        String             name;
        @XmlElement(name = "type")
        String             type;
        @XmlAnyAttribute
        Map<QName, String> values;
    }

    @XmlRootElement(name = "animation")
    public static class TexAnimation
    {
        @XmlAttribute(name = "diffs")
        String diffs;
        @XmlAttribute(name = "part")
        String part;
        @XmlAttribute(name = "tex")
        String tex;
        @XmlAttribute(name = "trigger")
        String trigger;
    }

    @XmlRootElement(name = "ModelAnimator")
    public static class XMLFile
    {
        @XmlElement(name = "model")
        Model model;
    }
}
