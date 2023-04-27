package thut.core.client.render.texturing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.ThutCaps;
import thut.api.entity.IMobTexturable;
import thut.core.client.render.animation.AnimationXML.ColourTex;
import thut.core.client.render.animation.AnimationXML.CustomTex;
import thut.core.client.render.animation.AnimationXML.Phase;
import thut.core.client.render.animation.AnimationXML.RNGFixed;
import thut.core.client.render.animation.AnimationXML.TexAnim;
import thut.core.client.render.animation.AnimationXML.TexCustom;
import thut.core.client.render.animation.AnimationXML.TexForm;
import thut.core.client.render.animation.AnimationXML.TexPart;
import thut.core.client.render.texturing.states.Colour;
import thut.core.client.render.texturing.states.RandomFixed;
import thut.core.client.render.texturing.states.RandomState;
import thut.core.client.render.texturing.states.Sequence;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class TextureHelper implements IPartTexturer
{

    private static class TexState
    {
        Map<String, double[]> infoStates = new Object2ObjectOpenHashMap<>();
        List<RandomState> randomStates = new ArrayList<>();
        Sequence sequence = null;
        // TODO way to handle cheaning this up.
        Map<Integer, RandomState> running = new Object2ObjectOpenHashMap<>();
        Map<Integer, Integer> setTimes = new Object2ObjectOpenHashMap<>();

        void addState(final String trigger, final String[] diffs)
        {
            final double[] arr = new double[diffs.length];
            for (int i = 0; i < arr.length; i++) arr[i] = Double.parseDouble(diffs[i].trim());

            if (trigger.contains("random")) this.randomStates.add(new RandomState(trigger, arr));
            else if (trigger.equals("sequence") || trigger.equals("time")) this.sequence = new Sequence(arr);
            else if (this.parseState(trigger, arr))
            {

            }
            else new NullPointerException("No Template found for " + trigger).printStackTrace();
        }

        boolean applyState(final double[] toFill, final IMobTexturable mob)
        {
            double dx = 0;
            double dy = 0;
            toFill[0] = dx;
            toFill[1] = dy;
            final List<String> states = mob.getTextureStates();
            for (final String state : states) if (this.infoStates.containsKey(state))
            {
                final double[] arr = this.infoStates.get(state);
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }

            if (this.running.containsKey(mob.getEntity().getId()))
            {
                final RandomState run = this.running.get(mob.getEntity().getId());
                final double[] arr = run.arr;
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                if (mob.getEntity().tickCount > this.setTimes.get(mob.getEntity().getId()) + run.duration)
                {
                    this.running.remove(mob.getEntity().getId());
                    this.setTimes.remove(mob.getEntity().getId());
                }
                return true;
            }
            // Randomise the order of this before checking, as they use the same
            // RNG for the seed, this makes it more fair that one will match.
            Collections.shuffle(this.randomStates);
            for (final RandomState state : this.randomStates)
            {
                if (state.apply(toFill, mob))
                {
                    this.running.put(mob.getEntity().getId(), state);
                    this.setTimes.put(mob.getEntity().getId(), mob.getEntity().tickCount);
                    return true;
                }
            }
            if (this.sequence != null && this.sequence.shift)
            {
                final int tick = mob.getEntity().tickCount % (this.sequence.arr.length / 2);
                dx = this.sequence.arr[tick * 2];
                dy = this.sequence.arr[tick * 2 + 1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }
            return false;
        }

        String modifyTexture(final IMobTexturable mob)
        {
            if (this.sequence != null && !this.sequence.shift)
            {
                final int tick = mob.getEntity().tickCount % (this.sequence.arr.length / 2);
                final int dx = (int) this.sequence.arr[tick * 2];
                return "" + dx;
            }
            return null;
        }

        private boolean parseState(String trigger, final double[] arr)
        {
            if (trigger != null) trigger = ThutCore.trim(trigger);
            else return false;
            this.infoStates.put(trigger, arr);
            return true;
        }
    }

    protected IMobTexturable mob;
    /** Map of part/material name -> texture name */
    Map<String, String> texNames = new Object2ObjectOpenHashMap<>();
    /** Map of part/material name -> map of custom state -> texture name */
    Map<String, Map<String, String>> texNames2 = new Object2ObjectOpenHashMap<>();
    public ResourceLocation default_tex;
    String default_path;

    boolean default_flat = true;

    /** Map of part/material name -> resource location */
    Map<String, ResourceLocation> texMap = new Object2ObjectOpenHashMap<>();

    Map<String, TexState> texStates = new Object2ObjectOpenHashMap<>();

    Map<String, Set<RandomFixed>> fixedOffsets = new Object2ObjectOpenHashMap<>();

    Map<String, Set<Colour>> colours = new Object2ObjectOpenHashMap<>();

    Map<String, String> formeMap = new Object2ObjectOpenHashMap<>();

    public TextureHelper()
    {}

    @Override
    public void reset()
    {
        this.texNames.clear();
        this.texNames2.clear();
        this.texStates.clear();
        this.formeMap.clear();
        this.texMap.clear();
        this.fixedOffsets.clear();
        this.colours.clear();
        this.default_flat = true;
        this.default_path = null;
        this.default_tex = null;
    }

    @Override
    public void init(final CustomTex customTex)
    {
        if (customTex == null) return;
        if (customTex.defaults != null) this.default_path = customTex.defaults;
        if (customTex.smoothing != null)
        {
            final boolean flat = !customTex.smoothing.equalsIgnoreCase("smooth");
            this.default_flat = flat;
        }
        this.clear();
        for (final TexAnim anim : customTex.anims)
        {
            final String name = ThutCore.trim(anim.part);
            final String trigger = anim.trigger.trim();
            final String[] diffs = anim.diffs.trim().split(",");
            TexState states = this.texStates.get(name);
            if (states == null) this.texStates.put(name, states = new TexState());
            states.addState(trigger, diffs);
        }
        for (final TexPart anim : customTex.parts)
        {
            final String name = ThutCore.trim(anim.name);
            final String partTex = anim.tex;
            this.addMapping(name, partTex);
        }
        for (final TexCustom anim : customTex.custom)
        {
            final String name = ThutCore.trim(anim.part);
            final String state = ThutCore.trim(anim.state);
            final String partTex = anim.tex;
            this.addCustomMapping(name, state, partTex);
        }
        for (final TexForm anim : customTex.forme)
        {
            final String name = ThutCore.trim(anim.name);
            final String tex = anim.tex;
            this.formeMap.put(name, tex);
        }
        for (final RNGFixed state : customTex.rngfixeds)
        {
            final RandomFixed s = new RandomFixed();
            s.seedModifier = state.seed;
            Set<RandomFixed> set = this.fixedOffsets.get(state.material);
            if (set == null) this.fixedOffsets.put(state.material, set = Sets.newHashSet());
            set.add(s);
        }
        for (final ColourTex state : customTex.colours)
        {
            final Colour s = new Colour();
            s.alpha = state.alpha;
            s.red = state.red;
            s.green = state.green;
            s.blue = state.blue;
            s.forme = state.forme;
            Set<Colour> set = this.colours.get(state.material);
            if (set == null) this.colours.put(state.material, set = Sets.newHashSet());
            set.add(s);
        }
    }

    private void clear()
    {
        this.texMap.clear();
        this.texStates.clear();
        this.formeMap.clear();
        this.texNames.clear();
        this.texNames2.clear();
    }

    @Override
    public void modifiyRGBA(final String part, final int[] rgbaIn)
    {
        for (final Colour state : this.colours.getOrDefault(part, Collections.emptySet()))
            state.apply(rgbaIn, this.mob);
    }

    @Override
    public void addCustomMapping(final String part, final String state, final String tex)
    {
        Map<String, String> partMap = this.texNames2.get(part);
        if (partMap == null)
        {
            partMap = new Object2ObjectOpenHashMap<>();
            this.texNames2.put(part, partMap);
        }
        partMap.put(state, tex);
    }

    @Override
    public void addMapping(final String part, final String tex)
    {
        this.texNames.put(part, tex);
    }

    @Override
    public ResourceLocation getTexture(final String part, final ResourceLocation default_)
    {
        if (this.mob == null) return default_;
        ResourceLocation tex = this.bindPerState(part);
        if (tex != null) return tex;
        final String defaults = this.formeMap.getOrDefault(this.mob.getForm(), this.default_path);
        final String texName = this.texNames.containsKey(part) ? this.texNames.get(part) : defaults;
        if (texName == null || texName.trim().isEmpty()) this.texNames.put(part, defaults);
        tex = this.getResource(texName);
        TexState state;
        String texMod;
        if ((state = this.texStates.get(part)) != null && (texMod = state.modifyTexture(this.mob)) != null)
            tex = this.getResource(tex.getPath() + texMod);
        tex = this.mob.preApply(tex);
        return tex;
    }

    @Override
    public void bindObject(final Object thing)
    {
        this.mob = null;
        if (thing instanceof ICapabilityProvider cap) this.mob = cap.getCapability(ThutCaps.MOBTEX_CAP).orElse(null);
        if (this.mob == null && thing instanceof Entity e) this.mob = new IMobTexturable()
        {
            Entity entity = e;
            String modid = RegHelper.getKey(this.entity.getType()).getNamespace();

            Map<ResourceLocation, ResourceLocation> remapped = new Object2ObjectOpenHashMap<>();

            @Override
            public Entity getEntity()
            {
                return this.entity;
            }

            @Override
            public String getModId()
            {
                return this.modid;
            }

            @Override
            public ResourceLocation preApply(final ResourceLocation in)
            {
                if (this.remapped.containsKey(in)) return this.remapped.get(in);
                if (!in.getPath().contains(".png"))
                {
                    final ResourceLocation updated = new ResourceLocation(in.getNamespace(),
                            "entity/textures/" + in.getPath() + ".png");
                    this.remapped.put(in, updated);
                }
                return this.remapped.getOrDefault(in, IMobTexturable.super.preApply(in));
            }
        };

        if (this.mob != null)
        {
            String form = mob.getForm();
            final String defaults = this.formeMap.getOrDefault(form, this.default_path);
            this.default_tex = this.getResource(defaults);
            this.default_path = this.default_tex.getPath();
        }
    }

    private ResourceLocation bindPerState(final String part)
    {
        final Map<String, String> partNames = this.texNames2.get(part);
        if (partNames == null || mob == null) return null;
        final List<String> states = this.mob.getTextureStates();
        for (final String key : partNames.keySet()) if (states.contains(key))
        {
            final String texKey = part + key;
            String tex;
            if ((tex = this.texNames.get(texKey)) != null)
            {}
            else
            {
                tex = partNames.get(key);
                this.texNames.put(texKey, tex);
            }
            TexState state;
            String texMod;
            if ((state = this.texStates.get(part)) != null && (texMod = state.modifyTexture(this.mob)) != null)
                tex = tex + texMod;
            return this.getResource(tex);
        }
        return null;
    }

    private ResourceLocation getResource(final String tex)
    {
        if (tex == null) return this.mob.getTexture(null);
        else if (tex.contains(":")) return new ResourceLocation(tex);
        else return new ResourceLocation(this.mob.getModId(), tex);
    }

    @Override
    public boolean hasMapping(final String part)
    {
        return this.texNames.containsKey(part) || this.default_tex != null;
    }

    @Override
    public boolean shiftUVs(final String part, final double[] toFill)
    {
        toFill[0] = toFill[1] = 0;
        if (this.mob == null) return false;
        final Set<RandomFixed> offsets = this.fixedOffsets.getOrDefault(part, Collections.emptySet());
        for (final RandomFixed state : offsets) state.applyState(toFill, this.mob);
        if (offsets.size() > 0) return true;
        TexState state;
        if ((state = this.texStates.get(part)) != null) return state.applyState(toFill, this.mob);
        return false;
    }

    @Override
    public void applyTexturePhase(final Phase phase)
    {
        if (this.mob != null) this.mob.applyTexturePhase(phase);
    }

}