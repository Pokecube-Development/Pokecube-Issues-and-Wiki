package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.core.inventory.TexturedSlot;

public class ExtractorContainer extends PoweredContainer<ExtractorTile>
{
    public static final ContainerType<ExtractorContainer> TYPE = new ContainerType<>(ExtractorContainer::new);

    public ExtractorContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public ExtractorContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(ExtractorContainer.TYPE, id, (c) ->
        {
            pos.consume((w, p) ->
            {
                final TileEntity temp = w.getTileEntity(p);
                // Server side
                if (temp instanceof ExtractorTile) c.tile = (ExtractorTile) temp;
            });
            // Client side
            if (c.tile == null) c.tile = new ExtractorTile();
            return c.tile;
        });

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, this.tile, 1, 3));

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 114, 35));

        final int di = 17;
        final int di2 = 18;
        final int dj2 = 48;
        final int dj = 32;

        // DNA Container
        this.addSlot(new TexturedSlot(this.tile, 0, dj + dj2, di, "pokecube_adventures:items/slot_bottle"));
        // Stabiliser
        this.addSlot(new TexturedSlot(this.tile, 1, dj + dj2, di + 35, "pokecube_adventures:items/slot_selector"));
        // DNA Source
        this.addSlot(new TexturedSlot(this.tile, 2, 47, di + di2, "pokecube_adventures:items/slot_dna"));

        this.bindPlayerInventory(invIn, -19);
    }

}
