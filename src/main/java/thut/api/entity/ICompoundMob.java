package thut.api.entity;

import net.minecraft.entity.Entity;

public interface ICompoundMob
{
    public interface ICompoundPart
    {
        ICompoundMob getBase();

        Entity getMob();
    }

    ICompoundPart[] getParts();

    Entity getMob();
}
