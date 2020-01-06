package thut.api.world.utils;

/**
 * @author Thutmose
 * @param <T>
 *            The type for this vector, this should be a number type, like
 *            Double, Float, Int, etc
 */
public interface Vector<T extends Number> extends Comparable<Vector<T>>
{
    /**
     * adds the given vector from us.
     *
     * @param other
     */
    void add(Vector<T> other);

    default double dot(Vector<T> other)
    {
        if (other.getDim() != this.getDim()) throw new IllegalArgumentException("must be same dimensionality to dot.");
        double l = 0;
        final T[] others = other.getVector();
        final T[] ours = this.getVector();
        for (int i = 0; i < ours.length; i++)
        {
            final double val1 = others[i].doubleValue();
            final double val2 = ours[i].doubleValue();
            l += val1 * val2;
        }
        return l;
    }

    /**
     * This should return the size of the array representing this vector.
     *
     * @return
     */
    int getDim();

    /**
     * @param index
     * @return the value at the given index.
     */
    default T getValue(int index)
    {
        return this.getVector()[index];
    }

    /**
     * This gets the entire vector as an array;
     *
     * @return
     */
    T[] getVector();

    default double norm()
    {
        return Math.sqrt(this.normSq());
    }

    default double normSq()
    {
        return this.dot(this);
    }

    /**
     * This sets the value at the given index.
     *
     * @param index
     * @param value
     * @return this vector
     */
    default Vector<T> setValue(int index, T value)
    {
        this.getVector()[index] = value;
        return this;
    }

    /**
     * Subtracts the given vector from us.
     *
     * @param other
     */
    void subtract(Vector<T> other);

}
