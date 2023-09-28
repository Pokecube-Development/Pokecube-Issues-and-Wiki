package thut.api.entity.animation;

import java.util.HashSet;
import java.util.Set;

import org.nfunk.jep.JEP;

public class AnimationComponent
{
    private static float[] unity_scale =
    { 1, 1, 1 };
    private static float[] pixel_scale =
    { 1 / 16f, 1 / 16f, 1 / 16f };

    public double[] posChange = new double[3];
    public double[] rotChange = new double[3];
    public double[] scaleChange = new double[3];
    public double[] colChange = new double[3];
    public double opacityChange = 0.0D;

    public double[] posOffset = new double[3];
    public double[] rotOffset = new double[3];
    public double[] colOffset =
    { 1, 1, 1 };
    public double[] scaleOffset =
    { 1, 1, 1 };
    public double opacityOffset = 1.0D;

    public String name = "";
    public float length = 0;
    public float startKey = 0;
    public boolean hidden = false;
    public boolean limbBased = false;
    public String identifier = "";

    public JEP[] _rotJEPs = new JEP[3];
    public JEP[] _posJEPs = new JEP[3];
    public JEP[] _colJEPs = new JEP[3];
    public JEP[] _scaleJEPs = new JEP[3];
    public JEP _opacJEP = null;

    public String[] _rotFunctions = new String[3];
    public String[] _posFunctions = new String[3];
    public String[] _colFunctions = new String[3];
    public String[] _scaleFunctions = new String[3];
    public String _opacFunction = null;

    public float[] _rotFuncScale = unity_scale;
    public float[] _posFuncScale = pixel_scale;

    public boolean _needJEPInit = false;

    public Set<String> _valid_channels = new HashSet<>();
}
