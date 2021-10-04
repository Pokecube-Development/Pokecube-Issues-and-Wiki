package pokecube.nbtedit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEntityHelper
{

    public static Set<Field> asSet(Field[] a, Field[] b)
    {
        final HashSet<Field> s = new HashSet<>();
        Collections.addAll(s, a);
        Collections.addAll(s, b);
        return s;
    }

    public static <T extends BlockEntity> void copyData(T from, T into) throws Exception
    {
        final Class<?> clazz = from.getClass();
        final Set<Field> fields = TileEntityHelper.asSet(clazz.getFields(), clazz.getDeclaredFields());
        final Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        for (final Field field : fields)
        {
            field.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(into, field.get(from));
        }
    }

}
