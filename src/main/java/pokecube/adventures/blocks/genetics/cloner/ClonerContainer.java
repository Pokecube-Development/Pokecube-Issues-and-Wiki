package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.core.inventory.TexturedSlot;

public class ClonerContainer extends PoweredContainer<ClonerTile>
{
    public static final ContainerType<ClonerContainer> TYPE = new ContainerType<>(ClonerContainer::new);

    public ClonerContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public ClonerContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(ClonerContainer.TYPE, id, (c) ->
        {
            pos.consume((w, p) ->
            {
                final TileEntity temp = w.getTileEntity(p);
                // Server side
                if (temp instanceof ClonerTile) c.tile = (ClonerTile) temp;
            });
            // Client side
            if (c.tile == null) c.tile = new ClonerTile();
            return c.tile;
        });

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, this.tile, 3, 3));

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 124, 35));

        final int di = 17;
        final int di2 = 9;
        final int dj = 32;

        int i = 0;
        int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18,
                "pokecube_adventures:items/slot_bottle"));
        i = 2;
        this.addSlot(new TexturedSlot(this.inv, 1, dj - 21 + j * 18, di + i * 18,
                "pokecube_adventures:items/slot_dna"));

        i = 0;
        this.addSlot(new Slot(this.inv, 2, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 3, dj + j * 18, di + di2 + i * 18));

        i = 0;
        j = 1;
        this.addSlot(new Slot(this.inv, 5, dj + j * 18, di + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 6, dj + j * 18, di + i * 18));
        i = 2;
        this.addSlot(new Slot(this.inv, 7, dj + j * 18, di + i * 18));

        j = 2;
        i = 0;
        this.addSlot(new Slot(this.inv, 8, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlot(new Slot(this.inv, 4, dj + j * 18, di + di2 + i * 18));

        this.bindPlayerInventory(invIn, -19);
    }
}
