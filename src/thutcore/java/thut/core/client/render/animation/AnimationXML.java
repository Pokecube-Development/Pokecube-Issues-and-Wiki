package thut.core.client.render.animation;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IExtendedModelPart.IPartRenderAdder;
import thut.core.common.ThutCore;
import thut.core.xml.bind.Factory;
import thut.core.xml.bind.annotation.XmlAnyAttribute;
import thut.core.xml.bind.annotation.XmlAttribute;
import thut.core.xml.bind.annotation.XmlElement;
import thut.core.xml.bind.annotation.XmlRootElement;

public class AnimationXML
{
    @XmlRootElement(name = "particle")
    public static class ParticleSource implements IPartRenderAdder
    {
        @XmlAttribute(name = "density")
        public float density = 0.1f;
        @XmlAttribute(name = "name")
        public String particle = "";
        @XmlAttribute(name = "parts")
        public String parts = "";
        @XmlAttribute(name = "velocity")
        public String velocity = "0,0,0";
        @XmlAttribute(name = "offset")
        public String offset = "0,0,0";

        public Set<String> _requiredStates = new HashSet<>();
        public Set<String> _bannedStates = new HashSet<>();

        private Set<String> _parts = new HashSet<>();

        private Vector3f _particlePosition = new Vector3f();
        private Vector3f _particleVelocity = new Vector3f();

        private ResourceLocation _particleType = null;
        private Vector3f _place = new Vector3f();
        private ParticleOptions _type = null;

        private void init()
        {
            _particleType = new ResourceLocation(particle);
            var parts = this.parts.split(":");
            _parts.clear();
            for (var p : parts) _parts.add(p);
            var _particle = ForgeRegistries.PARTICLE_TYPES.getValue(_particleType);
            if (_particle instanceof ParticleOptions type) _type = type;

            var v3 = AnimationLoader.getVector3(offset, null);
            if (v3 != null)
            {
                _particlePosition.set((float) v3.x, (float) v3.y, (float) v3.z);
            }
        }

        @Override
        public boolean shouldAddTo(IExtendedModelPart part)
        {
            if (_particleType == null) init();
            return _parts.contains(part.getName());
        }

        @Override
        public void onRender(PoseStack mat, IExtendedModelPart part)
        {
            Entity mob = part.convertToGlobal(mat, _place);
            if (mob == null || _type == null || !(mob.level instanceof ClientLevel level) || !mob.isAddedToWorld())
                return;
            if (ThutCore.getConfig().modelCullThreshold == -1) return;
            var animated = part.getAnimationHolder().get().getContext();
            var existing = animated.activeParticles().get(this);

            double rx = _place.x() + _particlePosition.x();
            double ry = _place.y() + _particlePosition.y();
            double rz = _place.z() + _particlePosition.z();

            double vx = _particleVelocity.x();
            double vy = _particleVelocity.y();
            double vz = _particleVelocity.z();

            if (existing instanceof Particle particle && particle.isAlive())
            {
                particle.setPos(rx, ry, rz);
            }
            else
            {
                var particle = Minecraft.getInstance().particleEngine.createParticle(_type, rx, ry, rz, vx, vy, vz);
                animated.activeParticles().put(this, particle);
            }
        }
    }

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
        public double opacityOffset = 1.0D;
        @XmlAttribute(name = "opacFuncs")
        public String opacFuncs = "";

        @XmlAttribute(name = "posChange")
        public String posChange = "0,0,0";
        @XmlAttribute(name = "posOffset")
        public String posOffset = "0,0,0";
        @XmlAttribute(name = "posFuncs")
        public String posFuncs = "";

        @XmlAttribute(name = "rotChange")
        public String rotChange = "0,0,0";
        @XmlAttribute(name = "rotOffset")
        public String rotOffset = "0,0,0";
        @XmlAttribute(name = "rotFuncs")
        public String rotFuncs = "";

        @XmlAttribute(name = "colChange")
        public String colChange = "0,0,0";
        @XmlAttribute(name = "colOffset")
        public String colOffset = "1,1,1";
        @XmlAttribute(name = "colFuncs")
        public String colFuncs = "";

        @XmlAttribute(name = "scaleChange")
        public String scaleChange = "0,0,0";
        @XmlAttribute(name = "scaleOffset")
        public String scaleOffset = "1,1,1";
        @XmlAttribute(name = "scaleFuncs")
        public String scaleFuncs = "";

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
        public boolean cull = false;
        @XmlAttribute(name = "shader")
        public String shader = "";
        @XmlAttribute(name = "tex")
        public String tex = "";
        @XmlAttribute(name = "height")
        public float height = -1;
        @XmlAttribute(name = "width")
        public float width = -1;
    }

    @XmlRootElement(name = "merges")
    public static class Merge
    {
        @XmlAttribute(name = "merge")
        public String merge;
        @XmlAttribute(name = "limbs")
        public String limbs;
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

        public void init()
        {
            if (this.defaults != null && !this.defaults.endsWith(".png"))
            {
                this.defaults = ThutCore.trim(this.defaults) + ".png";
            }
            this.parts.forEach(part -> {
                if (part.tex != null && !part.tex.endsWith(".png")) part.tex = ThutCore.trim(part.tex) + ".png";
            });
            this.forme.forEach(part -> {
                if (part.tex != null && !part.tex.endsWith(".png")) part.tex = ThutCore.trim(part.tex) + ".png";
            });
            this.custom.forEach(part -> {
                if (part.tex != null && !part.tex.endsWith(".png")) part.tex = ThutCore.trim(part.tex) + ".png";
            });
        }
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
        public int headAxis = 2;
        @XmlAttribute(name = "headAxis2")
        public int headAxis2 = 0;
        @XmlAttribute(name = "headCap")
        public String headCap = "-100, 100 ";
        @XmlAttribute(name = "headCap1")
        public String headCap1 = "-30, 70";
        @XmlAttribute(name = "headDir")
        public int headDir = -1;
        @XmlAttribute(name = "headDir2")
        public int headDir2 = -1;
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
        @XmlElement(name = "particle")
        public List<ParticleSource> particles = Lists.newArrayList();
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
