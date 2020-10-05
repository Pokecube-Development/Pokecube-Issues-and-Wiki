package pokecube.nbtedit.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import pokecube.core.PokecubeCore;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.gui.GuiEditNBTTree;
import pokecube.nbtedit.nbt.SaveStates;
import pokecube.nbtedit.packets.EntityRequestPacket;
import pokecube.nbtedit.packets.PacketHandler;
import pokecube.nbtedit.packets.TileRequestPacket;
import thut.core.common.network.Packet;

public class ClientProxy extends CommonProxy
{

    public static KeyBinding NBTEditKey;

    @SubscribeEvent
    public void onKey(final InputEvent.KeyInputEvent event)
    {
        if (ClientProxy.NBTEditKey.isPressed())
        {
            final RayTraceResult pos = Minecraft.getInstance().objectMouseOver;
            if (pos != null)
            {
                Packet ret = null;
                switch (pos.getType())
                {
                case BLOCK:
                    ret = new TileRequestPacket(((BlockRayTraceResult) pos).getPos());
                    break;
                case ENTITY:
                    ret = new EntityRequestPacket(((EntityRayTraceResult) pos).getEntity().getEntityId());
                    break;
                case MISS:
                    NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
                    return;
                default:
                    NBTEdit.proxy.sendMessage(null, "Error - No tile or entity selected", TextFormatting.RED);
                    return;
                }
                PacketHandler.INSTANCE.sendToServer(ret);
            }
        }
    }

    @Override
    public void openEditGUI(final BlockPos pos, final CompoundNBT tag)
    {
        Minecraft.getInstance().displayGuiScreen(new GuiEditNBTTree(pos, tag));
    }

    @Override
    public void openEditGUI(final int entityID, final CompoundNBT tag)
    {
        Minecraft.getInstance().displayGuiScreen(new GuiEditNBTTree(entityID, tag));
    }

    @Override
    public void openEditGUI(final int entityID, final String customName, final CompoundNBT tag)
    {
        Minecraft.getInstance().displayGuiScreen(new GuiEditNBTTree(entityID, customName, tag));
    }

    @Override
    public void sendMessage(final PlayerEntity player, final String message, final TextFormatting color)
    {
        final ITextComponent component = new StringTextComponent(message);
        component.getStyle().setColor(color);
        Minecraft.getInstance().player.sendMessage(component, Util.DUMMY_UUID);
    }

    @Override
    public void setupClient()
    {
        MinecraftForge.EVENT_BUS.register(this);
        try
        {
            final SaveStates save = NBTEdit.getSaveStates();
            save.load();
            save.save();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.catching(e);
        }
        ClientProxy.NBTEditKey = new KeyBinding("NBTEdit Shortcut", InputMappings.INPUT_INVALID.getKeyCode(),
                "key.categories.misc");
        ClientRegistry.registerKeyBinding(ClientProxy.NBTEditKey);
    }
}
