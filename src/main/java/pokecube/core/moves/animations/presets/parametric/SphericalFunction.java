package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import com.google.gson.JsonObject;

import net.minecraft.util.Mth;
import pokecube.api.moves.utils.IMoveAnimation;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "sphFunc")
public class SphericalFunction extends MoveAnimationBase
{
    JEP radial;
    JEP theta;
    JEP phi;

    public SphericalFunction()
    {}

    @Override
    public IMoveAnimation init(JsonObject preset)
    {
        super.init(preset);
        if (values.f_radial == null) values.f_radial = "t";
        if (values.f_phi == null) values.f_phi = "t*6.3";
        if (values.f_theta == null) values.f_theta = "t*3.1-1.5";
        this.initJEP(values.f_radial, this.radial = new JEP());
        this.initJEP(values.f_theta, this.theta = new JEP());
        this.initJEP(values.f_phi, this.phi = new JEP());
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
        this.phi.setVarValue("t", t);
        final double dPhi = this.phi.getValue();
        this.radial.setVarValue("t", t);
        final double dR = this.radial.getValue();
        this.theta.setVarValue("t", t);
        final double dTheta = this.theta.getValue();
        final double sinTheta = Mth.sin((float) dTheta);
        final double cosTheta = Mth.cos((float) dTheta);
        final double rsinPhi = Mth.sin((float) dPhi) * dR;
        final double rcosPhi = Mth.cos((float) dPhi) * dR;
        temp.set(rcosPhi * sinTheta, dR * cosTheta, rsinPhi * sinTheta);
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
