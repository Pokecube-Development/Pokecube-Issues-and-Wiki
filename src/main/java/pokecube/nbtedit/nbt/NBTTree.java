package pokecube.nbtedit.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.NBTHelper;
import pokecube.nbtedit.NBTStringHelper;

public class NBTTree
{

    public static String repeat(String c, int i)
    {
        final StringBuilder b = new StringBuilder(i + 1);
        for (int j = 0; j < i; ++j)
            b.append(c);
        return b.toString();
    }

    private final CompoundNBT baseTag;

    private Node<NamedNBT> root;

    public NBTTree(CompoundNBT tag)
    {
        this.baseTag = tag;
        this.construct();
    }

    public void addChildrenToList(Node<NamedNBT> parent, ListNBT list)
    {
        for (final Node<NamedNBT> child : parent.getChildren())
        {
            final INBT base = child.getObject().getNBT();
            if (base instanceof CompoundNBT)
            {
                final CompoundNBT newTag = new CompoundNBT();
                this.addChildrenToTag(child, newTag);
                list.add(newTag);
            }
            else if (base instanceof ListNBT)
            {
                final ListNBT newList = new ListNBT();
                this.addChildrenToList(child, newList);
                list.add(newList);
            }
            else list.add(base.copy());
        }
    }

    public void addChildrenToTag(Node<NamedNBT> parent, CompoundNBT tag)
    {
        for (final Node<NamedNBT> child : parent.getChildren())
        {
            final INBT base = child.getObject().getNBT();
            final String name = child.getObject().getName();
            if (base instanceof CompoundNBT)
            {
                final CompoundNBT newTag = new CompoundNBT();
                this.addChildrenToTag(child, newTag);
                tag.put(name, newTag);
            }
            else if (base instanceof ListNBT)
            {
                final ListNBT list = new ListNBT();
                this.addChildrenToList(child, list);
                tag.put(name, list);
            }
            else tag.put(name, base.copy());
        }
    }

    public void addChildrenToTree(Node<NamedNBT> parent)
    {
        final INBT tag = parent.getObject().getNBT();
        if (tag instanceof CompoundNBT)
        {
            final Map<String, INBT> map = NBTHelper.getMap((CompoundNBT) tag);
            for (final Entry<String, INBT> entry : map.entrySet())
            {
                final INBT base = entry.getValue();
                final Node<NamedNBT> child = new Node<>(parent, new NamedNBT(entry.getKey(), base));
                parent.addChild(child);
                this.addChildrenToTree(child);
            }

        }
        else if (tag instanceof ListNBT)
        {
            final ListNBT list = (ListNBT) tag;
            for (int i = 0; i < list.size(); ++i)
            {
                final INBT base = NBTHelper.getTagAt(list, i);
                final Node<NamedNBT> child = new Node<>(parent, new NamedNBT(base));
                parent.addChild(child);
                this.addChildrenToTree(child);
            }
        }
    }

    public boolean canDelete(Node<NamedNBT> node)
    {
        return node != this.root;
    }

    private void construct()
    {
        this.root = new Node<>(new NamedNBT("ROOT", this.baseTag.copy()));
        this.root.setDrawChildren(true);
        this.addChildrenToTree(this.root);
        this.sort(this.root);
    }

    public boolean delete(Node<NamedNBT> node)
    {
        return !(node == null || node == this.root) && this.deleteNode(node, this.root);
    }

    private boolean deleteNode(Node<NamedNBT> toDelete, Node<NamedNBT> cur)
    {
        for (final Iterator<Node<NamedNBT>> it = cur.getChildren().iterator(); it.hasNext();)
        {
            final Node<NamedNBT> child = it.next();
            if (child == toDelete)
            {
                it.remove();
                return true;
            }
            final boolean flag = this.deleteNode(toDelete, child);
            if (flag) return true;
        }
        return false;
    }

    public Node<NamedNBT> getRoot()
    {
        return this.root;
    }

    public void print()
    {
        this.print(this.root, 0);
    }

    private void print(Node<NamedNBT> n, int i)
    {
        System.out.println(NBTTree.repeat("\t", i) + NBTStringHelper.getNBTName(n.getObject()));
        for (final Node<NamedNBT> child : n.getChildren())
            this.print(child, i + 1);
    }

    public void sort(Node<NamedNBT> node)
    {
        Collections.sort(node.getChildren(), NBTEdit.SORTER);
        for (final Node<NamedNBT> c : node.getChildren())
            this.sort(c);
    }

    public CompoundNBT toCompoundNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        this.addChildrenToTag(this.root, tag);
        return tag;
    }

    public List<String> toStrings()
    {
        final List<String> s = new ArrayList<>();
        this.toStrings(s, this.root, 0);
        return s;
    }

    private void toStrings(List<String> s, Node<NamedNBT> n, int i)
    {
        s.add(NBTTree.repeat("   ", i) + NBTStringHelper.getNBTName(n.getObject()));
        for (final Node<NamedNBT> child : n.getChildren())
            this.toStrings(s, child, i + 1);
    }
}
