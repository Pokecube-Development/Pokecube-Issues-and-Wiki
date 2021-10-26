package pokecube.core.database.stats;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
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

    default IFormattableTextComponent getFailureMessage(final Entity trainer)
    {
        return new StringTextComponent("ERROR NEED MESSAGE");
    }
}
