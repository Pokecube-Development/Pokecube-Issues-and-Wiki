package pokecube.adventures.inventory.trainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.inventory.BaseContainer;

public class ContainerTrainer extends BaseContainer
{
    IHasPokemobs pokemobs;

    public ContainerTrainer(final int id, final Inventory ivplay, final FriendlyByteBuf data)
    {
        super(PokecubeAdv.TRAINER_CONT.get(), id);
        final LivingEntity entity = ivplay.player;
        final int num = data.readInt();
        final Entity mob = entity.level().getEntity(num);
        this.pokemobs = TrainerCaps.getHasPokemobs(mob);
        int index = 0;
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 2; ++j) this.addSlot(new Slot(this.pokemobs, index++, 26 + j * 18, 18 + i * 18)
            {
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return PokecubeManager.isFilled(stack);
                }

                @Override
                public void onTake(final Player thePlayer, final ItemStack stack)
                {
                    final IPokemob pokemob = PokecubeManager.itemToPokemob(stack, thePlayer.level());
                    if (pokemob != null)
                    {
                        pokemob.setOwner(thePlayer);
                        final ItemStack edited = PokecubeManager.pokemobToItem(pokemob);
                        stack.setTag(edited.getTag());
                    }
                    super.onTake(thePlayer, stack);
                }
            });
        this.bindPlayerInventory(ivplay, -19);
    }

    @Override
    public Container getInv()
    {
        return this.pokemobs;
    }

}
