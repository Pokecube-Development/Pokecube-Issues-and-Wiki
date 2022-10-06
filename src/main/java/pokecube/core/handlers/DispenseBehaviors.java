package pokecube.core.handlers;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.boats.GenericBoat;

public class DispenseBehaviors
{
    public static void addDispenseBehavior(final ItemLike item, final DispenseItemBehavior dispenseBehavior)
    {
        DispenserBlock.DISPENSER_REGISTRY.put(item.asItem(), dispenseBehavior);
    }

    public static void registerDefaults()
    {
        addDispenseBehavior(PokecubeItems.ENIGMA_BOAT.get(), new GenericBoatDispenseHandler(GenericBoat.Type.ENIGMA));
        addDispenseBehavior(PokecubeItems.LEPPA_BOAT.get(), new GenericBoatDispenseHandler(GenericBoat.Type.LEPPA));
        addDispenseBehavior(PokecubeItems.ORAN_BOAT.get(), new GenericBoatDispenseHandler(GenericBoat.Type.ORAN));
        addDispenseBehavior(PokecubeItems.PECHA_BOAT.get(), new GenericBoatDispenseHandler(GenericBoat.Type.PECHA));
        addDispenseBehavior(PokecubeItems.SITRUS_BOAT.get(), new GenericBoatDispenseHandler(GenericBoat.Type.SITRUS));
    }
}
