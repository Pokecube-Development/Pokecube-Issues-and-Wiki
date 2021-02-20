package pokecube.adventures.inventory.trainer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class ContainerTrainer extends BaseContainer
{
    IHasPokemobs pokemobs;

    public ContainerTrainer(final int id, final PlayerInventory ivplay, final PacketBuffer data)
    {
        super(PokecubeAdv.TRAINER_CONT.get(), id);
        final LivingEntity entity = ivplay.player;
        final int num = data.readInt();
        final Entity mob = entity.getEntityWorld().getEntityByID(num);
        this.pokemobs = TrainerCaps.getHasPokemobs(mob);
        int index = 0;
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 2; ++j)
                this.addSlot(new Slot(this.pokemobs, index++, 26 + j * 18, 18 + i * 18)
                {
                    @Override
                    public boolean isItemValid(final ItemStack stack)
                    {
                        return PokecubeManager.isFilled(stack);
                    }
                    @Override
                    public ItemStack onTake(final PlayerEntity thePlayer, final ItemStack stack)
                    {
                        final IPokemob pokemob = PokecubeManager.itemToPokemob(stack, thePlayer.getEntityWorld());
                        if (pokemob != null)
                        {
                            pokemob.setOwner(thePlayer);
                            final ItemStack edited = PokecubeManager.pokemobToItem(pokemob);
                            stack.setTag(edited.getTag());
                        }
                        return super.onTake(thePlayer, stack);
                    }
                });
        this.bindPlayerInventory(ivplay, -19);
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn)
    {
        return this.pokemobs.isUsableByPlayer(playerIn);
    }

    @Override
    public IInventory getInv()
    {
        return this.pokemobs;
    }

}
