package pokecube.nbtedit.forge;

import java.io.File;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    boolean reg = false;

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
        if (!this.reg) PermissionAPI.registerNode(NBTEdit.MODID, DefaultPermissionLevel.OP,
                "Allowed to use nbt edit commands.");
        this.reg = true;
        if (NBTEdit.opOnly ? PermissionAPI.hasPermission(player, NBTEdit.MODID)
                : player.getAbilities().instabuild)
            return true;
        return false;
    }

    public File getMinecraftDirectory()
    {
        return FMLPaths.GAMEDIR.get().toFile();
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
            final Component component = new TextComponent(message);
            component.getStyle().withColor(TextColor.fromLegacyFormat(color));
            player.sendMessage(component, Util.NIL_UUID);
        }
    }

    public void setupClient()
    {

    }

    private MinecraftServer server;

    @Override
    public void setServer(final MinecraftServer server)
    {
        this.server = server;
    }

    @Override
    public MinecraftServer getServer()
    {
        return this.server;
    }
}
