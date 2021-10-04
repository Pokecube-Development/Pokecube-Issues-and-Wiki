package pokecube.core.inventory.healer;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IHealer;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class HealerContainer extends BaseContainer implements IHealer
{
    public static SoundEvent                           HEAL_SOUND = null;
    public static final MenuType<HealerContainer> TYPE       = new MenuType<>(HealerContainer::new);

    private final SimpleContainer         inv = new HealerInventory();

    private final ContainerLevelAccess pos;

    public HealerContainer(final int id, final Inventory invIn)
    {
        this(id, invIn, ContainerLevelAccess.NULL);
    }

    public HealerContainer(final int id, final Inventory invIn, final ContainerLevelAccess pos)
    {
        super(HealerContainer.TYPE, id);
        this.pos = pos;
        int index = 0;
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 2; ++j)
                this.addSlot(new Slot(this.inv, index++, 62 + j * 18, 17 + i * 18)
                {
                    @Override
                    public boolean mayPlace(final ItemStack stack)
                    {
                        return this.container.canPlaceItem(this.getSlotIndex(), stack);
                    }
                });
        this.bindPlayerInventory(invIn, -19);
    }

    @Override
    public boolean stillValid(final Player playerIn)
    {
        return true;
    }

    @Override
    public Container getInv()
    {
        return this.inv;
    }

    /**
     * Heals all the Pokecubes in the heal inventory. It means, it sets the
     * damage with the value for a full healthy Pokemob for each of the 6
     * pokecubes.
     */
    @Override
    public void heal(final Level world)
    {
        if (!world.isClientSide) for (int i = 0; i < 6; i++)
        {
            final Slot slot = this.getSlot(i);
            if (PokecubeManager.isFilled(slot.getItem())) PokecubeManager.heal(slot.getItem(), world);
        }
    }

    @Override
    public void removed(final Player playerIn)
    {
        super.removed(playerIn);
        this.pos.execute((world, pos) ->
        {
            this.clearContainer(playerIn, world, this.inv);
        });
    }
}
