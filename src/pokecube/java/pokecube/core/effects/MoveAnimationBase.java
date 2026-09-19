package pokecube.core.effects;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import pokecube.api.PokecubeAPI;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.mutators.EffectMutator;
import pokecube.api.moves.MoveEntry;
import pokecube.api.effects.IAnimatedEffects;
import thut.api.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MoveAnimationBase implements IAnimatedEffects
{
    public static class Values
    {
        public String particle = "misc";

        public int rgba = 0xFFFFFFFF;
        public int duration = 5;
        public int applyAfter = 0;
        public int lifetime = 5;

        public boolean customColour = false;
        public boolean flat = false;
        public boolean reverse = false;
        public boolean absolute = false;

        public float density = 1;
        public float width = 1;
        public float angle = 0;

        public String mutator = "";
        public List<String> mutators = new ArrayList<>();

        public String rgba_string = null;
        public String reference = null;

        public String f_radial;
        public String f_phi;
        public String f_theta;

        public boolean horizontal = true;

        public String f_x;
        public String f_y;
        public String f_z;

        public String v_x;
        public String v_y;
        public String v_z;

        public List<EffectMutator> _mutators = new ArrayList<>();

        public void applyMutators(EffectPacketInfo info)
        {
            for (var m : _mutators) m.mutate(info);
        }

        public void addMutator(EffectMutator mutator)
        {
            if (!_mutators.contains(mutator)) _mutators.add(mutator);
        }
    }

    public Values values = new Values();
    protected boolean loaded = false;
    // This should be false for things like terrain moves
    protected boolean applyOnMoveUse = true;

    public boolean onMoveUse()
    {
        return applyOnMoveUse;
    }

    @Override
    public int getApplicationTick()
    {
        return this.values.applyAfter;
    }

    public int getColourFromMove(final MoveEntry move, int alpha)
    {
        alpha = Math.min(255, alpha);
        return move.getType(null).colour + 0x01000000 * alpha;
    }

    @Override
    public void tickMutators(EffectPacketInfo info)
    {
        this.values.applyMutators(info);
    }

    @Override
    public int getDuration()
    {
        return this.values.duration;
    }

    protected void loadValues(JsonObject preset)
    {
        if (preset != null)
        {
            try
            {
                this.values = JsonUtil.gson.fromJson(preset, Values.class);
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error(e);
                values = new Values();
            }
        }
        else values = new Values();
        loaded = true;
        this.values._mutators.clear();
        if (!values.mutator.isBlank())
        {
            var key = ResourceLocation.parse(values.mutator);
            var mutator = ParticleEffects.MUTATOR_REGISTRY.get(key);
            if (mutator != null) this.values._mutators.add(mutator);
        }
        this.values.mutators.forEach(name -> {
            var key = ResourceLocation.parse(name);
            var mutator = ParticleEffects.MUTATOR_REGISTRY.get(key);
            if (mutator != null) this.values._mutators.add(mutator);
        });
    }

    public IAnimatedEffects init(JsonObject preset)
    {
        if(!loaded) loadValues(preset);
        return this;
    }

    public void initColour(float time, final MoveEntry move)
    {
        this.reallyInitRGBA();
        if (this.values.customColour) return;
        if (this.values.particle == null)
        {
            this.values.rgba = this.getColourFromMove(move, 255);
            return;
        }
        switch (this.values.particle)
        {
        case "airbubble", "iceshard" -> this.values.rgba = 0x78000000 + DyeColor.CYAN.getTextColor();
        case "aurora" ->
        {
            final DyeColor colour = DyeColor.values()[new Random(((int) time) / 10).nextInt(DyeColor.values().length)];
            final int rand = colour.getTextColor();
            this.values.rgba = 0x61000000 + rand;
        }
        case "spark" -> this.values.rgba = 0x78000000 + DyeColor.YELLOW.getTextColor();
        default -> this.values.rgba = this.getColourFromMove(move, 255);
        }
    }

    @Override
    public void reallyInitRGBA()
    {
        if (this.values.rgba_string == null) return;
        final String val = this.values.rgba_string;
        this.values.rgba_string = null;
        final int alpha = 255;
        DyeColor colour = null;
        try
        {
            colour = DyeColor.byId(Integer.parseInt(val));
        }
        catch (final NumberFormatException e)
        {
            try
            {
                colour = DyeColor.valueOf(val);
            }
            catch (final Exception e1)
            {
                for (final DyeColor col : DyeColor.values())
                    if (col.getSerializedName().equals(val))
                    {
                        colour = col;
                        break;
                    }
            }
        }
        if (colour == null)
        {
            try
            {
                this.values.rgba = Integer.parseInt(val);
            }
            catch (final NumberFormatException ignored)
            {

            }
            return;
        }
        this.values.rgba = colour.getTextColor() | (0xFF | alpha >> 24);
        this.values.customColour = true;
    }

    @Override
    public void setDuration(final int duration)
    {
        this.values.duration = duration;
    }
}
