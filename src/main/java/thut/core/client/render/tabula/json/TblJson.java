package thut.core.client.render.tabula.json;

import java.util.ArrayList;

import thut.core.client.render.animation.Animation;

public class TblJson
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
