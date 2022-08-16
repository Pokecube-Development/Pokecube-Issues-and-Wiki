package pokecube.api.stats;

import java.util.HashMap;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

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
        return TComponent.literal("ERROR NEED MESSAGE");
    }
}
