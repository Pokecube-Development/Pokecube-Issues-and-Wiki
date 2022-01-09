package thut.api.inventory.npc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.IContainerFactory;
import thut.api.inventory.BaseContainer;

public class NpcContainer extends BaseContainer
{
    public static final MenuType<NpcContainer> TYPE = new MenuType<>(
            (IContainerFactory<NpcContainer>) NpcContainer::new);

    private ResourceLocation tex = new ResourceLocation("thutcore", "textures/gui/generic_4x2.png");

    private final Container wrapped;

    public NpcContainer(int id, final Inventory ivplay, final FriendlyByteBuf data)
    {
        super(TYPE, id);
        final LivingEntity entity = ivplay.player;
        final int num = data.readInt();
        final Entity mob = entity.getCommandSenderWorld().getEntity(num);

        if (!(mob instanceof Villager npc)) throw new IllegalStateException("Error with accessing inventory of " + mob);

        NpcInventory inv = new NpcInventory(new NpcWrapper(npc));
        this.wrapped = inv;

        int index = 0;
        for (int i = 0; i < 2; ++i)
            for (int j = 0; j < 4; ++j) this.addSlot(new Slot(inv, index++, 26 + j * 18, 18 + i * 18)
            {
                @Override
                public boolean mayPlace(final ItemStack stack)
                {
                    return super.mayPlace(stack);
                }

                @Override
                public void onTake(final Player thePlayer, final ItemStack stack)
                {
                    super.onTake(thePlayer, stack);
                }
            });
    }

    public ResourceLocation getTexture()
    {
        return tex;
    }

    @Override
    public Container getInv()
    {
        return wrapped;
    }
}
