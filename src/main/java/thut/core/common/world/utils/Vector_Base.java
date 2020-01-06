package thut.core.common.world.utils;

import thut.api.world.utils.Vector;

public abstract class Vector_Base<T extends Number> implements Vector<T>
{
    T[] array;

    public Vector_Base()
    {
        this.init();
    }

    @Override
    public int getDim()
    {
        return this.array.length;
    }

    @Override
    public T[] getVector()
    {
        // TODO Auto-generated method stub
        return this.array;
    }

    /** Construct the Typed array here. */
    abstract void init();

}
