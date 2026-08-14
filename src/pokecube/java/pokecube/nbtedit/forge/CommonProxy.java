package pokecube.nbtedit.forge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import pokecube.nbtedit.NBTEdit;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    static
    {
        PermNodes.registerBooleanNode(NBTEdit.MODID, NBTEdit.MODID, DefaultPermissionLevel.OP,
                "Allowed to use nbt edit commands.");
    }

    public boolean checkPermission(final CommandSourceStack cs)
    {
        ServerPlayer player;
        try
        {
            player = cs.getPlayerOrException();
        }
        catch (final CommandSyntaxException e)
        {
            return false;
        }
        return this.checkPermission(player);
    }

    public boolean checkPermission(final ServerPlayer player)
    {
        return NBTEdit.opOnly ? PermNodes.getBooleanPerm(player, NBTEdit.MODID) : player.getAbilities().instabuild;
    }

    public void openEditGUI(final BlockPos pos, final CompoundTag tag)
    {

    }

    public void openEditGUI(final int entityID, final CompoundTag tag)
    {

    }

    public void openEditGUI(final int entityID, final String customName, final CompoundTag tag)
    {

    }

    public void sendMessage(final Player player, final String message, final ChatFormatting color)
    {
        if (player != null)
        {
            final Component component = Component.literal(message);
            component.getStyle().withColor(TextColor.fromLegacyFormat(color));
            thut.lib.ChatHelper.sendSystemMessage(player, component);
        }
    }

    public void setupClient()
    {

    }
}
