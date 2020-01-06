package pokecube.core;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.entity.MobEntity;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.events.ClassGenEvent;

/**
 * This class generates the pokemob classes for each pokemob. It works by
 * copying the byte array from GenericPokemob.class, then modifying it
 * accordingly for each class it makes.
 *
 * @author Thutmose
 */
public class ByteClassLoader extends ClassLoader
{
    public static final ByteClassLoader loader = new ByteClassLoader(ByteClassLoader.getSystemClassLoader());
    private static byte[]               genericMobBytes;

    String resName = "GenericPokemob.class";

    public ByteClassLoader(ClassLoader ucl)
    {
        super(ucl);
    }

    public Class<? extends MobEntity> generatePokemobClass(PokedexEntry entry) throws ClassNotFoundException
    {
        try
        {
            final InputStream is = GenericPokemob.class.getResourceAsStream(this.resName);
            final ClassReader reader = new ClassReader(is);
            ByteClassLoader.genericMobBytes = reader.b.clone();
            is.close();
        }
        catch (final Exception e)
        {

            e.printStackTrace();
        }

        // Clone default generic class, then put into a class reader.
        final ClassReader reader = new ClassReader(ByteClassLoader.genericMobBytes);
        ClassWriter writer;
        byte[] genericMob = reader.b.clone();
        final String name = entry.getTrimmedName();
        final ClassNode changer = new ClassNode();
        reader.accept(changer, 0);

        // Change the name of the class to include pokemob's name.
        // This is all of the change that we actually need.
        changer.sourceFile = changer.sourceFile.replace(".java", "") + name + "_" + ".java";
        changer.name = changer.name + "_" + name;

        writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        changer.accept(writer);

        // Send event for various addons to edit the class as well.
        final ClassGenEvent evt = new ClassGenEvent(writer, changer, entry);
        MinecraftForge.EVENT_BUS.post(evt);
        writer.visitEnd();
        genericMob = writer.toByteArray();

        // Apply the changes made by addons
        final ClassReader cr = new ClassReader(genericMob);
        final ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);

        // Load class from edited byte array.
        @SuppressWarnings("unchecked")
        final Class<? extends MobEntity> c = (Class<? extends MobEntity>) this.loadClass(classNode.name, genericMob,
                true);
        return c;

    }

    public Class<?> loadClass(String name, byte[] jarBytes, boolean resolve) throws ClassNotFoundException
    {
        name = name.replace("/", ".");
        Class<?> clazz = null;
        try
        {
            clazz = super.loadClass(name, false);
        }
        catch (final Exception e1)
        {

        }
        if (clazz == null) try
        {
            final byte[] bytes = jarBytes;
            clazz = super.defineClass(name, bytes, 0, bytes.length);
            if (resolve) super.resolveClass(clazz);
        }
        catch (final Exception e)
        {
            clazz = super.loadClass(name, resolve);
        }
        return clazz;
    }

}
