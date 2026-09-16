package pokecube.core.effects.presets.parametric;

import com.google.gson.JsonObject;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.nfunk.jep.JEP;
import pokecube.api.effects.IAnimatedEffects;
import pokecube.core.PokecubeCore;
import pokecube.core.effects.AnimPreset;
import pokecube.core.effects.MoveAnimationBase;

import java.util.Random;
import java.util.function.Function;

@AnimPreset(getPreset = "cartFunc")
public class CartesianFunction extends MoveAnimationBase
{
    JEP rx;
    JEP ry;
    JEP rz;
    JEP vx;
    JEP vy;
    JEP vz;

    Function<Vector3f, Vector3f> ORIGIN_SHIFT = v->v;

    public CartesianFunction()
    {}

    @Override
    public IAnimatedEffects init(JsonObject preset)
    {
        super.init(preset);
        if (values.f_x == null) values.f_x = "0";
        if (values.f_y == null) values.f_y = "0";
        if (values.f_z == null) values.f_z = "0";
        this.initJEP(values.f_x, this.rx = new JEP());
        this.initJEP(values.f_y, this.ry = new JEP());
        this.initJEP(values.f_z, this.rz = new JEP());
        if (values.v_x == null) values.v_x = "0";
        if (values.v_y == null) values.v_y = "0";
        if (values.v_z == null) values.v_z = "0";
        this.initJEP(values.v_x, this.vx = new JEP());
        this.initJEP(values.v_y, this.vy = new JEP());
        this.initJEP(values.v_z, this.vz = new JEP());
        if ("chunk_centre".equals(values.reference))
        {
            ORIGIN_SHIFT = v -> {
                v.x = 16 * Mth.floor(v.x / 16) + 8;
                v.y = 16 * Mth.floor(v.y / 16) + 8;
                v.z = 16 * Mth.floor(v.z / 16) + 8;
                return v;
            };
        }
        return this;
    }

    private void initJEP(String func, JEP jep)
    {
        jep.initFunTab();
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex();
        // table
        jep.addVariable("t", 0);
        jep.addVariable("m", 0);
        jep.addVariable("d", 0);
        jep.parseExpression(func);
    }

    private void setVector(double t_0, double t_m, double d, Vector3f vec_r, Vector3f vec_v)
    {
        this.rx.setVarValue("t", t_0);
        this.rx.setVarValue("d", d);
        this.rx.setVarValue("m", t_m);

        this.ry.setVarValue("t", t_0);
        this.ry.setVarValue("d", d);
        this.ry.setVarValue("m", t_m);

        this.rz.setVarValue("t", t_0);
        this.rz.setVarValue("d", d);
        this.rz.setVarValue("m", t_m);
        vec_r.set(this.rx.getValue(), this.ry.getValue(), this.rz.getValue());

        this.vx.setVarValue("t", t_0);
        this.vx.setVarValue("d", d);
        this.vx.setVarValue("m", t_m);

        this.vy.setVarValue("t", t_0);
        this.vy.setVarValue("d", d);
        this.vy.setVarValue("m", t_m);

        this.vz.setVarValue("t", t_0);
        this.vz.setVarValue("d", d);
        this.vz.setVarValue("m", t_m);
        vec_v.set(this.vx.getValue(), this.vy.getValue(), this.vz.getValue());
    }

    @Override
    public void spawnClientEntities(EffectPacketInfo info, float partialTicks)
    {
        Vector3f source = values.reverse ? info.getSource() : info.getTarget();
        Vector3f target = values.reverse ? info.getTarget() : info.getSource();
        source = ORIGIN_SHIFT.apply(source);
        Vector3f dir = new Vector3f(target);
        dir.sub(source);
        double d = dir.length();
        Vector3f lft = new Vector3f(1, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);
        if (dir.lengthSquared() > 0 && !values.horizontal)
        {
            dir.cross(up, lft);
            lft.normalize();
            dir.cross(lft, up);
            up.normalize();
            dir.normalize();
        }
        else dir.set(0, 0, 1);
        final Vector3f vec_r = new Vector3f(), vec_v = new Vector3f();
        float scale = 1;
        if (!values.absolute)
        {
            scale = values.width;
            if (values.reverse) scale *= info.sourceScale;
            else scale *= info.targetScale;
        }
        int t_0 = info.currentTick;
        double t_1 = Math.min(t_0 + 2, info.endTick) + values.density * 0.1;
        double rng_v = values.density - 1;
        var RNG = new Random();
        for (double i = t_0; i <= t_1; i += values.density)
        {
            if (RNG.nextFloat() < rng_v) return;
            this.setVector(i, info.endTick, d, vec_r, vec_v);
            vec_r.set(lft.x * vec_r.x + up.x * vec_r.y + dir.x * vec_r.z,
                    lft.y * vec_r.x + up.y * vec_r.y + dir.y * vec_r.z,
                    lft.z * vec_r.x + up.z * vec_r.y + dir.z * vec_r.z);
            vec_r.mul(scale);
            vec_v.set(lft.x * vec_v.x + up.x * vec_v.y + dir.x * vec_v.z,
                    lft.y * vec_v.x + up.y * vec_v.y + dir.y * vec_v.z,
                    lft.z * vec_v.x + up.z * vec_v.y + dir.z * vec_v.z);
            PokecubeCore.spawnParticle(info.level, values.particle, vec_r.add(source), vec_v, values.rgba,
                    values.lifetime);
        }
    }
}
