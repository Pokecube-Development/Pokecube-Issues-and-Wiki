package thut.core.client.render.tabula.json;

import java.io.InputStream;
import java.util.ArrayList;

import thut.core.client.render.animation.Animation;
import thut.core.client.render.tabula.components.CubeGroup;
import thut.core.client.render.tabula.components.CubeInfo;

/**
 * Container class for
 * {@link net.ilexiconn.llibrary.client.model.tabula.ModelJson}. Use
 * {@link net.ilexiconn.llibrary.common.json.JsonHelper#parseTabulaModel(InputStream)}
 * to get a new instance.
 *
 * @author Gegy1000
 * @see net.ilexiconn.llibrary.client.model.tabula.ModelJson
 * @since 0.1.0
 */
public class JsonTabulaModel
{
    private final int textureWidth  = 64;
    private final int textureHeight = 32;

    private final double[] scale = new double[] { 1d, 1d, 1d };

    private ArrayList<CubeGroup> cubeGroups;
    private ArrayList<CubeInfo>  cubes;
    private ArrayList<Animation> anims;

    private int cubeCount;

    public ArrayList<Animation> getAnimations()
    {
        return this.anims;
    }

    public int getCubeCount()
    {
        return this.cubeCount;
    }

    public ArrayList<CubeGroup> getCubeGroups()
    {
        return this.cubeGroups;
    }

    public ArrayList<CubeInfo> getCubes()
    {
        return this.cubes;
    }

    public double[] getScale()
    {
        return this.scale;
    }

    public int getTextureHeight()
    {
        return this.textureHeight;
    }

    public int getTextureWidth()
    {
        return this.textureWidth;
    }
}