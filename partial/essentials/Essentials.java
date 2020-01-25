package thut.essentials;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod(Reference.MODID)
@EventBusSubscriber
public class Essentials
{
    public static final Config config = new Config();

    public Essentials()
    {
        // TODO Auto-generated constructor stub
    }

}
