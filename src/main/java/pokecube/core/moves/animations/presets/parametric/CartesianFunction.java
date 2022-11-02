package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import com.google.gson.JsonObject;

import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "cartFunc")
public class CartesianFunction extends MoveAnimationBase
{
    JEP x;
    JEP y;
    JEP z;

    public CartesianFunction()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        super.init(preset);
        if (values.f_x == null) values.f_x = "0";
        if (values.f_y == null) values.f_y = "0";
        if (values.f_z == null) values.f_z = "0";
        this.initJEP(values.f_x, this.x = new JEP());
        this.initJEP(values.f_y, this.y = new JEP());
        this.initJEP(values.f_z, this.z = new JEP());
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
        jep.parseExpression(func);
    }

    private void setVector(double t, Vector3 temp)
    {
        this.x.setVarValue("t", t);
        final double dx = this.x.getValue();
        this.y.setVarValue("t", t);
        final double dy = this.y.getValue();
        this.z.setVarValue("t", t);
        final double dz = this.z.getValue();
        temp.set(dx, dy, dz);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final Vector3 source = values.reverse ? info.source : info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final Vector3 temp = new Vector3();
        double scale = values.width;
        if (!values.absolute) if (values.reverse && info.attacker != null) scale *= info.attacker.getBbWidth();
        else if (!values.reverse && info.attacked != null) scale *= info.attacked.getBbWidth();
        for (double i = info.currentTick; i < info.currentTick + 1; i += values.density)
        {
            this.setVector(i, temp);
            temp.scalarMultBy(scale).addTo(source);
            PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle, temp, null, values.rgba,
                    values.lifetime);
        }
    }
}
