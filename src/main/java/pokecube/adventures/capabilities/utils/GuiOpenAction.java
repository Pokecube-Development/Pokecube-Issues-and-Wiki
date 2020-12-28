package pokecube.adventures.capabilities.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.inventory.trainer.ContainerTrainer;

public class GuiOpenAction extends Action
{

    public GuiOpenAction()
    {
        super("");
    }

    @Override
    public void doAction(final ActionContext action)
    {
        if (!(action.target instanceof ServerPlayerEntity)) return;
        final ServerPlayerEntity target = (ServerPlayerEntity) action.target;
        final Entity holder = action.holder;
        final IHasPokemobs trainer = TrainerCaps.getHasPokemobs(holder);
        if (trainer == null) return;
        if (!trainer.isUsableByPlayer(target)) return;
        final ServerPlayerEntity player = target;
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(0));
        buffer.writeInt(holder.getEntityId());
        final SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((i, p,
                e) -> new ContainerTrainer(i, p, buffer), holder.getDisplayName());
        NetworkHooks.openGui(player, provider, buf ->
        {
            buf.writeInt(holder.getEntityId());
        });
    }
}
