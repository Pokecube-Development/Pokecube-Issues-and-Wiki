package pokecube.nbtedit.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

    private final CompoundTag baseTag;

    private Node<NamedNBT> root;

    public NBTTree(CompoundTag tag)
    {
        this.baseTag = tag;
        this.construct();
    }

    public void addChildrenToList(Node<NamedNBT> parent, ListTag list)
    {
        for (final Node<NamedNBT> child : parent.getChildren())
        {
            final Tag base = child.getObject().getNBT();
            if (base instanceof CompoundTag)
            {
                final CompoundTag newTag = new CompoundTag();
                this.addChildrenToTag(child, newTag);
                list.add(newTag);
            }
            else if (base instanceof ListTag)
            {
                final ListTag newList = new ListTag();
                this.addChildrenToList(child, newList);
                list.add(newList);
            }
            else list.add(base.copy());
        }
    }

    public void addChildrenToTag(Node<NamedNBT> parent, CompoundTag tag)
    {
        for (final Node<NamedNBT> child : parent.getChildren())
        {
            final Tag base = child.getObject().getNBT();
            final String name = child.getObject().getName();
            if (base instanceof CompoundTag)
            {
                final CompoundTag newTag = new CompoundTag();
                this.addChildrenToTag(child, newTag);
                tag.put(name, newTag);
            }
            else if (base instanceof ListTag)
            {
                final ListTag list = new ListTag();
                this.addChildrenToList(child, list);
                tag.put(name, list);
            }
            else tag.put(name, base.copy());
        }
    }

    public void addChildrenToTree(Node<NamedNBT> parent)
    {
        final Tag tag = parent.getObject().getNBT();
        if (tag instanceof CompoundTag)
        {
            final Map<String, Tag> map = NBTHelper.getMap((CompoundTag) tag);
            for (final Entry<String, Tag> entry : map.entrySet())
            {
                final Tag base = entry.getValue();
                final Node<NamedNBT> child = new Node<>(parent, new NamedNBT(entry.getKey(), base));
                parent.addChild(child);
                this.addChildrenToTree(child);
            }

        }
        else if (tag instanceof ListTag)
        {
            final ListTag list = (ListTag) tag;
            for (int i = 0; i < list.size(); ++i)
            {
                final Tag base = NBTHelper.getTagAt(list, i);
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

    public CompoundTag toCompoundNBT()
    {
        final CompoundTag tag = new CompoundTag();
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
