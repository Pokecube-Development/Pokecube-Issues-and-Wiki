package pokecube.nbtedit.nbt;

import java.util.ArrayList;
import java.util.List;

public class Node<T>
{

    private List<Node<T>> children;

    private Node<T> parent;
    private T       obj;

    private boolean drawChildren;

    public Node()
    {
        this((T) null);
    }

    public Node(Node<T> parent)
    {
        this(parent, null);
    }

    public Node(Node<T> parent, T obj)
    {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.obj = obj;
    }

    public Node(T obj)
    {
        this.children = new ArrayList<>();
        this.obj = obj;
    }

    public void addChild(Node<T> n)
    {
        this.children.add(n);
    }

    public List<Node<T>> getChildren()
    {
        return this.children;
    }

    public T getObject()
    {
        return this.obj;
    }

    public Node<T> getParent()
    {
        return this.parent;
    }

    public boolean hasChildren()
    {
        return this.children.size() > 0;
    }

    public boolean hasParent()
    {
        return this.parent != null;
    }

    public boolean removeChild(Node<T> n)
    {
        return this.children.remove(n);
    }

    public void setDrawChildren(boolean draw)
    {
        this.drawChildren = draw;
    }

    public boolean shouldDrawChildren()
    {
        return this.drawChildren;
    }

    @Override
    public String toString()
    {
        return "" + this.obj;
    }

}
