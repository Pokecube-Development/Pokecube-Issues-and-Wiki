package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "cylFunc")
public class CylindricalFunction extends MoveAnimationBase
{
    JEP radial;
    JEP angular;

    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();

    public CylindricalFunction()
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.rgba = 0xFFFFFFFF;
        final String[] args = preset.split(":");
        this.particle = "misc";
        String fr = "z";
        String fphi = "0";
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            if (ident.equals("p")) this.particle = val;
            else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
            else if (ident.equals("c")) this.initRGBA(val);
            else if (ident.equals("f"))
            {
                final String[] funcs = val.split(",");
                fr = funcs[0];
                fphi = funcs[1];
            }
            else if (ident.equals("d")) this.density = Float.parseFloat(val);
        }
        this.initJEP(fr, this.radial = new JEP());
        this.initJEP(fphi, this.angular = new JEP());
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
        this.initColour(info.attacker.getEntityWorld().getDayTime() * 20, 0, info.move);
        final double dist = source.distanceTo(target);
        final double frac2 = info.currentTick / (float) this.getDuration();
        final double frac = dist * frac2;
        final double frac3 = dist * (info.currentTick + 1) / this.getDuration();
        final Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        final Vector3 temp1 = Vector3.getNewVector();
        final Vector3 angleF = temp.horizonalPerp().norm();
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (this.density < 1 && Math.random() > this.density) continue;
            if (i / dist > 1) return;
            this.setVector(angleF, temp, i / dist, temp1);
            PokecubeCore.spawnParticle(info.attacker.getEntityWorld(), this.particle, source.add(temp.scalarMult(i)
                    .addTo(temp1)), null, this.rgba, this.particleLife);
        }
    }
}
