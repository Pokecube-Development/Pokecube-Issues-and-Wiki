package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.core.inventory.TexturedSlot;

public class ExtractorContainer extends PoweredContainer<ExtractorTile>
{
    public ExtractorContainer(final int id, final Inventory invIn)
    {
        this(id, invIn, ContainerLevelAccess.NULL);
    }

    public ExtractorContainer(final int id, final Inventory invIn, final ContainerLevelAccess pos)
    {
        super(PokecubeAdv.EXTRACTOR_CONT.get(), id, (c) ->
        {
            pos.execute((w, p) ->
            {
                final BlockEntity temp = w.getBlockEntity(p);
                // Server side
                if (temp instanceof ExtractorTile) c.tile = (ExtractorTile) temp;
            });
            // Client side
            if (c.tile == null) c.tile = new ExtractorTile(invIn.player.blockPosition(), PokecubeAdv.EXTRACTOR.get()
                    .defaultBlockState());
            return c.tile;
        });

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, this.tile, 1, 3));

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 114, 35)
        {
            @Override
            public boolean mayPlace(final ItemStack stack)
            {
                return false;
            }
        });

        final int di = 17;
        final int di2 = 18;
        final int dj2 = 48;
        final int dj = 32;

        // DNA Container
        this.addSlot(new TexturedSlot(this.tile, 0, dj + dj2, di, "pokecube_adventures:gui/slot_bottle"));
        // Stabiliser
        this.addSlot(new TexturedSlot(this.tile, 1, dj + dj2, di + 35, "pokecube_adventures:gui/slot_selector"));
        // DNA Source
        this.addSlot(new TexturedSlot(this.tile, 2, 47, di + di2, "pokecube_adventures:gui/slot_dna"));

        this.bindPlayerInventory(invIn, -19);
    }

}
