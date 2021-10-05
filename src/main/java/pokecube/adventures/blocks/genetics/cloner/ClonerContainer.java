package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.helper.PoweredContainer;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.core.inventory.TexturedSlot;

public class ClonerContainer extends PoweredContainer<ClonerTile>
{
    public ClonerContainer(final int id, final Inventory invIn)
    {
        this(id, invIn, ContainerLevelAccess.NULL);
    }

    public ClonerContainer(final int id, final Inventory invIn, final ContainerLevelAccess pos)
    {
        super(PokecubeAdv.CLONER_CONT.get(), id, (c) ->
        {
            pos.execute((w, p) ->
            {
                final BlockEntity temp = w.getBlockEntity(p);
                // Server side
                if (temp instanceof ClonerTile) c.tile = (ClonerTile) temp;
            });
            // Client side
            if (c.tile == null) c.tile = new ClonerTile(invIn.player.blockPosition(), PokecubeAdv.CLONER.get()
                    .defaultBlockState());
            return c.tile;
        });

        this.tile.setCraftMatrix(new PoweredCraftingInventory(this, this.tile, 3, 3));

        this.addSlot(new Slot(this.tile, this.tile.getOutputSlot(), 124, 35)
        {
            @Override
            public boolean mayPlace(final ItemStack stack)
            {
                return false;
            }
        });

        final int di = 17;
        final int di2 = 9;
        final int dj = 32;

        int i = 0;
        int j = 0;
        this.addSlot(new TexturedSlot(this.inv, 0, dj - 21 + j * 18, di + i * 18,
                "pokecube_adventures:items/slot_bottle"));
        i = 2;
        this.addSlot(new TexturedSlot(this.inv, 1, dj - 21 + j * 18, di + i * 18,
                "pokecube_adventures:items/slot_egg"));

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
