package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import com.google.gson.JsonObject;

import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "cylFunc")
public class CylindricalFunction extends MoveAnimationBase
{
    JEP radial;
    JEP angular;

    public CylindricalFunction()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        super.init(preset);
        if (values.f_radial == null) values.f_radial = "z";
        if (values.f_phi == null) values.f_phi = "0";
        this.initJEP(values.f_radial, this.radial = new JEP());
        this.initJEP(values.f_phi, this.angular = new JEP());
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
        jep.addVariable("z", 0);
        jep.parseExpression(func);
    }

    private void setVector(Vector3 horizonalPerp, Vector3 dir, double z, Vector3 temp)
    {
        this.angular.setVarValue("z", z);
        final double angle = this.angular.getValue();
        horizonalPerp.rotateAboutLine(dir, angle, temp);
        temp.norm();
        this.radial.setVarValue("z", z);
        final double r = this.radial.getValue();
        temp.scalarMultBy(r);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        final Vector3 source = info.source;
        final Vector3 target = info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac2 = info.currentTick / (float) this.getDuration();
        final double frac = dist * frac2;
        final double frac3 = dist * (info.currentTick + 1) / this.getDuration();
        final Vector3 temp = new Vector3().set(target).subtractFrom(source).norm();
        final Vector3 temp1 = new Vector3();
        final Vector3 angleF = temp.horizonalPerp().norm();
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (values.density < 1 && Math.random() > values.density) continue;
            if (i / dist > 1) return;
            this.setVector(angleF, temp, i / dist, temp1);
            PokecubeCore.spawnParticle(info.attacker.getLevel(), values.particle,
                    source.add(temp.scalarMult(i).addTo(temp1)), null, values.rgba, values.lifetime);
        }
    }
}
