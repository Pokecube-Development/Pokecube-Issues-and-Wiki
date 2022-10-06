package pokecube.legends.handlers;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import pokecube.legends.entity.boats.LegendsBoat;
import pokecube.legends.init.ItemInit;

public class DispenseBehaviors
{
    public static void addDispenseBehavior(final ItemLike item, final DispenseItemBehavior dispenseBehavior)
    {
        DispenserBlock.DISPENSER_REGISTRY.put(item.asItem(), dispenseBehavior);
    }

    public static void registerDefaults()
    {
        addDispenseBehavior(ItemInit.AGED_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.AGED));
        addDispenseBehavior(ItemInit.CONCRETE_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.CONCRETE));
        addDispenseBehavior(ItemInit.CORRUPTED_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.CORRUPTED));
        addDispenseBehavior(ItemInit.DISTORTIC_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.DISTORTIC));
        addDispenseBehavior(ItemInit.INVERTED_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.INVERTED));
        addDispenseBehavior(ItemInit.MIRAGE_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.MIRAGE));
        addDispenseBehavior(ItemInit.TEMPORAL_BOAT.get(), new LegendsBoatDispenseHandler(LegendsBoat.Type.TEMPORAL));
    }
}
