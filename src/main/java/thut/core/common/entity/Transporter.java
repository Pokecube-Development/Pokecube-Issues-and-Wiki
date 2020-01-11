package thut.core.common.entity;

import net.minecraft.entity.Entity;
import thut.api.entity.ThutTeleporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class Transporter
{

    public static void teleportEntity(Entity mob, Vector3 loc, int dim, boolean b)
    {
        ThutTeleporter.transferTo(mob, new Vector4(loc.x, loc.y, loc.z, dim));
    }

}
