package pokecube.core.inventory.healer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundEvent;
import pokecube.core.interfaces.IHealer;
import pokecube.core.inventory.BaseContainer;
import pokecube.core.items.pokecubes.PokecubeManager;

public class HealerContainer extends BaseContainer implements IHealer
{
    public static SoundEvent                           HEAL_SOUND = null;
    public static final ContainerType<HealerContainer> TYPE       = new ContainerType<>(HealerContainer::new);

    private final Inventory         inv = new HealerInventory();
    private final IWorldPosCallable pos;

    public HealerContainer(final int id, final PlayerInventory invIn)
    {
        this(id, invIn, IWorldPosCallable.DUMMY);
    }

    public HealerContainer(final int id, final PlayerInventory invIn, final IWorldPosCallable pos)
    {
        super(HealerContainer.TYPE, id);
        this.pos = pos;
        int index = 0;
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 2; ++j)
                this.addSlot(new Slot(this.inv, index++, 62 + j * 18, 17 + i * 18)
                {
                    @Override
                    public boolean isItemValid(final ItemStack stack)
                    {
                        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
                    }
                });
        this.bindPlayerInventory(invIn, -19);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public IInventory getInv()
    {
        return this.inv;
    }

    @Override
    public int getInventorySlotCount()
    {
        return 6;
    }

    /**
     * Heals all the Pokecubes in the heal inventory. It means, it sets the
     * damage with the value for a full healthy Pokemob for each of the 6
     * pokecubes.
     */
    @Override
    public void heal()
    {
        for (int i = 0; i < 6; i++)
        {
            final Slot slot = this.getSlot(i);
            if (PokecubeManager.isFilled(slot.getStack())) PokecubeManager.heal(slot.getStack());
        }
    }

    @Override
    public void onContainerClosed(final PlayerEntity playerIn)
    {
        super.onContainerClosed(playerIn);
        this.pos.consume((world, pos) ->
        {
            this.clearContainer(playerIn, world, this.inv);
        });
    }
}
