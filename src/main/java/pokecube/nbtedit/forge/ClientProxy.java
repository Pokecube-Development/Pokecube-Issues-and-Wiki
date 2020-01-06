package pokecube.nbtedit.forge;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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

    private void drawBoundingBox(WorldRenderer r, float f, AxisAlignedBB aabb)
    {
        if (aabb == null) return;

        final Entity player = Minecraft.getInstance().getRenderViewEntity();

        final double var8 = player.lastTickPosX + (player.posX - player.lastTickPosX) * f;
        final double var10 = player.lastTickPosY + (player.posY - player.lastTickPosY) * f;
        final double var12 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * f;

        aabb = aabb.offset(-var8, -var10, -var12);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 0.0F, 0.0F, .5F);
        GL11.glLineWidth(3.5F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder worldRenderer = tessellator.getBuffer();

        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ);
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();

    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event)
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

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent event)
    {
        final Screen curScreen = Minecraft.getInstance().currentScreen;
        if (curScreen instanceof GuiEditNBTTree)
        {
            final GuiEditNBTTree screen = (GuiEditNBTTree) curScreen;
            final Entity e = screen.getEntity();

            if (e != null && e.isAlive()) this.drawBoundingBox(event.getContext(), event.getPartialTicks(), e
                    .getBoundingBox());
            else if (screen.isTileEntity())
            {
                final int x = screen.getBlockX();
                final int y = screen.y;
                final int z = screen.z;
                final World world = Minecraft.getInstance().world;
                final BlockPos pos = new BlockPos(x, y, z);
                final BlockState state = world.getBlockState(pos);
                final Block block = world.getBlockState(pos).getBlock();
                if (block != null) this.drawBoundingBox(event.getContext(), event.getPartialTicks(), state
                        .getCollisionShape(world, pos).getBoundingBox());
            }
        }
    }

    @Override
    public void sendMessage(PlayerEntity player, String message, TextFormatting color)
    {
        final ITextComponent component = new StringTextComponent(message);
        component.getStyle().setColor(color);
        Minecraft.getInstance().player.sendMessage(component);
    }

    @Override
    public void setupClient()
    {
        MinecraftForge.EVENT_BUS.register(this);
        final SaveStates save = NBTEdit.getSaveStates();
        save.load();
        save.save();
        ClientProxy.NBTEditKey = new KeyBinding("NBTEdit Shortcut", InputMappings.INPUT_INVALID.getKeyCode(),
                "key.categories.misc");
        ClientRegistry.registerKeyBinding(ClientProxy.NBTEditKey);
    }
}
