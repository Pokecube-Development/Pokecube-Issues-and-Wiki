package pokecube.core.database.stats;

import java.util.HashMap;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public interface ISpecialCaptureCondition
{
    public static final HashMap<PokedexEntry, ISpecialCaptureCondition> captureMap = new HashMap<>();

    boolean canCapture(Entity trainer);

    boolean canCapture(Entity trainer, IPokemob pokemon);

    default void onCaptureFail(final Entity trainer, final IPokemob pokemob)
    {

    }

    default MutableComponent getFailureMessage(final Entity trainer)
    {
        return new TextComponent("ERROR NEED MESSAGE");
    }
}
