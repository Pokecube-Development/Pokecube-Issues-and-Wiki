package thut.api.entity.multipart;

import com.google.common.collect.Sets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import thut.api.entity.IAnimated;
import thut.core.common.ThutCore;
import thut.core.common.network.PartSync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface IBodyPartMulitpart<T extends BodyPartEntity<E>, E extends Entity> extends IMultpart<T, E>
{

    @Override
    default void trySubDivideParts(float width, float length, float height)
    {
        var split = this.splitToParts(width, height, length, 0, 0, 0);
        getHolder().setParts(split);
        for (var p : split) getHolder().allParts().add(p);
    }

    BodyPartEntity.Factory<T, E> getFactory();

    default T makePart(final BodyPartEntity.BodyPart part, final float size, final Set<String> names)
    {
        final float dx = (float) (part.__pos__.x * size);
        final float dy = (float) (part.__pos__.y * size);
        final float dz = (float) (part.__pos__.z * size);

        final float sx = (float) (part.__size__.x * size);
        final float sy = (float) (part.__size__.y * size);
        final float sz = (float) (part.__size__.z * size);

        final float dw = Math.max(sx, sz);
        final float dh = sy;
        String name = part.name;
        int n = 0;
        while (names.contains(name)) name = part.name + n++;
        return getFactory().create(weSelf(), dw, dh, dx, dy, dz, name);
    }

    default List<T> splitToParts(float width, float height, float length, float x0, float y0, float z0)
    {
        List<T> ret = new ArrayList<T>();
        final int nx = Mth.ceil(width / this.maxW());
        final int nz = Mth.ceil(length / this.maxH());
        final int ny = Mth.ceil(height / this.maxW());

        final float dx = width / nx;
        final float dy = height / ny;
        final float dz = length / nz;

        final float dw = Math.max(width / nx, length / nz);
        final float dh = dy;
        int i = 0;

        Set<String> names = new HashSet<>();
        for (int y = 0; y < ny; y++)
            for (int x = 0; x < nx; x++)
                for (int z = 0; z < nz; z++)
                {
                    BodyPartEntity.BodyPart _part = new BodyPartEntity.BodyPart();
                    _part.name = "part_" + i;
                    _part.__size__ = new Vec3(dw, dh, dw);
                    _part.__pos__ = new Vec3(x * dx - nx * dx / 2f + x0, y * dy + y0, z * dz - nz * dz / 2f + z0);
                    var part = makePart(_part, 1, names);
                    ret.add(part);
                    i++;
                }
        return ret;
    }

    default void applyAnimations(IAnimated animHolder)
    {
        final List<String> anims = animHolder.getChoices();
        String old_pose = getHolder().holder().effective_pose;
        getHolder().holder().effective_pose = "idle";

        for (final String s : anims)
        {
            if (getHolder().partMap().containsKey(s))
            {
                getHolder().holder().effective_pose = s;
                break;
            }
        }
        if (old_pose.equals(getHolder().holder().effective_pose)) return;
        // Update the partmap if we know about this pose.
        if (getHolder().partMap().containsKey(getHolder().holder().effective_pose))
        {
            getHolder().setParts(getHolder().partMap().get(getHolder().holder().effective_pose));
            boolean subDivided = !getHolder().getParts().isEmpty();
            if (subDivided)
            {
                float width = Math.min(weSelf().dimensions.width(), maxW());
                float height = Math.min(weSelf().dimensions.height(), maxH());
                weSelf().dimensions = EntityDimensions.fixed(width, height);
                weSelf().noCulling = true;

                final boolean first = weSelf().firstTick;
                weSelf().firstTick = true;
                weSelf().refreshDimensions();
                weSelf().firstTick = first;
            }
            PartSync.sendUpdate(weSelf());
        }
    }

    default void addPart(final String key, final float size, final BodyPartEntity.BodyNode node)
    {
        try
        {
            List<T> list = new ArrayList<T>();
            final Set<String> names = Sets.newHashSet();
            for (int i = 0; i < node.parts.size(); i++)
            {
                var _node = node.parts.get(i);

                float sx = (float) (_node.__size__.x * size);
                float sy = (float) (_node.__size__.y * size);
                float sz = (float) (_node.__size__.z * size);

                if (sx > this.maxW() || sz > this.maxW() || sy > this.maxH())
                {
                    float x0 = (float) (_node.__pos__.x * size);
                    float y0 = (float) (_node.__pos__.y * size);
                    float z0 = (float) (_node.__pos__.z * size);

                    var split = this.splitToParts(sx, sy, sz, x0, y0, z0);
                    for (var part : split)
                    {
                        list.add(part);
                        getHolder().allParts().add(part);
                    }
                }
                else
                {
                    var nodePart = node.parts.get(i);
                    var part = this.makePart(nodePart, size, names);
                    if (nodePart.__ride__ != null) part.ride_point = nodePart.__ride__.toVector3f();
                    list.add(part);
                    getHolder().allParts().add(part);
                }
            }
            getHolder().partMap().put(key, list);
        }
        catch (NegativeArraySizeException e)
        {
            ThutCore.LOGGER.error(e);
        }
    }
}
