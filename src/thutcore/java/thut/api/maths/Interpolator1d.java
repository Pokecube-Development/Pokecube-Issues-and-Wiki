package thut.api.maths;

import net.minecraft.util.Mth;
import org.nfunk.jep.JEP;

public class Interpolator1d
{
    public static boolean validJEP(JEP jep, String function, String varName)
    {
        jep.initFunTab(); // clear the contents of the function table
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex(); // among other things adds i to the symbol table
        jep.addVariable(varName, 0);
        jep.parseExpression(function);
        return !jep.hasError();
    }

    String function, varName;
    double[] cache;
    JEP parser;
    double min, max, dv;

    public Interpolator1d(String function, String varName, double min, double max, int size)
    {
        this.min = min;
        this.max = max;
        dv = (max-min)/size;
        cache = new double[size];
        this.function = function;
        this.varName = varName;
    }

    public boolean init()
    {
        parser = new JEP();
        if(!validJEP(parser, function, varName)) return false;
        for(int i = 0; i<cache.length; i++)
        {
            double x = min + i*dv;
            parser.setVarValue(varName, x);
            cache[i] = parser.getValue();
        }
        return true;
    }

    public double interpolate(double input)
    {
        if(input<this.min) return cache[0];
        if(input>=this.max-dv) return cache[cache.length-1];
        double ind = (input-min)/dv;
        int index_low = (int) (ind);
        return Mth.lerp(ind-index_low, cache[index_low], cache[index_low+1]);
    }
}
