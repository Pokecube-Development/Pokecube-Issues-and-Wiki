package pokecube.core.handlers;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import pokecube.core.entity.boats.GenericBoat;

public class DispenseBehaviors
{
    public static void addDispenseBehavior(final ItemLike item, final DispenseItemBehavior dispenseBehavior)
    {
        DispenserBlock.DISPENSER_REGISTRY.put(item.asItem(), dispenseBehavior);
    }

    public static void registerDefaults()
    {
        GenericBoat.getTypes().forEach(type -> {
            addDispenseBehavior(type.item().get(), new GenericBoatDispenseHandler(type));
        });
    }
}
