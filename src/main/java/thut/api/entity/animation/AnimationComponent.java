package thut.api.entity.animation;

import org.nfunk.jep.JEP;

public class AnimationComponent
{
    public double[] posChange = new double[3];
    public double[] rotChange = new double[3];
    public double[] scaleChange = new double[3];
    public double opacityChange = 0.0D;
    public double[] posOffset = new double[3];
    public double[] rotOffset = new double[3];
    public double[] scaleOffset = new double[3];
    public double opacityOffset = 0.0D;
    public String name = "";
    public int length = 0;
    public int startKey = 0;
    public boolean hidden = false;
    public boolean limbBased = false;
    public String identifier = "";

    public JEP[] _rotFunctions = new JEP[3];
    public JEP[] _posFunctions = new JEP[3];
    public JEP[] _scaleFunctions = new JEP[3];
    
    public boolean _foundNoJEP = false;
}
