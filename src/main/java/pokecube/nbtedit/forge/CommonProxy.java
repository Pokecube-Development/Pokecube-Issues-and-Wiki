package pokecube.nbtedit.forge;

import java.io.File;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{

    boolean reg = false;

    public boolean checkPermission(CommandSource cs)
    {
        ServerPlayerEntity player;
        try
        {
            player = cs.asPlayer();
        }
        catch (final CommandSyntaxException e)
        {
            return false;
        }
        return this.checkPermission(player);
    }

    public boolean checkPermission(ServerPlayerEntity player)
    {
        if (!this.reg) PermissionAPI.registerNode(NBTEdit.MODID, DefaultPermissionLevel.OP,
                "Allowed to use nbt edit commands.");
        this.reg = true;
        if (NBTEdit.opOnly ? PermissionAPI.hasPermission(player, NBTEdit.MODID) : player.abilities.isCreativeMode)
            return true;
        return false;
    }

    public File getMinecraftDirectory()
    {
        return FMLPaths.GAMEDIR.get().toFile();
    }

    public void openEditGUI(BlockPos pos, CompoundNBT tag)
    {

    }

    public void openEditGUI(int entityID, CompoundNBT tag)
    {

    }

    public void openEditGUI(int entityID, String customName, CompoundNBT tag)
    {

    }

    public void sendMessage(PlayerEntity player, String message, TextFormatting color)
    {
        if (player != null)
        {
            final ITextComponent component = new StringTextComponent(message);
            component.getStyle().setColor(color);
            player.sendMessage(component);
        }
    }

    public void setupClient()
    {

    }
}
