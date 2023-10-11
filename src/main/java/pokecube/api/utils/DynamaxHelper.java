package pokecube.api.utils;

import java.util.UUID;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.genetics.genes.DynamaxGene;
import thut.api.Tracker;

public class DynamaxHelper
{
    public static void init()
    {
        PokecubeAPI.POKEMOB_BUS.addListener(DynamaxHelper::onFormRevert);
    }

    private static void onDynaRevert(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        entity.getPersistentData().remove("pokecube:dynatime");
    }

    private static void onFormRevert(ChangeForm.Revert event)
    {
        onDynaRevert(event.getPokemob());
    }

    private static final UUID DYNAMOD = new UUID(343523462346243l, 23453246267457l);

    public static AttributeModifier makeHealthBoost(double scale)
    {
        return new AttributeModifier(DYNAMOD, "pokecube:dynamax", scale, Operation.MULTIPLY_TOTAL);
    }

    public static void dynamax(IPokemob pokemob, int duration)
    {
        var entity = pokemob.getEntity();
        long time = Tracker.instance().getTick();
        entity.getPersistentData().putLong("pokecube:dynatime", time);
        entity.getPersistentData().putInt("pokecube:dynaend", duration);
        var info = DynamaxGene.getDyna(entity);
        float scale = 1.5f + 0.05f * info.dynaLevel;
        var hpBoost = makeHealthBoost(scale);
        entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(hpBoost);

        if (entity.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
        {
            var sizeBoost = new AttributeModifier(DYNAMOD, "pokecube:dynamax", PokecubeCore.getConfig().dynamax_scale,
                    Operation.MULTIPLY_TOTAL);
            entity.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get()).addTransientModifier(sizeBoost);
        }
    }

    public static boolean isDynamax(IPokemob pokemob)
    {
        var entity = pokemob.getEntity();
        return entity.getPersistentData().contains("pokecube:dynatime");
    }
}
