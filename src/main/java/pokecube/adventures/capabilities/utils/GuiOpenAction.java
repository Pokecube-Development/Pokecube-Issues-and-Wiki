package pokecube.adventures.capabilities.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkHooks;
import pokecube.adventures.inventory.trainer.ContainerTrainer;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.entity.trainers.actions.Action;
import pokecube.api.entity.trainers.actions.ActionContext;

public class GuiOpenAction extends Action
{

    public GuiOpenAction()
    {
        super("");
    }

    @Override
    public boolean doAction(final ActionContext action)
    {
        if (!(action.target instanceof ServerPlayer target)) return false;
        final Entity holder = action.holder;
        final IHasPokemobs trainer = TrainerCaps.getHasPokemobs(holder);
        if (trainer == null) return false;
        if (!trainer.stillValid(target)) return false;
        final ServerPlayer player = target;
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(0));
        buffer.writeInt(holder.getId());
        final SimpleMenuProvider provider = new SimpleMenuProvider((i, p, e) -> new ContainerTrainer(i, p, buffer),
                holder.getDisplayName());
        NetworkHooks.openGui(player, provider, buf -> {
            buf.writeInt(holder.getId());
        });
        return true;
    }
}
