package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "cartFunc")
public class CartesianFunction extends MoveAnimationBase
{
    JEP x;
    JEP y;
    JEP z;

    Vector3 v        = Vector3.getNewVector();
    boolean reverse  = false;
    boolean absolute = false;
    Vector3 v1       = Vector3.getNewVector();

    public CartesianFunction()
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.rgba = 0xFFFFFFFF;
        this.density = 0.5f;
        final String[] args = preset.split(":");
        this.particle = "misc";
        String fx = "0";
        String fy = "0";
        String fz = "0";
        for (int i = 1; i < args.length; i++)
        {
            final String ident = args[i].substring(0, 1);
            final String val = args[i].substring(1);
            if (ident.equals("d")) this.density = Float.parseFloat(val);
            else if (ident.equals("w")) this.width = Float.parseFloat(val);
            else if (ident.equals("r")) this.reverse = Boolean.parseBoolean(val);
            else if (ident.equals("p")) this.particle = val;
            else if (ident.equals("l")) this.particleLife = Integer.parseInt(val);
            else if (ident.equals("a")) this.absolute = Boolean.parseBoolean(val);
            else if (ident.equals("c")) this.initRGBA(val);
            else if (ident.equals("f"))
            {
                final String[] funcs = val.split(",");
                fx = funcs[0];
                fy = funcs[1];
                fz = funcs[2];
            }
        }
        this.initJEP(fx, this.x = new JEP());
        this.initJEP(fy, this.y = new JEP());
        this.initJEP(fz, this.z = new JEP());
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
        final Vector3 source = this.reverse ? info.source : info.target;
        this.initColour(info.attacker.getLevel().getDayTime() * 20, 0, info.move);
        final Vector3 temp = Vector3.getNewVector();
        double scale = this.width;
        if (!this.absolute) if (this.reverse && info.attacker != null) scale *= info.attacker.getBbWidth();
        else if (!this.reverse && info.attacked != null) scale *= info.attacked.getBbWidth();
        for (double i = info.currentTick; i < info.currentTick + 1; i += this.density)
        {
            this.setVector(i, temp);
            temp.scalarMultBy(scale).addTo(source);
            PokecubeCore.spawnParticle(info.attacker.getLevel(), this.particle, temp, null, this.rgba,
                    this.particleLife);
        }
    }
}
