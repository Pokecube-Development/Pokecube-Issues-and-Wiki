package pokecube.core.moves.animations.presets.parametric;

import com.google.gson.JsonObject;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.nfunk.jep.JEP;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;

@AnimPreset(getPreset = "cartFunc")
public class CartesianFunction extends MoveAnimationBase
{
    JEP rx;
    JEP ry;
    JEP rz;
    JEP vx;
    JEP vy;
    JEP vz;

    boolean horizontal = true;

    public CartesianFunction()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
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
        this.horizontal = values.horizontal;
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
        jep.addVariable("d", 0);
        jep.parseExpression(func);
    }

    private void setVector(double t, double d, Vector3f vec_r, Vector3f vec_v)
    {
        this.rx.setVarValue("t", t);
        this.rx.setVarValue("d", d);
        double dx = this.rx.getValue();
        this.ry.setVarValue("t", t);
        this.ry.setVarValue("d", d);
        double dy = this.ry.getValue();
        this.rz.setVarValue("t", t);
        this.rz.setVarValue("d", d);
        double dz = this.rz.getValue();
        vec_r.set(dx, dy, dz);
        this.vx.setVarValue("t", t);
        this.vx.setVarValue("d", d);
        dx = this.vx.getValue();
        this.vy.setVarValue("t", t);
        this.vy.setVarValue("d", d);
        dy = this.vy.getValue();
        this.vz.setVarValue("t", t);
        this.vz.setVarValue("d", d);
        dz = this.vz.getValue();
        vec_v.set(dx, dy, dz);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info, float partialTicks)
    {
        Vector3f source = values.reverse
                ? info.source.currentPosition().toVector3f()
                : info.target.currentPosition().toVector3f();
        Vector3f target = values.reverse
                ? info.target.currentPosition().toVector3f()
                : info.source.currentPosition().toVector3f();
        Vector3f dir = new Vector3f(target);
        dir.sub(source);
        double d = dir.length();
        Vector3f lft = new Vector3f(1, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);
        if (dir.lengthSquared() > 0)
        {
            // Only want to rotate x and z coords to match
            if (values.horizontal) dir.y = 0;
            dir.cross(up,lft);
            lft.normalize();
            dir.cross(lft, up);
            up.normalize();
            dir.normalize();
        }
        else dir.set(0,0,1);

        Vector4f dp = new Vector4f();

        this.initColour(info.currentTick, info.move);
        final Vector3f vec_r = new Vector3f(), vec_v = new Vector3f();
        float scale = 1;
        if (!values.absolute)
        {
            scale = values.width;
            if (values.reverse) scale *= info.attackerScale;
            else scale *= info.attackedScale;
        }
        if(!values.horizontal)
        {
            dp.set(0,0,1,1);
            System.out.println(dir+" "+lft+" "+up);
        }
        for (double i = info.currentTick; i < info.currentTick + 1; i += values.density)
        {
            this.setVector(i, d, vec_r, vec_v);
            vec_r.set(
                    lft.x * vec_r.x + up.x * vec_r.y + dir.x * vec_r.z,
                    lft.y * vec_r.x + up.y * vec_r.y + dir.y * vec_r.z,
                    lft.z * vec_r.x + up.z * vec_r.y + dir.z * vec_r.z
                    );
            vec_r.mul(scale);
            vec_v.set(
                    lft.x * vec_v.x + up.x * vec_v.y + dir.x * vec_v.z,
                    lft.y * vec_v.x + up.y * vec_v.y + dir.y * vec_v.z,
                    lft.z * vec_v.x + up.z * vec_v.y + dir.z * vec_v.z
            );
            PokecubeCore.spawnParticle(info.level, values.particle, vec_r.add(source), vec_v, values.rgba, values.lifetime);
        }
    }
}
