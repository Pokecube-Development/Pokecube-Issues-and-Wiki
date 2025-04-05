package pokecube.gimmicks.mega;

import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;

public interface IMegaCapability
{

    public static class Default implements IMegaCapability
    {
        public Default()
        {
        }

        @Override
        public PokedexEntry getEntry(final ItemStack stack)
        {
            return Database.missingno;
        }

        @Override
        public boolean isStone(final ItemStack stack)
        {
            return false;
        }

        @Override
        public boolean isValid(final ItemStack stack, final PokedexEntry entry)
        {
            return false;
        }
    }

    PokedexEntry getEntry(ItemStack stack);

    /** Check if the itemstack is a mega stone. */
    boolean isStone(ItemStack stack);

    /**
     * Check if the mega stone is valid for the given entry.
     *
     * @param stack
     * @param entry
     * @return
     */
    boolean isValid(ItemStack stack, PokedexEntry entry);
}
